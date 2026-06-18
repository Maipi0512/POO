package farmared.controladores;

import farmared.modelo.enums.EstadoComprobante;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m6_ordenes_pago.CancelacionComprobante;
import farmared.modelo.modulos.m3_impuestos.Impuesto;
import farmared.modelo.modulos.m6_ordenes_pago.MedioPago;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modelo.modulos.m3_impuestos.Retencion;
import farmared.dto.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DS2 — Emitir Orden de Pago.
 * Controlador singleton del modulo 6 (RF-18, RF-19, RF-20).
 */
public class OrdenPagoController {

    private static OrdenPagoController instancia;

    private final List<Proveedor>  proveedores;
    private final List<Impuesto>   impuestos;
    private final List<OrdenPago>  ordenesPago;
    private int contadorOP = 1;

    private OrdenPagoController(List<Proveedor> proveedores, List<Impuesto> impuestos,
                                 List<OrdenPago> ordenesPago) {
        this.proveedores = proveedores;
        this.impuestos   = impuestos;
        this.ordenesPago = ordenesPago;
    }

    public static void inicializar(List<Proveedor> proveedores, List<Impuesto> impuestos,
                                    List<OrdenPago> ordenesPago) {
        instancia = new OrdenPagoController(proveedores, impuestos, ordenesPago);
    }

    public static OrdenPagoController getInstance() {
        if (instancia == null) throw new IllegalStateException("OrdenPagoController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS2 paso 1: obtener comprobantes impagos
    // =========================================================================

    /** DS2: busca proveedor y retorna su lista de comprobantes con saldo pendiente. */
    public List<Comprobante> iniciarOrdenPago(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        List<Comprobante> impagos = new ArrayList<>();
        for (Comprobante c : prov.getComprobantes()) {
            if (c.getSaldoPendiente() > 0 && c.getEstado() != EstadoComprobante.ANULADO)
                impagos.add(c);
        }
        return impagos;
    }

    // =========================================================================
    // DS2 paso 2: crear OP, cancelaciones y retenciones
    // =========================================================================

    /**
     * DS2: crea OrdenPago, agrega cancelaciones por cada comprobante seleccionado
     * y calcula retenciones según los impuestos parametrizados y certificados del proveedor.
     */
    public OrdenPago seleccionarComprobantes(String cuitProveedor,
                                              Map<Comprobante, Double> seleccion,
                                              Date fechaEmision) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");

        OrdenPago op = new OrdenPago("OP-" + String.format("%08d", contadorOP++), prov, fechaEmision);

        for (Map.Entry<Comprobante, Double> e : seleccion.entrySet()) {
            double imp = e.getValue();
            boolean total = Math.abs(imp - e.getKey().getSaldoPendiente()) < 0.01;
            op.agregarCancelacion(new CancelacionComprobante(e.getKey(), imp, total));
        }

        calcularRetenciones(op, prov);
        op.generar();
        return op;
    }

    // =========================================================================
    // DS2 paso 3: confirmar pago
    // =========================================================================

    /** DS2: procesa los medios de pago y cierra la orden. */
    public OrdenPago confirmarPago(OrdenPago op, List<MedioPago> mediosPago) {
        if (!op.procesarPago(mediosPago))
            throw new IllegalStateException(
                "Medios de pago invalidos o no cubren el neto $" + op.getImporteNeto());
        op.cerrarOrden();
        ordenesPago.add(op);
        // DS2: notificar al proveedor por cada comprobante cancelado
        for (var cancelacion : op.obtenerCancelaciones())
            op.getProveedor().registrarPago(cancelacion.getImporteCancelado());
        return op;
    }

    // =========================================================================
    // DS2 loop: cálculo de retenciones (RF-20)
    // =========================================================================

    private void calcularRetenciones(OrdenPago op, Proveedor prov) {
        double base = op.getImporteBruto();
        for (Impuesto imp : impuestos) {
            if (prov.tieneCertificadoVigente(imp.getTipo())) continue;
            double importe = imp.calcularRetencion(base, prov);
            if (importe > 0)
                op.agregarRetencion(new Retencion(imp, base, imp.determinarPorcentaje(base), importe));
        }
    }

    // =========================================================================
    // Consultas
    // =========================================================================

    public Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }

    public List<OrdenPago> getOrdenesEmitidas()  { return new ArrayList<>(ordenesPago); }
    public List<OrdenPagoDTO> getOrdenesEmitidasDTO() { return DtoMapper.toOrdenPagoDTOList(ordenesPago); }

    public List<Proveedor> getProveedores()       { return new ArrayList<>(proveedores); }
    public List<ProveedorDTO> getProveedoresDTO() { return DtoMapper.toProveedorDTOList(proveedores); }

    public List<OrdenPago> buscarPagosPorProveedor(String cuit) {
        List<OrdenPago> resultado = new ArrayList<>();
        for (OrdenPago op : ordenesPago)
            if (op.getProveedor().getCuit().equals(cuit)) resultado.add(op);
        return resultado;
    }

    public List<OrdenPagoDTO> buscarPagosPorProveedorDTO(String cuit) {
        return DtoMapper.toOrdenPagoDTOList(buscarPagosPorProveedor(cuit));
    }

    public List<ComprobanteDTO> iniciarOrdenPagoDTO(String cuitProveedor) {
        return DtoMapper.toComprobanteDTOList(iniciarOrdenPago(cuitProveedor));
    }

    private final java.util.Map<String, OrdenPago> opDrafts = new java.util.HashMap<>();

    public OrdenPagoDTO seleccionarComprobantesDTO(String cuitProveedor,
                                                  Map<String, Double> seleccion,
                                                  Date fechaEmision) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");

        java.util.Map<Comprobante, Double> domainSeleccion = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Double> e : seleccion.entrySet()) {
            Comprobante comp = null;
            for (Comprobante c : prov.getComprobantes()) {
                if (c.getNumero().equals(e.getKey())) {
                    comp = c;
                    break;
                }
            }
            if (comp == null) throw new IllegalArgumentException("Comprobante " + e.getKey() + " no encontrado.");
            domainSeleccion.put(comp, e.getValue());
        }

        OrdenPago op = seleccionarComprobantes(cuitProveedor, domainSeleccion, fechaEmision);
        opDrafts.put(op.getNumero(), op);
        return DtoMapper.toDTO(op);
    }

    public void confirmarPagoDTO(String numeroOP, List<MedioPago> mediosPago) {
        OrdenPago op = opDrafts.remove(numeroOP);
        if (op == null) {
            for (OrdenPago o : ordenesPago) {
                if (o.getNumero().equals(numeroOP)) {
                    op = o;
                    break;
                }
            }
        }
        if (op == null) throw new IllegalArgumentException("Orden de pago no encontrada o ya procesada.");
        confirmarPago(op, mediosPago);
    }
}

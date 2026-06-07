package farmared.controllers;

import farmared.enums.EstadoComprobante;
import farmared.enums.TipoImpuesto;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m6_ordenes_pago.Impuesto;
import farmared.modulos.m6_ordenes_pago.Retencion;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.CancelacionComprobante;
import farmared.modulos.m6_ordenes_pago.MedioPago;
import farmared.modulos.m6_ordenes_pago.OrdenPago;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton controlador de Ordenes de Pago (DS2 - Emitir Orden de Pago).
 *
 * Flujo DS2:
 *   iniciarOrdenPago(idProveedor)
 *     → buscarProveedorPorId → obtenerCuentaCorriente → comprobantesImpagos
 *   seleccionarComprobantes(idProveedor, seleccion, fechaEmision)
 *     → new OrdenPago → loop CancelacionComprobante → calcularRetenciones → generar()
 *   confirmarPago(unaOrdenPago, mediosPago)
 *     → loop procesarPago() en cada MedioPago → procesarPago(mediosPago) → cerrarOrden()
 *     → loop registrarPago(importeCancelado) por comprobante cancelado
 */
public class OrdenPagoController {

    private static OrdenPagoController instancia;

    private List<Proveedor>  proveedores;
    private List<OrdenPago>  ordenesPago;
    private List<Impuesto>   impuestos;

    private int contadorOP = 1;

    private OrdenPagoController() {
        proveedores = new ArrayList<>();
        ordenesPago = new ArrayList<>();
        impuestos   = new ArrayList<>();
    }

    public static OrdenPagoController getInstance() {
        if (instancia == null) {
            instancia = new OrdenPagoController();
        }
        return instancia;
    }

    // =========================================================================
    // DS2: iniciarOrdenPago(idProveedor)
    // =========================================================================

    /** DS2 loop "en coleccion de proveedores". */
    public Proveedor buscarProveedorPorId(String idProveedor) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(idProveedor)) return p;
        return null;
    }

    /**
     * DS2 paso 1: iniciarOrdenPago(idProveedor).
     * Obtiene la cuenta corriente y devuelve los comprobantes con saldo pendiente.
     */
    public List<Comprobante> iniciarOrdenPago(String idProveedor) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null)
            throw new IllegalArgumentException("Proveedor no encontrado: " + idProveedor);

        unProveedor.obtenerCuentaCorriente();

        List<Comprobante> comprobantesImpagos = new ArrayList<>();
        for (Comprobante c : unProveedor.getComprobantes()) {
            if (c.getSaldoPendiente() > 0
                    && c.getEstado() != EstadoComprobante.ANULADO
                    && c.esSumaDeuda()) {
                comprobantesImpagos.add(c);
            }
        }
        return comprobantesImpagos;
    }

    // =========================================================================
    // DS2: seleccionarComprobantes (crea la OP y calcula retenciones)
    // =========================================================================

    /**
     * DS2: seleccionarComprobantes(comprobantes, montos).
     * new OrdenPago → loop new CancelacionComprobante → calcularRetenciones → generar().
     * Devuelve la OP en estado GENERADA con importeNeto listo.
     */
    public OrdenPago seleccionarComprobantes(String idProveedor,
                                              Map<Comprobante, Double> seleccion,
                                              Date fechaEmision) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null)
            throw new IllegalArgumentException("Proveedor no encontrado: " + idProveedor);

        OrdenPago unaOrdenPago = new OrdenPago(
            "OP-" + String.format("%08d", contadorOP++), unProveedor, fechaEmision);

        for (Map.Entry<Comprobante, Double> e : seleccion.entrySet()) {
            double imp = e.getValue();
            boolean esTotal = Math.abs(imp - e.getKey().getSaldoPendiente()) < 0.01;
            CancelacionComprobante unaCancelacion =
                new CancelacionComprobante(e.getKey(), imp, esTotal);
            unaOrdenPago.agregarCancelacion(unaCancelacion);
        }

        calcularRetenciones(unaOrdenPago, unProveedor);
        unaOrdenPago.generar();
        return unaOrdenPago;
    }

    // =========================================================================
    // DS2: calcularRetenciones(unaOrdenPago, unProveedor)
    // =========================================================================

    /**
     * DS2 loop "cada tipo de impuesto: IVA, IIBB, Ganancias".
     * tieneCertificadoVigente → estaVigente(fechaActual) → false → calcularRetencion
     * → EscalaRetencion.aplicaA(importeBruto) → new Retencion → agregarRetencion.
     */
    public void calcularRetenciones(OrdenPago unaOrdenPago, Proveedor unProveedor) {
        double importeBruto = unaOrdenPago.getImporteBruto();
        for (Impuesto imp : impuestos) {
            if (unProveedor.tieneCertificadoVigente(imp.getTipo())) continue;
            double importeRetencion = imp.calcularRetencion(importeBruto, unProveedor);
            if (importeRetencion > 0) {
                Retencion unaRetencion = new Retencion(
                    imp, importeBruto, imp.determinarPorcentaje(importeBruto), importeRetencion);
                unaOrdenPago.agregarRetencion(unaRetencion);
            }
        }
    }

    // =========================================================================
    // DS2: confirmarPago(unaOrdenPago, mediosPago)
    // =========================================================================

    /**
     * DS2: confirmarPago(unaOrdenPago, mediosPago).
     * Loop: procesarPago() en cada MedioPago → true.
     * procesarPago(mediosPago) en OrdenPago → true.
     * cerrarOrden() → EMITIDA (internamente registrarPago en cada comprobante cancelado).
     */
    public OrdenPago confirmarPago(OrdenPago unaOrdenPago, List<MedioPago> mediosPago) {
        // DS2 loop: cada medio de pago en mediosPago
        for (MedioPago mp : mediosPago) {
            if (!mp.procesarPago()) {
                throw new IllegalStateException(
                    "Error al procesar medio de pago: " + mp.obtenerDescripcion());
            }
        }
        // DS2: procesarPago(mediosPago) → true
        if (!unaOrdenPago.procesarPago(mediosPago)) {
            throw new IllegalStateException(
                "Los medios de pago no cubren el neto: $" + unaOrdenPago.getImporteNeto());
        }
        // DS2: cerrarOrden() → EMITIDA (+ loop registrarPago internamente)
        unaOrdenPago.cerrarOrden();
        ordenesPago.add(unaOrdenPago);
        return unaOrdenPago;
    }

    // =========================================================================
    // RF-25: Reporte de retenciones por tipo de impuesto
    // =========================================================================

    public Map<TipoImpuesto, Double> reporteRetencionesPorTipo() {
        Map<TipoImpuesto, Double> result = new EnumMap<>(TipoImpuesto.class);
        for (TipoImpuesto t : TipoImpuesto.values()) result.put(t, 0.0);
        for (OrdenPago op : ordenesPago)
            for (Retencion r : op.getRetenciones())
                result.merge(r.getImpuesto().getTipo(), r.getImporte(), Double::sum);
        return result;
    }

    // =========================================================================
    // Registro de entidades (llamado por SistemaGestionCompras)
    // =========================================================================

    public void agregarProveedor(Proveedor p) { if (!proveedores.contains(p)) proveedores.add(p); }
    public void agregarImpuesto(Impuesto i)   { impuestos.add(i); }

    public List<OrdenPago> getOrdenesPago()   { return new ArrayList<>(ordenesPago); }
    public List<Proveedor> getProveedores()   { return new ArrayList<>(proveedores); }
    public List<Impuesto>  getImpuestos()     { return new ArrayList<>(impuestos); }
}

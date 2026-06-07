package farmared.controllers;

import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.CancelacionComprobante;
import farmared.modulos.m6_ordenes_pago.OrdenPago;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton controlador de Cuenta Corriente (DS4 - Obtener Cuenta Corriente del Proveedor).
 *
 * Flujo DS4:
 *   consultarCuentaCorriente(idProveedor)
 *     → buscarProveedorPorId → obtenerCuentaCorriente() [loop afectarCuentaCorriente + calcularDeudaVigente]
 *     → buscarPagosPorProveedor → historialPagos
 *     → loop OrdenPago: obtenerCancelaciones → listaCancelaciones
 *       → loop Cancelacion: getComprobante → comprobanteCancelado
 *                           getImporteCancelado → importeCancelado
 *     → Cuenta Corriente detallada
 */
public class CuentaCorrienteController {

    private static CuentaCorrienteController instancia;

    private List<Proveedor> proveedores;
    private List<OrdenPago> ordenesPago;

    private CuentaCorrienteController() {
        proveedores = new ArrayList<>();
        ordenesPago = new ArrayList<>();
    }

    public static CuentaCorrienteController getInstance() {
        if (instancia == null) {
            instancia = new CuentaCorrienteController();
        }
        return instancia;
    }

    // =========================================================================
    // DS4: consultarCuentaCorriente(idProveedor)
    // =========================================================================

    /** DS4 loop "en coleccion de proveedores" (paso 2-3). */
    public Proveedor buscarProveedorPorId(String idProveedor) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(idProveedor)) return p;
        return null;
    }

    /** DS4 loop "en coleccion de ordenes de pago" (paso 9-10). */
    public List<OrdenPago> buscarPagosPorProveedor(Proveedor unProveedor) {
        List<OrdenPago> historialPagos = new ArrayList<>();
        for (OrdenPago op : ordenesPago) {
            if (op.getProveedor().getCuit().equals(unProveedor.getCuit())) {
                historialPagos.add(op);
            }
        }
        return historialPagos;
    }

    /**
     * DS4 completo: consultarCuentaCorriente(idProveedor).
     * Devuelve un mapa con deudaVigente, comprobantes y pagos aplicados.
     */
    public Map<String, Object> consultarCuentaCorriente(String idProveedor) {
        // DS4 paso 2-3
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null)
            throw new IllegalArgumentException("Proveedor no encontrado: " + idProveedor);

        // DS4 paso 4-8: obtenerCuentaCorriente → loop afectarCuentaCorriente → calcularDeudaVigente
        double totalDeudaVigente = unProveedor.obtenerCuentaCorriente();

        // DS4 paso 9-10: buscarPagosPorProveedor(unProveedor) → historialPagos
        List<OrdenPago> historialPagos = buscarPagosPorProveedor(unProveedor);

        // DS4 paso 11-16: loop OrdenPago → obtenerCancelaciones → loop Cancelacion
        List<Map<String, Object>> detallePagos = new ArrayList<>();
        for (OrdenPago op : historialPagos) {
            // DS4 paso 11-12
            List<CancelacionComprobante> listaCancelaciones = op.obtenerCancelaciones();
            for (CancelacionComprobante cancelacion : listaCancelaciones) {
                // DS4 paso 13-14
                Comprobante comprobanteCancelado = cancelacion.getComprobante();
                // DS4 paso 15-16
                double importeCancelado = cancelacion.getImporteCancelado();

                Map<String, Object> entrada = new LinkedHashMap<>();
                entrada.put("nroOrdenPago",    op.getNumero());
                entrada.put("comprobante",     comprobanteCancelado.getNumero());
                entrada.put("tipoComprobante", comprobanteCancelado.getTipo().toString());
                entrada.put("importeCancelado", importeCancelado);
                entrada.put("esCancelacionTotal", cancelacion.isEsCancelacionTotal());
                detallePagos.add(entrada);
            }
        }

        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("proveedor",        unProveedor.getRazonSocial());
        resultado.put("cuit",             unProveedor.getCuit());
        resultado.put("totalDeudaVigente", totalDeudaVigente);
        resultado.put("comprobantes",     unProveedor.getComprobantes());
        resultado.put("pagosAplicados",   detallePagos);
        return resultado;
    }

    // =========================================================================
    // RF-22: Documentos impagos
    // =========================================================================

    public List<Comprobante> listarDocumentosImpagos(String idProveedor) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null) return new ArrayList<>();
        List<Comprobante> result = new ArrayList<>();
        for (Comprobante c : unProveedor.getComprobantes()) {
            if (c.getSaldoPendiente() > 0 && c.esSumaDeuda()) result.add(c);
        }
        return result;
    }

    // =========================================================================
    // RF-23 M7: Detalle de pagos realizados por proveedor
    // =========================================================================

    public List<Map<String, Object>> detallarPagosPorProveedor(String idProveedor) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null) return new ArrayList<>();
        List<Map<String, Object>> resultado = new ArrayList<>();
        for (OrdenPago op : buscarPagosPorProveedor(unProveedor)) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("nroOrdenPago",      op.getNumero());
            fila.put("fecha",             op.getFechaEmision());
            fila.put("importeBruto",      op.getImporteBruto());
            fila.put("totalRetenciones",  op.getTotalRetenciones());
            fila.put("importeNeto",       op.getImporteNeto());
            fila.put("mediosPago",        op.getMediosPago());
            resultado.add(fila);
        }
        return resultado;
    }

    // =========================================================================
    // RF-24: Deuda vigente por proveedor
    // =========================================================================

    public Map<String, Double> consultarDeudaVigentePorProveedor() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Proveedor p : proveedores) {
            result.put(p.getRazonSocial() + " (" + p.getCuit() + ")", p.obtenerCuentaCorriente());
        }
        return result;
    }

    // =========================================================================
    // Registro de entidades (llamado por SistemaGestionCompras)
    // =========================================================================

    public void agregarProveedor(Proveedor p) { if (!proveedores.contains(p)) proveedores.add(p); }
    public void agregarOrdenPago(OrdenPago op) { if (!ordenesPago.contains(op)) ordenesPago.add(op); }

    public List<Proveedor> getProveedores() { return new ArrayList<>(proveedores); }
    public List<OrdenPago> getOrdenesPago() { return new ArrayList<>(ordenesPago); }
}

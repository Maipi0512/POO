package farmared.modulos.m7_consultas;

import farmared.enums.TipoImpuesto;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m5_comprobantes.Factura;
import farmared.modulos.m6_ordenes_pago.OrdenPago;
import farmared.sistema.SistemaGestionCompras;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DS4 — Obtener Cuenta Corriente del Proveedor.
 * Controlador singleton del modulo 7 — Consultas y Reportes (RF-21 a RF-27).
 */
public class ReportesController {

    private static ReportesController instancia;
    private final SistemaGestionCompras sistema;

    private ReportesController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public static void inicializar(SistemaGestionCompras sistema) {
        instancia = new ReportesController(sistema);
    }

    public static ReportesController getInstance() {
        if (instancia == null) throw new IllegalStateException("ReportesController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS4 - Cuenta corriente detallada
    // =========================================================================

    /**
     * DS4: busca proveedor, obtiene sus comprobantes e historial de pagos.
     * Retorna la lista de comprobantes para que la UI itere (esSumaDeuda, getDetalles).
     */
    public List<Comprobante> consultarCuentaCorriente(String idProveedor) {
        return sistema.consultarCuentaCorriente(idProveedor);
    }

    /** DS4: busca órdenes de pago del proveedor para mostrar historial. */
    public List<OrdenPago> buscarPagosPorProveedor(String cuitProveedor) {
        return sistema.consultarPagosPorProveedor(cuitProveedor);
    }

    // =========================================================================
    // Reportes adicionales (RF-22 a RF-27)
    // =========================================================================

    public List<Comprobante> listarDocumentosImpagos(String cuitProveedor) {
        return sistema.listarDocumentosImpagos(cuitProveedor);
    }

    public Map<String, Double> consultarDeudaVigentePorProveedor() {
        return sistema.consultarDeudaVigentePorProveedor();
    }

    public Map<TipoImpuesto, Double> reporteRetencionesPorTipo() {
        return sistema.reporteRetencionesPorTipo();
    }

    public List<Map<String, Object>> generarLibroIVACompras() {
        return sistema.generarLibroIVACompras();
    }

    public List<Factura> consultarFacturasPorDia(Date fecha, String cuitProveedor) {
        return sistema.consultarFacturasPorDia(fecha, cuitProveedor);
    }

    public List<PrecioAcordado> consultarCompulsaPrecios(String codigoProducto) {
        return sistema.consultarCompulsaPrecios(codigoProducto);
    }

    public List<Proveedor> getProveedores() {
        return sistema.getProveedores();
    }

    public Proveedor buscarProveedorPorId(String cuit) {
        return sistema.buscarProveedorPorId(cuit);
    }
}

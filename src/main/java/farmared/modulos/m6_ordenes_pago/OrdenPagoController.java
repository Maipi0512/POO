package farmared.modulos.m6_ordenes_pago;

import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.sistema.SistemaGestionCompras;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DS2 — Emitir Orden de Pago.
 * Controlador singleton del modulo 6 (RF-18, RF-19, RF-20).
 */
public class OrdenPagoController {

    private static OrdenPagoController instancia;
    private final SistemaGestionCompras sistema;

    private OrdenPagoController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public static void inicializar(SistemaGestionCompras sistema) {
        instancia = new OrdenPagoController(sistema);
    }

    public static OrdenPagoController getInstance() {
        if (instancia == null) throw new IllegalStateException("OrdenPagoController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS2 paso 1: obtener comprobantes impagos
    // =========================================================================

    /** DS2: busca proveedor y retorna su lista de comprobantes con saldo pendiente. */
    public List<Comprobante> iniciarOrdenPago(String idProveedor) {
        return sistema.iniciarOrdenPago(idProveedor);
    }

    // =========================================================================
    // DS2 paso 2: crear OP, cancelaciones y calcular retenciones
    // =========================================================================

    /**
     * DS2: crea OrdenPago, agrega cancelaciones por cada comprobante seleccionado
     * y calcula retenciones según los impuestos parametrizados y certificados del proveedor.
     */
    public OrdenPago seleccionarComprobantes(String idProveedor,
                                              Map<Comprobante, Double> seleccion,
                                              Date fechaEmision) {
        return sistema.prepararOrdenPago(idProveedor, seleccion, fechaEmision);
    }

    // =========================================================================
    // DS2 paso 3: confirmar pago con medios múltiples
    // =========================================================================

    /** DS2: procesa los medios de pago y cierra la orden. */
    public OrdenPago confirmarPago(OrdenPago op, List<MedioPago> mediosPago) {
        return sistema.confirmarOrdenPago(op, mediosPago);
    }

    // =========================================================================
    // Consultas
    // =========================================================================

    public List<OrdenPago> getOrdenesEmitidas() {
        return sistema.reporteOrdenesPago();
    }

    public Proveedor buscarProveedorPorId(String cuit) {
        return sistema.buscarProveedorPorId(cuit);
    }

    public List<Proveedor> getProveedores() {
        return sistema.getProveedores();
    }
}

package farmared.modulos.m5_comprobantes;

import farmared.enums.TipoComprobante;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.sistema.SistemaGestionCompras;

import java.util.Date;
import java.util.List;

/**
 * DS3 — Registrar Factura / Nota de Crédito / Nota de Débito.
 * Controlador singleton del modulo 5 (RF-14 a RF-17).
 */
public class FacturaController {

    private static FacturaController instancia;
    private final SistemaGestionCompras sistema;

    private FacturaController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public static void inicializar(SistemaGestionCompras sistema) {
        instancia = new FacturaController(sistema);
    }

    public static FacturaController getInstance() {
        if (instancia == null) throw new IllegalStateException("FacturaController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS3 - flujo completo
    // =========================================================================

    /**
     * DS3: registra factura, valida contra OC y afecta cuenta corriente del proveedor.
     * Si hay desvío o no tiene OC, requiere supervisor.
     */
    public Factura registrarFactura(String numero, TipoComprobante tipo,
                                    Date fechaEmision, Date fechaRecepcion,
                                    List<DetalleComprobante> detalles,
                                    String cuitProveedor, List<String> nrosOC,
                                    Usuario supervisor, String motivo) {
        return sistema.registrarFactura(numero, tipo, fechaEmision, fechaRecepcion,
                detalles, cuitProveedor, nrosOC, supervisor, motivo);
    }

    public NotaCredito registrarNotaCredito(String numero, Date fechaEmision, Date fechaRecepcion,
                                             List<DetalleComprobante> detalles, String cuitProveedor) {
        return sistema.registrarNotaCredito(numero, fechaEmision, fechaRecepcion, detalles, cuitProveedor);
    }

    public NotaDebito registrarNotaDebito(String numero, Date fechaEmision, Date fechaRecepcion,
                                           List<DetalleComprobante> detalles, String cuitProveedor) {
        return sistema.registrarNotaDebito(numero, fechaEmision, fechaRecepcion, detalles, cuitProveedor);
    }

    // =========================================================================
    // Validaciones DS3 (expuestas para que la UI pueda pre-validar)
    // =========================================================================

    public boolean validarProductos(List<DetalleComprobante> factura, List<DetalleOC> oc) {
        return sistema.validarProductos(factura, oc);
    }

    public boolean validarPrecios(List<DetalleComprobante> factura, List<DetalleOC> oc) {
        return sistema.validarPrecios(factura, oc);
    }

    public boolean validarImpuestos(List<DetalleComprobante> factura) {
        return sistema.validarImpuestos(factura);
    }

    // =========================================================================
    // Consultas
    // =========================================================================

    public List<Comprobante> getComprobantes() {
        return sistema.getComprobantes();
    }

    public Proveedor buscarProveedorPorId(String cuit) {
        return sistema.buscarProveedorPorId(cuit);
    }

    public List<Proveedor> getProveedores() {
        return sistema.getProveedores();
    }

    public List<Usuario> listarSupervisores() {
        return sistema.listarSupervisores();
    }
}

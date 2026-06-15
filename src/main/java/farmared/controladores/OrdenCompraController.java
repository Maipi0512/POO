package farmared.controladores;

import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m4_ordenes_compra.*;
import farmared.modelo.SistemaGestionCompras;

import java.util.List;

/**
 * DS1 — Generar Orden de Compra.
 * Controlador singleton del modulo 4 (RF-11, RF-12, RF-13).
 */
public class OrdenCompraController {

    private static OrdenCompraController instancia;
    private final SistemaGestionCompras sistema;

    private OrdenCompraController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public static void inicializar(SistemaGestionCompras sistema) {
        instancia = new OrdenCompraController(sistema);
    }

    public static OrdenCompraController getInstance() {
        if (instancia == null) throw new IllegalStateException("OrdenCompraController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS1 - flujo completo
    // =========================================================================

    /** DS1 paso 1: crea OC vacía para el proveedor. */
    public OrdenCompra crearOrdenCompra(String idProveedor) {
        return sistema.crearOrdenCompra(idProveedor);
    }

    /** DS1 loop: agrega un ítem buscando precio vigente del producto. */
    public void agregarItem(OrdenCompra oc, String codigoProducto, double cantidad, int nroLinea) {
        sistema.agregarItemOC(oc, codigoProducto, cantidad, nroLinea);
    }

    /** DS1 cierre: valida tope de deuda y emite (con o sin supervisor). */
    public OrdenCompra emitirOrdenCompra(OrdenCompra oc, Usuario supervisor, String motivo) {
        return sistema.emitirOrdenCompra(oc, supervisor, motivo);
    }

    // =========================================================================
    // Consultas de apoyo (buscar en colección — DS1 loops)
    // =========================================================================

    public Proveedor buscarProveedorPorId(String cuit) {
        return sistema.buscarProveedorPorId(cuit);
    }

    public List<Proveedor> getProveedores() {
        return sistema.getProveedores();
    }

    public List<Producto> listarProductosPorProveedor(String cuitProveedor) {
        return sistema.listarProductosPorProveedor(cuitProveedor);
    }

    public double obtenerPrecioVigente(String codigoProducto, String cuitProveedor) {
        return sistema.obtenerPrecioVigente(codigoProducto, cuitProveedor);
    }

    public List<OrdenCompra> getOrdenesCompra() {
        return sistema.reporteOrdenesCompra();
    }

    public List<Usuario> listarSupervisores() {
        return sistema.listarSupervisores();
    }
}

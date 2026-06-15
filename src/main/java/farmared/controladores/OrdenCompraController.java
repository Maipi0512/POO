package farmared.controladores;

import farmared.modulos.m1_usuarios.Autorizacion;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;

import java.util.ArrayList;
import java.util.List;

/**
 * DS1 — Generar Orden de Compra.
 * Controlador singleton del modulo 4 (RF-11, RF-12, RF-13).
 */
public class OrdenCompraController {

    private static OrdenCompraController instancia;

    private final List<Proveedor>   proveedores;
    private final List<Producto>    productos;
    private final List<OrdenCompra> ordenesCompra;
    private final List<Usuario>     usuarios;
    private int contadorOC          = 1;
    private int contadorAutorizacion = 1;

    private OrdenCompraController(List<Proveedor> proveedores, List<Producto> productos,
                                   List<OrdenCompra> ordenesCompra, List<Usuario> usuarios) {
        this.proveedores   = proveedores;
        this.productos     = productos;
        this.ordenesCompra = ordenesCompra;
        this.usuarios      = usuarios;
    }

    public static void inicializar(List<Proveedor> proveedores, List<Producto> productos,
                                    List<OrdenCompra> ordenesCompra, List<Usuario> usuarios) {
        instancia = new OrdenCompraController(proveedores, productos, ordenesCompra, usuarios);
    }

    public static OrdenCompraController getInstance() {
        if (instancia == null) throw new IllegalStateException("OrdenCompraController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS1 — flujo completo
    // =========================================================================

    /** DS1 paso 1: crea OC vacía para el proveedor. */
    public OrdenCompra crearOrdenCompra(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuitProveedor);
        return new OrdenCompra("OC-" + String.format("%08d", contadorOC++), prov);
    }

    /** DS1 loop: agrega un ítem buscando precio vigente del producto. */
    public void agregarItem(OrdenCompra oc, String codigoProducto, double cantidad, int nroLinea) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        if (producto == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);
        PrecioAcordado precio = producto.obtenerUltimoPrecio(oc.getProveedor());
        if (precio == null || !precio.estaVigente())
            throw new IllegalStateException("Sin precio vigente para " + codigoProducto);
        oc.agregarDetalle(new DetalleOC(nroLinea, producto, cantidad, precio.getPrecioUnitario()));
    }

    /** Variante que acepta precio manual (para productos sin precio acordado vigente). */
    public void agregarItemConPrecio(OrdenCompra oc, String codigoProducto, double cantidad, int nroLinea, double precio) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        if (producto == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);
        oc.agregarDetalle(new DetalleOC(nroLinea, producto, cantidad, precio));
    }

    /** DS1 cierre: valida tope de deuda y emite (con o sin supervisor). */
    public OrdenCompra emitirOrdenCompra(OrdenCompra oc, Usuario supervisor, String motivo) {
        oc.calcularTotal();
        if (oc.getProveedor().validarNuevaOC(oc.getImporteTotal())) {
            oc.emitir();
        } else {
            if (supervisor == null || !supervisor.esAutorizador())
                throw new IllegalStateException(
                    "La OC supera el tope de deuda. Se requiere autorizacion de Supervisor.");
            Autorizacion auth = new Autorizacion(contadorAutorizacion++, supervisor,
                    motivo != null ? motivo : "Autorizacion OC por exceso de tope");
            oc.emitirConAutorizacion(auth);
        }
        ordenesCompra.add(oc);
        return oc;
    }

    public void anularOrdenCompra(String numeroOC) {
        OrdenCompra oc = buscarOrdenCompraPorNumero(numeroOC);
        if (oc == null) throw new IllegalArgumentException("OC no encontrada: " + numeroOC);
        oc.anular();
    }

    // =========================================================================
    // Búsquedas (loops DS1-DS3)
    // =========================================================================

    public Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }

    public OrdenCompra buscarOrdenCompraPorNumero(String numero) {
        for (OrdenCompra oc : ordenesCompra)
            if (oc.getNumero().equals(numero)) return oc;
        return null;
    }

    private Producto buscarProductoPorCodigo(String codigo) {
        for (Producto p : productos)
            if (p.getCodigoInterno().equals(codigo)) return p;
        return null;
    }

    public List<Producto> listarProductosPorProveedor(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) return new ArrayList<>();
        List<farmared.modulos.m2_proveedores.Rubro> rubros = prov.getRubros();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (!p.isActivo()) continue;
            if (!rubros.isEmpty() && rubros.contains(p.getRubro())) resultado.add(p);
        }
        return resultado;
    }

    public double obtenerPrecioVigente(String codigoProducto, String cuitProveedor) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (producto == null || prov == null) return -1;
        PrecioAcordado precio = producto.obtenerUltimoPrecio(prov);
        return (precio != null && precio.estaVigente()) ? precio.getPrecioUnitario() : -1;
    }

    public List<Proveedor> getProveedores()    { return new ArrayList<>(proveedores); }
    public List<OrdenCompra> getOrdenesCompra() { return new ArrayList<>(ordenesCompra); }

    public List<Usuario> listarSupervisores() {
        List<Usuario> sup = new ArrayList<>();
        for (Usuario u : usuarios) if (u.esAutorizador()) sup.add(u);
        return sup;
    }
}

package farmared.controladores;

import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m3_impuestos.Impuesto;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modelo.modulos.m8_usuarios.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton de aplicacion: inicializa todos los controladores MVC con colecciones
 * compartidas por referencia. Cualquier modificacion es visible en todos los modulos.
 */
public final class AppContext {

    private static AppContext instancia;

    private final List<Proveedor>   proveedores   = new ArrayList<>();
    private final List<Rubro>       rubros        = new ArrayList<>();
    private final List<Impuesto>    impuestos     = new ArrayList<>();
    private final List<Producto>    productos     = new ArrayList<>();
    private final List<OrdenCompra> ordenesCompra = new ArrayList<>();
    private final List<Comprobante> comprobantes  = new ArrayList<>();
    private final List<OrdenPago>   ordenesPago   = new ArrayList<>();
    private final List<Usuario>     usuarios      = new ArrayList<>();

    private Usuario usuarioActual;

    private AppContext() {
        ProveedorController.inicializar(proveedores, rubros, impuestos, usuarios);
        ProductoController.inicializar(productos, proveedores);
        OrdenCompraController.inicializar(proveedores, productos, ordenesCompra, usuarios);
        FacturaController.inicializar(proveedores, ordenesCompra, comprobantes, usuarios);
        OrdenPagoController.inicializar(proveedores, impuestos, ordenesPago);
        ReportesController.inicializar(proveedores, comprobantes, ordenesPago, productos);
        UsuarioController.inicializar(usuarios);
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) instancia = new AppContext();
        return instancia;
    }

    public ProveedorController   getProveedorCtrl()   { return ProveedorController.getInstance(); }
    public ProductoController    getProductoCtrl()    { return ProductoController.getInstance(); }
    public OrdenCompraController getOrdenCompraCtrl() { return OrdenCompraController.getInstance(); }
    public FacturaController     getFacturaCtrl()     { return FacturaController.getInstance(); }
    public OrdenPagoController   getOrdenPagoCtrl()   { return OrdenPagoController.getInstance(); }
    public ReportesController    getReportesCtrl()    { return ReportesController.getInstance(); }
    public UsuarioController     getUsuarioCtrl()     { return UsuarioController.getInstance(); }

    public Usuario getUsuarioActual()          { return usuarioActual; }
    public void    setUsuarioActual(Usuario u) { usuarioActual = u; }
    public void    cerrarSesion()              { usuarioActual = null; }
}

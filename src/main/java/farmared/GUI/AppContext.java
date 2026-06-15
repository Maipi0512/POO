package farmared.GUI;

import farmared.controladores.FacturaController;
import farmared.controladores.OrdenCompraController;
import farmared.controladores.OrdenPagoController;
import farmared.controladores.ProductoController;
import farmared.controladores.ProveedorController;
import farmared.controladores.ReportesController;
import farmared.controladores.UsuarioController;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.Impuesto;
import farmared.modulos.m6_ordenes_pago.OrdenPago;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton de aplicacion: inicializa todos los controladores MVC con colecciones
 * compartidas por referencia. Cualquier modificacion es inmediatamente visible en
 * todos los modulos sin necesidad de un facade central.
 */
public final class AppContext {

    private static AppContext instancia;

    // Colecciones compartidas — todos los controladores apuntan a los mismos objetos
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

        DatosIniciales.cargar();
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) instancia = new AppContext();
        return instancia;
    }

    // Accesores de controladores
    public ProveedorController   getProveedorCtrl()   { return ProveedorController.getInstance(); }
    public ProductoController    getProductoCtrl()    { return ProductoController.getInstance(); }
    public OrdenCompraController getOrdenCompraCtrl() { return OrdenCompraController.getInstance(); }
    public FacturaController     getFacturaCtrl()     { return FacturaController.getInstance(); }
    public OrdenPagoController   getOrdenPagoCtrl()   { return OrdenPagoController.getInstance(); }
    public ReportesController    getReportesCtrl()    { return ReportesController.getInstance(); }
    public UsuarioController     getUsuarioCtrl()     { return UsuarioController.getInstance(); }

    // Sesion de usuario
    public Usuario getUsuarioActual()          { return usuarioActual; }
    public void    setUsuarioActual(Usuario u) { usuarioActual = u; }
    public void    cerrarSesion()              { usuarioActual = null; }
}

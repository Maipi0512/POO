package farmared.controladores;

import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m3_impuestos.Impuesto;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modelo.modulos.m8_usuarios.Usuario;
<<<<<<< HEAD
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m3_impuestos.Impuesto;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.vistas.DatosIniciales;
=======
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

import java.util.ArrayList;
import java.util.List;

/**
<<<<<<< HEAD
 * Singleton de aplicación que centraliza las colecciones en memoria de todo el modelo,
 * inicializa cada controlador y carga los datos de ejemplo iniciales.
 * Cumple con el patrón Singleton y MVC al independizar el estado del negocio
 * de una única clase fachada controladora.
=======
 * Singleton de aplicacion: inicializa todos los controladores MVC con colecciones
 * compartidas por referencia. Cualquier modificacion es visible en todos los modulos.
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
 */
public final class AppContext {

    private static AppContext instancia;

<<<<<<< HEAD
    // Colecciones de dominio compartidas por referencia en los controladores.
=======
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
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
<<<<<<< HEAD

        DatosIniciales.cargar(proveedores, productos, rubros, impuestos, usuarios);
=======
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) instancia = new AppContext();
        return instancia;
    }

<<<<<<< HEAD
    // Accesores de controladores de módulo
=======
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
    public ProveedorController   getProveedorCtrl()   { return ProveedorController.getInstance(); }
    public ProductoController    getProductoCtrl()    { return ProductoController.getInstance(); }
    public OrdenCompraController getOrdenCompraCtrl() { return OrdenCompraController.getInstance(); }
    public FacturaController     getFacturaCtrl()     { return FacturaController.getInstance(); }
    public OrdenPagoController   getOrdenPagoCtrl()   { return OrdenPagoController.getInstance(); }
    public ReportesController    getReportesCtrl()    { return ReportesController.getInstance(); }
    public UsuarioController     getUsuarioCtrl()     { return UsuarioController.getInstance(); }

<<<<<<< HEAD
    // Sesión de usuario
=======
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
    public Usuario getUsuarioActual()          { return usuarioActual; }
    public void    setUsuarioActual(Usuario u) { usuarioActual = u; }
    public void    cerrarSesion()              { usuarioActual = null; }
}

package farmared.controladores;

import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Controlador singleton — Modulo 2: Catalogo de Productos (RF-05 a RF-07).
 */
public class ProductoController {

    private static ProductoController instancia;

    private final List<Producto>  productos;
    private final List<Proveedor> proveedores;

    private ProductoController(List<Producto> productos, List<Proveedor> proveedores) {
        this.productos   = productos;
        this.proveedores = proveedores;
    }

    public static void inicializar(List<Producto> productos, List<Proveedor> proveedores) {
        instancia = new ProductoController(productos, proveedores);
    }

    public static ProductoController getInstance() {
        if (instancia == null) throw new IllegalStateException("ProductoController no inicializado.");
        return instancia;
    }

    // RF-05: Registrar producto
    public void registrarProducto(Producto producto) {
        for (Producto p : productos) {
            if (p.getCodigoInterno().equals(producto.getCodigoInterno()))
                throw new IllegalArgumentException("Codigo duplicado: " + producto.getCodigoInterno());
            if (p.getDescripcion().equalsIgnoreCase(producto.getDescripcion()))
                throw new IllegalArgumentException(
                        "Ya existe un producto con esa descripcion: \"" + p.getDescripcion() + "\".\n" +
                        "Si es distinta dosis, incluya la dosis en la descripcion (ej: Ibuprofeno 600mg).");
        }
        productos.add(producto);
    }

    // RF-05: Dar de baja
    public void darBajaProducto(String codigoInterno) {
        Producto p = buscarProductoPorCodigo(codigoInterno);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoInterno);
        p.setActivo(false);
    }

    // RF-06: Precio acordado (cierra el vigente anterior del mismo proveedor si existe)
    public void agregarPrecioAcordado(String codigoProducto, String cuitProveedor, double precioUnitario) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        if (producto == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuitProveedor);
        Date hoy = new Date();
        for (PrecioAcordado pa : producto.getPreciosAcordados()) {
            if (pa.getProveedor().equals(prov) && pa.estaVigente()) {
                pa.setFechaFinVigencia(hoy);
            }
        }
        producto.agregarPrecioAcordado(new PrecioAcordado(precioUnitario, hoy, null, prov));
    }

    // RF-07: Precio vigente (DS1 loop)
    public double obtenerPrecioVigente(String codigoProducto, String cuitProveedor) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (producto == null || prov == null) return -1;
        PrecioAcordado precio = producto.obtenerUltimoPrecio(prov);
        return (precio != null && precio.estaVigente()) ? precio.getPrecioUnitario() : -1;
    }

    // RF-07: Compulsa de precios (todos los precios históricos de un producto)
    public List<PrecioAcordado> consultarCompulsaPrecios(String codigoProducto) {
        Producto p = buscarProductoPorCodigo(codigoProducto);
        return p != null ? p.obtenerPreciosHistoricos() : Collections.emptyList();
    }

    // RF-06: Productos del proveedor (DS1 loop "listar productos")
    public List<Producto> listarProductosPorProveedor(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) return Collections.emptyList();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (!p.isActivo()) continue;
            PrecioAcordado precio = p.obtenerUltimoPrecio(prov);
            if (precio != null && precio.estaVigente()) resultado.add(p);
        }
        return resultado;
    }

    public Producto buscarProductoPorCodigo(String codigo) {
        for (Producto p : productos)
            if (p.getCodigoInterno().equals(codigo)) return p;
        return null;
    }

    public List<Producto> listarTodos() { return new ArrayList<>(productos); }

    private Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }
}

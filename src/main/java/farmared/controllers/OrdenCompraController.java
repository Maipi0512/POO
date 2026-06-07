package farmared.controllers;

import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m1_usuarios.Autorizacion;
import farmared.modulos.m1_usuarios.Usuario;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton controlador de Ordenes de Compra (DS1 - Generar Orden de Compra).
 *
 * Flujo DS1:
 *   crearOrdenCompra(idProveedor)
 *     → loop buscarProveedorPorId → new OrdenCompra(unProveedor)
 *   agregarItem(oc, codigoProducto, cantidad)
 *     → loop buscarProductoPorCodigo → obtenerUltimoPrecio → new DetalleOC → agregarDetalle
 *   emitirOrdenCompra(oc)
 *     → calcularTotal → validarNuevaOC(importeTotal) → emitir() / emitirConAutorizacion()
 */
public class OrdenCompraController {

    private static OrdenCompraController instancia;

    private List<Proveedor> proveedores;
    private List<Producto>  productos;
    private List<OrdenCompra> ordenes;

    private int contadorOC           = 1;
    private int contadorAutorizacion = 1;

    private OrdenCompraController() {
        proveedores = new ArrayList<>();
        productos   = new ArrayList<>();
        ordenes     = new ArrayList<>();
    }

    public static OrdenCompraController getInstance() {
        if (instancia == null) {
            instancia = new OrdenCompraController();
        }
        return instancia;
    }

    // =========================================================================
    // DS1: crearOrdenCompra(idProveedor)
    // =========================================================================

    /** DS1 loop "en coleccion de proveedores". */
    public Proveedor buscarProveedorPorId(String idProveedor) {
        for (Proveedor p : proveedores) {
            if (p.getCuit().equals(idProveedor)) return p;
        }
        return null;
    }

    /**
     * DS1 paso 1: Ctrl = OrdenCompraController.getInstance() / crearOrdenCompra(idProveedor).
     * Busca proveedor, instancia OrdenCompra y la devuelve lista para recibir items.
     */
    public OrdenCompra crearOrdenCompra(String idProveedor) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null)
            throw new IllegalArgumentException("Proveedor no encontrado: " + idProveedor);
        return new OrdenCompra("OC-" + String.format("%08d", contadorOC++), unProveedor);
    }

    // =========================================================================
    // DS1: agregarItem(oc, codigoProducto, cantidad)
    // =========================================================================

    /** DS1 loop "en coleccion de productos". */
    public Producto buscarProductoPorCodigo(String codigoProducto) {
        for (Producto p : productos) {
            if (p.getCodigoInterno().equals(codigoProducto)) return p;
        }
        return null;
    }

    /**
     * DS1: agregarItem loop.
     * Busca producto, obtiene precio vigente (estaVigente → true),
     * crea DetalleOC (calcularSubtotal), lo agrega a la OC.
     */
    public void agregarItem(OrdenCompra unaOrdenCompra, String codigoProducto, double cantidad) {
        Producto unProducto = buscarProductoPorCodigo(codigoProducto);
        if (unProducto == null)
            throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);

        PrecioAcordado unPrecio = unProducto.obtenerUltimoPrecio(unaOrdenCompra.getProveedor());
        if (unPrecio == null || !unPrecio.estaVigente())
            throw new IllegalStateException("Sin precio vigente para: " + codigoProducto);

        int nroLinea = unaOrdenCompra.getDetalles().size() + 1;
        DetalleOC unDetalleOC = new DetalleOC(nroLinea, unProducto, cantidad, unPrecio.getPrecioUnitario());
        unaOrdenCompra.agregarDetalle(unDetalleOC);
    }

    // =========================================================================
    // DS1: emitirOrdenCompra(unaOrdenCompra)
    // =========================================================================

    /**
     * DS1: emitirOrdenCompra(unaOrdenCompra).
     * calcularTotal → validarNuevaOC(importeTotal) → emitir() o emitirConAutorizacion().
     * Si supera el tope y no hay supervisor, lanza excepcion.
     */
    public OrdenCompra emitirOrdenCompra(OrdenCompra unaOrdenCompra, Usuario supervisor, String motivo) {
        double importeTotal = unaOrdenCompra.calcularTotal();
        boolean valida = unaOrdenCompra.getProveedor().validarNuevaOC(importeTotal);
        if (valida) {
            unaOrdenCompra.emitir();
        } else {
            if (supervisor == null || !supervisor.esAutorizador())
                throw new IllegalStateException(
                    "OC supera el tope de deuda. Se requiere autorizacion de Supervisor.");
            Autorizacion auth = new Autorizacion(contadorAutorizacion++, supervisor,
                motivo != null ? motivo : "Autorizacion OC por exceso de tope");
            unaOrdenCompra.emitirConAutorizacion(auth);
        }
        ordenes.add(unaOrdenCompra);
        return unaOrdenCompra;
    }

    public OrdenCompra emitirOrdenCompra(OrdenCompra unaOrdenCompra) {
        return emitirOrdenCompra(unaOrdenCompra, null, null);
    }

    // =========================================================================
    // Busquedas y consultas
    // =========================================================================

    public OrdenCompra buscarOrdenCompraPorNumero(String numero) {
        for (OrdenCompra oc : ordenes)
            if (oc.getNumero().equals(numero)) return oc;
        return null;
    }

    public void anularOrdenCompra(String numero) {
        OrdenCompra oc = buscarOrdenCompraPorNumero(numero);
        if (oc == null) throw new IllegalArgumentException("OC no encontrada: " + numero);
        oc.anular();
    }

    // =========================================================================
    // RF-07 / M7: Compulsa de precios de un producto
    // =========================================================================

    /** Retorna todos los precios historicos y vigentes del producto, por proveedor. */
    public List<PrecioAcordado> consultarCompulsaPrecios(String codigoProducto) {
        Producto p = buscarProductoPorCodigo(codigoProducto);
        return p != null ? p.obtenerPreciosHistoricos() : new ArrayList<>();
    }

    // =========================================================================
    // Registro de entidades
    // =========================================================================

    public void agregarProveedor(Proveedor p) {
        if (!proveedores.contains(p)) proveedores.add(p);
    }

    public void agregarProducto(Producto p) {
        if (!productos.contains(p)) productos.add(p);
    }

    public List<Proveedor>   getProveedores() { return new ArrayList<>(proveedores); }
    public List<Producto>    getProductos()   { return new ArrayList<>(productos); }
    public List<OrdenCompra> getOrdenes()     { return new ArrayList<>(ordenes); }
}

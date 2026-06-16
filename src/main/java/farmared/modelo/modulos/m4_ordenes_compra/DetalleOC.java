package farmared.modelo.modulos.m4_ordenes_compra;

import farmared.modelo.modulos.m2_productos.Producto;

/** Linea de una Orden de Compra (DS1, paso new DetalleOC → calcularSubtotal). */
public class DetalleOC {

    private int nroLinea;
    private Producto producto;
    private double cantidad;
    private double precioUnitario;
    private double subtotal;

    public DetalleOC(int nroLinea, Producto producto, double cantidad, double precioUnitario) {
        this.nroLinea = nroLinea;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = calcularSubtotal();
    }

    /** DS1: calcula el subtotal de la línea (cantidad × precioUnitario). */
    public double calcularSubtotal() {
        return Math.round(cantidad * precioUnitario * 100.0) / 100.0;
    }

    public int getNroLinea()          { return nroLinea; }
    public Producto getProducto()     { return producto; }
    public double getCantidad()       { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
    public double getSubtotal()       { return subtotal; }

    @Override
    public String toString() {
        return "DetalleOC{L" + nroLinea + " | " + producto.getDescripcion()
                + " x" + cantidad + " @ $" + precioUnitario + " = $" + subtotal + "}";
    }
}

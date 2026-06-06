package farmared.modulos.m5_comprobantes;

import farmared.modulos.m2_productos.Producto;

/** Linea de un Comprobante: producto, cantidad, precio unitario e IVA. */
public class DetalleComprobante {

    private int nroLinea;
    private Producto producto;
    private double cantidad;
    private double precioUnitario;
    private double alicuotaIVA;
    private double subtotal;
    private double importeIVA;

    public DetalleComprobante(int nroLinea, Producto producto,
                               double cantidad, double precioUnitario, double alicuotaIVA) {
        this.nroLinea = nroLinea;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.alicuotaIVA = alicuotaIVA;
        this.subtotal    = Math.round(cantidad * precioUnitario * 100.0) / 100.0;
        this.importeIVA  = Math.round(subtotal * (alicuotaIVA / 100.0) * 100.0) / 100.0;
    }

    public double getTotalConIVA() { return subtotal + importeIVA; }

    public int getNroLinea()         { return nroLinea; }
    public Producto getProducto()    { return producto; }
    public double getCantidad()      { return cantidad; }
    public double getPrecioUnitario(){ return precioUnitario; }
    public double getAlicuotaIVA()   { return alicuotaIVA; }
    public double getSubtotal()      { return subtotal; }
    public double getImporteIVA()    { return importeIVA; }

    @Override
    public String toString() {
        return "Linea{" + nroLinea + " | " + producto.getDescripcion()
                + " x" + cantidad + " @ $" + precioUnitario
                + " | sub=$" + subtotal + "}";
    }
}

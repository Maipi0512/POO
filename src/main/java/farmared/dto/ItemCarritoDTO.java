package farmared.dto;

public final class ItemCarritoDTO {
    private final String codigoProducto;
    private final double cantidad;
    private final double precioUnitario;

    public ItemCarritoDTO(String codigoProducto, double cantidad, double precioUnitario) {
        this.codigoProducto = codigoProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getCodigoProducto() { return codigoProducto; }
    public double getCantidad() { return cantidad; }
    public double getPrecioUnitario() { return precioUnitario; }
}

package farmared.dto;

import java.util.Date;
import java.util.List;

public final class OrdenCompraDTO {
    private final String numero;
    private final Date fechaEmision;
    private final String cuitProveedor;
    private final String razonSocialProveedor;
    private final double importeTotal;
    private final String estado;
    private final String autorizadoPor;
    private final List<DetalleOCDTO> detalles;

    public OrdenCompraDTO(String numero, Date fechaEmision, String cuitProveedor,
                          String razonSocialProveedor, double importeTotal, String estado,
                          String autorizadoPor, List<DetalleOCDTO> detalles) {
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.cuitProveedor = cuitProveedor;
        this.razonSocialProveedor = razonSocialProveedor;
        this.importeTotal = importeTotal;
        this.estado = estado;
        this.autorizadoPor = autorizadoPor;
        this.detalles = detalles != null ? List.copyOf(detalles) : List.of();
    }

    public String getNumero() { return numero; }
    public Date getFechaEmision() { return fechaEmision; }
    public String getCuitProveedor() { return cuitProveedor; }
    public String getRazonSocialProveedor() { return razonSocialProveedor; }
    public double getImporteTotal() { return importeTotal; }
    public String getEstado() { return estado; }
    public String getAutorizadoPor() { return autorizadoPor; }
    public List<DetalleOCDTO> getDetalles() { return detalles; }

    @Override
    public String toString() { return numero + " | " + razonSocialProveedor + " | $" + importeTotal; }

    public static final class DetalleOCDTO {
        private final int nroLinea;
        private final String codigoProducto;
        private final String descripcionProducto;
        private final double cantidad;
        private final double precioUnitario;
        private final double subtotal;

        public DetalleOCDTO(int nroLinea, String codigoProducto, String descripcionProducto,
                            double cantidad, double precioUnitario, double subtotal) {
            this.nroLinea = nroLinea;
            this.codigoProducto = codigoProducto;
            this.descripcionProducto = descripcionProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.subtotal = subtotal;
        }

        public int getNroLinea() { return nroLinea; }
        public String getCodigoProducto() { return codigoProducto; }
        public String getDescripcionProducto() { return descripcionProducto; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getSubtotal() { return subtotal; }
    }
}

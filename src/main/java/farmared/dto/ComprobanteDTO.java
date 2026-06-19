package farmared.dto;

import java.util.Date;
import java.util.List;

public final class ComprobanteDTO {
    private final String numero;
    private final String tipo;
    private final Date fechaEmision;
    private final Date fechaRecepcion;
    private final double importeNeto;
    private final double importeIVA;
    private final double importeTotal;
    private final double saldoPendiente;
    private final String estado;
    private final String cuitProveedor;
    private final String razonSocialProveedor;
    private final String autorizadoPor;
    private final List<DetalleComprobanteDTO> detalles;

    public ComprobanteDTO(String numero, String tipo, Date fechaEmision, Date fechaRecepcion,
                          double importeNeto, double importeIVA, double importeTotal,
                          double saldoPendiente, String estado, String cuitProveedor,
                          String razonSocialProveedor, String autorizadoPor,
                          List<DetalleComprobanteDTO> detalles) {
        this.numero = numero;
        this.tipo = tipo;
        this.fechaEmision = fechaEmision;
        this.fechaRecepcion = fechaRecepcion;
        this.importeNeto = importeNeto;
        this.importeIVA = importeIVA;
        this.importeTotal = importeTotal;
        this.saldoPendiente = saldoPendiente;
        this.estado = estado;
        this.cuitProveedor = cuitProveedor;
        this.razonSocialProveedor = razonSocialProveedor;
        this.autorizadoPor = autorizadoPor;
        this.detalles = detalles != null ? List.copyOf(detalles) : List.of();
    }

    public String getNumero() { return numero; }
    public String getTipo() { return tipo; }
    public Date getFechaEmision() { return fechaEmision; }
    public Date getFechaRecepcion() { return fechaRecepcion; }
    public double getImporteNeto() { return importeNeto; }
    public double getImporteIVA() { return importeIVA; }
    public double getImporteTotal() { return importeTotal; }
    public double getSaldoPendiente() { return saldoPendiente; }
    public String getEstado() { return estado; }
    public String getCuitProveedor() { return cuitProveedor; }
    public String getRazonSocialProveedor() { return razonSocialProveedor; }
    public String getAutorizadoPor() { return autorizadoPor; }
    public List<DetalleComprobanteDTO> getDetalles() { return detalles; }

    @Override
    public String toString() { return tipo + " " + numero + " ($" + importeTotal + ")"; }

    public static final class DetalleComprobanteDTO {
        private final int nroLinea;
        private final String codigoProducto;
        private final String descripcionProducto;
        private final double cantidad;
        private final double precioUnitario;
        private final double alicuotaIVA;
        private final double subtotal;
        private final double importeIVA;

        public DetalleComprobanteDTO(int nroLinea, String codigoProducto, String descripcionProducto,
                                     double cantidad, double precioUnitario, double alicuotaIVA,
                                     double subtotal, double importeIVA) {
            this.nroLinea = nroLinea;
            this.codigoProducto = codigoProducto;
            this.descripcionProducto = descripcionProducto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.alicuotaIVA = alicuotaIVA;
            this.subtotal = subtotal;
            this.importeIVA = importeIVA;
        }

        public int getNroLinea() { return nroLinea; }
        public String getCodigoProducto() { return codigoProducto; }
        public String getDescripcionProducto() { return descripcionProducto; }
        public double getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }
        public double getAlicuotaIVA() { return alicuotaIVA; }
        public double getSubtotal() { return subtotal; }
        public double getImporteIVA() { return importeIVA; }
    }
}

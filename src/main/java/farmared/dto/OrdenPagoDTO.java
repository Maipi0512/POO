package farmared.dto;

import java.util.Date;
import java.util.List;

public final class OrdenPagoDTO {
    private final String numero;
    private final Date fechaEmision;
    private final String cuitProveedor;
    private final String razonSocialProveedor;
    private final double importeBruto;
    private final double totalRetenciones;
    private final double importeNeto;
    private final String estado;
    private final List<CancelacionComprobanteDTO> cancelaciones;
    private final List<RetencionDTO> retenciones;

    public OrdenPagoDTO(String numero, Date fechaEmision, String cuitProveedor,
                        String razonSocialProveedor, double importeBruto, double totalRetenciones,
                        double importeNeto, String estado,
                        List<CancelacionComprobanteDTO> cancelaciones, List<RetencionDTO> retenciones) {
        this.numero = numero;
        this.fechaEmision = fechaEmision;
        this.cuitProveedor = cuitProveedor;
        this.razonSocialProveedor = razonSocialProveedor;
        this.importeBruto = importeBruto;
        this.totalRetenciones = totalRetenciones;
        this.importeNeto = importeNeto;
        this.estado = estado;
        this.cancelaciones = cancelaciones != null ? List.copyOf(cancelaciones) : List.of();
        this.retenciones = retenciones != null ? List.copyOf(retenciones) : List.of();
    }

    public String getNumero() { return numero; }
    public Date getFechaEmision() { return fechaEmision; }
    public String getCuitProveedor() { return cuitProveedor; }
    public String getRazonSocialProveedor() { return razonSocialProveedor; }
    public double getImporteBruto() { return importeBruto; }
    public double getTotalRetenciones() { return totalRetenciones; }
    public double getImporteNeto() { return importeNeto; }
    public String getEstado() { return estado; }
    public List<CancelacionComprobanteDTO> getCancelaciones() { return cancelaciones; }
    public List<RetencionDTO> getRetenciones() { return retenciones; }

    @Override
    public String toString() { return numero + " | " + razonSocialProveedor + " | Neto: $" + importeNeto; }

    public static final class CancelacionComprobanteDTO {
        private final String numeroComprobante;
        private final double importeCancelado;
        private final boolean esPagoTotal;

        public CancelacionComprobanteDTO(String numeroComprobante, double importeCancelado, boolean esPagoTotal) {
            this.numeroComprobante = numeroComprobante;
            this.importeCancelado = importeCancelado;
            this.esPagoTotal = esPagoTotal;
        }

        public String getNumeroComprobante() { return numeroComprobante; }
        public double getImporteCancelado() { return importeCancelado; }
        public boolean isEsPagoTotal() { return esPagoTotal; }
    }

    public static final class RetencionDTO {
        private final String impuestoNombre;
        private final double baseImponible;
        private final double alicuota;
        private final double importe;

        public RetencionDTO(String impuestoNombre, double baseImponible, double alicuota, double importe) {
            this.impuestoNombre = impuestoNombre;
            this.baseImponible = baseImponible;
            this.alicuota = alicuota;
            this.importe = importe;
        }

        public String getImpuestoNombre() { return impuestoNombre; }
        public double getBaseImponible() { return baseImponible; }
        public double getAlicuota() { return alicuota; }
        public double getImporte() { return importe; }
    }
}

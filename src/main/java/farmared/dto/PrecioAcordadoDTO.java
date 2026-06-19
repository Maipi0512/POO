package farmared.dto;

import java.util.Date;

public final class PrecioAcordadoDTO {
    private final double precioUnitario;
    private final Date fechaAcuerdo;
    private final Date fechaVencimiento;
    private final String cuitProveedor;
    private final String razonSocialProveedor;
    private final boolean vigente;

    public PrecioAcordadoDTO(double precioUnitario, Date fechaAcuerdo, Date fechaVencimiento,
                             String cuitProveedor, String razonSocialProveedor, boolean vigente) {
        this.precioUnitario = precioUnitario;
        this.fechaAcuerdo = fechaAcuerdo;
        this.fechaVencimiento = fechaVencimiento;
        this.cuitProveedor = cuitProveedor;
        this.razonSocialProveedor = razonSocialProveedor;
        this.vigente = vigente;
    }

    public double getPrecioUnitario() { return precioUnitario; }
    public Date getFechaAcuerdo() { return fechaAcuerdo; }
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public String getCuitProveedor() { return cuitProveedor; }
    public String getRazonSocialProveedor() { return razonSocialProveedor; }
    public boolean isVigente() { return vigente; }
}

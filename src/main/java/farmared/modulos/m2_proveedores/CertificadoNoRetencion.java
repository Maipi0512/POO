package farmared.modulos.m2_proveedores;

import farmared.enums.TipoImpuesto;
import java.util.Date;

/**
 * Certificado de no retencion presentado por el proveedor (RF-09).
 * Mientras este vigente, el sistema NO aplica la retencion del impuesto asociado.
 * Al vencer, la retencion se reactiva automaticamente.
 */
public class CertificadoNoRetencion {

    private String numeroCertificado;
    private TipoImpuesto tipoImpuesto;
    private Date fechaEmision;
    private Date fechaVigencia;
    private double baseImponible;

    public CertificadoNoRetencion(String numeroCertificado, TipoImpuesto tipoImpuesto,
                                   Date fechaEmision, Date fechaVigencia, double baseImponible) {
        this.numeroCertificado = numeroCertificado;
        this.tipoImpuesto = tipoImpuesto;
        this.fechaEmision = fechaEmision;
        this.fechaVigencia = fechaVigencia;
        this.baseImponible = baseImponible;
    }

    /**
     * Retorna true si el certificado cubre la fecha indicada.
     * Llamado en DS2 antes de calcular retenciones.
     */
    public boolean estaVigente(Date fecha) {
        return fecha != null
            && !fecha.before(fechaEmision)
            && !fecha.after(fechaVigencia);
    }

    public String getNumeroCertificado() { return numeroCertificado; }
    public TipoImpuesto getTipoImpuesto(){ return tipoImpuesto; }
    public Date getFechaEmision()        { return fechaEmision; }
    public Date getFechaVigencia()       { return fechaVigencia; }
    public double getBaseImponible()     { return baseImponible; }

    @Override
    public String toString() {
        return "Certificado{" + numeroCertificado + ", tipo=" + tipoImpuesto
                + ", vigencia=" + fechaVigencia + "}";
    }
}

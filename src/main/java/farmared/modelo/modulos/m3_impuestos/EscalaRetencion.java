package farmared.modelo.modulos.m3_impuestos;

/**
 * Tramo de monto con su porcentaje de retencion (RF-10).
 * Ejemplo: $0–$10.000 → 3%, $10.001–$50.000 → 5%, sin limite → 7%.
 */
public class EscalaRetencion {

    private double montoDesde;
    private double montoHasta;   // 0 = sin limite superior
    private double porcentaje;

    public EscalaRetencion(double montoDesde, double montoHasta, double porcentaje) {
        this.montoDesde = montoDesde;
        this.montoHasta = montoHasta;
        this.porcentaje = porcentaje;
    }

    /** Retorna true si el monto cae dentro de este tramo. */
    public boolean aplicaA(double monto) {
        boolean superaMinimo = monto >= montoDesde;
        boolean bajoMaximo   = (montoHasta == 0) || (monto <= montoHasta);
        return superaMinimo && bajoMaximo;
    }

    public double getMontoDesde()  { return montoDesde; }
    public double getMontoHasta()  { return montoHasta; }
    public double getPorcentaje()  { return porcentaje; }
}

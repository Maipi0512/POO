package farmared.modulos.m6_ordenes_pago;

/**
 * Retencion impositiva efectuada en una Orden de Pago (RF-20).
 * Guarda el impuesto origen, la base, el porcentaje aplicado y el importe.
 */
public class Retencion {

    private Impuesto impuesto;
    private double base;
    private double porcentajeAplicado;
    private double importe;

    public Retencion(Impuesto impuesto, double base, double porcentajeAplicado, double importe) {
        this.impuesto = impuesto;
        this.base = base;
        this.porcentajeAplicado = porcentajeAplicado;
        this.importe = importe;
    }

    public Impuesto getImpuesto()          { return impuesto; }
    public double getBase()                { return base; }
    public double getPorcentajeAplicado()  { return porcentajeAplicado; }
    public double getImporte()             { return importe; }

    @Override
    public String toString() {
        return "Retencion{" + impuesto.getNombre()
                + ", base=" + base + ", %" + porcentajeAplicado
                + ", importe=" + importe + "}";
    }
}

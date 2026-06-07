package farmared.modulos.m6_ordenes_pago;

import farmared.enums.TipoImpuesto;
import farmared.modulos.m2_proveedores.Proveedor;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta base para los impuestos retenibles (RF-08, RF-10).
 *
 * Polimorfismo: cada subclase implementa calcularRetencion() con su logica propia.
 * El sistema llama al metodo sin saber el tipo concreto → polimorfismo.
 */
public abstract class Impuesto {

    private int idImpuesto;
    private String nombre;
    private TipoImpuesto tipo;
    protected double porcentajeBase;
    protected double minimoNoImponible;
    protected List<EscalaRetencion> escalas;

    public Impuesto(int idImpuesto, String nombre, TipoImpuesto tipo,
                    double porcentajeBase, double minimoNoImponible) {
        this.idImpuesto = idImpuesto;
        this.nombre = nombre;
        this.tipo = tipo;
        this.porcentajeBase = porcentajeBase;
        this.minimoNoImponible = minimoNoImponible;
        this.escalas = new ArrayList<>();
    }

    public void agregarEscala(EscalaRetencion escala) {
        escalas.add(escala);
    }

    /**
     * Busca el porcentaje aplicable segun escalas; usa porcentajeBase si no hay escalas.
     */
    public double determinarPorcentaje(double base) {
        for (EscalaRetencion e : escalas) {
            if (e.aplicaA(base)) return e.getPorcentaje();
        }
        return porcentajeBase;
    }

    /**
     * Calcula el importe a retener. ABSTRACTO: cada subclase aplica su logica.
     *
     * @param base  monto bruto del pago
     * @param prov  proveedor al que se paga
     * @return      importe a retener (0 si no aplica o no supera el minimo)
     */
    public abstract double calcularRetencion(double base, Proveedor prov);

    public int getIdImpuesto()              { return idImpuesto; }
    public String getNombre()               { return nombre; }
    public TipoImpuesto getTipo()           { return tipo; }
    public double getPorcentajeBase()       { return porcentajeBase; }
    public double getMinimoNoImponible()    { return minimoNoImponible; }

    public void setMinimoNoImponible(double mni) { this.minimoNoImponible = mni; }

    @Override
    public String toString() {
        return nombre + " (" + tipo + ", base=" + porcentajeBase + "%)";
    }
}

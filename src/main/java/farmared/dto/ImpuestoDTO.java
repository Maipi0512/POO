package farmared.dto;

public final class ImpuestoDTO {
    private final int idImpuesto;
    private final String nombre;
    private final String tipo;
    private final double porcentajeBase;
    private final double minimoNoImponible;

    public ImpuestoDTO(int idImpuesto, String nombre, String tipo, double porcentajeBase, double minimoNoImponible) {
        this.idImpuesto = idImpuesto;
        this.nombre = nombre;
        this.tipo = tipo;
        this.porcentajeBase = porcentajeBase;
        this.minimoNoImponible = minimoNoImponible;
    }

    public int getIdImpuesto() { return idImpuesto; }
    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public double getPorcentajeBase() { return porcentajeBase; }
    public double getMinimoNoImponible() { return minimoNoImponible; }

    @Override
    public String toString() { return nombre + " (" + tipo + ")"; }
}

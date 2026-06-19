package farmared.dto;

public final class RubroDTO {
    private final int idRubro;
    private final String nombre;
    private final String descripcion;

    public RubroDTO(int idRubro, String nombre, String descripcion) {
        this.idRubro = idRubro;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdRubro() { return idRubro; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() { return nombre; }
}

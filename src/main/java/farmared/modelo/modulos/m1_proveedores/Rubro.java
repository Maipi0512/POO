package farmared.modelo.modulos.m1_proveedores;

/**
 * Categoria que clasifica el tipo de bienes o servicios (RF-02).
 * Un Proveedor puede tener muchos Rubros.
 * Un Producto pertenece a exactamente un Rubro.
 */
public class Rubro {

    private int idRubro;
    private String nombre;
    private String descripcion;

    public Rubro(int idRubro, String nombre, String descripcion) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rubro no puede ser vacio.");
        }
        this.idRubro = idRubro;
        this.nombre = nombre.trim();
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    public int getIdRubro()        { return idRubro; }
    public String getNombre()      { return nombre; }
    public String getDescripcion() { return descripcion; }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rubro no puede ser vacio.");
        }
        this.nombre = nombre.trim();
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion != null ? descripcion.trim() : "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rubro)) return false;
        return idRubro == ((Rubro) o).idRubro;
    }

    @Override
    public int hashCode() { return Integer.hashCode(idRubro); }

    @Override
    public String toString() { return nombre; }
}

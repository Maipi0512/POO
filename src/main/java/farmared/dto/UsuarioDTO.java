package farmared.dto;

public final class UsuarioDTO {
    private final int idUsuario;
    private final String nombre;
    private final String apellido;
    private final String username;
    private final String rol;
    private final boolean activo;

    public UsuarioDTO(int idUsuario, String nombre, String apellido, String username, String rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.rol = rol;
        this.activo = activo;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }

    @Override
    public String toString() { return nombre + " " + apellido + " (" + username + ")"; }
}

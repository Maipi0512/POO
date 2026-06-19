package farmared.dto;

public final class UsuarioDTO {
    private final int idUsuario;
    private final String nombre;
    private final String apellido;
    private final String username;
    private final String rol;

    public UsuarioDTO(int idUsuario, String nombre, String apellido, String username, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }

    @Override
    public String toString() { return nombre + " " + apellido + " (" + username + ")"; }
}

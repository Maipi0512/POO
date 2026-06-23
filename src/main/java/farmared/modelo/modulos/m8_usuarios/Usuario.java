package farmared.modelo.modulos.m8_usuarios;

import farmared.modelo.enums.RolUsuario;

public class Usuario {

    private int idUsuario;
    private String nombre;
    private String apellido;
    private String username;
    private String password;
    private RolUsuario rol;
    private boolean activo;

    public Usuario(int idUsuario, String nombre, String apellido,
                   String username, String password, RolUsuario rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede ser vacio.");
        }
        if (apellido == null || apellido.isBlank()) {
            throw new IllegalArgumentException("El apellido no puede ser vacio.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El username no puede ser vacio.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("El password no puede ser vacio.");
        }
        this.idUsuario = idUsuario;
        this.nombre = nombre.trim();
        this.apellido = apellido.trim();
        this.username = username.trim();
        this.password = password.trim();
        this.rol = rol;
        this.activo = true;
    }

    /** Solo SUPERVISOR puede autorizar operaciones que superan limites (RF-12, RF-17). */
    public boolean esAutorizador() {
        return rol == RolUsuario.SUPERVISOR;
    }

    public boolean esAdministrador() {
        return rol == RolUsuario.ADMINISTRADOR;
    }

    public boolean validarCredenciales(String usuario, String clave) {
        return activo && username.equals(usuario) && password.equals(clave);
    }

    public int getIdUsuario()  { return idUsuario; }
    public String getNombre()  { return nombre; }
    public String getApellido(){ return apellido; }
    public String getUsername(){ return username; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo()  { return activo; }

    public void setActivo(boolean activo)    { this.activo = activo; }
    public void setRol(RolUsuario rol)       { this.rol = rol; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return nombre + " " + apellido + " [" + rol + "]";
    }
}

package farmared.modulos.m1_usuarios;

import farmared.enums.RolUsuario;

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
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.username = username;
        this.password = password;
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

    public int getIdUsuario()  { return idUsuario; }
    public String getNombre()  { return nombre; }
    public String getApellido(){ return apellido; }
    public String getUsername(){ return username; }
    public RolUsuario getRol() { return rol; }
    public boolean isActivo()  { return activo; }

    public void setActivo(boolean activo) { this.activo = activo; }
    public void setRol(RolUsuario rol)    { this.rol = rol; }

    @Override
    public String toString() {
        return nombre + " " + apellido + " [" + rol + "]";
    }
}

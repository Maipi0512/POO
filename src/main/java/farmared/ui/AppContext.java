package farmared.ui;

import farmared.modulos.m8_usuarios.Usuario;
import farmared.sistema.SistemaGestionCompras;

/**
 * Singleton que centraliza el acceso al sistema y al usuario logueado (patron MVC).
 */
public final class AppContext {

    private static AppContext instancia;

    private final SistemaGestionCompras sistema;
    private Usuario usuarioActual;

    private AppContext() {
        sistema = new SistemaGestionCompras("FarmaRed", "1.0");
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) {
            instancia = new AppContext();
        }
        return instancia;
    }

    public SistemaGestionCompras getSistema() {
        return sistema;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }
}

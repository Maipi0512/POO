package farmared.controladores;

import farmared.modulos.m1_usuarios.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador singleton — Modulo 8: Usuarios y Seguridad (RF-28, RF-29).
 */
public class UsuarioController {

    private static UsuarioController instancia;

    private final List<Usuario> usuarios;

    private UsuarioController(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public static void inicializar(List<Usuario> usuarios) {
        instancia = new UsuarioController(usuarios);
    }

    public static UsuarioController getInstance() {
        if (instancia == null) throw new IllegalStateException("UsuarioController no inicializado.");
        return instancia;
    }

    public void registrarUsuario(Usuario usuario) {
        usuarios.add(usuario);
    }

    public Usuario autenticarUsuario(String username, String password) {
        for (Usuario u : usuarios)
            if (u.validarCredenciales(username, password)) return u;
        return null;
    }

    public List<Usuario> listarSupervisores() {
        List<Usuario> sup = new ArrayList<>();
        for (Usuario u : usuarios) if (u.esAutorizador()) sup.add(u);
        return sup;
    }

    public List<Usuario> listarTodos() { return new ArrayList<>(usuarios); }
}

package farmared.controladores;

import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.dto.*;

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

    public UsuarioDTO autenticarUsuarioDTO(String username, String password) {
        return DtoMapper.toDTO(autenticarUsuario(username, password));
    }

    public List<Usuario> listarSupervisores() {
        List<Usuario> sup = new ArrayList<>();
        for (Usuario u : usuarios) if (u.esAutorizador()) sup.add(u);
        return sup;
    }

    public List<UsuarioDTO> listarSupervisoresDTO() {
        return DtoMapper.toUsuarioDTOList(listarSupervisores());
    }

    public List<Usuario> listarTodos() { return new ArrayList<>(usuarios); }
    public List<UsuarioDTO> listarTodosDTO() { return DtoMapper.toUsuarioDTOList(usuarios); }

    public void darBajaUsuario(String username) {
        Usuario target = buscarPorUsername(username);
        if (target.getRol() == farmared.modelo.enums.RolUsuario.ADMINISTRADOR && contarAdminsActivos() <= 1) {
            throw new IllegalStateException("No se puede dar de baja al unico administrador activo.");
        }
        target.setActivo(false);
    }

    public void reactivarUsuario(String username) {
        buscarPorUsername(username).setActivo(true);
    }

    public void modificarRol(String username, farmared.modelo.enums.RolUsuario nuevoRol) {
        Usuario target = buscarPorUsername(username);
        if (target.getRol() == farmared.modelo.enums.RolUsuario.ADMINISTRADOR
                && nuevoRol != farmared.modelo.enums.RolUsuario.ADMINISTRADOR
                && contarAdminsActivos() <= 1) {
            throw new IllegalStateException("No se puede cambiar el rol del unico administrador activo.");
        }
        target.setRol(nuevoRol);
    }

    public void cambiarPassword(String username, String nuevaPassword) {
        if (nuevaPassword == null || nuevaPassword.isBlank()) {
            throw new IllegalArgumentException("La nueva password no puede ser vacia.");
        }
        buscarPorUsername(username).setPassword(nuevaPassword.trim());
    }

    public int siguienteId() {
        return usuarios.stream().mapToInt(Usuario::getIdUsuario).max().orElse(0) + 1;
    }

    private Usuario buscarPorUsername(String username) {
        for (Usuario u : usuarios)
            if (u.getUsername().equals(username)) return u;
        throw new IllegalArgumentException("Usuario no encontrado: " + username);
    }

    private long contarAdminsActivos() {
        return usuarios.stream()
                .filter(u -> u.isActivo() && u.getRol() == farmared.modelo.enums.RolUsuario.ADMINISTRADOR)
                .count();
    }
}

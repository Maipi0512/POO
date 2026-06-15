package farmared.modelo.enums;

/**
 * Rol que define los permisos del usuario dentro del sistema.
 * OPERADOR → registra comprobantes, emite OC y OP.
 * SUPERVISOR → puede autorizar operaciones que superan limites (tope deuda, desvios).
 * ADMINISTRADOR → gestiona usuarios y configuracion del sistema.
 */
public enum RolUsuario {
    ADMINISTRADOR,
    OPERADOR,
    SUPERVISOR
}

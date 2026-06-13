package farmared.sistema.observador;

/** Patron Observer: las vistas se actualizan ante cambios del sistema. */
public interface ObservadorSistema {
    void actualizar(String evento);
}

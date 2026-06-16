package farmared.vistas.observador;

import java.util.ArrayList;
import java.util.List;

/** Subject del patron Observer. Notifica a las vistas registradas. */
public class NotificadorSistema {

    private static NotificadorSistema instancia;

    private final List<ObservadorSistema> observadores = new ArrayList<>();

    private NotificadorSistema() {}

    public static synchronized NotificadorSistema getInstancia() {
        if (instancia == null) instancia = new NotificadorSistema();
        return instancia;
    }

    public void suscribir(ObservadorSistema observador) {
        if (!observadores.contains(observador)) observadores.add(observador);
    }

    public void desuscribir(ObservadorSistema observador) {
        observadores.remove(observador);
    }

    public void notificar(String evento) {
        for (ObservadorSistema o : new ArrayList<>(observadores)) {
            o.actualizar(evento);
        }
    }
}

package farmared;

import farmared.controladores.AppContext;
import farmared.vistas.DatosIniciales;
import farmared.vistas.LoginDialog;
import farmared.vistas.VistaPrincipal;

import javax.swing.*;

/**
 * Punto de entrada de la aplicacion con interfaz grafica Swing.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::iniciarAplicacion);
    }

    public static void iniciarAplicacion() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        AppContext.getInstancia(); // inicializa controladores y datos

        LoginDialog login = new LoginDialog(null);
        login.setVisible(true);

        if (login.fueAceptado()) {
            VistaPrincipal ventana = new VistaPrincipal();
            ventana.setVisible(true);
        } else {
            System.exit(0);
        }
    }
}

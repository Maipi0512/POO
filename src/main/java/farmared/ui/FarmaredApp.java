package farmared.ui;

import javax.swing.*;

/**
 * Punto de entrada de la aplicacion con interfaz grafica Swing.
 */
public class FarmaredApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FarmaredApp::iniciarAplicacion);
    }

    public static void iniciarAplicacion() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        DatosIniciales.cargar(AppContext.getInstancia().getSistema());

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

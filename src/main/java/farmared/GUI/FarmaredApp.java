<<<<<<<< HEAD:src/main/java/farmared/Main.java
﻿package farmared;

import farmared.controladores.AppContext;
import farmared.vistas.DatosIniciales;
import farmared.vistas.LoginDialog;
import farmared.vistas.VistaPrincipal;
========
package farmared.GUI;
>>>>>>>> 4f7806ab87b6a3fe759880a16e996f93a8bf6870:src/main/java/farmared/GUI/FarmaredApp.java

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

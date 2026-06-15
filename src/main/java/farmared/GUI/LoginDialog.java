<<<<<<<< HEAD:src/main/java/farmared/vistas/LoginDialog.java
﻿package farmared.vistas;

import farmared.controladores.AppContext;
import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.vistas.util.UiUtil;
========
package farmared.GUI;

import farmared.modulos.m1_usuarios.Usuario;
import farmared.GUI.util.UiUtil;
>>>>>>>> 4f7806ab87b6a3fe759880a16e996f93a8bf6870:src/main/java/farmared/GUI/LoginDialog.java

import javax.swing.*;
import java.awt.*;

/**
 * Vista de login. El controlador valida contra SistemaGestionCompras (MVC).
 */
public class LoginDialog extends JDialog {

    private final JTextField campoUsuario = new JTextField(18);
    private final JPasswordField campoClave = new JPasswordField(18);
    private boolean aceptado;

    public LoginDialog(Frame owner) {
        super(owner, "FarmaRed - Inicio de sesion", true);
        construir();
    }

    private void construir() {
        setLayout(new BorderLayout(10, 10));
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Usuario:"), gbc);
        gbc.gridx = 1;
        form.add(campoUsuario, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Contrasena:"), gbc);
        gbc.gridx = 1;
        form.add(campoClave, gbc);

        JLabel ayuda = new JLabel("<html>Demo: alopez/pass (operador) | crios/pass (supervisor)</html>");
        ayuda.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ingresar = new JButton("Ingresar");
        JButton cancelar = new JButton("Cancelar");
        botones.add(ingresar);
        botones.add(cancelar);

        add(form, BorderLayout.CENTER);
        add(ayuda, BorderLayout.NORTH);
        add(botones, BorderLayout.SOUTH);

        ingresar.addActionListener(e -> autenticar());
        cancelar.addActionListener(e -> dispose());
        getRootPane().setDefaultButton(ingresar);

        pack();
        setLocationRelativeTo(getOwner());
        setResizable(false);
    }

    private void autenticar() {
        String usuario = campoUsuario.getText().trim();
        String clave = new String(campoClave.getPassword());

        if (usuario.isEmpty() || clave.isEmpty()) {
            UiUtil.mostrarError(this, "Complete usuario y contrasena.");
            return;
        }

        Usuario logueado = AppContext.getInstancia().getUsuarioCtrl().autenticarUsuario(usuario, clave);
        if (logueado == null) {
            UiUtil.mostrarError(this, "Credenciales invalidas.");
            return;
        }

        AppContext.getInstancia().setUsuarioActual(logueado);
        aceptado = true;
        dispose();
    }

    public boolean fueAceptado() {
        return aceptado;
    }
}

package farmared.vistas.paneles;

import farmared.controladores.AppContext;
import farmared.controladores.UsuarioController;
import farmared.dto.UsuarioDTO;
import farmared.modelo.enums.RolUsuario;
import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.vistas.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelUsuarios extends JPanel {

    private final UsuarioController controlador = AppContext.getInstancia().getUsuarioCtrl();
    private final String usernameActual = AppContext.getInstancia().getUsuarioActual().getUsername();

    private final JTable tabla = new JTable();

    private final JTextField nombre      = new JTextField(15);
    private final JTextField apellido    = new JTextField(15);
    private final JTextField username    = new JTextField(15);
    private final JPasswordField password = new JPasswordField(15);
    private final JComboBox<RolUsuario> rol = new JComboBox<>(RolUsuario.values());

    private final JButton btnNuevo      = new JButton("Nuevo");
    private final JButton btnRegistrar  = new JButton("Registrar");
    private final JButton btnModRol     = new JButton("Modificar Rol");
    private final JButton btnCambiarPass = new JButton("Cambiar Password");
    private final JButton btnReactivar  = new JButton("Reactivar");
    private final JButton btnBaja       = new JButton("Dar de Baja");

    public PanelUsuarios() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(
                new String[]{"ID", "Nombre", "Apellido", "Username", "Rol", "Activo"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        boolean esAdmin = AppContext.getInstancia().getUsuarioActual().esAdministrador();

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        if (esAdmin) {
            add(construirFormulario(), BorderLayout.EAST);
        }

        btnNuevo.addActionListener(e -> limpiarFormulario());
        btnRegistrar.addActionListener(e -> registrar());
        btnModRol.addActionListener(e -> modificarRol());
        btnCambiarPass.addActionListener(e -> cambiarPassword());
        btnReactivar.addActionListener(e -> reactivar());
        btnBaja.addActionListener(e -> darBaja());

        btnModRol.setEnabled(esAdmin);
        btnCambiarPass.setEnabled(esAdmin);
        btnReactivar.setEnabled(esAdmin);
        btnBaja.setEnabled(esAdmin);

        cargarDatos();
    }

    private JPanel construirFormulario() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Gestion de usuario"));
        form.setPreferredSize(new Dimension(270, 0));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; form.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; form.add(nombre, gbc);

        gbc.gridy = ++row;
        gbc.gridx = 0; form.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; form.add(apellido, gbc);

        gbc.gridy = ++row;
        gbc.gridx = 0; form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; form.add(username, gbc);

        gbc.gridy = ++row;
        gbc.gridx = 0; form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; form.add(password, gbc);

        gbc.gridy = ++row;
        gbc.gridx = 0; form.add(new JLabel("Rol:"), gbc);
        gbc.gridx = 1; form.add(rol, gbc);

        gbc.gridy = ++row; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.CENTER;
        JPanel botones = new JPanel(new GridLayout(3, 2, 4, 4));
        botones.add(btnNuevo);
        botones.add(btnRegistrar);
        botones.add(btnModRol);
        botones.add(btnCambiarPass);
        botones.add(btnReactivar);
        botones.add(btnBaja);
        form.add(botones, gbc);

        return form;
    }

    public void cargarDatos() {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
        for (UsuarioDTO u : controlador.listarTodosDTO()) {
            model.addRow(new Object[]{
                    u.getIdUsuario(),
                    u.getNombre(),
                    u.getApellido(),
                    u.getUsername(),
                    u.getRol(),
                    u.isActivo() ? "Si" : "No"
            });
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        nombre.setText((String) model.getValueAt(fila, 1));
        apellido.setText((String) model.getValueAt(fila, 2));
        username.setText((String) model.getValueAt(fila, 3));
        password.setText("");
        String rolStr = (String) model.getValueAt(fila, 4);
        try { rol.setSelectedItem(RolUsuario.valueOf(rolStr)); } catch (Exception ignored) {}
        username.setEditable(false);
    }

    private void limpiarFormulario() {
        nombre.setText("");
        apellido.setText("");
        username.setText("");
        password.setText("");
        rol.setSelectedIndex(0);
        username.setEditable(true);
        tabla.clearSelection();
    }

    private void registrar() {
        String nom  = nombre.getText().trim();
        String ape  = apellido.getText().trim();
        String user = username.getText().trim();
        String pass = new String(password.getPassword()).trim();
        RolUsuario rolSel = (RolUsuario) rol.getSelectedItem();

        if (nom.isEmpty() || ape.isEmpty() || user.isEmpty() || pass.isEmpty()) {
            UiUtil.mostrarError(this, "Complete todos los campos.");
            return;
        }
        boolean existe = controlador.listarTodosDTO().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(user));
        if (existe) {
            UiUtil.mostrarError(this, "El username '" + user + "' ya esta en uso.");
            return;
        }
        try {
            int id = controlador.siguienteId();
            controlador.registrarUsuario(new Usuario(id, nom, ape, user, pass, rolSel));
            UiUtil.mostrarInfo(this, "Usuario '" + user + "' registrado correctamente.");
            limpiarFormulario();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void modificarRol() {
        String user = username.getText().trim();
        if (user.isEmpty()) { UiUtil.mostrarError(this, "Seleccione un usuario de la tabla."); return; }
        RolUsuario nuevoRol = (RolUsuario) rol.getSelectedItem();
        if (!UiUtil.confirmar(this, "Cambiar rol de '" + user + "' a " + nuevoRol + "?")) return;
        try {
            controlador.modificarRol(user, nuevoRol);
            UiUtil.mostrarInfo(this, "Rol actualizado correctamente.");
            limpiarFormulario();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void cambiarPassword() {
        String user = username.getText().trim();
        if (user.isEmpty()) { UiUtil.mostrarError(this, "Seleccione un usuario de la tabla."); return; }
        String pass = new String(password.getPassword()).trim();
        if (pass.isEmpty()) { UiUtil.mostrarError(this, "Ingrese la nueva password en el campo Password."); return; }
        if (!UiUtil.confirmar(this, "Cambiar password del usuario '" + user + "'?")) return;
        try {
            controlador.cambiarPassword(user, pass);
            UiUtil.mostrarInfo(this, "Password actualizada correctamente.");
            password.setText("");
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void reactivar() {
        String user = username.getText().trim();
        if (user.isEmpty()) { UiUtil.mostrarError(this, "Seleccione un usuario de la tabla."); return; }
        if (!UiUtil.confirmar(this, "Reactivar al usuario '" + user + "'?")) return;
        try {
            controlador.reactivarUsuario(user);
            UiUtil.mostrarInfo(this, "Usuario reactivado.");
            limpiarFormulario();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void darBaja() {
        String user = username.getText().trim();
        if (user.isEmpty()) { UiUtil.mostrarError(this, "Seleccione un usuario de la tabla."); return; }
        if (user.equals(usernameActual)) {
            UiUtil.mostrarError(this, "No puede darse de baja a si mismo.");
            return;
        }
        if (!UiUtil.confirmar(this, "Dar de baja al usuario '" + user + "'?\nNo podra iniciar sesion.")) return;
        try {
            controlador.darBajaUsuario(user);
            UiUtil.mostrarInfo(this, "Usuario dado de baja.");
            limpiarFormulario();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }
}

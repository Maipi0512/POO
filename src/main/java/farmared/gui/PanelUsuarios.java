package farmared.gui;

import farmared.enums.RolUsuario;
import farmared.modulos.m1_usuarios.Autorizacion;
import farmared.modulos.m1_usuarios.Usuario;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class PanelUsuarios extends JPanel {

    private final List<Usuario> usuarios = new ArrayList<>();
    private int contadorId = 1;

    private final JTextField tfNombre   = new JTextField(14);
    private final JTextField tfApellido = new JTextField(14);
    private final JTextField tfUsername = new JTextField(12);
    private final JTextField tfPassword = new JPasswordField(12);
    private final JComboBox<RolUsuario> cbRol = new JComboBox<>(RolUsuario.values());

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
        new String[]{"ID", "Nombre", "Apellido", "Username", "Rol", "Autorizador"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextArea taLog = new JTextArea(4, 0);

    public PanelUsuarios() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M1 — Módulo de Usuarios y Seguridad</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(), buildTablaPanel());
        split.setDividerLocation(340);
        add(split, BorderLayout.CENTER);

        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        cargarDemoData();
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Registrar Usuario"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFila(p, g, row++, "Nombre:",    tfNombre);
        addFila(p, g, row++, "Apellido:",  tfApellido);
        addFila(p, g, row++, "Username:",  tfUsername);
        addFila(p, g, row++, "Password:",  tfPassword);
        addFila(p, g, row++, "Rol:",       cbRol);

        g.gridy = row; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnAgregar = new JButton("Registrar Usuario");
        btnAgregar.addActionListener(this::registrarUsuario);
        p.add(btnAgregar, g);

        row++;
        g.gridy = row; g.gridx = 0; g.gridwidth = 2;
        p.add(new JSeparator(), g);

        row++;
        g.gridy = row;
        JTextArea info = new JTextArea(
            "Roles del sistema:\n\n" +
            "OPERADOR:\n  Opera el sistema (OC,\n  facturas, pagos)\n\n" +
            "SUPERVISOR:\n  Autoriza excepciones\n  (tope deuda, desvíos)\n\n" +
            "ADMINISTRADOR:\n  Gestiona configuración\n  del sistema"
        );
        info.setEditable(false);
        info.setBackground(p.getBackground());
        info.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        p.add(info, g);

        return p;
    }

    private JPanel buildTablaPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Usuarios del Sistema"));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        JButton btnTest = new JButton("Probar autorización con usuario seleccionado");
        btnTest.addActionListener(this::probarAutorizacion);
        botones.add(btnTest);
        p.add(botones, BorderLayout.SOUTH);

        return p;
    }

    private void addFila(JPanel p, GridBagConstraints g, int row, String label, JComponent campo) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(campo, g);
    }

    private void registrarUsuario(ActionEvent e) {
        String nombre   = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String username = tfUsername.getText().trim();
        String password = ((JPasswordField) tfPassword).getText();
        RolUsuario rol  = (RolUsuario) cbRol.getSelectedItem();

        if (nombre.isEmpty() || apellido.isEmpty() || username.isEmpty()) {
            log("✗ Nombre, Apellido y Username son obligatorios.");
            return;
        }
        if (usuarios.stream().anyMatch(u -> u.getUsername().equals(username))) {
            log("✗ Ya existe un usuario con el username '" + username + "'.");
            return;
        }

        Usuario nuevo = new Usuario(contadorId++, nombre, apellido, username, password, rol);
        usuarios.add(nuevo);
        agregarFilaTabla(nuevo);
        log("✓ Usuario registrado: " + nuevo);
        limpiarForm();
    }

    private void probarAutorizacion(ActionEvent e) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { log("→ Seleccioná un usuario en la tabla primero."); return; }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Usuario u = usuarios.stream().filter(x -> x.getIdUsuario() == id).findFirst().orElse(null);
        if (u == null) return;
        if (!u.esAutorizador()) {
            log("✗ " + u.getNombre() + " [" + u.getRol() + "] no puede autorizar (requiere SUPERVISOR).");
            return;
        }
        try {
            Autorizacion auth = new Autorizacion(1, u, "Test de autorización desde M1");
            log("✓ Autorización generada por " + u.getNombre() + ": " + auth);
        } catch (Exception ex) {
            log("✗ " + ex.getMessage());
        }
    }

    private void agregarFilaTabla(Usuario u) {
        modeloTabla.addRow(new Object[]{
            u.getIdUsuario(), u.getNombre(), u.getApellido(),
            u.getUsername(), u.getRol(), u.esAutorizador() ? "Sí" : "No"
        });
    }

    private void cargarDemoData() {
        agregarUsuarioDemo("Ana",    "García",   "agarcia",  RolUsuario.OPERADOR);
        agregarUsuarioDemo("Carlos", "Ríos",     "crios",    RolUsuario.SUPERVISOR);
        agregarUsuarioDemo("Laura",  "Martínez", "lmartinez",RolUsuario.ADMINISTRADOR);
    }

    private void agregarUsuarioDemo(String n, String a, String u, RolUsuario r) {
        Usuario usr = new Usuario(contadorId++, n, a, u, "demo", r);
        usuarios.add(usr);
        agregarFilaTabla(usr);
    }

    private void limpiarForm() {
        tfNombre.setText(""); tfApellido.setText(""); tfUsername.setText("");
        ((JPasswordField) tfPassword).setText(""); cbRol.setSelectedIndex(0);
    }

    public List<Usuario> getUsuarios() { return usuarios; }

    public Usuario getSupervisor() {
        return usuarios.stream().filter(Usuario::esAutorizador).findFirst().orElse(null);
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

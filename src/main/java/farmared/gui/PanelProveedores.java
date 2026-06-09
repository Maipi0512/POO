package farmared.gui;

import farmared.controllers.*;
import farmared.enums.CondicionIVA;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PanelProveedores extends JPanel {

    private static int contadorId = 1;

    private final JTextField tfCuit      = new JTextField(16);
    private final JTextField tfRazon     = new JTextField(20);
    private final JTextField tfFantasia  = new JTextField(18);
    private final JTextField tfDomicilio = new JTextField(24);
    private final JTextField tfTelefono  = new JTextField(14);
    private final JTextField tfEmail     = new JTextField(20);
    private final JComboBox<CondicionIVA> cbIVA = new JComboBox<>(CondicionIVA.values());
    private final JTextField tfIIBB      = new JTextField(14);
    private final JTextField tfTope      = new JTextField("0.0", 10);
    private final JList<String> listRubros;
    private final DefaultListModel<String> modeloRubros = new DefaultListModel<>();

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
        new String[]{"CUIT", "Razón Social", "Cond. IVA", "Tope Deuda", "Rubros"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextArea taLog = new JTextArea(3, 0);

    public PanelProveedores() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M2 — Módulo de Proveedores</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        inicializarRubros();
        listRubros = new JList<>(modeloRubros);
        listRubros.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listRubros.setVisibleRowCount(5);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(), buildTablaPanel());
        split.setDividerLocation(380);
        add(split, BorderLayout.CENTER);

        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        cargarProveedoresDemoEnTabla();
    }

    private void inicializarRubros() {
        for (Rubro r : MainFrame.rubros) {
            modeloRubros.addElement(r.getNombre());
        }
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Registrar Proveedor"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 6, 3, 6);
        g.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFila(p, g, row++, "CUIT (*):         ", tfCuit);
        addFila(p, g, row++, "Razón Social (*): ", tfRazon);
        addFila(p, g, row++, "Nombre Fantasía:  ", tfFantasia);
        addFila(p, g, row++, "Domicilio:        ", tfDomicilio);
        addFila(p, g, row++, "Teléfono:         ", tfTelefono);
        addFila(p, g, row++, "Email:            ", tfEmail);
        addFila(p, g, row++, "Condición IVA (*):  ", cbIVA);
        addFila(p, g, row++, "Nro. IIBB:        ", tfIIBB);
        addFila(p, g, row++, "Tope Máx. Deuda:  ", tfTope);

        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.anchor = GridBagConstraints.NORTHWEST;
        p.add(new JLabel("Rubros:           "), g);
        g.gridx = 1; g.fill = GridBagConstraints.BOTH; g.weighty = 1.0;
        JScrollPane scrollRubros = new JScrollPane(listRubros);
        scrollRubros.setPreferredSize(new Dimension(160, 80));
        p.add(scrollRubros, g);
        g.weighty = 0; g.fill = GridBagConstraints.NONE;
        row++;

        g.gridy = row; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnReg = new JButton("Registrar Proveedor");
        btnReg.addActionListener(this::registrarProveedor);
        p.add(btnReg, g);

        return p;
    }

    private JPanel buildTablaPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Proveedores Registrados"));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(130);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(160);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDetalle = new JButton("Ver detalle completo");
        btnDetalle.addActionListener(this::verDetalle);
        botones.add(btnDetalle);
        JButton btnRefresh = new JButton("Refrescar tabla");
        btnRefresh.addActionListener(e -> refrescarTabla());
        botones.add(btnRefresh);
        p.add(botones, BorderLayout.SOUTH);

        return p;
    }

    private void addFila(JPanel p, GridBagConstraints g, int row, String label, JComponent campo) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(campo, g);
    }

    private void registrarProveedor(ActionEvent e) {
        String cuit   = tfCuit.getText().trim();
        String razon  = tfRazon.getText().trim();

        if (cuit.isEmpty() || razon.isEmpty()) {
            log("✗ CUIT y Razón Social son obligatorios.");
            return;
        }
        if (OrdenCompraController.getInstance().buscarProveedorPorId(cuit) != null) {
            log("✗ Ya existe un proveedor con CUIT '" + cuit + "'.");
            return;
        }

        double tope = 0.0;
        try { tope = Double.parseDouble(tfTope.getText().trim()); }
        catch (NumberFormatException ex) { log("⚠ Tope inválido, se usará 0."); }

        Proveedor prov = new Proveedor(
            cuit, razon,
            tfFantasia.getText().trim().isEmpty()  ? razon : tfFantasia.getText().trim(),
            tfDomicilio.getText().trim(),
            tfTelefono.getText().trim(),
            tfEmail.getText().trim(),
            (CondicionIVA) cbIVA.getSelectedItem(),
            tfIIBB.getText().trim(),
            new Date()
        );
        prov.setTopeMaximoDeuda(tope);

        for (String nombre : listRubros.getSelectedValuesList()) {
            Rubro r = buscarRubroPorNombre(nombre);
            if (r != null) prov.agregarRubro(r);
        }

        OrdenCompraController.getInstance().agregarProveedor(prov);
        FacturaController.getInstance().agregarProveedor(prov);
        OrdenPagoController.getInstance().agregarProveedor(prov);
        CuentaCorrienteController.getInstance().agregarProveedor(prov);

        agregarFilaTabla(prov);
        log("✓ Proveedor registrado: " + prov + " | Registrado en los 4 controladores.");
        limpiarForm();
    }

    private Rubro buscarRubroPorNombre(String nombre) {
        return MainFrame.rubros.stream()
            .filter(r -> r.getNombre().equals(nombre))
            .findFirst().orElse(null);
    }

    private void verDetalle(ActionEvent e) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { log("→ Seleccioná un proveedor en la tabla primero."); return; }
        String cuit = (String) modeloTabla.getValueAt(fila, 0);
        Proveedor p = OrdenCompraController.getInstance().buscarProveedorPorId(cuit);
        if (p == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("═══ DETALLE PROVEEDOR ═══\n");
        sb.append("CUIT:         ").append(p.getCuit()).append("\n");
        sb.append("Razón Social: ").append(p.getRazonSocial()).append("\n");
        sb.append("Nombre Fant.: ").append(p.getNombreFantasia()).append("\n");
        sb.append("Domicilio:    ").append(p.getDomicilioComercial()).append("\n");
        sb.append("Teléfono:     ").append(p.getTelefono()).append("\n");
        sb.append("Email:        ").append(p.getEmail()).append("\n");
        sb.append("Condición IVA:").append(p.getCondicionIVA()).append("\n");
        sb.append("IIBB:         ").append(p.getNumeroIngresosBrutos()).append("\n");
        sb.append("Tope deuda:   $").append(p.getTopeMaximoDeuda()).append("\n");
        sb.append("Rubros:       ");
        p.getRubros().forEach(r -> sb.append(r.getNombre()).append(" "));
        sb.append("\n");
        sb.append("Deuda vigente:$").append(p.obtenerCuentaCorriente()).append("\n");
        sb.append("Comprobantes: ").append(p.getComprobantes().size()).append("\n");
        sb.append("Certificados: ").append(p.getCertificados().size()).append("\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Detalle Proveedor", JOptionPane.INFORMATION_MESSAGE);
    }

    private void agregarFilaTabla(Proveedor p) {
        String rubros = p.getRubros().stream()
            .map(Rubro::getNombre)
            .reduce("", (a, b) -> a.isEmpty() ? b : a + ", " + b);
        modeloTabla.addRow(new Object[]{
            p.getCuit(), p.getRazonSocial(), p.getCondicionIVA(),
            "$" + p.getTopeMaximoDeuda(), rubros
        });
    }

    private void refrescarTabla() {
        modeloTabla.setRowCount(0);
        OrdenCompraController.getInstance().getProveedores().forEach(this::agregarFilaTabla);
        log("↺ Tabla actualizada (" + OrdenCompraController.getInstance().getProveedores().size() + " proveedores)");
    }

    private void cargarProveedoresDemoEnTabla() {
        OrdenCompraController.getInstance().getProveedores().forEach(this::agregarFilaTabla);
    }

    private void limpiarForm() {
        tfCuit.setText(""); tfRazon.setText(""); tfFantasia.setText("");
        tfDomicilio.setText(""); tfTelefono.setText(""); tfEmail.setText("");
        tfIIBB.setText(""); tfTope.setText("0.0");
        cbIVA.setSelectedIndex(0); listRubros.clearSelection();
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

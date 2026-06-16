package farmared.vistas.paneles;

import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.TipoImpuesto;
import farmared.modelo.modulos.m1_proveedores.CertificadoNoRetencion;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.vistas.observador.NotificadorSistema;
import farmared.vistas.observador.ObservadorSistema;
import farmared.controladores.AppContext;
import farmared.controladores.ProveedorController;
import farmared.vistas.util.UiUtil;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PanelProveedores extends JPanel {

    private final ProveedorController controlador =
            AppContext.getInstancia().getProveedorCtrl();

    private final JTable tabla = new JTable();
    private final JTextField cuit = new JTextField(14);
    private final JTextField razonSocial = new JTextField(20);
    private final JTextField nombreFantasia = new JTextField(20);
    private final JTextField domicilio = new JTextField(20);
    private final JTextField telefono = new JTextField(12);
    private final JTextField email = new JTextField(20);
    private final JComboBox<CondicionIVA> condicionIVA = new JComboBox<>(CondicionIVA.values());
    private final JTextField ingresosBrutos = new JTextField(12);
    private final JTextField topeDeuda = new JTextField(10);

    private final JPanel rubrosPanel = new JPanel();
    private final List<JCheckBox> checkboxRubros = new ArrayList<>();

    public PanelProveedores() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(
                new String[]{"CUIT", "Razon Social", "Fantasia", "Cond. IVA", "Activo", "Tope", "Deuda"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        agregarCampo(form, gbc, y++, "CUIT:", cuit);
        agregarCampo(form, gbc, y++, "Razon Social:", razonSocial);
        agregarCampo(form, gbc, y++, "Nombre Fantasia:", nombreFantasia);
        agregarCampo(form, gbc, y++, "Domicilio:", domicilio);
        agregarCampo(form, gbc, y++, "Telefono:", telefono);
        agregarCampo(form, gbc, y++, "Email:", email);
        agregarCampo(form, gbc, y++, "Condicion IVA:", condicionIVA);
        agregarCampo(form, gbc, y++, "Nro. Ing. Brutos:", ingresosBrutos);
        agregarCampo(form, gbc, y++, "Tope max. deuda:", topeDeuda);

        rubrosPanel.setLayout(new BoxLayout(rubrosPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollRubros = new JScrollPane(rubrosPanel);
        scrollRubros.setPreferredSize(new Dimension(200, 90));
        scrollRubros.setBorder(BorderFactory.createTitledBorder("Rubros"));

        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.BOTH;
        form.add(scrollRubros, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton registrar = new JButton("Registrar");
        JButton modificar = new JButton("Modificar");
        JButton baja = new JButton("Dar de baja");
        JButton certificado = new JButton("Cert. no retencion");
        JButton refrescar = new JButton("Refrescar");
        acciones.add(registrar);
        acciones.add(modificar);
        acciones.add(baja);
        acciones.add(certificado);
        acciones.add(refrescar);

        registrar.addActionListener(e -> registrarProveedor());
        modificar.addActionListener(e -> modificarProveedor());
        baja.addActionListener(e -> darBaja());
        certificado.addActionListener(e -> agregarCertificado());
        refrescar.addActionListener(e -> cargarDatos());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(form, BorderLayout.EAST);
        add(acciones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent campo) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    public void cargarDatos() {
        // Reconstruir checkboxes con los rubros actuales
        rubrosPanel.removeAll();
        checkboxRubros.clear();
        for (Rubro r : controlador.listarRubros()) {
            JCheckBox cb = new JCheckBox(r.getNombre());
            cb.putClientProperty("rubro", r);
            checkboxRubros.add(cb);
            rubrosPanel.add(cb);
        }
        rubrosPanel.revalidate();
        rubrosPanel.repaint();

        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
        for (Proveedor p : controlador.listarProveedores()) {
            model.addRow(new Object[]{
                    p.getCuit(),
                    p.getRazonSocial(),
                    p.getNombreFantasia(),
                    p.getCondicionIVA(),
                    p.isActivo() ? "Si" : "No",
                    UiUtil.formatearMoneda(p.getTopeMaximoDeuda()),
                    UiUtil.formatearMoneda(p.obtenerCuentaCorriente())
            });
        }
    }

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        String cuitSeleccionado = (String) tabla.getValueAt(fila, 0);
        cuit.setText(cuitSeleccionado);
        razonSocial.setText((String) tabla.getValueAt(fila, 1));
        nombreFantasia.setText((String) tabla.getValueAt(fila, 2));
        cuit.setEditable(false);

        // Marcar los checkboxes segun los rubros del proveedor
        Proveedor prov = controlador.buscarProveedorPorId(cuitSeleccionado);
        if (prov != null) {
            List<Rubro> rubrosDelProv = prov.getRubros();
            for (JCheckBox cb : checkboxRubros) {
                Rubro r = (Rubro) cb.getClientProperty("rubro");
                cb.setSelected(rubrosDelProv.contains(r));
            }
        }
    }

    private List<Rubro> getRubrosSeleccionados() {
        List<Rubro> seleccionados = new ArrayList<>();
        for (JCheckBox cb : checkboxRubros) {
            if (cb.isSelected())
                seleccionados.add((Rubro) cb.getClientProperty("rubro"));
        }
        return seleccionados;
    }

    private void registrarProveedor() {
        try {
            cuit.setEditable(true);
            if (cuit.getText().trim().isEmpty() || razonSocial.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("CUIT y Razon Social son obligatorios.");
            }
            double tope = topeDeuda.getText().trim().isEmpty() ? 0
                    : UiUtil.parsearDouble(topeDeuda.getText(), "Tope deuda");

            controlador.registrarProveedor(
                    cuit.getText().trim(), razonSocial.getText().trim(), nombreFantasia.getText().trim(),
                    domicilio.getText().trim(), telefono.getText().trim(), email.getText().trim(),
                    (CondicionIVA) condicionIVA.getSelectedItem(), ingresosBrutos.getText().trim(),
                    tope, getRubrosSeleccionados()
            );
            UiUtil.mostrarInfo(this, "Proveedor registrado.");
            limpiarFormulario();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void modificarProveedor() {
        try {
            if (cuit.getText().trim().isEmpty()) throw new IllegalArgumentException("Seleccione un proveedor.");
            double tope = topeDeuda.getText().trim().isEmpty() ? 0
                    : UiUtil.parsearDouble(topeDeuda.getText(), "Tope deuda");

            controlador.modificarProveedor(
                    cuit.getText().trim(), razonSocial.getText().trim(), nombreFantasia.getText().trim(),
                    domicilio.getText().trim(), telefono.getText().trim(), email.getText().trim(),
                    (CondicionIVA) condicionIVA.getSelectedItem(), tope,
                    getRubrosSeleccionados()
            );
            UiUtil.mostrarInfo(this, "Proveedor actualizado.");
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void darBaja() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UiUtil.mostrarError(this, "Seleccione un proveedor.");
            return;
        }
        String cuitProv = (String) tabla.getValueAt(fila, 0);
        if (UiUtil.confirmar(this, "Dar de baja al proveedor " + cuitProv + "?")) {
            try {
                controlador.darBajaProveedor(cuitProv);
                UiUtil.mostrarInfo(this, "Proveedor dado de baja.");
                cargarDatos();
            } catch (Exception ex) {
                UiUtil.mostrarError(this, ex.getMessage());
            }
        }
    }

    private void limpiarFormulario() {
        cuit.setText("");
        cuit.setEditable(true);
        razonSocial.setText("");
        nombreFantasia.setText("");
        domicilio.setText("");
        telefono.setText("");
        email.setText("");
        topeDeuda.setText("");
        for (JCheckBox cb : checkboxRubros) cb.setSelected(false);
    }

    private void agregarCertificado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UiUtil.mostrarError(this, "Seleccione un proveedor.");
            return;
        }
        String cuitProv = (String) tabla.getValueAt(fila, 0);
        TipoImpuesto tipo = (TipoImpuesto) JOptionPane.showInputDialog(
                this, "Tipo de impuesto:", "Certificado de no retencion",
                JOptionPane.QUESTION_MESSAGE, null, TipoImpuesto.values(), TipoImpuesto.IVA
        );
        if (tipo == null) return;

        String vigenciaTxt = JOptionPane.showInputDialog(this, "Dias de vigencia desde hoy:", "90");
        if (vigenciaTxt == null) return;

        try {
            int dias = Integer.parseInt(vigenciaTxt.trim());
            Date hoy = new Date();
            Date vencimiento = new Date(hoy.getTime() + (long) dias * 24 * 60 * 60 * 1000);
            controlador.registrarCertificadoNoRetencion(cuitProv,
                    new CertificadoNoRetencion("CERT-" + System.currentTimeMillis(), tipo, hoy, vencimiento, 0.0));
            UiUtil.mostrarInfo(this, "Certificado registrado.");
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }
}

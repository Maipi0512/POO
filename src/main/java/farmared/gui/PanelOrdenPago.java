package farmared.gui;

import farmared.controllers.*;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.*;
import farmared.modulos.m6_ordenes_pago.ChequePropio;
import farmared.modulos.m6_ordenes_pago.TransferenciaBancaria;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PanelOrdenPago extends JPanel {

    private final JComboBox<String> cbProveedor  = new JComboBox<>();
    private final JComboBox<String> cbMedioPago  = new JComboBox<>(
        new String[]{"Efectivo", "Cheque", "Transferencia bancaria"});
    private final JTextField tfDetalleMedio = new JTextField("(opcional: banco / nro. cheque)", 24);

    private final DefaultTableModel modeloImpagos = new DefaultTableModel(
        new String[]{"Comprobante", "Tipo", "Total", "Saldo pendiente"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaImpagos = new JTable(modeloImpagos);

    private final DefaultTableModel modeloOPs = new DefaultTableModel(
        new String[]{"Nro. OP", "Proveedor", "Bruto", "Retenciones", "Neto", "Estado"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaOPs = new JTable(modeloOPs);

    private final JTextArea taLog = new JTextArea(4, 0);

    private OrdenPago opActual;

    public PanelOrdenPago() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M6 — DS2 · Emitir Orden de Pago</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        JSplitPane splitV = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildFormPanel(), buildTablasPanel());
        splitV.setDividerLocation(280);
        add(splitV, BorderLayout.CENTER);

        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        cargarProveedores();
    }

    private void cargarProveedores() {
        cbProveedor.removeAllItems();
        for (Proveedor p : OrdenPagoController.getInstance().getProveedores()) {
            cbProveedor.addItem(p.getCuit() + " · " + p.getRazonSocial());
        }
    }

    private String getCuit() {
        String item = (String) cbProveedor.getSelectedItem();
        return (item == null) ? null : item.split(" · ")[0];
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Generar Orden de Pago (DS2)"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFila(p, g, row++, "Proveedor:", cbProveedor);

        g.gridy = row - 1; g.gridx = 2;
        JButton btnRefresh = new JButton("↺ Actualizar");
        btnRefresh.addActionListener(ev -> { cargarProveedores(); log("↺ Proveedores actualizados."); });
        p.add(btnRefresh, g);

        g.gridy = row; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(new JSeparator(), g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        row++;

        g.gridy = row; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnIniciar = new JButton("1. Ver comprobantes impagos del proveedor");
        btnIniciar.addActionListener(this::iniciarOP);
        p.add(btnIniciar, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        row++;

        g.gridy = row; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnGenerar = new JButton("2. Generar OP (calcular retenciones sobre todos los impagos)");
        btnGenerar.addActionListener(this::generarOP);
        p.add(btnGenerar, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        row++;

        g.gridy = row; g.gridx = 0; g.gridwidth = 3;
        p.add(new JSeparator(), g);
        g.gridwidth = 1;
        row++;

        addFila(p, g, row++, "Medio de pago:", cbMedioPago);
        addFila(p, g, row++, "Detalle:", tfDetalleMedio);

        g.gridy = row; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnConfirmar = new JButton("3. Confirmar Pago");
        btnConfirmar.setFont(btnConfirmar.getFont().deriveFont(Font.BOLD));
        btnConfirmar.addActionListener(this::confirmarPago);
        p.add(btnConfirmar, g);

        return p;
    }

    private JPanel buildTablasPanel() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));

        JPanel pImpagos = new JPanel(new BorderLayout(4, 4));
        pImpagos.setBorder(BorderFactory.createTitledBorder("Comprobantes impagos"));
        tablaImpagos.setRowHeight(20);
        tablaImpagos.getTableHeader().setReorderingAllowed(false);
        pImpagos.add(new JScrollPane(tablaImpagos), BorderLayout.CENTER);

        JPanel pOPs = new JPanel(new BorderLayout(4, 4));
        pOPs.setBorder(BorderFactory.createTitledBorder("Órdenes de Pago emitidas"));
        tablaOPs.setRowHeight(20);
        tablaOPs.getTableHeader().setReorderingAllowed(false);
        pOPs.add(new JScrollPane(tablaOPs), BorderLayout.CENTER);

        p.add(pImpagos);
        p.add(pOPs);
        return p;
    }

    private void addFila(JPanel p, GridBagConstraints g, int row, String label, JComponent campo) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(campo, g);
    }

    private void iniciarOP(ActionEvent ev) {
        String cuit = getCuit();
        if (cuit == null) { log("✗ Seleccioná un proveedor."); return; }
        try {
            List<Comprobante> impagos = OrdenPagoController.getInstance().iniciarOrdenPago(cuit);
            modeloImpagos.setRowCount(0);
            if (impagos.isEmpty()) {
                log("→ Sin comprobantes impagos para " + cuit + ". Registrá una factura en M5 primero.");
                return;
            }
            for (Comprobante c : impagos) {
                modeloImpagos.addRow(new Object[]{
                    c.getNumero(), c.getTipo(),
                    String.format("$%.2f", c.getImporteTotal()),
                    String.format("$%.2f", c.getSaldoPendiente())
                });
            }
            log("✓ " + impagos.size() + " comprobante(s) impago(s) para " + cuit);
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void generarOP(ActionEvent ev) {
        String cuit = getCuit();
        if (cuit == null) { log("✗ Seleccioná un proveedor."); return; }
        try {
            List<Comprobante> impagos = OrdenPagoController.getInstance().iniciarOrdenPago(cuit);
            if (impagos.isEmpty()) {
                log("→ Sin comprobantes impagos. Registrá una factura en M5 primero."); return;
            }
            Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
            for (Comprobante c : impagos) seleccion.put(c, c.getSaldoPendiente());
            opActual = OrdenPagoController.getInstance()
                           .seleccionarComprobantes(cuit, seleccion, new Date());
            log("✓ OP generada: " + opActual.getNumero());
            log("  Bruto:             $" + String.format("%.2f", opActual.getImporteBruto()));
            log("  Total retenciones: $" + String.format("%.2f", opActual.getTotalRetenciones()));
            log("  Neto a pagar:      $" + String.format("%.2f", opActual.getImporteNeto()));
            if (!opActual.getRetenciones().isEmpty())
                log("  Retenciones: " + opActual.getRetenciones());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void confirmarPago(ActionEvent ev) {
        if (opActual == null) { log("→ Primero generá la OP (paso 2)."); return; }
        String cuit = getCuit();
        try {
            List<MedioPago> medios = new ArrayList<>();
            String sel = (String) cbMedioPago.getSelectedItem();
            double neto = opActual.getImporteNeto();
            if ("Efectivo".equals(sel)) {
                medios.add(new Efectivo(1, neto, new Date()));
            } else if ("Cheque".equals(sel)) {
                Date venc = new Date(System.currentTimeMillis() + 2592000000L);
                medios.add(new ChequePropio(1, neto, new Date(),
                    "CHQ-001", new Date(), venc, "Firmante", "Banco", "001-001"));
            } else {
                medios.add(new TransferenciaBancaria(1, neto, new Date(),
                    "CBU-ORIGEN", "CBU-DESTINO", "TRF-001"));
            }
            OrdenPago confirmada = OrdenPagoController.getInstance().confirmarPago(opActual, medios);
            CuentaCorrienteController.getInstance().agregarOrdenPago(confirmada);

            modeloOPs.addRow(new Object[]{
                confirmada.getNumero(),
                confirmada.getProveedor().getRazonSocial(),
                String.format("$%.2f", confirmada.getImporteBruto()),
                String.format("$%.2f", confirmada.getTotalRetenciones()),
                String.format("$%.2f", confirmada.getImporteNeto()),
                confirmada.getEstado()
            });
            modeloImpagos.setRowCount(0);

            Proveedor prov = OrdenCompraController.getInstance().buscarProveedorPorId(cuit);
            double deudaPost = (prov != null) ? prov.obtenerCuentaCorriente() : 0.0;
            log("✓ Pago confirmado: " + confirmada.getNumero()
                + " | Medio: " + sel + " | Neto: $" + String.format("%.2f", neto));
            log("  Deuda post-pago: $" + String.format("%.2f", deudaPost));
            log("→ Podés ver el resumen completo en M7 · Consultas.");
            opActual = null;
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

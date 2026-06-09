package farmared.gui;

import farmared.controllers.*;
import farmared.enums.RolUsuario;
import farmared.enums.TipoComprobante;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PanelComprobantes extends JPanel {

    private final JComboBox<String> cbOC        = new JComboBox<>();
    private final JComboBox<TipoComprobante> cbTipo =
        new JComboBox<>(new TipoComprobante[]{ TipoComprobante.FACTURA_A,
                                               TipoComprobante.FACTURA_B,
                                               TipoComprobante.FACTURA_C });
    private final JTextField tfNumero    = new JTextField("(auto)", 12);
    private final JTextField tfFechaEmi  = new JTextField(java.time.LocalDate.now().toString(), 12);
    private final JTextField tfFechaVenc = new JTextField(java.time.LocalDate.now().plusDays(30).toString(), 12);
    private final JTextField tfMotivo    = new JTextField("Desvío autorizado", 18);

    private final JTextField tfMontoNC = new JTextField("0.0", 8);
    private final JTextField tfMontoND = new JTextField("0.0", 8);

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
        new String[]{"Nro.", "Tipo", "Proveedor", "Fecha Emisión", "Total", "Estado", "Saldo"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextArea taLog = new JTextArea(4, 0);

    public PanelComprobantes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M5 — DS3 · Registrar Comprobantes Recibidos</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(), buildTablaPanel());
        split.setDividerLocation(420);
        add(split, BorderLayout.CENTER);

        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        cargarOCs();
        cargarComprobantesEnTabla();
    }

    private void cargarOCs() {
        cbOC.removeAllItems();
        for (OrdenCompra oc : OrdenCompraController.getInstance().getOrdenes()) {
            cbOC.addItem(oc.getNumero() + " · " + oc.getProveedor().getRazonSocial()
                         + " · $" + String.format("%.2f", oc.calcularTotal()));
        }
        if (cbOC.getItemCount() == 0) cbOC.addItem("(sin OC emitidas — ir a M4 primero)");
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout(4, 4));

        // Sección Factura
        JPanel pFact = new JPanel(new GridBagLayout());
        pFact.setBorder(BorderFactory.createTitledBorder("Registrar Factura (vinculada a OC)"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 5, 3, 5);
        g.anchor = GridBagConstraints.WEST;

        int row = 0;
        addFila(pFact, g, row++, "OC de referencia:", cbOC);

        g.gridy = row; g.gridx = 2; g.gridwidth = 1;
        JButton btnRefOC = new JButton("↺");
        btnRefOC.setToolTipText("Actualizar lista de OC");
        btnRefOC.addActionListener(ev -> { cargarOCs(); log("↺ OC actualizadas."); });
        pFact.add(btnRefOC, g);
        row++;

        addFila(pFact, g, row++, "Tipo:", cbTipo);
        addFila(pFact, g, row++, "Número:", tfNumero);
        addFila(pFact, g, row++, "Fecha emisión:", tfFechaEmi);
        addFila(pFact, g, row++, "Fecha vencimiento:", tfFechaVenc);
        addFila(pFact, g, row++, "Motivo (si hay desvío):", tfMotivo);

        g.gridy = row; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnFact = new JButton("Registrar Factura");
        btnFact.setFont(btnFact.getFont().deriveFont(Font.BOLD));
        btnFact.addActionListener(this::registrarFactura);
        pFact.add(btnFact, g);

        // Sección NC / ND
        JPanel pNota = new JPanel(new GridBagLayout());
        pNota.setBorder(BorderFactory.createTitledBorder("Nota de Crédito / Débito (última OC)"));
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        row = 0;
        addFila(pNota, g, row++, "Monto NC ($):", tfMontoNC);
        g.gridy = row; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnNC = new JButton("Registrar Nota de Crédito");
        btnNC.addActionListener(this::registrarNC);
        pNota.add(btnNC, g);
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        row++;

        addFila(pNota, g, row++, "Monto ND ($):", tfMontoND);
        g.gridy = row; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnND = new JButton("Registrar Nota de Débito");
        btnND.addActionListener(this::registrarND);
        pNota.add(btnND, g);

        outer.add(pFact, BorderLayout.CENTER);
        outer.add(pNota, BorderLayout.SOUTH);
        return outer;
    }

    private JPanel buildTablaPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Comprobantes Registrados"));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("↺ Refrescar");
        btnRefresh.addActionListener(ev -> { cargarComprobantesEnTabla(); log("↺ Tabla actualizada."); });
        JPanel bots = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bots.add(btnRefresh);
        p.add(bots, BorderLayout.SOUTH);
        return p;
    }

    private void addFila(JPanel p, GridBagConstraints g, int row, String label, JComponent campo) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(campo, g);
    }

    private OrdenCompra getOCSeleccionada() {
        String item = (String) cbOC.getSelectedItem();
        if (item == null || item.startsWith("(sin")) return null;
        String nroOC = item.split(" · ")[0];
        return OrdenCompraController.getInstance().getOrdenes().stream()
            .filter(oc -> oc.getNumero().equals(nroOC)).findFirst().orElse(null);
    }

    private void registrarFactura(ActionEvent ev) {
        OrdenCompra oc = getOCSeleccionada();
        if (oc == null) { log("→ Emití una OC en M4 primero y seleccionala."); return; }
        if (oc.getDetalles().isEmpty()) { log("✗ La OC seleccionada no tiene ítems."); return; }
        try {
            List<DetalleComprobante> dets = new ArrayList<>();
            for (DetalleOC d : oc.getDetalles()) {
                dets.add(new DetalleComprobante(dets.size() + 1,
                    d.getProducto(), d.getCantidad(), d.getPrecioUnitario(), 21.0));
            }
            String nro = tfNumero.getText().trim().equals("(auto)") ? null : tfNumero.getText().trim();
            TipoComprobante tipo = (TipoComprobante) cbTipo.getSelectedItem();
            Usuario sup = new Usuario(99, "Supervisor", "Sistema", "supervisor", "pass", RolUsuario.SUPERVISOR);
            String motivo = tfMotivo.getText().trim();
            Factura f = FacturaController.getInstance().registrarFactura(
                nro, tipo, new Date(), new Date(), dets,
                oc.getProveedor().getCuit(),
                Collections.singletonList(oc.getNumero()),
                sup, motivo);
            agregarFilaTabla(f);
            log("✓ Factura registrada: " + f.getNumero()
                + " | Total: $" + String.format("%.2f", f.getImporteTotal())
                + " | Deuda: $" + oc.getProveedor().obtenerCuentaCorriente());
            log("→ Ahora podés ir a M6 para generar la Orden de Pago.");
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void registrarNC(ActionEvent ev) {
        OrdenCompra oc = getOCSeleccionada();
        if (oc == null) { log("→ Seleccioná una OC primero."); return; }
        if (oc.getDetalles().isEmpty()) { log("✗ La OC seleccionada no tiene ítems."); return; }
        double monto;
        try { monto = Double.parseDouble(tfMontoNC.getText().trim()); }
        catch (NumberFormatException e) { log("✗ Monto NC inválido."); return; }
        if (monto <= 0) { log("✗ El monto debe ser mayor a 0."); return; }
        try {
            DetalleOC d = oc.getDetalles().get(0);
            double precio = monto;
            List<DetalleComprobante> dets = Collections.singletonList(
                new DetalleComprobante(1, d.getProducto(), 1, precio, 21.0));
            NotaCredito nc = FacturaController.getInstance().registrarNotaCredito(
                null, new Date(), new Date(), dets, oc.getProveedor().getCuit());
            agregarFilaTabla(nc);
            log("✓ Nota de Crédito: " + nc.getNumero()
                + " | $" + String.format("%.2f", nc.getImporteTotal())
                + " | Deuda: $" + oc.getProveedor().obtenerCuentaCorriente());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void registrarND(ActionEvent ev) {
        OrdenCompra oc = getOCSeleccionada();
        if (oc == null) { log("→ Seleccioná una OC primero."); return; }
        if (oc.getDetalles().isEmpty()) { log("✗ La OC seleccionada no tiene ítems."); return; }
        double monto;
        try { monto = Double.parseDouble(tfMontoND.getText().trim()); }
        catch (NumberFormatException e) { log("✗ Monto ND inválido."); return; }
        if (monto <= 0) { log("✗ El monto debe ser mayor a 0."); return; }
        try {
            DetalleOC d = oc.getDetalles().get(0);
            List<DetalleComprobante> dets = Collections.singletonList(
                new DetalleComprobante(1, d.getProducto(), 1, monto, 21.0));
            NotaDebito nd = FacturaController.getInstance().registrarNotaDebito(
                null, new Date(), new Date(), dets, oc.getProveedor().getCuit());
            agregarFilaTabla(nd);
            log("✓ Nota de Débito: " + nd.getNumero()
                + " | $" + String.format("%.2f", nd.getImporteTotal())
                + " | Deuda: $" + oc.getProveedor().obtenerCuentaCorriente());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void agregarFilaTabla(Comprobante c) {
        modeloTabla.addRow(new Object[]{
            c.getNumero(), c.getTipo(), c.getProveedor().getRazonSocial(),
            c.getFechaEmision(), String.format("$%.2f", c.getImporteTotal()),
            c.getEstado(), String.format("$%.2f", c.getSaldoPendiente())
        });
    }

    private void cargarComprobantesEnTabla() {
        modeloTabla.setRowCount(0);
        FacturaController.getInstance().getComprobantes().forEach(this::agregarFilaTabla);
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

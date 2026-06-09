package farmared.gui;

import farmared.controllers.*;
import farmared.enums.RolUsuario;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PanelOrdenCompra extends JPanel {

    private static class ItemPendiente {
        final String codigoProducto;
        final double cantidad;
        ItemPendiente(String cod, double cant) { codigoProducto = cod; cantidad = cant; }
    }

    private final JComboBox<String> cbProveedor = new JComboBox<>();
    private final JComboBox<String> cbProducto  = new JComboBox<>();
    private final JLabel            lbPrecio    = new JLabel("Precio: —");
    private final JTextField        tfCantidad  = new JTextField("1", 8);
    private final JTextField        tfMotivo    = new JTextField("Autorizado por dirección", 24);
    private final JLabel            lbTotal     = new JLabel("Total estimado: $0.00");

    private final DefaultTableModel modeloItems = new DefaultTableModel(
        new String[]{"Producto", "Cantidad", "Precio unit.", "Subtotal"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tablaItems = new JTable(modeloItems);
    private final JTextArea taLog = new JTextArea(4, 0);

    private final List<ItemPendiente> itemsPendientes = new ArrayList<>();

    public PanelOrdenCompra() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M4 — DS1 · Generar Orden de Compra</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildFormPanel(), buildTablaPanel());
        split.setDividerLocation(220);
        add(split, BorderLayout.CENTER);

        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        cargarProveedores();
        cbProveedor.addActionListener(ev -> onProveedorCambiado());
        cbProducto.addActionListener(ev -> mostrarPrecio());
    }

    // ─── Carga de datos ──────────────────────────────────────────────────────

    private void cargarProveedores() {
        cbProveedor.removeAllItems();
        List<Proveedor> provs = OrdenCompraController.getInstance().getProveedores();
        if (provs.isEmpty()) {
            cbProveedor.addItem("(sin proveedores — registrá uno en M2)");
        } else {
            for (Proveedor p : provs)
                cbProveedor.addItem(p.getCuit() + " · " + p.getRazonSocial());
        }
        actualizarProductos();
    }

    private void onProveedorCambiado() {
        if (!itemsPendientes.isEmpty()) {
            log("⚠ Proveedor cambiado — se limpió la lista de productos.");
            limpiarItems();
        }
        actualizarProductos();
    }

    private void actualizarProductos() {
        cbProducto.removeAllItems();
        lbPrecio.setText("Precio: —");

        Proveedor prov = getProveedorSeleccionado();
        if (prov == null) return;

        List<Producto> todos = OrdenCompraController.getInstance().getProductos();
        List<Producto> disponibles = new ArrayList<>();
        for (Producto p : todos) {
            PrecioAcordado pa = p.obtenerUltimoPrecio(prov);
            if (pa != null && pa.estaVigente()) disponibles.add(p);
        }

        if (disponibles.isEmpty()) {
            cbProducto.addItem("(sin productos con precio para este proveedor — ir a M3)");
            log("⚠ " + prov.getRazonSocial() + " no tiene productos con precio vigente. "
                + "Registrá un producto en M3 y asignale precio con este proveedor.");
        } else {
            for (Producto p : disponibles) {
                PrecioAcordado pa = p.obtenerUltimoPrecio(prov);
                cbProducto.addItem(p.getCodigoInterno() + " · " + p.getDescripcion()
                    + "  [$" + String.format("%.2f", pa.getPrecioUnitario()) + "]");
            }
        }
        mostrarPrecio();
    }

    private void mostrarPrecio() {
        String cod = getCodigoProductoSeleccionado();
        Proveedor prov = getProveedorSeleccionado();
        if (cod == null || prov == null) { lbPrecio.setText("Precio: —"); return; }
        Producto prod = OrdenCompraController.getInstance().buscarProductoPorCodigo(cod);
        if (prod == null) { lbPrecio.setText("Precio: —"); return; }
        PrecioAcordado pa = prod.obtenerUltimoPrecio(prov);
        if (pa != null && pa.estaVigente())
            lbPrecio.setText("Precio vigente: $" + String.format("%.2f", pa.getPrecioUnitario()));
        else
            lbPrecio.setText("Sin precio vigente");
    }

    // ─── Construcción del formulario ─────────────────────────────────────────

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Paso 1 — Cargar productos"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Proveedor
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Proveedor:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(cbProveedor, g);
        g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JButton btnRefProv = new JButton("↺");
        btnRefProv.setToolTipText("Recargar proveedores desde M2");
        btnRefProv.addActionListener(ev -> cargarProveedores());
        p.add(btnRefProv, g);
        row++;

        // Producto
        g.gridy = row; g.gridx = 0; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Producto:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(cbProducto, g);
        g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JButton btnRefProd = new JButton("↺");
        btnRefProd.setToolTipText("Recargar productos desde M3");
        btnRefProd.addActionListener(ev -> actualizarProductos());
        p.add(btnRefProd, g);
        row++;

        // Precio
        g.gridy = row; g.gridx = 0; g.gridwidth = 3; g.fill = GridBagConstraints.HORIZONTAL;
        lbPrecio.setFont(lbPrecio.getFont().deriveFont(Font.BOLD));
        lbPrecio.setForeground(new Color(0, 100, 0));
        p.add(lbPrecio, g);
        g.gridwidth = 1;
        row++;

        // Cantidad + Agregar
        g.gridy = row; g.gridx = 0; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Cantidad:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(tfCantidad, g);
        g.gridx = 2; g.fill = GridBagConstraints.NONE;
        JButton btnItem = new JButton("+ Agregar producto");
        btnItem.addActionListener(this::agregarItem);
        p.add(btnItem, g);
        row++;

        // Separador
        g.gridy = row; g.gridx = 0; g.gridwidth = 3;
        p.add(new JSeparator(), g);
        g.gridwidth = 1;
        row++;

        // Motivo + Emitir (Paso 2)
        g.gridy = row; g.gridx = 0; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel("Motivo (si supera tope):"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(tfMotivo, g);
        g.gridx = 2; g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JButton btnEmitir = new JButton("Emitir Orden de Compra →");
        btnEmitir.setFont(btnEmitir.getFont().deriveFont(Font.BOLD));
        btnEmitir.addActionListener(this::emitirOC);
        p.add(btnEmitir, g);

        return p;
    }

    private JPanel buildTablaPanel() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(BorderFactory.createTitledBorder("Paso 2 — Productos cargados"));
        tablaItems.setRowHeight(20);
        tablaItems.getTableHeader().setReorderingAllowed(false);

        lbTotal.setFont(lbTotal.getFont().deriveFont(Font.BOLD, 13f));
        lbTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton btnLimpiar = new JButton("Limpiar lista");
        btnLimpiar.addActionListener(ev -> limpiarItems());

        JPanel south = new JPanel(new BorderLayout());
        south.add(lbTotal, BorderLayout.CENTER);
        south.add(btnLimpiar, BorderLayout.EAST);

        p.add(new JScrollPane(tablaItems), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Proveedor getProveedorSeleccionado() {
        String item = (String) cbProveedor.getSelectedItem();
        if (item == null || item.startsWith("(sin")) return null;
        String cuit = item.split(" · ")[0];
        return OrdenCompraController.getInstance().buscarProveedorPorId(cuit);
    }

    private String getCodigoProductoSeleccionado() {
        String item = (String) cbProducto.getSelectedItem();
        if (item == null || item.startsWith("(sin")) return null;
        return item.split(" · ")[0];
    }

    private void limpiarItems() {
        itemsPendientes.clear();
        modeloItems.setRowCount(0);
        lbTotal.setText("Total estimado: $0.00");
    }

    private void actualizarTotal() {
        Proveedor prov = getProveedorSeleccionado();
        if (prov == null) { lbTotal.setText("Total estimado: $0.00"); return; }
        double total = 0;
        for (ItemPendiente it : itemsPendientes) {
            Producto prod = OrdenCompraController.getInstance().buscarProductoPorCodigo(it.codigoProducto);
            if (prod != null) {
                PrecioAcordado pa = prod.obtenerUltimoPrecio(prov);
                if (pa != null) total += pa.getPrecioUnitario() * it.cantidad;
            }
        }
        lbTotal.setText("Total estimado: $" + String.format("%.2f", total));
    }

    // ─── Acciones ─────────────────────────────────────────────────────────────

    private void agregarItem(ActionEvent ev) {
        Proveedor prov = getProveedorSeleccionado();
        if (prov == null) { log("✗ Seleccioná un proveedor."); return; }
        String cod = getCodigoProductoSeleccionado();
        if (cod == null) { log("✗ No hay productos disponibles. Registrá uno en M3 con precio para este proveedor."); return; }
        double cant;
        try { cant = Double.parseDouble(tfCantidad.getText().trim()); }
        catch (NumberFormatException e) { log("✗ Cantidad inválida."); return; }
        if (cant <= 0) { log("✗ La cantidad debe ser mayor a 0."); return; }

        Producto prod = OrdenCompraController.getInstance().buscarProductoPorCodigo(cod);
        PrecioAcordado pa = prod.obtenerUltimoPrecio(prov);
        double subtotal = pa.getPrecioUnitario() * cant;

        itemsPendientes.add(new ItemPendiente(cod, cant));
        modeloItems.addRow(new Object[]{
            prod.getDescripcion(), cant,
            String.format("$%.2f", pa.getPrecioUnitario()),
            String.format("$%.2f", subtotal)
        });
        actualizarTotal();
        log("✓ " + prod.getDescripcion() + " x" + cant + " = $" + String.format("%.2f", subtotal));
    }

    private void emitirOC(ActionEvent ev) {
        Proveedor prov = getProveedorSeleccionado();
        if (prov == null) { log("✗ Seleccioná un proveedor."); return; }
        if (itemsPendientes.isEmpty()) { log("✗ Agregá al menos un producto antes de emitir."); return; }
        try {
            OrdenCompra oc = OrdenCompraController.getInstance().crearOrdenCompra(prov.getCuit());
            for (ItemPendiente item : itemsPendientes)
                OrdenCompraController.getInstance().agregarItem(oc, item.codigoProducto, item.cantidad);

            Usuario sup = new Usuario(99, "Supervisor", "Sistema", "supervisor", "pass", RolUsuario.SUPERVISOR);
            String motivo = tfMotivo.getText().trim().isEmpty() ? null : tfMotivo.getText().trim();
            OrdenCompra emitida = OrdenCompraController.getInstance().emitirOrdenCompra(oc, sup, motivo);
            FacturaController.getInstance().agregarOrdenCompra(emitida);

            log("✓ OC emitida: " + emitida.getNumero()
                + " | Total: $" + String.format("%.2f", emitida.calcularTotal())
                + " | Estado: " + emitida.getEstado());
            log("→ Podés ir a M5 para registrar la factura del proveedor.");

            mostrarDocumentoOC(emitida);
            limpiarItems();
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void mostrarDocumentoOC(OrdenCompra oc) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Arial,sans-serif; margin:20px;'>");

        // Encabezado
        html.append("<table width='100%' style='border-bottom:2px solid #222; margin-bottom:12px;'><tr>")
            .append("<td><font size='5'><b>ORDEN DE COMPRA</b></font></td>")
            .append("<td align='right'>")
            .append("<b>N°:</b> ").append(oc.getNumero()).append("<br>")
            .append("<b>Fecha:</b> ").append(sdf.format(oc.getFechaEmision()))
            .append("</td></tr></table>");

        // Datos del proveedor
        html.append("<table width='100%' style='background:#f0f0f0; border:1px solid #bbb; margin-bottom:14px;'><tr><td style='padding:8px;'>")
            .append("<b>Proveedor:</b> ").append(oc.getProveedor().getRazonSocial()).append("<br>")
            .append("<b>CUIT:</b> ").append(oc.getProveedor().getCuit())
            .append("</td></tr></table>");

        // Tabla de ítems
        html.append("<table width='100%' border='1' cellspacing='0' cellpadding='5' style='border-collapse:collapse;'>")
            .append("<tr style='background:#333; color:white;'>")
            .append("<th>N°</th><th>Descripción</th><th>Cantidad</th><th>Precio Unit.</th><th>Subtotal</th>")
            .append("</tr>");

        for (DetalleOC det : oc.getDetalles()) {
            html.append("<tr>")
                .append("<td align='center'>").append(det.getNroLinea()).append("</td>")
                .append("<td>").append(det.getProducto().getDescripcion()).append("</td>")
                .append("<td align='center'>").append(det.getCantidad()).append("</td>")
                .append("<td align='right'>$").append(String.format("%.2f", det.getPrecioUnitario())).append("</td>")
                .append("<td align='right'>$").append(String.format("%.2f", det.getSubtotal())).append("</td>")
                .append("</tr>");
        }
        html.append("</table>");

        // Total
        html.append("<br><table width='100%'><tr><td align='right'>")
            .append("<font size='4'><b>TOTAL: $").append(String.format("%.2f", oc.calcularTotal()))
            .append("</b></font></td></tr></table>");

        // Estado y autorización
        html.append("<br><b>Estado:</b> ").append(oc.getEstado());
        if (oc.getAutorizacion() != null) {
            html.append("<br><b>Autorización:</b> ").append(oc.getAutorizacion().getMotivo())
                .append(" (").append(oc.getAutorizacion().getSupervisor().getNombre()).append(")");
        }

        // Líneas de firma
        html.append("<br><br><br><table width='100%'><tr>")
            .append("<td align='center' width='50%'>____________________<br>Firma Responsable</td>")
            .append("<td align='center' width='50%'>____________________<br>Firma Proveedor</td>")
            .append("</tr></table>");

        html.append("</body></html>");

        JEditorPane editor = new JEditorPane("text/html", html.toString());
        editor.setEditable(false);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
            "Orden de Compra — " + oc.getNumero(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(6, 6));
        dialog.add(new JScrollPane(editor), BorderLayout.CENTER);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        JPanel bots = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bots.add(btnCerrar);
        dialog.add(bots, BorderLayout.SOUTH);

        dialog.setSize(660, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void log(String msg) {
        taLog.append(msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }
}

package farmared.gui;

import farmared.controllers.*;
import farmared.enums.RolUsuario;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

public class PanelOrdenCompra extends JPanel {

    private final JTextField tfCuit     = new JTextField(MainFrame.CUIT_DEMO, 22);
    private final JTextField tfCodigo   = new JTextField(MainFrame.CODIGO_PROD, 12);
    private final JTextField tfCantidad = new JTextField("100", 8);
    private final JTextField tfMotivo   = new JTextField("Compra urgente autorizada", 22);
    private final JTextArea  taLog      = new JTextArea();
    private OrdenCompra ocActual;

    public PanelOrdenCompra() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildForm(), BorderLayout.NORTH);
        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(taLog), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 3;
        p.add(titulo("DS1 — Generar Orden de Compra"), g);
        g.gridwidth = 1;

        // Paso 1: crear OC
        g.gridy = 1; g.gridx = 0; p.add(new JLabel("CUIT Proveedor:"), g);
        g.gridx = 1; p.add(tfCuit, g);
        g.gridx = 2;
        JButton btnCrear = new JButton("1. Crear OC");
        btnCrear.addActionListener(this::crearOC);
        p.add(btnCrear, g);

        g.gridy = 2; g.gridx = 0; g.gridwidth = 3;
        p.add(new JSeparator(), g);
        g.gridwidth = 1;

        // Paso 2: agregar ítem
        g.gridy = 3; g.gridx = 0; p.add(new JLabel("Código producto:"), g);
        g.gridx = 1; p.add(tfCodigo, g);

        g.gridy = 4; g.gridx = 0; p.add(new JLabel("Cantidad:"), g);
        g.gridx = 1; p.add(tfCantidad, g);
        g.gridx = 2;
        JButton btnItem = new JButton("2. Agregar ítem");
        btnItem.addActionListener(this::agregarItem);
        p.add(btnItem, g);

        g.gridy = 5; g.gridx = 0; g.gridwidth = 3;
        p.add(new JSeparator(), g);
        g.gridwidth = 1;

        // Paso 3: motivo + emitir (motivo se usa si el importe excede el tope y requiere autorización del supervisor)
        g.gridy = 6; g.gridx = 0; p.add(new JLabel("Motivo autorización:"), g);
        g.gridx = 1; p.add(tfMotivo, g);
        g.gridx = 2;
        JButton btnEmitir = new JButton("3. Emitir OC");
        btnEmitir.addActionListener(this::emitirOC);
        p.add(btnEmitir, g);

        return p;
    }

    private void crearOC(ActionEvent e) {
        try {
            ocActual = OrdenCompraController.getInstance()
                           .crearOrdenCompra(tfCuit.getText().trim());
            log("✓ OC creada: " + ocActual.getNumero()
                + " | Proveedor: " + ocActual.getProveedor().getRazonSocial());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void agregarItem(ActionEvent e) {
        if (ocActual == null) { log("→ Primero creá una OC (paso 1)."); return; }
        try {
            double cant = Double.parseDouble(tfCantidad.getText().trim());
            OrdenCompraController.getInstance()
                .agregarItem(ocActual, tfCodigo.getText().trim(), cant);
            log("✓ Ítem agregado: " + tfCodigo.getText().trim() + " x" + cant
                + " | Subtotal OC: $" + ocActual.calcularTotal());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void emitirOC(ActionEvent e) {
        if (ocActual == null) { log("→ Primero creá una OC (paso 1)."); return; }
        try {
            Usuario sup = new Usuario(2, "Carlos", "Rios", "crios", "pass", RolUsuario.SUPERVISOR);
            String motivo = tfMotivo.getText().trim().isEmpty() ? null : tfMotivo.getText().trim();
            OrdenCompra emitida = OrdenCompraController.getInstance()
                                      .emitirOrdenCompra(ocActual, sup, motivo);
            FacturaController.getInstance().agregarOrdenCompra(emitida);
            log("✓ OC emitida: " + emitida);
            log("  Total: $" + emitida.calcularTotal() + " | Estado: " + emitida.getEstado());
            log("→ Ahora podés ir a M5 para registrar la factura.");
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void log(String msg) { taLog.append(msg + "\n"); }

    private JLabel titulo(String txt) {
        JLabel l = new JLabel("<html><b>" + txt + "</b></html>");
        l.setFont(l.getFont().deriveFont(13f));
        return l;
    }
}

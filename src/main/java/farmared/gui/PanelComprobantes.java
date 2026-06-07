package farmared.gui;

import farmared.controllers.*;
import farmared.enums.TipoComprobante;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class PanelComprobantes extends JPanel {

    private final JTextArea taLog = new JTextArea();

    public PanelComprobantes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildForm(), BorderLayout.NORTH);
        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(taLog), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JLabel tit = new JLabel("<html><b>DS3 — Registrar Comprobantes recibidos</b></html>");
        tit.setFont(tit.getFont().deriveFont(13f));
        p.add(tit);

        JButton btnFact = new JButton("Registrar Factura A");
        btnFact.addActionListener(this::registrarFactura);
        p.add(btnFact);

        JButton btnNC = new JButton("Registrar Nota de Crédito");
        btnNC.addActionListener(this::registrarNC);
        p.add(btnNC);

        JButton btnND = new JButton("Registrar Nota de Débito");
        btnND.addActionListener(this::registrarND);
        p.add(btnND);

        return p;
    }

    private void registrarFactura(ActionEvent e) {
        OrdenCompra oc = ultimaOCEmitida();
        if (oc == null) return;
        try {
            DetalleOC d = oc.getDetalles().get(0);
            DetalleComprobante det = new DetalleComprobante(
                1, d.getProducto(), d.getCantidad(), d.getPrecioUnitario(), 21.0);
            Factura f = FacturaController.getInstance().registrarFactura(
                null, TipoComprobante.FACTURA_A, new Date(), new Date(),
                Collections.singletonList(det),
                oc.getProveedor().getCuit(),
                Collections.singletonList(oc.getNumero()),
                null, null);
            log("✓ Factura registrada: " + f);
            log("  Deuda vigente: $" + oc.getProveedor().obtenerCuentaCorriente());
            log("→ Ahora podés ir a M6 para generar la Orden de Pago.");
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void registrarNC(ActionEvent e) {
        OrdenCompra oc = ultimaOCEmitida();
        if (oc == null) return;
        try {
            DetalleOC d = oc.getDetalles().get(0);
            // NC: 10 unidades a $150 = $1500 de crédito
            List<DetalleComprobante> dets = new ArrayList<>();
            dets.add(new DetalleComprobante(1, d.getProducto(), 10, 150.0, 21.0));
            NotaCredito nc = FacturaController.getInstance().registrarNotaCredito(
                null, new Date(), new Date(), dets, oc.getProveedor().getCuit());
            log("✓ Nota de Crédito registrada: " + nc);
            log("  Deuda vigente: $" + oc.getProveedor().obtenerCuentaCorriente());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void registrarND(ActionEvent e) {
        OrdenCompra oc = ultimaOCEmitida();
        if (oc == null) return;
        try {
            DetalleOC d = oc.getDetalles().get(0);
            // ND: 5 unidades a $100 = $500 de débito adicional
            List<DetalleComprobante> dets = new ArrayList<>();
            dets.add(new DetalleComprobante(1, d.getProducto(), 5, 100.0, 21.0));
            NotaDebito nd = FacturaController.getInstance().registrarNotaDebito(
                null, new Date(), new Date(), dets, oc.getProveedor().getCuit());
            log("✓ Nota de Débito registrada: " + nd);
            log("  Deuda vigente: $" + oc.getProveedor().obtenerCuentaCorriente());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private OrdenCompra ultimaOCEmitida() {
        List<OrdenCompra> ocs = OrdenCompraController.getInstance().getOrdenes();
        if (ocs.isEmpty()) {
            log("→ Primero emití una OC en la pestaña M4.");
            return null;
        }
        return ocs.get(ocs.size() - 1);
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

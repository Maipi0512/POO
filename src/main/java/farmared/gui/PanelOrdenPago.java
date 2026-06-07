package farmared.gui;

import farmared.controllers.*;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PanelOrdenPago extends JPanel {

    private final JTextArea taLog = new JTextArea();
    private OrdenPago opActual;

    public PanelOrdenPago() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildForm(), BorderLayout.NORTH);
        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(taLog), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));

        JLabel tit = new JLabel("<html><b>DS2 — Emitir Orden de Pago</b></html>");
        tit.setFont(tit.getFont().deriveFont(13f));
        p.add(tit);

        JButton btnIniciar = new JButton("1. Ver comprobantes impagos");
        btnIniciar.addActionListener(this::iniciarOP);
        p.add(btnIniciar);

        JButton btnGenerar = new JButton("2. Generar OP (calcular retenciones)");
        btnGenerar.addActionListener(this::generarOP);
        p.add(btnGenerar);

        JButton btnConfirmar = new JButton("3. Confirmar pago (efectivo)");
        btnConfirmar.addActionListener(this::confirmarPago);
        p.add(btnConfirmar);

        return p;
    }

    private void iniciarOP(ActionEvent e) {
        try {
            List<Comprobante> impagos = OrdenPagoController.getInstance()
                                            .iniciarOrdenPago(MainFrame.CUIT_DEMO);
            if (impagos.isEmpty()) {
                log("→ Sin comprobantes impagos. Registrá una factura en M5 primero.");
                return;
            }
            log("Comprobantes impagos (" + impagos.size() + "):");
            for (Comprobante c : impagos)
                log("  · " + c + " | saldo: $" + c.getSaldoPendiente());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void generarOP(ActionEvent e) {
        try {
            List<Comprobante> impagos = OrdenPagoController.getInstance()
                                            .iniciarOrdenPago(MainFrame.CUIT_DEMO);
            if (impagos.isEmpty()) {
                log("→ Sin comprobantes impagos para pagar.");
                return;
            }
            Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
            for (Comprobante c : impagos)
                seleccion.put(c, c.getSaldoPendiente());

            opActual = OrdenPagoController.getInstance()
                           .seleccionarComprobantes(MainFrame.CUIT_DEMO, seleccion, new Date());

            log("✓ Orden de Pago generada:");
            log("  Bruto:             $" + opActual.getImporteBruto());
            log("  Total retenciones: $" + opActual.getTotalRetenciones());
            log("  Neto a pagar:      $" + opActual.getImporteNeto());
            log("  Retenciones: " + opActual.getRetenciones());
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void confirmarPago(ActionEvent e) {
        if (opActual == null) { log("→ Primero generá la OP (paso 2)."); return; }
        try {
            List<MedioPago> medios = new ArrayList<>();
            medios.add(new Efectivo(1, opActual.getImporteNeto(), new Date()));

            OrdenPago confirmada = OrdenPagoController.getInstance()
                                       .confirmarPago(opActual, medios);
            CuentaCorrienteController.getInstance().agregarOrdenPago(confirmada);

            log("✓ Pago confirmado: " + confirmada);
            log("  Deuda post-pago: $" + OrdenCompraController.getInstance()
                    .buscarProveedorPorId(MainFrame.CUIT_DEMO).obtenerCuentaCorriente());
            log("→ Ahora podés ver el resumen en M7 · Consultas.");
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

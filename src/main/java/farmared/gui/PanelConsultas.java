package farmared.gui;

import farmared.controllers.*;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modulos.m3_productos.PrecioAcordado;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class PanelConsultas extends JPanel {

    private final JTextArea taLog = new JTextArea();

    public PanelConsultas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        add(buildBotones(), BorderLayout.NORTH);
        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(taLog), BorderLayout.CENTER);
    }

    private JPanel buildBotones() {
        JPanel p = new JPanel(new GridLayout(0, 3, 6, 6));
        p.setBorder(BorderFactory.createTitledBorder("M7 — Consultas Generales y Reportes"));

        agregar(p, "DS4 · Cuenta Corriente",          this::cuentaCorriente);
        agregar(p, "Documentos impagos",               this::documentosImpagos);
        agregar(p, "Detalle de pagos",                 this::detallePagos);
        agregar(p, "Compulsa de precios MED-001",      this::compulsaPrecios);
        agregar(p, "Deuda vigente por proveedor",      this::deudaVigente);
        agregar(p, "Retenciones por tipo",             this::retencionesPorTipo);
        agregar(p, "Libro IVA Compras",                this::libroIVA);
        agregar(p, "Facturas por día y proveedor",     this::facturasPorDia);
        agregar(p, "Órdenes de compra emitidas",       this::ordenesCompra);
        agregar(p, "Órdenes de pago emitidas",         this::ordenesPago);

        return p;
    }

    private void agregar(JPanel p, String label, java.awt.event.ActionListener al) {
        JButton b = new JButton(label);
        b.addActionListener(al);
        p.add(b);
    }

    private void cuentaCorriente(ActionEvent e) {
        try {
            Map<String, Object> cc = CuentaCorrienteController.getInstance()
                                         .consultarCuentaCorriente(MainFrame.CUIT_DEMO);
            log("── Cuenta Corriente ──");
            cc.forEach((k, v) -> log("  " + k + ": " + v));
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void documentosImpagos(ActionEvent e) {
        List<Comprobante> imp = CuentaCorrienteController.getInstance()
                                    .listarDocumentosImpagos(MainFrame.CUIT_DEMO);
        log("── Documentos impagos ──");
        if (imp.isEmpty()) log("  (ninguno)");
        else imp.forEach(c -> log("  · " + c + " | $" + c.getSaldoPendiente()));
    }

    private void detallePagos(ActionEvent e) {
        List<Map<String, Object>> pagos = CuentaCorrienteController.getInstance()
                                              .detallarPagosPorProveedor(MainFrame.CUIT_DEMO);
        log("── Detalle de pagos ──");
        if (pagos.isEmpty()) log("  (ninguno)");
        else pagos.forEach(p -> log("  · " + p));
    }

    private void compulsaPrecios(ActionEvent e) {
        List<PrecioAcordado> precios = OrdenCompraController.getInstance()
                                           .consultarCompulsaPrecios(MainFrame.CODIGO_PROD);
        log("── Compulsa de precios MED-001 ──");
        if (precios.isEmpty()) log("  (sin precios registrados)");
        else precios.forEach(p -> log("  · " + p));
    }

    private void deudaVigente(ActionEvent e) {
        log("── Deuda vigente por proveedor ──");
        CuentaCorrienteController.getInstance()
            .consultarDeudaVigentePorProveedor()
            .forEach((k, v) -> log("  · " + k + ": $" + v));
    }

    private void retencionesPorTipo(ActionEvent e) {
        log("── Retenciones por tipo de impuesto ──");
        OrdenPagoController.getInstance()
            .reporteRetencionesPorTipo()
            .forEach((k, v) -> log("  · " + k + ": $" + v));
    }

    private void libroIVA(ActionEvent e) {
        log("── Libro IVA Compras ──");
        FacturaController.getInstance()
            .generarLibroIVACompras()
            .forEach(linea -> log("  · " + linea));
    }

    private void facturasPorDia(ActionEvent e) {
        log("── Facturas por día y proveedor ──");
        FacturaController.getInstance()
            .totalFacturasPorDiaYProveedor()
            .forEach((k, v) -> log("  · " + k + ": " + v));
    }

    private void ordenesCompra(ActionEvent e) {
        log("── Órdenes de Compra emitidas ──");
        List<farmared.modulos.m4_ordenes_compra.OrdenCompra> ocs =
            OrdenCompraController.getInstance().getOrdenes();
        if (ocs.isEmpty()) log("  (ninguna)");
        else ocs.forEach(oc -> log("  · " + oc));
    }

    private void ordenesPago(ActionEvent e) {
        log("── Órdenes de Pago emitidas ──");
        List<OrdenPago> ops = OrdenPagoController.getInstance().getOrdenesPago();
        if (ops.isEmpty()) log("  (ninguna)");
        else ops.forEach(op -> log("  · " + op));
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

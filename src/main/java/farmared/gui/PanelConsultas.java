package farmared.gui;

import farmared.controllers.*;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.OrdenPago;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

public class PanelConsultas extends JPanel {

    private final JComboBox<String> cbProveedor = new JComboBox<>();
    private final JComboBox<String> cbProducto  = new JComboBox<>();
    private final JTextArea taLog = new JTextArea();

    public PanelConsultas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M7 — Consultas Generales y Reportes</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            buildControles(), buildCentro());
        split.setDividerLocation(180);
        add(split, BorderLayout.CENTER);

        cargarCombos();
    }

    private void cargarCombos() {
        cbProveedor.removeAllItems();
        cbProveedor.addItem("(todos)");
        for (Proveedor p : CuentaCorrienteController.getInstance().getProveedores()) {
            cbProveedor.addItem(p.getCuit() + " · " + p.getRazonSocial());
        }

        cbProducto.removeAllItems();
        cbProducto.addItem("(todos)");
        for (Producto p : OrdenCompraController.getInstance().getProductos()) {
            cbProducto.addItem(p.getCodigoInterno() + " · " + p.getDescripcion());
        }
    }

    private String getCuit() {
        String item = (String) cbProveedor.getSelectedItem();
        if (item == null || item.startsWith("(todos)")) return null;
        return item.split(" · ")[0];
    }

    private String getCodigoProd() {
        String item = (String) cbProducto.getSelectedItem();
        if (item == null || item.startsWith("(todos)")) return null;
        return item.split(" · ")[0];
    }

    private JPanel buildControles() {
        JPanel outer = new JPanel(new BorderLayout(4, 4));
        outer.setBorder(BorderFactory.createTitledBorder("Filtros de consulta"));

        JPanel filtros = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridy = 0; g.gridx = 0; filtros.add(new JLabel("Proveedor:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; filtros.add(cbProveedor, g);

        g.gridy = 0; g.gridx = 2; g.fill = GridBagConstraints.NONE;
        filtros.add(new JLabel("  Producto (compulsa):"), g);
        g.gridx = 3; g.fill = GridBagConstraints.HORIZONTAL; filtros.add(cbProducto, g);

        g.gridy = 0; g.gridx = 4; g.fill = GridBagConstraints.NONE;
        JButton btnRefresh = new JButton("↺ Actualizar filtros");
        btnRefresh.addActionListener(ev -> cargarCombos());
        filtros.add(btnRefresh, g);

        outer.add(filtros, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(0, 4, 5, 5));
        botones.setBorder(BorderFactory.createTitledBorder("Consultas disponibles"));

        btn(botones, "Cuenta Corriente",         this::cuentaCorriente);
        btn(botones, "Documentos impagos",        this::documentosImpagos);
        btn(botones, "Detalle de pagos",          this::detallePagos);
        btn(botones, "Deuda por proveedor",       this::deudaVigente);
        btn(botones, "Compulsa de precios",       this::compulsaPrecios);
        btn(botones, "Libro IVA Compras",         this::libroIVA);
        btn(botones, "Retenciones por tipo",      this::retencionesPorTipo);
        btn(botones, "Facturas por día/prov.",    this::facturasPorDia);
        btn(botones, "Órdenes de Compra",         this::ordenesCompra);
        btn(botones, "Órdenes de Pago",           this::ordenesPago);
        btn(botones, "Limpiar pantalla",          this::limpiar);

        outer.add(botones, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildCentro() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Resultado"));
        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        p.add(new JScrollPane(taLog), BorderLayout.CENTER);
        return p;
    }

    private void btn(JPanel p, String label, java.awt.event.ActionListener al) {
        JButton b = new JButton(label);
        b.addActionListener(al);
        p.add(b);
    }

    // ─── Consultas por proveedor ─────────────────────────────────────────────

    private void cuentaCorriente(ActionEvent ev) {
        String cuit = getCuit();
        if (cuit == null) {
            log("── Cuenta Corriente (todos los proveedores) ──");
            for (Proveedor p : CuentaCorrienteController.getInstance().getProveedores()) {
                mostrarCC(p.getCuit());
            }
            return;
        }
        log("── Cuenta Corriente: " + cuit + " ──");
        mostrarCC(cuit);
    }

    private void mostrarCC(String cuit) {
        try {
            Map<String, Object> cc = CuentaCorrienteController.getInstance()
                                         .consultarCuentaCorriente(cuit);
            cc.forEach((k, v) -> log("  " + k + ": " + v));
        } catch (Exception ex) { log("✗ " + ex.getMessage()); }
    }

    private void documentosImpagos(ActionEvent ev) {
        String cuit = getCuit();
        List<Proveedor> proveedores = cuit != null
            ? List.of(obtenerProveedor(cuit))
            : CuentaCorrienteController.getInstance().getProveedores();
        log("── Documentos impagos" + (cuit != null ? " · " + cuit : " (todos)") + " ──");
        boolean alguno = false;
        for (Proveedor p : proveedores) {
            if (p == null) continue;
            List<Comprobante> imp = CuentaCorrienteController.getInstance()
                                       .listarDocumentosImpagos(p.getCuit());
            if (!imp.isEmpty()) {
                log("  " + p.getRazonSocial() + ":");
                imp.forEach(c -> log("    · " + c.getNumero() + " | " + c.getTipo()
                                     + " | saldo: $" + String.format("%.2f", c.getSaldoPendiente())));
                alguno = true;
            }
        }
        if (!alguno) log("  (ningún documento impago)");
    }

    private void detallePagos(ActionEvent ev) {
        String cuit = getCuit();
        List<Proveedor> proveedores = cuit != null
            ? List.of(obtenerProveedor(cuit))
            : CuentaCorrienteController.getInstance().getProveedores();
        log("── Detalle de pagos" + (cuit != null ? " · " + cuit : " (todos)") + " ──");
        boolean alguno = false;
        for (Proveedor p : proveedores) {
            if (p == null) continue;
            List<Map<String, Object>> pagos = CuentaCorrienteController.getInstance()
                                                  .detallarPagosPorProveedor(p.getCuit());
            if (!pagos.isEmpty()) {
                log("  " + p.getRazonSocial() + ":");
                pagos.forEach(pg -> log("    · " + pg));
                alguno = true;
            }
        }
        if (!alguno) log("  (sin pagos registrados)");
    }

    private void deudaVigente(ActionEvent ev) {
        log("── Deuda vigente por proveedor ──");
        Map<String, Double> deudas = CuentaCorrienteController.getInstance()
                                         .consultarDeudaVigentePorProveedor();
        if (deudas.isEmpty()) { log("  (ninguna)"); return; }
        deudas.forEach((k, v) -> log("  · " + k + ": $" + String.format("%.2f", v)));
    }

    // ─── Consultas por producto ───────────────────────────────────────────────

    private void compulsaPrecios(ActionEvent ev) {
        String cod = getCodigoProd();
        if (cod == null) {
            log("── Compulsa de precios (todos los productos) ──");
            for (Producto prod : OrdenCompraController.getInstance().getProductos()) {
                mostrarCompulsa(prod.getCodigoInterno());
            }
            return;
        }
        log("── Compulsa de precios: " + cod + " ──");
        mostrarCompulsa(cod);
    }

    private void mostrarCompulsa(String cod) {
        List<PrecioAcordado> precios = OrdenCompraController.getInstance()
                                           .consultarCompulsaPrecios(cod);
        if (precios.isEmpty()) { log("  " + cod + ": (sin precios registrados)"); return; }
        precios.forEach(p -> log("  · " + cod + " | Prov: "
            + p.getProveedor().getRazonSocial()
            + " | $" + p.getPrecioUnitario()
            + " | Vigente: " + p.estaVigente()));
    }

    // ─── Reportes globales ────────────────────────────────────────────────────

    private void retencionesPorTipo(ActionEvent ev) {
        log("── Retenciones por tipo de impuesto ──");
        Map<?, Double> ret = OrdenPagoController.getInstance().reporteRetencionesPorTipo();
        if (ret.values().stream().allMatch(v -> v == 0.0)) {
            log("  (sin retenciones — confirmá un pago en M6 primero)"); return;
        }
        ret.forEach((k, v) -> log("  · " + k + ": $" + String.format("%.2f", v)));
    }

    private void libroIVA(ActionEvent ev) {
        log("── Libro IVA Compras ──");
        List<Map<String, Object>> libro = FacturaController.getInstance().generarLibroIVACompras();
        if (libro.isEmpty()) { log("  (sin comprobantes — registrá facturas en M5 primero)"); return; }
        libro.forEach(linea -> log("  · " + linea));
    }

    private void facturasPorDia(ActionEvent ev) {
        log("── Facturas por día y proveedor ──");
        Map<String, Map<String, Object>> res = FacturaController.getInstance()
                                                    .totalFacturasPorDiaYProveedor();
        if (res.isEmpty()) { log("  (ninguna)"); return; }
        res.forEach((k, v) -> log("  · " + k + " → $"
            + String.format("%.2f", (double) v.get("montoTotal"))
            + " (" + v.get("cantidad") + " factura/s)"));
    }

    private void ordenesCompra(ActionEvent ev) {
        log("── Órdenes de Compra emitidas ──");
        List<OrdenCompra> ocs = OrdenCompraController.getInstance().getOrdenes();
        if (ocs.isEmpty()) { log("  (ninguna — creá una en M4 primero)"); return; }
        String filtro = getCuit();
        ocs.stream()
           .filter(oc -> filtro == null || oc.getProveedor().getCuit().equals(filtro))
           .forEach(oc -> log("  · " + oc.getNumero()
               + " | " + oc.getProveedor().getRazonSocial()
               + " | Total: $" + String.format("%.2f", oc.calcularTotal())
               + " | Estado: " + oc.getEstado()));
    }

    private void ordenesPago(ActionEvent ev) {
        log("── Órdenes de Pago emitidas ──");
        List<OrdenPago> ops = OrdenPagoController.getInstance().getOrdenesPago();
        if (ops.isEmpty()) { log("  (ninguna — emití un pago en M6 primero)"); return; }
        String filtro = getCuit();
        ops.stream()
           .filter(op -> filtro == null || op.getProveedor().getCuit().equals(filtro))
           .forEach(op -> log("  · " + op.getNumero()
               + " | " + op.getProveedor().getRazonSocial()
               + " | Bruto: $" + String.format("%.2f", op.getImporteBruto())
               + " | Neto: $" + String.format("%.2f", op.getImporteNeto())
               + " | Estado: " + op.getEstado()));
    }

    private void limpiar(ActionEvent ev) { taLog.setText(""); }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private Proveedor obtenerProveedor(String cuit) {
        return CuentaCorrienteController.getInstance().getProveedores().stream()
            .filter(p -> p.getCuit().equals(cuit)).findFirst().orElse(null);
    }

    private void log(String msg) {
        taLog.append(msg + "\n");
        taLog.setCaretPosition(taLog.getDocument().getLength());
    }
}

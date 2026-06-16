package farmared.vistas.paneles;

import farmared.modelo.enums.TipoImpuesto;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.Factura;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.controladores.AppContext;
import farmared.controladores.ReportesController;
import farmared.vistas.util.UiUtil;
<<<<<<< HEAD

=======
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PanelConsultas extends JPanel {

    private final ReportesController ctrl = AppContext.getInstancia().getReportesCtrl();

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JTextField codigoProducto = new JTextField(10);
    private final JTable tabla = new JTable();
    private final JLabel lblTitulo = new JLabel("Seleccione una consulta");

    public PanelConsultas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(new String[]{"Columna 1"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cuentaCorriente = new JButton("Cuenta corriente");
        JButton impagos        = new JButton("Documentos impagos");
        JButton deuda          = new JButton("Deuda por proveedor");
        JButton retenciones    = new JButton("Retenciones por tipo");
        JButton libroIVA       = new JButton("Libro IVA Compras");
        JButton compulsa       = new JButton("Compulsa precios");
        JButton facturasDia    = new JButton("Facturas del dia");
        JButton pagos          = new JButton("Pagos por proveedor");

        superior.add(new JLabel("Proveedor:")); superior.add(comboProveedores);
        superior.add(codigoProducto);
        superior.add(cuentaCorriente); superior.add(impagos); superior.add(deuda);
        superior.add(retenciones); superior.add(libroIVA); superior.add(compulsa);
        superior.add(facturasDia); superior.add(pagos);

        cuentaCorriente.addActionListener(e -> consultarCuentaCorriente());
        impagos.addActionListener(e -> consultarImpagos());
        deuda.addActionListener(e -> consultarDeuda());
        retenciones.addActionListener(e -> consultarRetenciones());
        libroIVA.addActionListener(e -> consultarLibroIVA());
        compulsa.addActionListener(e -> consultarCompulsa());
        facturasDia.addActionListener(e -> consultarFacturasDia());
        pagos.addActionListener(e -> consultarPagos());

        add(superior, BorderLayout.NORTH);
        add(lblTitulo, BorderLayout.SOUTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarDatos();
    }

    public void cargarDatos() {
        comboProveedores.removeAllItems();
        comboProveedores.addItem("");
        for (var p : ctrl.getProveedores())
            comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
    }

    private String obtenerCuitSeleccionado() {
        String item = (String) comboProveedores.getSelectedItem();
        if (item == null || item.isBlank()) throw new IllegalArgumentException("Seleccione un proveedor.");
        return item.split(" - ")[0];
    }

    private void setTabla(String titulo, String[] columnas, Object[][] filas) {
        lblTitulo.setText(titulo);
        tabla.setModel(new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        for (Object[] fila : filas) model.addRow(fila);
    }

    private void consultarCuentaCorriente() {
        try {
            List<Comprobante> docs = ctrl.consultarCuentaCorriente(obtenerCuitSeleccionado());
            Object[][] filas = new Object[docs.size()][5];
            for (int i = 0; i < docs.size(); i++) {
                Comprobante c = docs.get(i);
                filas[i] = new Object[]{c.getNumero(), c.getTipo(),
                        UiUtil.formatearFecha(c.getFechaEmision()),
                        UiUtil.formatearMoneda(c.getImporteTotal()), UiUtil.formatearMoneda(c.getSaldoPendiente())};
            }
            setTabla("Cuenta corriente", new String[]{"Numero", "Tipo", "Fecha", "Total", "Saldo"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarImpagos() {
        try {
            List<Comprobante> docs = ctrl.listarDocumentosImpagos(obtenerCuitSeleccionado());
            Object[][] filas = new Object[docs.size()][4];
            for (int i = 0; i < docs.size(); i++) {
                Comprobante c = docs.get(i);
                filas[i] = new Object[]{c.getNumero(), c.getTipo(),
                        UiUtil.formatearFecha(c.getFechaEmision()), UiUtil.formatearMoneda(c.getSaldoPendiente())};
            }
            setTabla("Documentos impagos", new String[]{"Numero", "Tipo", "Fecha", "Saldo"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarDeuda() {
        Map<String, Double> deudas = ctrl.consultarDeudaVigentePorProveedor();
        Object[][] filas = new Object[deudas.size()][2]; int i = 0;
        for (Map.Entry<String, Double> e : deudas.entrySet())
            filas[i++] = new Object[]{e.getKey(), UiUtil.formatearMoneda(e.getValue())};
        setTabla("Deuda vigente por proveedor", new String[]{"Proveedor", "Deuda"}, filas);
    }

    private void consultarRetenciones() {
        Map<TipoImpuesto, Double> datos = ctrl.reporteRetencionesPorTipo();
        Object[][] filas = new Object[datos.size()][2]; int i = 0;
        for (Map.Entry<TipoImpuesto, Double> e : datos.entrySet())
            filas[i++] = new Object[]{e.getKey(), UiUtil.formatearMoneda(e.getValue())};
        setTabla("Retenciones por tipo de impuesto", new String[]{"Tipo", "Total retenido"}, filas);
    }

    private void consultarLibroIVA() {
        List<Map<String, Object>> libro = ctrl.generarLibroIVACompras();
        Object[][] filas = new Object[libro.size()][8];
        for (int i = 0; i < libro.size(); i++) {
            Map<String, Object> l = libro.get(i);
            filas[i] = new Object[]{l.get("cuit"), l.get("razonSocial"),
                    UiUtil.formatearFecha((Date) l.get("fechaEmision")), l.get("tipo"),
                    l.get("base21"), l.get("iva21"), l.get("base10_5"), l.get("importeTotal")};
        }
        setTabla("Libro IVA Compras",
                new String[]{"CUIT", "Razon Social", "Fecha", "Tipo", "Base 21%", "IVA 21%", "Base 10.5%", "Total"},
                filas);
    }

    private void consultarCompulsa() {
        try {
            String codigo = codigoProducto.getText().trim();
            if (codigo.isEmpty()) throw new IllegalArgumentException("Ingrese codigo de producto.");
            var precios = ctrl.consultarCompulsaPrecios(codigo);
            Object[][] filas = new Object[precios.size()][4];
            for (int i = 0; i < precios.size(); i++) {
                var pa = precios.get(i);
                filas[i] = new Object[]{pa.getProveedor().getRazonSocial(),
                        UiUtil.formatearMoneda(pa.getPrecioUnitario()),
                        UiUtil.formatearFecha(pa.getFechaAcuerdo()),
                        pa.estaVigente() ? "Vigente" : "Historico"};
            }
            setTabla("Compulsa de precios - " + codigo,
                    new String[]{"Proveedor", "Precio", "Fecha acuerdo", "Estado"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarFacturasDia() {
        try {
            String cuit = (comboProveedores.getSelectedItem() == null
                    || comboProveedores.getSelectedItem().equals("")) ? null : obtenerCuitSeleccionado();
            List<Factura> facturas = ctrl.consultarFacturasPorDia(new Date(), cuit);
            Object[][] filas = new Object[facturas.size()][4];
            for (int i = 0; i < facturas.size(); i++) {
                Factura f = facturas.get(i);
                filas[i] = new Object[]{f.getNumero(), f.getProveedor().getRazonSocial(),
                        UiUtil.formatearMoneda(f.getImporteTotal()), UiUtil.formatearFecha(f.getFechaRecepcion())};
            }
            setTabla("Facturas recibidas hoy", new String[]{"Numero", "Proveedor", "Importe", "Recepcion"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarPagos() {
        try {
            List<OrdenPago> pagos = ctrl.buscarPagosPorProveedor(obtenerCuitSeleccionado());
            Object[][] filas = new Object[pagos.size()][5];
            for (int i = 0; i < pagos.size(); i++) {
                OrdenPago op = pagos.get(i);
                filas[i] = new Object[]{op.getNumero(), UiUtil.formatearFecha(op.getFechaEmision()),
                        UiUtil.formatearMoneda(op.getImporteBruto()),
                        UiUtil.formatearMoneda(op.getTotalRetenciones()),
                        UiUtil.formatearMoneda(op.getImporteNeto())};
            }
            setTabla("Pagos del proveedor", new String[]{"OP", "Fecha", "Bruto", "Retenciones", "Neto"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }
}

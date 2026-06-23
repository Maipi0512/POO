package farmared.vistas.paneles;

import farmared.modelo.enums.TipoImpuesto;
import farmared.dto.ComprobanteDTO;
import farmared.dto.OrdenCompraDTO;
import farmared.dto.OrdenPagoDTO;
import farmared.dto.PrecioAcordadoDTO;
import farmared.dto.ProveedorDTO;
import farmared.controladores.AppContext;
import farmared.controladores.ReportesController;
import farmared.vistas.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PanelConsultas extends JPanel {

    private final ReportesController ctrl = AppContext.getInstancia().getReportesCtrl();

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JTextField codigoProducto = new JTextField(10);
    private final JSpinner spinnerFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
    private final JTable tabla = new JTable();
    private final JLabel lblTitulo = new JLabel("Seleccione una consulta");

    public PanelConsultas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(new String[]{"Columna 1"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });

        // --- Fila 1: filtros ---
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFecha, "dd/MM/yyyy");
        spinnerFecha.setEditor(editor);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        filtros.add(new JLabel("Proveedor:")); filtros.add(comboProveedores);
        filtros.add(new JLabel("Cod. Producto:")); filtros.add(codigoProducto);
        filtros.add(new JLabel("Fecha:")); filtros.add(spinnerFecha);

        // --- Fila 2: botones en grilla 2x5 ---
        JButton cuentaCorriente = new JButton("Cuenta corriente");
        JButton impagos        = new JButton("Documentos impagos");
        JButton deuda          = new JButton("Deuda por proveedor");
        JButton retenciones    = new JButton("Retenciones por tipo");
        JButton libroIVA       = new JButton("Libro IVA Compras");
        JButton compulsa       = new JButton("Compulsa precios");
        JButton facturasDia    = new JButton("Facturas del dia");
        JButton pagos          = new JButton("Pagos por proveedor");
        JButton ordenesCompra  = new JButton("Ordenes de compra");
        JButton ordenesPago    = new JButton("Ordenes de pago");

        JPanel botones = new JPanel(new GridLayout(2, 5, 4, 4));
        botones.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
        botones.add(cuentaCorriente); botones.add(impagos);    botones.add(deuda);
        botones.add(retenciones);    botones.add(libroIVA);
        botones.add(compulsa);       botones.add(facturasDia); botones.add(pagos);
        botones.add(ordenesCompra);  botones.add(ordenesPago);

        cuentaCorriente.addActionListener(e -> consultarCuentaCorriente());
        impagos.addActionListener(e -> consultarImpagos());
        deuda.addActionListener(e -> consultarDeuda());
        retenciones.addActionListener(e -> consultarRetenciones());
        libroIVA.addActionListener(e -> consultarLibroIVA());
        compulsa.addActionListener(e -> consultarCompulsa());
        facturasDia.addActionListener(e -> consultarFacturasDia());
        pagos.addActionListener(e -> consultarPagos());
        ordenesCompra.addActionListener(e -> consultarOrdenesCompra());
        ordenesPago.addActionListener(e -> consultarOrdenesPago());

        JPanel norte = new JPanel(new BorderLayout());
        norte.add(filtros, BorderLayout.NORTH);
        norte.add(botones, BorderLayout.CENTER);

        lblTitulo.setFont(lblTitulo.getFont().deriveFont(java.awt.Font.BOLD));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        add(norte, BorderLayout.NORTH);
        add(lblTitulo, BorderLayout.SOUTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        cargarDatos();
    }

    public void cargarDatos() {
        comboProveedores.removeAllItems();
        comboProveedores.addItem("");
        for (ProveedorDTO p : ctrl.getProveedoresDTO())
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
            List<ComprobanteDTO> docs = ctrl.consultarCuentaCorrienteDTO(obtenerCuitSeleccionado());
            Object[][] filas = new Object[docs.size()][5];
            for (int i = 0; i < docs.size(); i++) {
                ComprobanteDTO c = docs.get(i);
                filas[i] = new Object[]{c.getNumero(), c.getTipo(),
                        UiUtil.formatearFecha(c.getFechaEmision()),
                        UiUtil.formatearMoneda(c.getImporteTotal()), UiUtil.formatearMoneda(c.getSaldoPendiente())};
            }
            setTabla("Cuenta corriente", new String[]{"Numero", "Tipo", "Fecha", "Total", "Saldo"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarImpagos() {
        try {
            List<ComprobanteDTO> docs = ctrl.listarDocumentosImpagosDTO(obtenerCuitSeleccionado());
            Object[][] filas = new Object[docs.size()][4];
            for (int i = 0; i < docs.size(); i++) {
                ComprobanteDTO c = docs.get(i);
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
        Object[][] filas = new Object[libro.size()][9];
        for (int i = 0; i < libro.size(); i++) {
            Map<String, Object> l = libro.get(i);
            filas[i] = new Object[]{l.get("cuit"), l.get("razonSocial"),
                    UiUtil.formatearFecha((Date) l.get("fechaEmision")), l.get("tipo"),
                    UiUtil.formatearMoneda((Double) l.get("base21")),
                    UiUtil.formatearMoneda((Double) l.get("iva21")),
                    UiUtil.formatearMoneda((Double) l.get("base10_5")),
                    UiUtil.formatearMoneda((Double) l.get("iva10_5")),
                    UiUtil.formatearMoneda((Double) l.get("importeTotal"))};
        }
        setTabla("Libro IVA Compras",
                new String[]{"CUIT", "Razon Social", "Fecha", "Tipo", "Base 21%", "IVA 21%", "Base 10.5%", "IVA 10.5%", "Total"},
                filas);
    }

    private void consultarCompulsa() {
        try {
            String codigo = codigoProducto.getText().trim();
            if (codigo.isEmpty()) throw new IllegalArgumentException("Ingrese codigo de producto.");
            var precios = ctrl.consultarCompulsaPreciosDTO(codigo);
            Object[][] filas = new Object[precios.size()][6];
            for (int i = 0; i < precios.size(); i++) {
                PrecioAcordadoDTO pa = precios.get(i);
                filas[i] = new Object[]{pa.getRazonSocialProveedor(),
                        pa.getRubrosProveedor(),
                        UiUtil.formatearMoneda(pa.getPrecioUnitario()),
                        UiUtil.formatearFecha(pa.getFechaAcuerdo()),
                        UiUtil.formatearFecha(pa.getFechaVencimiento()),
                        pa.isVigente() ? "Vigente" : "Historico"};
            }
            setTabla("Compulsa de precios - " + codigo,
                    new String[]{"Proveedor", "Rubro", "Precio", "Desde", "Vencimiento", "Estado"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarFacturasDia() {
        try {
            boolean sinFiltroProveedor = comboProveedores.getSelectedItem() == null
                    || comboProveedores.getSelectedItem().equals("");
            String cuit = sinFiltroProveedor ? null : obtenerCuitSeleccionado();
            Date fechaSeleccionada = (Date) spinnerFecha.getValue();
            List<ComprobanteDTO> facturas = ctrl.consultarFacturasPorDiaDTO(fechaSeleccionada, cuit);
            String fechaStr = new java.text.SimpleDateFormat("dd/MM/yyyy").format(fechaSeleccionada);
            double totalGlobal = facturas.stream().mapToDouble(ComprobanteDTO::getImporteTotal).sum();

            if (sinFiltroProveedor) {
                // Agrupar por proveedor: una fila por proveedor con cantidad y total
                Map<String, double[]> agrupado = new LinkedHashMap<>();
                for (ComprobanteDTO f : facturas) {
                    agrupado.computeIfAbsent(f.getRazonSocialProveedor(), k -> new double[]{0, 0});
                    agrupado.get(f.getRazonSocialProveedor())[0]++;
                    agrupado.get(f.getRazonSocialProveedor())[1] += f.getImporteTotal();
                }
                Object[][] filas = new Object[agrupado.size()][3];
                int i = 0;
                for (Map.Entry<String, double[]> e : agrupado.entrySet())
                    filas[i++] = new Object[]{e.getKey(), (int) e.getValue()[0],
                            UiUtil.formatearMoneda(e.getValue()[1])};
                String titulo = String.format("Facturas del %s — %d factura(s) | Total: %s",
                        fechaStr, facturas.size(), UiUtil.formatearMoneda(totalGlobal));
                setTabla(titulo, new String[]{"Proveedor", "Cantidad", "Total"}, filas);
            } else {
                // Proveedor específico: mostrar facturas individuales
                Object[][] filas = new Object[facturas.size()][3];
                for (int i = 0; i < facturas.size(); i++) {
                    ComprobanteDTO f = facturas.get(i);
                    filas[i] = new Object[]{f.getNumero(),
                            UiUtil.formatearMoneda(f.getImporteTotal()),
                            UiUtil.formatearFecha(f.getFechaRecepcion())};
                }
                String titulo = String.format("Facturas del %s — %s — %d factura(s) | Total: %s",
                        fechaStr, facturas.isEmpty() ? "" : facturas.get(0).getRazonSocialProveedor(),
                        facturas.size(), UiUtil.formatearMoneda(totalGlobal));
                setTabla(titulo, new String[]{"Numero", "Importe", "Recepcion"}, filas);
            }
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarPagos() {
        try {
            List<OrdenPagoDTO> pagos = ctrl.buscarPagosPorProveedorDTO(obtenerCuitSeleccionado());
            Object[][] filas = new Object[pagos.size()][5];
            for (int i = 0; i < pagos.size(); i++) {
                OrdenPagoDTO op = pagos.get(i);
                filas[i] = new Object[]{op.getNumero(), UiUtil.formatearFecha(op.getFechaEmision()),
                        UiUtil.formatearMoneda(op.getImporteBruto()),
                        UiUtil.formatearMoneda(op.getTotalRetenciones()),
                        UiUtil.formatearMoneda(op.getImporteNeto())};
            }
            setTabla("Pagos del proveedor", new String[]{"OP", "Fecha", "Bruto", "Retenciones", "Neto"}, filas);
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void consultarOrdenesCompra() {
        String cuit = comboProveedores.getSelectedItem() == null
                || comboProveedores.getSelectedItem().equals("") ? null : obtenerCuitSeleccionado();
        List<OrdenCompraDTO> ocs = ctrl.listarOrdenesCompraDTO(cuit);
        Object[][] filas = new Object[ocs.size()][5];
        for (int i = 0; i < ocs.size(); i++) {
            OrdenCompraDTO oc = ocs.get(i);
            filas[i] = new Object[]{oc.getNumero(),
                    oc.getRazonSocialProveedor(),
                    UiUtil.formatearFecha(oc.getFechaEmision()),
                    UiUtil.formatearMoneda(oc.getImporteTotal()),
                    oc.getEstado()};
        }
        setTabla("Ordenes de compra emitidas",
                new String[]{"Numero", "Proveedor", "Fecha", "Total", "Estado"}, filas);
    }

    private void consultarOrdenesPago() {
        String cuit = comboProveedores.getSelectedItem() == null
                || comboProveedores.getSelectedItem().equals("") ? null : obtenerCuitSeleccionado();
        List<OrdenPagoDTO> ops = ctrl.listarOrdenesPagoDTO(cuit);
        Object[][] filas = new Object[ops.size()][5];
        for (int i = 0; i < ops.size(); i++) {
            OrdenPagoDTO op = ops.get(i);
            filas[i] = new Object[]{op.getNumero(),
                    op.getRazonSocialProveedor(),
                    UiUtil.formatearFecha(op.getFechaEmision()),
                    UiUtil.formatearMoneda(op.getImporteNeto()),
                    op.getEstado()};
        }
        setTabla("Ordenes de pago emitidas",
                new String[]{"Numero", "Proveedor", "Fecha", "Neto", "Estado"}, filas);
    }
}

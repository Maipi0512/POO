package farmared.GUI.paneles;

import farmared.controladores.FacturaController;
import farmared.enums.TipoComprobante;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.GUI.AppContext;
import farmared.GUI.util.CarritoUtil;
import farmared.GUI.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DS3 - Registrar Comprobante.
 * Flujo: elegir proveedor → elegir OC → los ítems se pre-cargan → registrar factura/NC/ND.
 */
public class PanelComprobantes extends JPanel {

    private final FacturaController ctrl = AppContext.getInstancia().getFacturaCtrl();

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JComboBox<TipoComprobante> tipoComprobante = new JComboBox<>(TipoComprobante.values());
    private final JComboBox<OrdenCompra> comboOC = new JComboBox<>();
    private final JLabel lblResumen = new JLabel("Sin items.");
    private final JTable tablaDetalle = new JTable();
    private final JTable tablaComprobantes = new JTable();

    private final List<DetalleComprobante> detallesPendientes = new ArrayList<>();

    public PanelComprobantes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tablaDetalle.setModel(new DefaultTableModel(
                new String[]{"Linea", "Codigo", "Descripcion", "Cant.", "Precio Unit.", "IVA %", "Subtotal", "Total c/IVA"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });

        tablaComprobantes.setModel(new DefaultTableModel(
                new String[]{"Numero", "Tipo", "Proveedor", "Items", "Neto", "Total", "Saldo", "Estado"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });

        comboOC.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value == null ? "-- Sin OC (compra directa) --" : value.toString());
            if (isSelected) { lbl.setOpaque(true); lbl.setBackground(list.getSelectionBackground()); }
            return lbl;
        });

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        superior.add(new JLabel("Proveedor:")); superior.add(comboProveedores);
        superior.add(new JLabel("Tipo:"));      superior.add(tipoComprobante);
        superior.add(new JLabel("OC:"));        superior.add(comboOC);

        JButton quitar    = new JButton("Quitar linea seleccionada");
        JButton registrar = new JButton("Registrar comprobante");
        registrar.setFont(registrar.getFont().deriveFont(Font.BOLD));
        JButton limpiar = new JButton("Limpiar");

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        acciones.add(quitar); acciones.add(registrar); acciones.add(limpiar); acciones.add(lblResumen);

        comboProveedores.addActionListener(e -> cargarOCsProveedor());
        comboOC.addActionListener(e -> cargarItemsDeOC());
        quitar.addActionListener(e -> quitarLinea());
        registrar.addActionListener(e -> registrarComprobante());
        limpiar.addActionListener(e -> limpiarDetalle());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaDetalle), new JScrollPane(tablaComprobantes));
        split.setResizeWeight(0.5);

        add(superior, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);

        cargarDatos();
    }

    public void cargarDatos() {
        comboProveedores.removeAllItems();
        for (var p : ctrl.getProveedores())
            comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
        cargarOCsProveedor();

        DefaultTableModel model = (DefaultTableModel) tablaComprobantes.getModel();
        model.setRowCount(0);
        for (Comprobante c : ctrl.listar()) {
            model.addRow(new Object[]{
                    c.getNumero(), c.getTipo(), c.getProveedor().getRazonSocial(),
                    c.getDetalles().size(), UiUtil.formatearMoneda(c.getImporteNeto()),
                    UiUtil.formatearMoneda(c.getImporteTotal()),
                    UiUtil.formatearMoneda(c.getSaldoPendiente()), c.getEstado()
            });
        }
    }

    private void cargarOCsProveedor() {
        comboOC.removeAllItems();
        detallesPendientes.clear();
        try {
            String cuit = obtenerCuitSeleccionado();
            comboOC.addItem(null);
            for (OrdenCompra oc : ctrl.listarOCsPorProveedor(cuit))
                comboOC.addItem(oc);
        } catch (Exception ignored) {}
        refrescarTablaDetalle();
    }

    private void cargarItemsDeOC() {
        OrdenCompra oc = (OrdenCompra) comboOC.getSelectedItem();
        detallesPendientes.clear();
        if (oc != null) {
            int linea = 1;
            for (DetalleOC doc : oc.getDetalles()) {
                detallesPendientes.add(new DetalleComprobante(
                        linea++,
                        doc.getProducto(),
                        doc.getCantidad(),
                        doc.getPrecioUnitario(),
                        doc.getProducto().getTipoIVA().getPorcentaje()
                ));
            }
        }
        refrescarTablaDetalle();
    }

    private void quitarLinea() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila < 0) { UiUtil.mostrarError(this, "Seleccione una linea para quitar."); return; }
        detallesPendientes.remove(fila);
        renumerarLineas();
        refrescarTablaDetalle();
    }

    private void renumerarLineas() {
        List<DetalleComprobante> nuevos = new ArrayList<>();
        int n = 1;
        for (DetalleComprobante d : detallesPendientes)
            nuevos.add(new DetalleComprobante(n++, d.getProducto(), d.getCantidad(),
                    d.getPrecioUnitario(), d.getAlicuotaIVA()));
        detallesPendientes.clear();
        detallesPendientes.addAll(nuevos);
    }

    private void registrarComprobante() {
        if (detallesPendientes.isEmpty()) {
            UiUtil.mostrarError(this, "Seleccione una OC para cargar sus items."); return;
        }
        CarritoUtil.ResumenCarrito resumen = CarritoUtil.resumenComprobante(detallesPendientes);
        if (!UiUtil.confirmar(this, resumen.formatearComprobante() + "\n\nRegistrar comprobante?"))
            return;
        try {
            String cuit = obtenerCuitSeleccionado();
            TipoComprobante tipo = (TipoComprobante) tipoComprobante.getSelectedItem();
            List<String> ocs = new ArrayList<>();
            OrdenCompra ocSeleccionada = (OrdenCompra) comboOC.getSelectedItem();
            if (ocSeleccionada != null) ocs.add(ocSeleccionada.getNumero());

            List<DetalleComprobante> copia = new ArrayList<>(detallesPendientes);
            Usuario supervisor = null;

            boolean esFactura = tipo == TipoComprobante.FACTURA_A
                    || tipo == TipoComprobante.FACTURA_B || tipo == TipoComprobante.FACTURA_C;

            if (esFactura && (ocs.isEmpty() || ctrl.requiereSupervisor(cuit, copia, ocs))) {
                String motivo = ocs.isEmpty() ? "Compra directa sin OC" : "Desvio de precios o impuestos respecto a la OC";
                supervisor = solicitarSupervisor(motivo);
                if (supervisor == null) return;
            }

            Comprobante comp = ctrl.registrar(cuit, tipo, copia, ocs, supervisor, "Registro desde GUI");
            UiUtil.mostrarInfo(this, String.format(
                    "%s registrado: %s\n%d item(s) | Total: %s",
                    comp.getTipo(), comp.getNumero(),
                    comp.getDetalles().size(), UiUtil.formatearMoneda(comp.getImporteTotal())
            ));
            limpiarDetalle();
            cargarDatos();
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private Usuario solicitarSupervisor(String motivo) {
        if (!UiUtil.confirmar(this, motivo + "\n¿Desea continuar con autorizacion de supervisor?")) return null;
        List<Usuario> supervisores = ctrl.listarSupervisores();
        if (supervisores.isEmpty()) { UiUtil.mostrarError(this, "No hay supervisores registrados."); return null; }
        return (Usuario) JOptionPane.showInputDialog(
                this, "Seleccione supervisor:", "Autorizacion",
                JOptionPane.QUESTION_MESSAGE, null, supervisores.toArray(), supervisores.get(0));
    }

    private String obtenerCuitSeleccionado() {
        String item = (String) comboProveedores.getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Seleccione un proveedor.");
        return item.split(" - ")[0];
    }

    private void limpiarDetalle() { detallesPendientes.clear(); refrescarTablaDetalle(); }

    private void refrescarTablaDetalle() {
        DefaultTableModel model = (DefaultTableModel) tablaDetalle.getModel();
        model.setRowCount(0);
        for (DetalleComprobante d : detallesPendientes) {
            model.addRow(new Object[]{
                    d.getNroLinea(), d.getProducto().getCodigoInterno(), d.getProducto().getDescripcion(),
                    d.getCantidad(), UiUtil.formatearMoneda(d.getPrecioUnitario()),
                    d.getAlicuotaIVA() + "%", UiUtil.formatearMoneda(d.getSubtotal()),
                    UiUtil.formatearMoneda(d.getTotalConIVA())
            });
        }
        if (detallesPendientes.isEmpty()) {
            lblResumen.setText("Sin items.");
        } else {
            CarritoUtil.ResumenCarrito resumen = CarritoUtil.resumenComprobante(detallesPendientes);
            lblResumen.setText(resumen.formatearComprobante());
        }
    }
}

package farmared.vistas.paneles;

import farmared.modelo.enums.EstadoOrdenCompra;
import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.dto.ProveedorDTO;
import farmared.dto.ComprobanteDTO;
import farmared.dto.UsuarioDTO;
import farmared.vistas.observador.NotificadorSistema;
import farmared.vistas.observador.ObservadorSistema;
import farmared.controladores.AppContext;
import farmared.controladores.FacturaController;
import farmared.vistas.util.CarritoUtil;
import farmared.vistas.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelComprobantes extends JPanel implements ObservadorSistema {

    private final FacturaController controlador = AppContext.getInstancia().getFacturaCtrl();


    private final JComboBox<String>        comboProveedores = new JComboBox<>();
    private final JComboBox<TipoComprobante> tipoComprobante = new JComboBox<>(TipoComprobante.values());
    private final JComboBox<OrdenCompra>   comboOC          = new JComboBox<>();
    private final JLabel lblCarrito = new JLabel("Carrito: 0 item(s) | Total: $0.00");
    private final JTable tablaDetalle = new JTable();
    private final JTable tablaComprobantes = new JTable();

    private final List<ComprobanteDTO.DetalleComprobanteDTO> detallesPendientes = new ArrayList<>();
    private boolean actualizandoCombos = false;

    public PanelComprobantes() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tablaDetalle.setModel(new DefaultTableModel(
                new String[]{"Linea", "Producto", "Descripcion", "Cant.", "Precio", "IVA", "Subtotal", "Total c/IVA"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        tablaComprobantes.setModel(new DefaultTableModel(
                new String[]{"Numero", "Tipo", "Proveedor", "Items", "Neto", "Total", "Saldo", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel superior = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        int col = 0;
        superior.add(new JLabel("Proveedor:"), gbc(col, 0));
        gbc.gridx = ++col; superior.add(comboProveedores, gbc);
        superior.add(new JLabel("Tipo:"), gbc(++col, 0));
        gbc.gridx = ++col; superior.add(tipoComprobante, gbc);
        superior.add(new JLabel("OC:"), gbc(++col, 0));
        gbc.gridx = ++col; superior.add(comboOC, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton registrar  = new JButton("Registrar comprobante");
        acciones.add(registrar);
        acciones.add(lblCarrito);

        comboOC.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean hasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
                if (value == null) setText("(Sin OC)");
                else {
                    OrdenCompra oc = (OrdenCompra) value;
                    setText(oc.getNumero() + "  |  " + UiUtil.formatearMoneda(oc.getImporteTotal())
                            + "  [" + oc.getEstado() + "]");
                }
                return this;
            }
        });

        comboOC.addActionListener(e -> cargarDesdeOC());
        comboProveedores.addActionListener(e -> cargarProductosProveedor());
        registrar.addActionListener(e -> registrarComprobante());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaDetalle),
                new JScrollPane(tablaComprobantes));
        split.setResizeWeight(0.45);

        add(superior, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);

        NotificadorSistema.getInstancia().suscribir(this);
        cargarDatos();
    }

    private GridBagConstraints gbc(int x, int y) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = x;
        g.gridy = y;
        g.insets = new Insets(4, 4, 4, 4);
        g.anchor = GridBagConstraints.WEST;
        return g;
    }

    public void cargarDatos() {
        comboProveedores.removeAllItems();
        for (ProveedorDTO p : controlador.getProveedoresDTO()) {
            comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
        }
        cargarProductosProveedor();

        DefaultTableModel model = (DefaultTableModel) tablaComprobantes.getModel();
        model.setRowCount(0);
        for (ComprobanteDTO c : controlador.listarDTO()) {
            model.addRow(new Object[]{
                    c.getNumero(),
                    c.getTipo(),
                    c.getRazonSocialProveedor(),
                    c.getDetalles().size(),
                    UiUtil.formatearMoneda(c.getImporteNeto()),
                    UiUtil.formatearMoneda(c.getImporteTotal()),
                    UiUtil.formatearMoneda(c.getSaldoPendiente()),
                    c.getEstado()
            });
        }
    }

    private void cargarDesdeOC() {
        if (actualizandoCombos) return;
        OrdenCompra oc = (OrdenCompra) comboOC.getSelectedItem();
        detallesPendientes.clear();
        if (oc != null) {
            int linea = 1;
            for (DetalleOC d : oc.getDetalles()) {
                double alicuota = d.getProducto().getTipoIVA().getPorcentaje();
                double subtotal = Math.round(d.getCantidad() * d.getPrecioUnitario() * 100.0) / 100.0;
                double iva      = Math.round(subtotal * (alicuota / 100.0) * 100.0) / 100.0;
                detallesPendientes.add(new ComprobanteDTO.DetalleComprobanteDTO(
                        linea++, d.getProducto().getCodigoInterno(), d.getProducto().getDescripcion(),
                        d.getCantidad(), d.getPrecioUnitario(), alicuota, subtotal, iva
                ));
            }
        }
        refrescarCarrito();
    }

    private void cargarProductosProveedor() {
        actualizandoCombos = true;
        comboOC.removeAllItems();
        comboOC.addItem(null);
        try {
            String cuit = obtenerCuitSeleccionado();
            for (OrdenCompra oc : controlador.listarOCsPorProveedor(cuit)) {
                if (oc.getEstado() != EstadoOrdenCompra.ANULADA
                        && oc.getEstado() != EstadoOrdenCompra.FACTURADA) {
                    comboOC.addItem(oc);
                }
            }
        } catch (Exception ignored) {
        }
        actualizandoCombos = false;
    }

    private String obtenerCuitSeleccionado() {
        String item = (String) comboProveedores.getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Seleccione un proveedor.");
        return item.split(" - ")[0];
    }

    private void registrarComprobante() {
        if (detallesPendientes.isEmpty()) {
            UiUtil.mostrarError(this, "El carrito esta vacio. Agregue al menos un item.");
            return;
        }

        CarritoUtil.ResumenCarrito resumen = CarritoUtil.resumenComprobanteDTO(detallesPendientes);
        if (!UiUtil.confirmar(this,
                resumen.formatearComprobante() + "\n\nRegistrar comprobante con estos datos?")) {
            return;
        }

        try {
            String cuit = obtenerCuitSeleccionado();
            TipoComprobante tipo = (TipoComprobante) tipoComprobante.getSelectedItem();
            List<String> ocs = new ArrayList<>();
            OrdenCompra ocSeleccionada = (OrdenCompra) comboOC.getSelectedItem();
            if (ocSeleccionada != null) ocs.add(ocSeleccionada.getNumero());

            List<ComprobanteDTO.DetalleComprobanteDTO> copia = new ArrayList<>(detallesPendientes);
            String supervisorUsername = null;

            boolean esFactura = tipo == TipoComprobante.FACTURA_A
                    || tipo == TipoComprobante.FACTURA_B || tipo == TipoComprobante.FACTURA_C;

            if (esFactura && (ocs.isEmpty() || controlador.requiereSupervisorDTO(cuit, copia, ocs))) {
                String motivo = ocs.isEmpty() ? "Comprobante sin OC"
                        : "Desvio de precios o impuestos respecto a la OC";
                UsuarioDTO supervisor = solicitarSupervisor(motivo);
                if (supervisor == null) return;
                supervisorUsername = supervisor.getUsername();
            }

            ComprobanteDTO comprobante = controlador.registrarDTO(cuit, tipo, copia, ocs, supervisorUsername,
                    "Registro desde interfaz grafica");

            UiUtil.mostrarInfo(this, String.format(
                    "%s registrado: %s\n%d item(s) | Total: %s",
                    comprobante.getTipo(), comprobante.getNumero(),
                    comprobante.getDetalles().size(),
                    UiUtil.formatearMoneda(comprobante.getImporteTotal())
            ));
            limpiarCarrito();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private UsuarioDTO solicitarSupervisor(String motivo) {
        if (!UiUtil.confirmar(this, motivo + "\nDesea continuar con autorizacion?")) return null;
        List<UsuarioDTO> supervisores = controlador.listarSupervisoresDTO();
        if (supervisores.isEmpty()) {
            UiUtil.mostrarError(this, "No hay supervisores registrados.");
            return null;
        }
        return (UsuarioDTO) JOptionPane.showInputDialog(
                this, "Seleccione supervisor:", "Autorizacion",
                JOptionPane.QUESTION_MESSAGE, null,
                supervisores.toArray(), supervisores.get(0)
        );
    }

    private void limpiarCarrito() {
        detallesPendientes.clear();
        refrescarCarrito();
    }

    private void refrescarCarrito() {
        DefaultTableModel model = (DefaultTableModel) tablaDetalle.getModel();
        model.setRowCount(0);
        for (ComprobanteDTO.DetalleComprobanteDTO d : detallesPendientes) {
            double totalConIva = Math.round((d.getSubtotal() + d.getImporteIVA()) * 100.0) / 100.0;
            model.addRow(new Object[]{
                    d.getNroLinea(),
                    d.getCodigoProducto(),
                    d.getDescripcionProducto(),
                    d.getCantidad(),
                    UiUtil.formatearMoneda(d.getPrecioUnitario()),
                    d.getAlicuotaIVA() + "%",
                    UiUtil.formatearMoneda(d.getSubtotal()),
                    UiUtil.formatearMoneda(totalConIva)
            });
        }
        CarritoUtil.ResumenCarrito resumen = CarritoUtil.resumenComprobanteDTO(detallesPendientes);
        lblCarrito.setText(resumen.formatearComprobante());
    }

    @Override
    public void actualizar(String evento) {
        if ("COMPROBANTE_REGISTRADO".equals(evento) || "OC_EMITIDA".equals(evento)
                || "PROVEEDOR_REGISTRADO".equals(evento)) {
            cargarDatos();
        }
    }
}

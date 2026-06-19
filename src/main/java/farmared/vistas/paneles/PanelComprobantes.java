package farmared.vistas.paneles;

import farmared.modelo.enums.EstadoOrdenCompra;
import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m8_usuarios.Usuario;
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
    private final JComboBox<Producto>      comboProductos   = new JComboBox<>();
    private final JComboBox<OrdenCompra>   comboOC          = new JComboBox<>();
    private final JTextField cantidad = new JTextField(6);
    private final JTextField precioUnitario = new JTextField(8);
    private final JLabel lblCarrito = new JLabel("Carrito: 0 item(s) | Total: $0.00");
    private final JTable tablaDetalle = new JTable();
    private final JTable tablaComprobantes = new JTable();

    private final List<DetalleComprobante> detallesPendientes = new ArrayList<>();
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

        col = 0;
        gbc.gridy = 1;
        superior.add(new JLabel("Producto:"), gbc(col, 1));
        gbc.gridx = ++col; gbc.gridwidth = 2; superior.add(comboProductos, gbc);
        gbc.gridwidth = 1;
        superior.add(new JLabel("Cant:"), gbc(++col, 1));
        gbc.gridx = ++col; superior.add(cantidad, gbc);
        superior.add(new JLabel("Precio:"), gbc(++col, 1));
        gbc.gridx = ++col; superior.add(precioUnitario, gbc);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton agregar    = new JButton("Agregar al carrito");
        JButton quitar     = new JButton("Quitar linea");
        JButton verificar  = new JButton("Verificar vs OC");
        JButton registrar  = new JButton("Registrar comprobante");
        JButton limpiar    = new JButton("Vaciar carrito");
        acciones.add(agregar);
        acciones.add(quitar);
        acciones.add(verificar);
        acciones.add(registrar);
        acciones.add(limpiar);
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

        verificar.addActionListener(e -> verificarVsOC());
        comboOC.addActionListener(e -> cargarDesdeOC());
        comboProveedores.addActionListener(e -> cargarProductosProveedor());
        comboProductos.addActionListener(e -> autocompletarPrecio());
        agregar.addActionListener(e -> agregarAlCarrito());
        quitar.addActionListener(e -> quitarLinea());
        registrar.addActionListener(e -> registrarComprobante());
        limpiar.addActionListener(e -> limpiarCarrito());

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
        for (var p : controlador.getProveedores()) {
            comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
        }
        cargarProductosProveedor();

        DefaultTableModel model = (DefaultTableModel) tablaComprobantes.getModel();
        model.setRowCount(0);
        for (Comprobante c : controlador.listar()) {
            model.addRow(new Object[]{
                    c.getNumero(),
                    c.getTipo(),
                    c.getProveedor().getRazonSocial(),
                    c.getDetalles().size(),
                    UiUtil.formatearMoneda(c.getImporteNeto()),
                    UiUtil.formatearMoneda(c.getImporteTotal()),
                    UiUtil.formatearMoneda(c.getSaldoPendiente()),
                    c.getEstado()
            });
        }
    }

    private void verificarVsOC() {
        OrdenCompra oc = (OrdenCompra) comboOC.getSelectedItem();
        if (oc == null) { UiUtil.mostrarError(this, "Seleccione una OC para comparar."); return; }
        if (detallesPendientes.isEmpty()) { UiUtil.mostrarError(this, "El carrito esta vacio."); return; }

        String[] cols = {"Codigo", "Descripcion", "Cant. OC", "Cant. Comp.", "Precio OC", "Precio Comp.", "Dif. Precio", "OK"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        boolean todoOk = true;
        for (DetalleComprobante dc : detallesPendientes) {
            String codigo = dc.getProducto().getCodigoInterno();
            DetalleOC docOC = null;
            for (DetalleOC d : oc.getDetalles()) {
                if (d.getProducto().getCodigoInterno().equals(codigo)) { docOC = d; break; }
            }
            if (docOC == null) {
                model.addRow(new Object[]{
                        codigo, dc.getProducto().getDescripcion(),
                        "-", dc.getCantidad(),
                        "-", UiUtil.formatearMoneda(dc.getPrecioUnitario()),
                        "N/A (no esta en OC)", "NO"
                });
                todoOk = false;
            } else {
                double difPct = Math.abs(dc.getPrecioUnitario() - docOC.getPrecioUnitario())
                        / docOC.getPrecioUnitario() * 100;
                boolean precioOk = difPct <= 0.01;
                boolean cantOk   = Math.abs(dc.getCantidad() - docOC.getCantidad()) < 0.001;
                boolean lineaOk  = precioOk && cantOk;
                todoOk = todoOk && lineaOk;
                model.addRow(new Object[]{
                        codigo, dc.getProducto().getDescripcion(),
                        docOC.getCantidad(), dc.getCantidad(),
                        UiUtil.formatearMoneda(docOC.getPrecioUnitario()),
                        UiUtil.formatearMoneda(dc.getPrecioUnitario()),
                        precioOk ? "0%" : String.format("%.2f%%", difPct),
                        lineaOk ? "Si" : "NO"
                });
            }
        }

        JTable tablaComp = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                String ok = (String) getValueAt(row, 7);
                c.setBackground("Si".equals(ok)
                        ? new Color(198, 239, 206)
                        : new Color(255, 199, 206));
                c.setForeground(Color.BLACK);
                return c;
            }
        };

        JScrollPane scroll = new JScrollPane(tablaComp);
        scroll.setPreferredSize(new Dimension(680, 200));
        String titulo = todoOk ? "Todo coincide con la OC" : "HAY DIFERENCIAS con la OC";
        JOptionPane.showMessageDialog(this, scroll, titulo,
                todoOk ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }

    private void cargarDesdeOC() {
        if (actualizandoCombos) return;
        OrdenCompra oc = (OrdenCompra) comboOC.getSelectedItem();

        actualizandoCombos = true;
        comboProductos.removeAllItems();
        if (oc == null) {
            try {
                for (Producto p : AppContext.getInstancia().getOrdenCompraCtrl()
                        .listarProductosPorProveedor(obtenerCuitSeleccionado())) {
                    comboProductos.addItem(p);
                }
            } catch (Exception ignored) {}
        } else {
            for (DetalleOC d : oc.getDetalles()) comboProductos.addItem(d.getProducto());
            detallesPendientes.clear();
            int linea = 1;
            for (DetalleOC d : oc.getDetalles()) {
                detallesPendientes.add(new DetalleComprobante(
                        linea++, d.getProducto(), d.getCantidad(),
                        d.getPrecioUnitario(), d.getProducto().getTipoIVA().getPorcentaje()
                ));
            }
            refrescarCarrito();
        }
        actualizandoCombos = false;
        autocompletarPrecio();
    }

    private void cargarProductosProveedor() {
        actualizandoCombos = true;
        comboProductos.removeAllItems();
        comboOC.removeAllItems();
        comboOC.addItem(null);
        try {
            String cuit = obtenerCuitSeleccionado();
            for (Producto p : AppContext.getInstancia().getOrdenCompraCtrl().listarProductosPorProveedor(cuit)) {
                comboProductos.addItem(p);
            }
            for (OrdenCompra oc : controlador.listarOCsPorProveedor(cuit)) {
                if (oc.getEstado() != EstadoOrdenCompra.ANULADA
                        && oc.getEstado() != EstadoOrdenCompra.FACTURADA) {
                    comboOC.addItem(oc);
                }
            }
            autocompletarPrecio();
        } catch (Exception ignored) {
        }
        actualizandoCombos = false;
    }

    private void autocompletarPrecio() {
        Producto prod = (Producto) comboProductos.getSelectedItem();
        if (prod == null) return;
        try {
            double precio = AppContext.getInstancia().getOrdenCompraCtrl().obtenerPrecioVigente(prod.getCodigoInterno(), obtenerCuitSeleccionado());
            if (precio > 0) precioUnitario.setText(String.valueOf(precio));
        } catch (Exception ignored) {
        }
    }

    private String obtenerCuitSeleccionado() {
        String item = (String) comboProveedores.getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Seleccione un proveedor.");
        return item.split(" - ")[0];
    }

    private void agregarAlCarrito() {
        try {
            Producto producto = (Producto) comboProductos.getSelectedItem();
            if (producto == null) throw new IllegalArgumentException("Seleccione un producto del proveedor.");

            double cant = UiUtil.parsearDouble(cantidad.getText(), "Cantidad");
            if (cant <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");

            double precio;
            if (precioUnitario.getText().trim().isEmpty()) {
                precio = AppContext.getInstancia().getOrdenCompraCtrl().obtenerPrecioVigente(producto.getCodigoInterno(), obtenerCuitSeleccionado());
                if (precio <= 0) throw new IllegalArgumentException("Sin precio vigente para este proveedor.");
            } else {
                precio = UiUtil.parsearDouble(precioUnitario.getText(), "Precio");
            }

            int linea = detallesPendientes.size() + 1;
            DetalleComprobante det = new DetalleComprobante(
                    linea, producto, cant, precio, producto.getTipoIVA().getPorcentaje()
            );
            detallesPendientes.add(det);
            cantidad.setText("");
            refrescarCarrito();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void quitarLinea() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila < 0) {
            UiUtil.mostrarError(this, "Seleccione una linea del carrito.");
            return;
        }
        detallesPendientes.remove(fila);
        renumerarLineas();
        refrescarCarrito();
    }

    private void renumerarLineas() {
        List<DetalleComprobante> nuevos = new ArrayList<>();
        int n = 1;
        for (DetalleComprobante d : detallesPendientes) {
            nuevos.add(new DetalleComprobante(n++, d.getProducto(), d.getCantidad(),
                    d.getPrecioUnitario(), d.getAlicuotaIVA()));
        }
        detallesPendientes.clear();
        detallesPendientes.addAll(nuevos);
    }

    private void registrarComprobante() {
        if (detallesPendientes.isEmpty()) {
            UiUtil.mostrarError(this, "El carrito esta vacio. Agregue al menos un item.");
            return;
        }

        CarritoUtil.ResumenCarrito resumen = CarritoUtil.resumenComprobante(detallesPendientes);
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

            List<DetalleComprobante> copia = new ArrayList<>(detallesPendientes);
            Usuario supervisor = null;

            boolean esFactura = tipo == TipoComprobante.FACTURA_A
                    || tipo == TipoComprobante.FACTURA_B || tipo == TipoComprobante.FACTURA_C;

            if (esFactura && (ocs.isEmpty() || controlador.requiereSupervisor(cuit, copia, ocs))) {
                String motivo = ocs.isEmpty() ? "Comprobante sin OC"
                        : "Desvio de precios o impuestos respecto a la OC";
                supervisor = solicitarSupervisor(motivo);
                if (supervisor == null) return;
            }

            Comprobante comprobante = controlador.registrar(cuit, tipo, copia, ocs, supervisor,
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

    private Usuario solicitarSupervisor(String motivo) {
        if (!UiUtil.confirmar(this, motivo + "\nDesea continuar con autorizacion?")) return null;
        List<Usuario> supervisores = controlador.listarSupervisores();
        if (supervisores.isEmpty()) {
            UiUtil.mostrarError(this, "No hay supervisores registrados.");
            return null;
        }
        return (Usuario) JOptionPane.showInputDialog(
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
        for (DetalleComprobante d : detallesPendientes) {
            model.addRow(new Object[]{
                    d.getNroLinea(),
                    d.getProducto().getCodigoInterno(),
                    d.getProducto().getDescripcion(),
                    d.getCantidad(),
                    UiUtil.formatearMoneda(d.getPrecioUnitario()),
                    d.getAlicuotaIVA() + "%",
                    UiUtil.formatearMoneda(d.getSubtotal()),
                    UiUtil.formatearMoneda(d.getTotalConIVA())
            });
        }
        CarritoUtil.ResumenCarrito resumen = CarritoUtil.resumenComprobante(detallesPendientes);
        lblCarrito.setText(resumen.formatearComprobante());
    }

    @Override
    public void actualizar(String evento) {
        if ("COMPROBANTE_REGISTRADO".equals(evento) || "OC_EMITIDA".equals(evento)
                || "PROVEEDOR_REGISTRADO".equals(evento)) {
            cargarDatos();
            cargarProductosProveedor();
        }
    }
}

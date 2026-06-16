package farmared.vistas.paneles;

import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m2_productos.Producto;
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

<<<<<<< HEAD
    private final FacturaController controlador = AppContext.getInstancia().getFacturaCtrl();

=======
    private final ComprobanteController controlador = new ComprobanteController();
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JComboBox<TipoComprobante> tipoComprobante = new JComboBox<>(TipoComprobante.values());
    private final JComboBox<Producto> comboProductos = new JComboBox<>();
    private final JTextField nroOC = new JTextField(12);
    private final JTextField cantidad = new JTextField(6);
    private final JTextField precioUnitario = new JTextField(8);
    private final JLabel lblCarrito = new JLabel("Carrito: 0 item(s) | Total: $0.00");
    private final JTable tablaDetalle = new JTable();
    private final JTable tablaComprobantes = new JTable();

    private final List<DetalleComprobante> detallesPendientes = new ArrayList<>();

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
        superior.add(new JLabel("OC (opc.):"), gbc(++col, 0));
        gbc.gridx = ++col; superior.add(nroOC, gbc);

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
        JButton agregar = new JButton("Agregar al carrito");
        JButton quitar = new JButton("Quitar linea");
        JButton registrar = new JButton("Registrar comprobante");
        JButton limpiar = new JButton("Vaciar carrito");
        acciones.add(agregar);
        acciones.add(quitar);
        acciones.add(registrar);
        acciones.add(limpiar);
        acciones.add(lblCarrito);

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
<<<<<<< HEAD
        for (var p : controlador.getProveedores()) {
=======
        for (var p : AppContext.getInstancia().getProveedorCtrl().listarProveedores()) {
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
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

    private void cargarProductosProveedor() {
        comboProductos.removeAllItems();
        try {
            String cuit = obtenerCuitSeleccionado();
<<<<<<< HEAD
            for (Producto p : AppContext.getInstancia().getOrdenCompraCtrl().listarProductosPorProveedor(cuit)) {
=======
            for (Producto p : AppContext.getInstancia().getProductoCtrl().listarProductosPorProveedor(cuit)) {
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
                comboProductos.addItem(p);
            }
            autocompletarPrecio();
        } catch (Exception ignored) {
        }
    }

    private void autocompletarPrecio() {
        Producto prod = (Producto) comboProductos.getSelectedItem();
        if (prod == null) return;
        try {
<<<<<<< HEAD
            double precio = AppContext.getInstancia().getOrdenCompraCtrl().obtenerPrecioVigente(prod.getCodigoInterno(), obtenerCuitSeleccionado());
=======
            double precio = AppContext.getInstancia().getProductoCtrl().obtenerPrecioVigente(prod.getCodigoInterno(), obtenerCuitSeleccionado());
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
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
<<<<<<< HEAD
                precio = AppContext.getInstancia().getOrdenCompraCtrl().obtenerPrecioVigente(producto.getCodigoInterno(), obtenerCuitSeleccionado());
=======
                precio = AppContext.getInstancia().getProductoCtrl().obtenerPrecioVigente(producto.getCodigoInterno(), obtenerCuitSeleccionado());
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
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
            if (!nroOC.getText().trim().isEmpty()) ocs.add(nroOC.getText().trim());

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

package farmared.GUI.paneles;

import farmared.controladores.OrdenCompraController;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.GUI.AppContext;
import farmared.GUI.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PanelOrdenesCompra extends JPanel {

    private final OrdenCompraController ctrl = AppContext.getInstancia().getOrdenCompraCtrl();

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JComboBox<Producto> comboProductos = new JComboBox<>();
    private final JTable tablaDetalle = new JTable();
    private final JTable tablaOC = new JTable();
    private final JTextField cantidad = new JTextField(6);
    private final JTextField precioUnitario = new JTextField(8);
    private final JLabel lblCarrito = new JLabel("Carrito: 0 item(s) | Total: $0.00");

    private final List<ItemCarritoOC> carrito = new ArrayList<>();
    private String cuitProveedorActual;

    private static class ItemCarritoOC {
        final Producto producto;
        final double cantidad;
        final double precioUnitario;

        ItemCarritoOC(Producto producto, double cantidad, double precioUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        double getSubtotal() { return Math.round(cantidad * precioUnitario * 100.0) / 100.0; }
    }

    public PanelOrdenesCompra() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tablaDetalle.setModel(new DefaultTableModel(
                new String[]{"Linea", "Codigo", "Descripcion", "Cantidad", "Precio Unit.", "Subtotal"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });

        tablaOC.setModel(new DefaultTableModel(
                new String[]{"Numero", "Proveedor", "Items", "Fecha", "Total", "Estado"}, 0
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        });

        JButton agregarItem = new JButton("Agregar al carrito");
        JButton quitarItem = new JButton("Quitar linea");
        JButton vaciar = new JButton("Vaciar carrito");
        JButton emitir = new JButton("Emitir OC");
        emitir.setFont(emitir.getFont().deriveFont(java.awt.Font.BOLD));

        precioUnitario.setToolTipText("Se autocompleta si hay precio acordado; de lo contrario ingreselo manualmente.");

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        fila1.add(new JLabel("Proveedor:")); fila1.add(comboProveedores);
        fila1.add(new JLabel("Producto:"));  fila1.add(comboProductos);
        fila1.add(new JLabel("Cant:"));      fila1.add(cantidad);
        fila1.add(new JLabel("Precio:"));    fila1.add(precioUnitario);
        fila1.add(agregarItem); fila1.add(quitarItem);

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        fila2.add(vaciar); fila2.add(emitir); fila2.add(lblCarrito);

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(fila1, BorderLayout.NORTH);
        superior.add(fila2, BorderLayout.SOUTH);

        comboProveedores.addActionListener(e -> cambiarProveedor());
        comboProductos.addActionListener(e -> autocompletarPrecio());
        agregarItem.addActionListener(e -> agregarItem());
        quitarItem.addActionListener(e -> quitarItem());
        vaciar.addActionListener(e -> vaciarCarrito());
        emitir.addActionListener(e -> emitirOC());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaDetalle), new JScrollPane(tablaOC));
        split.setResizeWeight(0.45);

        add(superior, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
        cargarDatos();
    }

    public void cargarDatos() {
        comboProveedores.removeAllItems();
        for (var p : ctrl.getProveedores())
            comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
        cambiarProveedor();

        DefaultTableModel model = (DefaultTableModel) tablaOC.getModel();
        model.setRowCount(0);
        for (OrdenCompra oc : ctrl.getOrdenesCompra()) {
            model.addRow(new Object[]{
                    oc.getNumero(), oc.getProveedor().getRazonSocial(), oc.getDetalles().size(),
                    UiUtil.formatearFecha(oc.getFechaEmision()),
                    UiUtil.formatearMoneda(oc.getImporteTotal()), oc.getEstado()
            });
        }
    }

    private void cambiarProveedor() {
        try {
            String nuevoCuit = obtenerCuitSeleccionado();
            if (cuitProveedorActual != null && !nuevoCuit.equals(cuitProveedorActual) && !carrito.isEmpty()) {
                if (!UiUtil.confirmar(this, "Cambiar de proveedor vaciara el carrito. Continuar?")) {
                    comboProveedores.setSelectedItem(buscarItemCombo(cuitProveedorActual));
                    return;
                }
                carrito.clear(); refrescarCarrito();
            }
            cuitProveedorActual = nuevoCuit;
            comboProductos.removeAllItems();
            for (Producto p : ctrl.listarProductosPorProveedor(nuevoCuit)) comboProductos.addItem(p);
        } catch (Exception ignored) {}
    }

    private String buscarItemCombo(String cuit) {
        for (int i = 0; i < comboProveedores.getItemCount(); i++) {
            String item = comboProveedores.getItemAt(i);
            if (item != null && item.startsWith(cuit)) return item;
        }
        return null;
    }

    private String obtenerCuitSeleccionado() {
        String item = (String) comboProveedores.getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Seleccione un proveedor.");
        return item.split(" - ")[0];
    }

    private void autocompletarPrecio() {
        Producto prod = (Producto) comboProductos.getSelectedItem();
        if (prod == null) { precioUnitario.setText(""); return; }
        try {
            double precio = ctrl.obtenerPrecioVigente(prod.getCodigoInterno(), obtenerCuitSeleccionado());
            if (precio > 0) precioUnitario.setText(String.valueOf(precio));
            else precioUnitario.setText("");
        } catch (Exception ignored) { precioUnitario.setText(""); }
    }

    private void agregarItem() {
        try {
            Producto producto = (Producto) comboProductos.getSelectedItem();
            if (producto == null) throw new IllegalArgumentException("Seleccione un producto.");
            double cant = UiUtil.parsearDouble(cantidad.getText(), "Cantidad");
            if (cant <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
            String precioTxt = precioUnitario.getText().trim();
            if (precioTxt.isEmpty()) throw new IllegalArgumentException("Ingrese el precio unitario.");
            double precio = UiUtil.parsearDouble(precioTxt, "Precio");
            if (precio <= 0) throw new IllegalArgumentException("El precio debe ser mayor a cero.");
            carrito.add(new ItemCarritoOC(producto, cant, precio));
            cantidad.setText("");
            refrescarCarrito();
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void quitarItem() {
        int fila = tablaDetalle.getSelectedRow();
        if (fila < 0) { UiUtil.mostrarError(this, "Seleccione una linea del carrito."); return; }
        carrito.remove(fila); refrescarCarrito();
    }

    private void vaciarCarrito() { carrito.clear(); refrescarCarrito(); }

    private void emitirOC() {
        if (carrito.isEmpty()) { UiUtil.mostrarError(this, "Agregue al menos un item al carrito."); return; }
        double total = carrito.stream().mapToDouble(ItemCarritoOC::getSubtotal).sum();
        if (!UiUtil.confirmar(this, String.format(
                "Carrito: %d item(s) | Total: %s\n\nEmitir orden de compra?",
                carrito.size(), UiUtil.formatearMoneda(total)))) return;
        try {
            String cuit = obtenerCuitSeleccionado();
            OrdenCompra oc = ctrl.crearOrdenCompra(cuit);
            int linea = 1;
            for (ItemCarritoOC item : carrito)
                ctrl.agregarItemConPrecio(oc, item.producto.getCodigoInterno(), item.cantidad, linea++, item.precioUnitario);

            Usuario supervisor = null;
            oc.calcularTotal();
            if (!oc.validarTopeDeuda()) {
                supervisor = solicitarSupervisor("La OC supera el tope de deuda del proveedor.");
                if (supervisor == null) return;
            }

            ctrl.emitirOrdenCompra(oc, supervisor, "Autorizacion desde interfaz grafica");
            UiUtil.mostrarInfo(this, String.format(
                    "OC emitida: %s\nProveedor: %s\n%d item(s) | Total: %s",
                    oc.getNumero(), oc.getProveedor().getRazonSocial(),
                    oc.getDetalles().size(), UiUtil.formatearMoneda(oc.getImporteTotal())
            ));
            carrito.clear(); refrescarCarrito(); cargarDatos();
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private Usuario solicitarSupervisor(String motivo) {
        if (!UiUtil.confirmar(this, motivo + "\nDesea solicitar autorizacion de supervisor?")) return null;
        List<Usuario> supervisores = ctrl.listarSupervisores();
        if (supervisores.isEmpty()) { UiUtil.mostrarError(this, "No hay supervisores registrados."); return null; }
        return (Usuario) JOptionPane.showInputDialog(
                this, "Seleccione supervisor:", "Autorizacion",
                JOptionPane.QUESTION_MESSAGE, null, supervisores.toArray(), supervisores.get(0));
    }

    private void refrescarCarrito() {
        DefaultTableModel model = (DefaultTableModel) tablaDetalle.getModel();
        model.setRowCount(0);
        double total = 0; int linea = 1;
        for (ItemCarritoOC item : carrito) {
            model.addRow(new Object[]{
                    linea++, item.producto.getCodigoInterno(), item.producto.getDescripcion(),
                    item.cantidad, UiUtil.formatearMoneda(item.precioUnitario),
                    UiUtil.formatearMoneda(item.getSubtotal())
            });
            total += item.getSubtotal();
        }
        lblCarrito.setText(String.format("Carrito: %d item(s) | Total: %s",
                carrito.size(), UiUtil.formatearMoneda(total)));
    }
}

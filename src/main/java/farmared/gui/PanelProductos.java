package farmared.gui;

import farmared.controllers.OrdenCompraController;
import farmared.enums.TipoIVA;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;

public class PanelProductos extends JPanel {

    private final JTextField tfCodigo      = new JTextField(12);
    private final JTextField tfDescripcion = new JTextField(22);
    private final JTextField tfUnidad      = new JTextField(12);
    private final JComboBox<TipoIVA> cbIVA = new JComboBox<>(TipoIVA.values());
    private final JComboBox<String>  cbRubro;

    private final JComboBox<String> cbProveedorPrecio;
    private final JTextField tfPrecio = new JTextField("0.0", 10);

    private final DefaultTableModel modeloTabla = new DefaultTableModel(
        new String[]{"Código", "Descripción", "Unidad", "IVA", "Rubro", "Precios"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable tabla = new JTable(modeloTabla);
    private final JTextArea taLog = new JTextArea(3, 0);

    private Producto productoEnCurso = null;

    public PanelProductos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titulo = new JLabel("<html><b>M3 — Módulo de Productos y Servicios</b></html>");
        titulo.setFont(titulo.getFont().deriveFont(14f));
        add(titulo, BorderLayout.NORTH);

        cbRubro = new JComboBox<>();
        cbProveedorPrecio = new JComboBox<>();
        cargarRubros();
        cargarProveedores();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            buildFormPanel(), buildTablaPanel());
        split.setDividerLocation(360);
        add(split, BorderLayout.CENTER);

        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        cargarProductosDemoEnTabla();
    }

    private void cargarRubros() {
        cbRubro.removeAllItems();
        for (Rubro r : MainFrame.rubros) {
            cbRubro.addItem(r.getNombre());
        }
    }

    private void cargarProveedores() {
        cbProveedorPrecio.removeAllItems();
        for (Proveedor p : OrdenCompraController.getInstance().getProveedores()) {
            cbProveedorPrecio.addItem(p.getCuit() + " · " + p.getRazonSocial());
        }
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 6, 3, 6);
        g.anchor = GridBagConstraints.WEST;

        JPanel pDatos = new JPanel(new GridBagLayout());
        pDatos.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));
        int row = 0;
        addFila(pDatos, g, row++, "Código (*):    ", tfCodigo);
        addFila(pDatos, g, row++, "Descripción(*):  ", tfDescripcion);
        addFila(pDatos, g, row++, "Unidad medida:   ", tfUnidad);
        addFila(pDatos, g, row++, "Tipo IVA (*):    ", cbIVA);
        addFila(pDatos, g, row++, "Rubro (*):       ", cbRubro);

        g.gridy = row; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JButton btnNuevo = new JButton("1. Crear Producto");
        btnNuevo.addActionListener(this::crearProducto);
        pDatos.add(btnNuevo, g);

        JButton btnRefRubros = new JButton("↺ Refrescar Rubros");
        btnRefRubros.addActionListener(e -> { cargarRubros(); log("↺ Rubros actualizados."); });
        row++;
        g.gridy = row;
        pDatos.add(btnRefRubros, g);

        JPanel pPrecio = new JPanel(new GridBagLayout());
        pPrecio.setBorder(BorderFactory.createTitledBorder("Agregar Precio Acordado"));
        g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        row = 0;

        g.gridy = row; g.gridx = 0; pPrecio.add(new JLabel("Proveedor:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        pPrecio.add(cbProveedorPrecio, g);
        row++;

        g.gridy = row; g.gridx = 0; g.fill = GridBagConstraints.NONE;
        pPrecio.add(new JLabel("Precio unitario:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        pPrecio.add(tfPrecio, g);
        row++;

        g.gridy = row; g.gridx = 0; g.gridwidth = 2; g.fill = GridBagConstraints.HORIZONTAL;
        JPanel btnsPrecio = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        JButton btnAgrPrecio = new JButton("2. Agregar precio al producto");
        btnAgrPrecio.addActionListener(this::agregarPrecio);
        JButton btnRefProv = new JButton("↺ Refrescar proveedores");
        btnRefProv.addActionListener(e -> { cargarProveedores(); log("↺ Proveedores actualizados."); });
        btnsPrecio.add(btnAgrPrecio);
        btnsPrecio.add(btnRefProv);
        pPrecio.add(btnsPrecio, g);

        row++;
        g.gridy = row;
        JButton btnGuardar = new JButton("3. Guardar Producto en catálogo");
        btnGuardar.setFont(btnGuardar.getFont().deriveFont(Font.BOLD));
        btnGuardar.addActionListener(this::guardarProducto);
        pPrecio.add(btnGuardar, g);

        JPanel contenedor = new JPanel(new BorderLayout(4, 4));
        contenedor.add(pDatos,  BorderLayout.NORTH);
        contenedor.add(pPrecio, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel buildTablaPanel() {
        JPanel p = new JPanel(new BorderLayout(6, 6));
        p.setBorder(BorderFactory.createTitledBorder("Catálogo de Productos"));

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(22);
        tabla.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnDetalle = new JButton("Ver precios del producto");
        btnDetalle.addActionListener(this::verPrecios);
        botones.add(btnDetalle);
        p.add(botones, BorderLayout.SOUTH);

        return p;
    }

    private void addFila(JPanel p, GridBagConstraints g, int row, String label, JComponent campo) {
        g.gridy = row; g.gridx = 0; g.gridwidth = 1; g.fill = GridBagConstraints.NONE;
        p.add(new JLabel(label), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        p.add(campo, g);
    }

    private void crearProducto(ActionEvent e) {
        String codigo = tfCodigo.getText().trim();
        String desc   = tfDescripcion.getText().trim();
        if (codigo.isEmpty() || desc.isEmpty()) {
            log("✗ Código y Descripción son obligatorios."); return;
        }
        if (OrdenCompraController.getInstance().buscarProductoPorCodigo(codigo) != null) {
            log("✗ Ya existe un producto con código '" + codigo + "'."); return;
        }

        String rubroNombre = (String) cbRubro.getSelectedItem();
        Rubro rubro = MainFrame.rubros.stream()
            .filter(r -> r.getNombre().equals(rubroNombre))
            .findFirst().orElse(null);
        if (rubro == null) { log("✗ Rubro inválido."); return; }

        String unidad = tfUnidad.getText().trim().isEmpty() ? "unidad" : tfUnidad.getText().trim();
        productoEnCurso = new Producto(codigo, desc, unidad, (TipoIVA) cbIVA.getSelectedItem(), rubro);
        log("✓ Producto creado en memoria: " + productoEnCurso.getCodigoInterno()
            + " — " + productoEnCurso.getDescripcion());
        log("  Ahora agregá al menos un precio (paso 2) y luego guardá (paso 3).");
    }

    private void agregarPrecio(ActionEvent e) {
        if (productoEnCurso == null) {
            log("→ Primero creá el producto (paso 1)."); return;
        }
        if (cbProveedorPrecio.getItemCount() == 0) {
            log("✗ No hay proveedores. Registrá uno en M2 primero y luego refrescá."); return;
        }

        double precio = 0.0;
        try { precio = Double.parseDouble(tfPrecio.getText().trim()); }
        catch (NumberFormatException ex) { log("✗ Precio inválido."); return; }
        if (precio <= 0) { log("✗ El precio debe ser mayor a 0."); return; }

        String item = (String) cbProveedorPrecio.getSelectedItem();
        if (item == null) { log("✗ Seleccioná un proveedor."); return; }
        String cuit = item.split(" · ")[0];
        Proveedor prov = OrdenCompraController.getInstance().buscarProveedorPorId(cuit);
        if (prov == null) { log("✗ Proveedor no encontrado."); return; }

        PrecioAcordado pa = new PrecioAcordado(precio, new Date(), null, prov);
        productoEnCurso.agregarPrecioAcordado(pa);
        log("✓ Precio $" + precio + " para proveedor " + prov.getRazonSocial()
            + " agregado al producto.");
    }

    private void guardarProducto(ActionEvent e) {
        if (productoEnCurso == null) {
            log("→ Primero creá el producto (paso 1)."); return;
        }
        if (productoEnCurso.getPreciosAcordados().isEmpty()) {
            log("⚠ El producto no tiene precios. Podés guardarlo igual, pero no podrá usarse en OC.");
        }
        OrdenCompraController.getInstance().agregarProducto(productoEnCurso);
        agregarFilaTabla(productoEnCurso);
        log("✓ Producto guardado en catálogo: " + productoEnCurso);
        productoEnCurso = null;
        limpiarForm();
    }

    private void verPrecios(ActionEvent e) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { log("→ Seleccioná un producto en la tabla primero."); return; }
        String codigo = (String) modeloTabla.getValueAt(fila, 0);
        Producto prod = OrdenCompraController.getInstance().buscarProductoPorCodigo(codigo);
        if (prod == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("Precios acordados para: ").append(prod.getDescripcion()).append("\n\n");
        List<PrecioAcordado> precios = prod.obtenerPreciosHistoricos();
        if (precios.isEmpty()) {
            sb.append("  (ningún precio registrado)");
        } else {
            for (PrecioAcordado pa : precios) {
                sb.append("  · Proveedor: ").append(pa.getProveedor().getRazonSocial())
                  .append("\n    Precio: $").append(pa.getPrecioUnitario())
                  .append(" | Vigente: ").append(pa.estaVigente()).append("\n");
            }
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Precios del Producto", JOptionPane.INFORMATION_MESSAGE);
    }

    private void agregarFilaTabla(Producto prod) {
        int cantPrecios = prod.getPreciosAcordados().size();
        modeloTabla.addRow(new Object[]{
            prod.getCodigoInterno(), prod.getDescripcion(),
            prod.getUnidadMedida(), prod.getTipoIVA(),
            prod.getRubro().getNombre(),
            cantPrecios + " precio(s)"
        });
    }

    private void cargarProductosDemoEnTabla() {
        OrdenCompraController.getInstance().getProductos().forEach(this::agregarFilaTabla);
    }

    private void limpiarForm() {
        tfCodigo.setText(""); tfDescripcion.setText(""); tfUnidad.setText("");
        tfPrecio.setText("0.0");
        cbIVA.setSelectedIndex(0);
        if (cbRubro.getItemCount() > 0) cbRubro.setSelectedIndex(0);
    }

    private void log(String msg) { taLog.append(msg + "\n"); }
}

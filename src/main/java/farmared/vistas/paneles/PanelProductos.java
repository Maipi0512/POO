package farmared.vistas.paneles;

import farmared.modelo.enums.TipoIVA;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.controladores.AppContext;
import farmared.controladores.ProductoController;
import farmared.controladores.ProveedorController;
import farmared.vistas.util.UiUtil;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Date;

public class PanelProductos extends JPanel {

    private final ProductoController  prodCtrl  = AppContext.getInstancia().getProductoCtrl();
    private final ProveedorController provCtrl  = AppContext.getInstancia().getProveedorCtrl();

    private final JTable tabla = new JTable();
    private final JTextField codigo = new JTextField(12);
    private final JTextField descripcion = new JTextField(20);
    private final JTextField unidad = new JTextField(10);
    private final JComboBox<TipoIVA> tipoIVA = new JComboBox<>(TipoIVA.values());
    private final JComboBox<Rubro> rubro = new JComboBox<>();
    private final JComboBox<Proveedor> proveedor = new JComboBox<>();
    private final JTextField precio = new JTextField(10);

    public PanelProductos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(
                new String[]{"Codigo", "Descripcion", "UDM", "IVA", "Rubro", "Activo"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        agregarCampo(form, gbc, y++, "Codigo interno:", codigo);
        agregarCampo(form, gbc, y++, "Descripcion:", descripcion);
        agregarCampo(form, gbc, y++, "Unidad medida:", unidad);
        agregarCampo(form, gbc, y++, "Tipo IVA:", tipoIVA);
        agregarCampo(form, gbc, y++, "Rubro:", rubro);
        agregarCampo(form, gbc, y++, "Proveedor:", proveedor);
        agregarCampo(form, gbc, y++, "Precio acordado:", precio);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton registrar = new JButton("Registrar producto");
        JButton agregarPrecio = new JButton("Agregar precio a producto existente");
        JButton baja = new JButton("Dar de baja producto");
        JButton compulsa = new JButton("Ver compulsa de precios");
        JButton refrescar = new JButton("Refrescar");
        acciones.add(registrar);
        acciones.add(agregarPrecio);
        acciones.add(baja);
        acciones.add(compulsa);
        acciones.add(refrescar);

        registrar.addActionListener(e -> registrarProducto());
        agregarPrecio.addActionListener(e -> agregarPrecioExistente());
        baja.addActionListener(e -> darBajaProducto());
        compulsa.addActionListener(e -> mostrarCompulsa());
        refrescar.addActionListener(e -> cargarDatos());

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(form, BorderLayout.EAST);
        add(acciones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent campo) {
        gbc.gridx = 0; gbc.gridy = y;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    public void cargarDatos() {
        rubro.removeAllItems();
        for (Rubro r : provCtrl.listarRubros()) rubro.addItem(r);

        proveedor.removeAllItems();
        for (Proveedor p : provCtrl.listarProveedores()) proveedor.addItem(p);

        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
        for (Producto prod : prodCtrl.listarTodos()) {
            model.addRow(new Object[]{
                    prod.getCodigoInterno(),
                    prod.getDescripcion(),
                    prod.getUnidadMedida(),
                    prod.getTipoIVA(),
                    prod.getRubro().getNombre(),
                    prod.isActivo() ? "Si" : "No"
            });
        }
    }

    private void darBajaProducto() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { UiUtil.mostrarError(this, "Seleccione un producto."); return; }
        String cod = (String) tabla.getValueAt(fila, 0);
        if (UiUtil.confirmar(this, "Dar de baja el producto " + cod + "?")) {
            try {
                prodCtrl.darBajaProducto(cod);
                UiUtil.mostrarInfo(this, "Producto dado de baja.");
                cargarDatos();
            } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
        }
    }

    private void registrarProducto() {
        try {
            if (codigo.getText().trim().isEmpty())
                throw new IllegalArgumentException("El codigo interno es obligatorio.");
            if (rubro.getSelectedItem() == null || proveedor.getSelectedItem() == null)
                throw new IllegalArgumentException("Seleccione rubro y proveedor.");

            Producto producto = new Producto(
                    codigo.getText().trim(), descripcion.getText().trim(),
                    unidad.getText().trim().isEmpty() ? "unidad" : unidad.getText().trim(),
                    (TipoIVA) tipoIVA.getSelectedItem(), (Rubro) rubro.getSelectedItem()
            );

            if (!precio.getText().trim().isEmpty()) {
                double valor = UiUtil.parsearDouble(precio.getText(), "Precio");
                producto.agregarPrecioAcordado(
                        new PrecioAcordado(valor, new Date(), null, (Proveedor) proveedor.getSelectedItem())
                );
            }

            prodCtrl.registrarProducto(producto);
            UiUtil.mostrarInfo(this, "Producto registrado.");
            cargarDatos();
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void agregarPrecioExistente() {
        try {
            if (codigo.getText().trim().isEmpty() || precio.getText().trim().isEmpty())
                throw new IllegalArgumentException("Ingrese codigo y precio.");
            if (proveedor.getSelectedItem() == null)
                throw new IllegalArgumentException("Seleccione proveedor.");
            Producto existente = prodCtrl.buscarProductoPorCodigo(codigo.getText().trim());
            if (existente == null)
                throw new IllegalArgumentException("Producto no encontrado. Use 'Registrar producto' primero.");
            double valor = UiUtil.parsearDouble(precio.getText(), "Precio");
            Proveedor prov = (Proveedor) proveedor.getSelectedItem();
            prodCtrl.agregarPrecioAcordado(codigo.getText().trim(), prov.getCuit(), valor);
            UiUtil.mostrarInfo(this, "Precio agregado para " + prov.getRazonSocial());
            cargarDatos();
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void mostrarCompulsa() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { UiUtil.mostrarError(this, "Seleccione un producto."); return; }
        String cod = (String) tabla.getValueAt(fila, 0);
        StringBuilder sb = new StringBuilder("Compulsa de precios - ").append(cod).append("\n\n");
        for (PrecioAcordado pa : prodCtrl.consultarCompulsaPrecios(cod)) {
            sb.append(pa.getProveedor().getRazonSocial())
              .append(" | $").append(pa.getPrecioUnitario())
              .append(" | ").append(UiUtil.formatearFecha(pa.getFechaAcuerdo()))
              .append(pa.estaVigente() ? " (vigente)" : " (historico)").append("\n");
        }
        JTextArea area = new JTextArea(sb.toString(), 12, 40);
        area.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Compulsa de precios",
                JOptionPane.INFORMATION_MESSAGE);
    }
}

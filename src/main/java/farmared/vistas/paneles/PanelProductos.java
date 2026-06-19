package farmared.vistas.paneles;

import farmared.modelo.enums.TipoIVA;
import farmared.dto.ProveedorDTO;
import farmared.dto.RubroDTO;
import farmared.dto.PrecioAcordadoDTO;
import farmared.dto.ProductoDTO;
import farmared.controladores.AppContext;
import farmared.controladores.ProductoController;
import farmared.controladores.ProveedorController;
import farmared.vistas.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelProductos extends JPanel {

    private final ProductoController  prodCtrl = AppContext.getInstancia().getProductoCtrl();
    private final ProveedorController provCtrl = AppContext.getInstancia().getProveedorCtrl();

    private final JTable tabla = new JTable();
    private final JTextField              codigo      = new JTextField(12);
    private final JTextField              descripcion = new JTextField(20);
    private final JTextField              unidad      = new JTextField(10);
    private final JComboBox<TipoIVA>      tipoIVA     = new JComboBox<>(TipoIVA.values());
    private final JComboBox<RubroDTO>     rubro       = new JComboBox<>();
    private final JComboBox<ProveedorDTO> proveedor   = new JComboBox<>();
    private final JTextField              precio      = new JTextField(10);

    public PanelProductos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(
                new String[]{"Codigo", "Descripcion", "UDM", "IVA", "Rubro", "Activo"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
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
        JButton nuevo            = new JButton("Nuevo");
        JButton registrar        = new JButton("Registrar");
        JButton agregarPrecio    = new JButton("Agregar precio");
        JButton baja             = new JButton("Dar de baja");
        JButton verPrecioAcord   = new JButton("Ver precio acordado");
        JButton refrescar        = new JButton("Refrescar");
        acciones.add(nuevo);
        acciones.add(registrar);
        acciones.add(agregarPrecio);
        acciones.add(baja);
        acciones.add(verPrecioAcord);
        acciones.add(refrescar);

        nuevo.addActionListener(e          -> limpiarFormulario());
        registrar.addActionListener(e      -> registrarProducto());
        agregarPrecio.addActionListener(e  -> agregarPrecioExistente());
        baja.addActionListener(e           -> darBajaProducto());
        verPrecioAcord.addActionListener(e -> verPrecioAcordado());
        refrescar.addActionListener(e      -> cargarDatos());

        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) cargarSeleccion();
        });

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(form, BorderLayout.EAST);
        add(acciones, BorderLayout.SOUTH);

        cargarDatos();
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int y, String etiqueta, JComponent campo) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1;
        panel.add(new JLabel(etiqueta), gbc);
        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    public void cargarDatos() {
        rubro.removeAllItems();
        for (RubroDTO r : provCtrl.listarRubrosDTO()) rubro.addItem(r);

        proveedor.removeAllItems();
        for (ProveedorDTO p : provCtrl.listarProveedoresDTO()) proveedor.addItem(p);

        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
        for (ProductoDTO prod : prodCtrl.listarTodosDTO()) {
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

    private void cargarSeleccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;

        String cod = (String) tabla.getValueAt(fila, 0);
        ProductoDTO prod = prodCtrl.buscarProductoDTO(cod);
        if (prod == null) return;

        codigo.setText(prod.getCodigoInterno());
        codigo.setEditable(false);
        descripcion.setText(prod.getDescripcion());
        unidad.setText(prod.getUnidadMedida());
        tipoIVA.setSelectedItem(TipoIVA.valueOf(prod.getTipoIVA()));
        for (int i = 0; i < rubro.getItemCount(); i++) {
            if (rubro.getItemAt(i).getIdRubro() == prod.getRubro().getIdRubro()) {
                rubro.setSelectedIndex(i);
                break;
            }
        }
    }

    private void limpiarFormulario() {
        codigo.setText("");
        codigo.setEditable(true);
        descripcion.setText("");
        unidad.setText("");
        precio.setText("");
        tipoIVA.setSelectedIndex(0);
        if (rubro.getItemCount()     > 0) rubro.setSelectedIndex(0);
        if (proveedor.getItemCount() > 0) proveedor.setSelectedIndex(0);
        tabla.clearSelection();
    }

    private void registrarProducto() {
        try {
            if (codigo.getText().trim().isEmpty())
                throw new IllegalArgumentException("El codigo interno es obligatorio.");
            if (descripcion.getText().trim().isEmpty())
                throw new IllegalArgumentException("La descripcion es obligatoria.");
            if (rubro.getSelectedItem() == null)
                throw new IllegalArgumentException("Seleccione un rubro.");

            RubroDTO rDto = (RubroDTO) rubro.getSelectedItem();
            ProveedorDTO pDto = (ProveedorDTO) proveedor.getSelectedItem();
            Double precioInicial = null;
            String cuitProv = null;
            if (!precio.getText().trim().isEmpty() && pDto != null) {
                precioInicial = UiUtil.parsearDouble(precio.getText(), "Precio");
                cuitProv = pDto.getCuit();
            }

            prodCtrl.registrarProductoDTO(
                    codigo.getText().trim(), descripcion.getText().trim(),
                    unidad.getText().trim().isEmpty() ? "unidad" : unidad.getText().trim(),
                    (TipoIVA) tipoIVA.getSelectedItem(), rDto.getIdRubro(),
                    cuitProv, precioInicial
            );

            UiUtil.mostrarInfo(this, "Producto registrado.");
            limpiarFormulario();
            cargarDatos();
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void darBajaProducto() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { UiUtil.mostrarError(this, "Seleccione un producto."); return; }
        String cod = (String) tabla.getValueAt(fila, 0);
        if (UiUtil.confirmar(this, "El producto " + cod + " quedara inactivo (baja logica). Continuar?")) {
            try {
                prodCtrl.darBajaProducto(cod);
                UiUtil.mostrarInfo(this, "Producto marcado como inactivo. Sigue visible en la lista.");
                limpiarFormulario();
                cargarDatos();
            } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
        }
    }

    private void agregarPrecioExistente() {
        try {
            if (codigo.getText().trim().isEmpty())
                throw new IllegalArgumentException("Seleccione un producto de la tabla primero.");
            if (precio.getText().trim().isEmpty())
                throw new IllegalArgumentException("Ingrese el precio acordado.");
            if (proveedor.getSelectedItem() == null)
                throw new IllegalArgumentException("Seleccione un proveedor.");
            ProductoDTO existente = prodCtrl.buscarProductoDTO(codigo.getText().trim());
            if (existente == null)
                throw new IllegalArgumentException("Producto no encontrado.");

            double valor = UiUtil.parsearDouble(precio.getText(), "Precio");
            ProveedorDTO prov = (ProveedorDTO) proveedor.getSelectedItem();
            prodCtrl.agregarPrecioAcordado(codigo.getText().trim(), prov.getCuit(), valor);
            precio.setText("");
            UiUtil.mostrarInfo(this, "Precio actualizado para " + prov.getRazonSocial() + ".");
        } catch (Exception ex) { UiUtil.mostrarError(this, ex.getMessage()); }
    }

    private void verPrecioAcordado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { UiUtil.mostrarError(this, "Seleccione un producto."); return; }
        String cod = (String) tabla.getValueAt(fila, 0);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Proveedor", "Precio", "Desde", "Vigente"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        for (PrecioAcordadoDTO pa : prodCtrl.consultarCompulsaPreciosDTO(cod)) {
            model.addRow(new Object[]{
                    pa.getRazonSocialProveedor(),
                    UiUtil.formatearMoneda(pa.getPrecioUnitario()),
                    UiUtil.formatearFecha(pa.getFechaAcuerdo()),
                    pa.isVigente() ? "Si" : "No"
            });
        }

        JTable tablaPrecios = new JTable(model);
        JScrollPane scroll = new JScrollPane(tablaPrecios);
        scroll.setPreferredSize(new Dimension(480, 180));
        JOptionPane.showMessageDialog(this, scroll,
                "Precios acordados — " + cod, JOptionPane.INFORMATION_MESSAGE);
    }
}

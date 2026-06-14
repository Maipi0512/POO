package farmared.ui.paneles;

import farmared.modulos.m2_proveedores.Rubro;
import farmared.sistema.observador.NotificadorSistema;
import farmared.sistema.observador.ObservadorSistema;
import farmared.ui.AppContext;
import farmared.ui.controladores.ProveedorController;
import farmared.ui.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Vista para alta de rubros (RF-02). */
public class PanelRubros extends JPanel implements ObservadorSistema {

    private final ProveedorController controlador =
            new ProveedorController(AppContext.getInstancia().getSistema());

    private final JTable tabla = new JTable();
    private final JTextField nombre = new JTextField(20);
    private final JTextField descripcion = new JTextField(25);

    public PanelRubros() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.setModel(new DefaultTableModel(new String[]{"ID", "Nombre", "Descripcion"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nombre:"));
        form.add(nombre);
        form.add(new JLabel("Descripcion:"));
        form.add(descripcion);
        JButton registrar = new JButton("Registrar rubro");
        registrar.addActionListener(e -> registrarRubro());
        form.add(registrar);

        add(form, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        NotificadorSistema.getInstancia().suscribir(this);
        cargarDatos();
    }

    public void cargarDatos() {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
        for (Rubro r : controlador.listarRubros()) {
            model.addRow(new Object[]{r.getIdRubro(), r.getNombre(), r.getDescripcion()});
        }
    }

    private void registrarRubro() {
        try {
            if (nombre.getText().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del rubro es obligatorio.");
            }
            controlador.registrarRubro(nombre.getText().trim(), descripcion.getText().trim());
            nombre.setText("");
            descripcion.setText("");
            UiUtil.mostrarInfo(this, "Rubro registrado.");
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    @Override
    public void actualizar(String evento) {
        if ("RUBRO_REGISTRADO".equals(evento) || "DATOS_ACTUALIZADOS".equals(evento)) {
            cargarDatos();
        }
    }
}

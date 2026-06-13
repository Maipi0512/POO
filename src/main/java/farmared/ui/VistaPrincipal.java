package farmared.ui;

import farmared.ui.paneles.*;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del sistema. Organiza los modulos en pestanas (patron MVC - Vista).
 */
public class VistaPrincipal extends JFrame {

    private final PanelProveedores panelProveedores = new PanelProveedores();
    private final PanelRubros panelRubros = new PanelRubros();
    private final PanelProductos panelProductos = new PanelProductos();
    private final PanelOrdenesCompra panelOrdenesCompra = new PanelOrdenesCompra();
    private final PanelComprobantes panelComprobantes = new PanelComprobantes();
    private final PanelOrdenesPago panelOrdenesPago = new PanelOrdenesPago();
    private final PanelConsultas panelConsultas = new PanelConsultas();

    public VistaPrincipal() {
        super("FarmaRed - Sistema de Gestion de Compras");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        JLabel usuario = new JLabel("Usuario: " + AppContext.getInstancia().getUsuarioActual());
        usuario.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Proveedores", panelProveedores);
        pestanas.addTab("Rubros", panelRubros);
        pestanas.addTab("Productos", panelProductos);
        pestanas.addTab("Ordenes de Compra", panelOrdenesCompra);
        pestanas.addTab("Comprobantes", panelComprobantes);
        pestanas.addTab("Ordenes de Pago", panelOrdenesPago);
        pestanas.addTab("Consultas", panelConsultas);

        pestanas.addChangeListener(e -> refrescarPestanaActiva(pestanas));

        JButton cerrarSesion = new JButton("Cerrar sesion");
        cerrarSesion.addActionListener(e -> {
            AppContext.getInstancia().cerrarSesion();
            dispose();
            FarmaredApp.iniciarAplicacion();
        });

        JPanel barra = new JPanel(new BorderLayout());
        barra.add(usuario, BorderLayout.WEST);
        barra.add(cerrarSesion, BorderLayout.EAST);

        add(barra, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
    }

    private void refrescarPestanaActiva(JTabbedPane pestanas) {
        Component panel = pestanas.getSelectedComponent();
        if (panel instanceof PanelProveedores) ((PanelProveedores) panel).cargarDatos();
        else if (panel instanceof PanelRubros) ((PanelRubros) panel).cargarDatos();
        else if (panel instanceof PanelProductos) ((PanelProductos) panel).cargarDatos();
        else if (panel instanceof PanelOrdenesCompra) ((PanelOrdenesCompra) panel).cargarDatos();
        else if (panel instanceof PanelComprobantes) ((PanelComprobantes) panel).cargarDatos();
        else if (panel instanceof PanelOrdenesPago) ((PanelOrdenesPago) panel).cargarDatos();
        else if (panel instanceof PanelConsultas) ((PanelConsultas) panel).cargarDatos();
    }
}

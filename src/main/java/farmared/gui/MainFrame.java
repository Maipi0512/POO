package farmared.gui;

import farmared.controllers.*;
import farmared.enums.CondicionIVA;
import farmared.enums.TipoIVA;
import farmared.modulos.m2_proveedores.*;
import farmared.modulos.m3_productos.*;
import farmared.modulos.m6_ordenes_pago.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainFrame extends JFrame {

    static final List<Rubro> rubros = new ArrayList<>();

    private JTabbedPane tabs;

    public MainFrame() {
        super("Farmared — Sistema de Gestión de Compras y Abastecimiento");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 720);
        setLocationRelativeTo(null);

        initRubros();
        initData();

        setJMenuBar(buildMenuBar());

        tabs = new JTabbedPane();
        tabs.addTab("M1 · Usuarios",          new PanelUsuarios());
        tabs.addTab("M2 · Proveedores",        new PanelProveedores());
        tabs.addTab("M3 · Productos",          new PanelProductos());
        tabs.addTab("M4 · Órdenes de Compra",  new PanelOrdenCompra());
        tabs.addTab("M5 · Comprobantes",       new PanelComprobantes());
        tabs.addTab("M6 · Órdenes de Pago",    new PanelOrdenPago());
        tabs.addTab("M7 · Consultas",          new PanelConsultas());
        add(tabs, BorderLayout.CENTER);

        JLabel status = new JLabel(
            "  Sistema inicializado · 2 proveedores demo · 2 productos demo",
            SwingConstants.LEFT);
        status.setBorder(BorderFactory.createEtchedBorder());
        add(status, BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mModulos = new JMenu("Módulos");
        String[] nombres = {
            "M1 · Usuarios y Seguridad",
            "M2 · Proveedores",
            "M3 · Productos y Servicios",
            "M4 · Órdenes de Compra",
            "M5 · Comprobantes Recibidos",
            "M6 · Órdenes de Pago",
            "M7 · Consultas Generales"
        };
        for (int i = 0; i < nombres.length; i++) {
            final int idx = i;
            JMenuItem item = new JMenuItem(nombres[i]);
            item.addActionListener(e -> tabs.setSelectedIndex(idx));
            mModulos.add(item);
        }
        bar.add(mModulos);

        JMenu mAyuda = new JMenu("Ayuda");
        JMenuItem iFlujo = new JMenuItem("Flujo del sistema...");
        iFlujo.addActionListener(e -> mostrarFlujo());
        mAyuda.add(iFlujo);
        bar.add(mAyuda);

        return bar;
    }

    private void mostrarFlujo() {
        JTextArea ta = new JTextArea(
            "FLUJO OPERATIVO TÍPICO\n\n" +
            "1. M2 — Registrá proveedores (CUIT, razón social, rubros, tope de deuda)\n" +
            "2. M3 — Agregá productos al catálogo y asignales precios por proveedor\n" +
            "3. M4 — Creá una Orden de Compra:\n" +
            "        Seleccioná proveedor → Crear OC → Agregar ítems → Emitir OC\n" +
            "        (si supera el tope de deuda, requiere autorización de SUPERVISOR)\n" +
            "4. M5 — Registrá la factura recibida del proveedor:\n" +
            "        Vinculá a la OC → se validan productos y precios\n" +
            "        (desvíos o compra directa requieren autorización de SUPERVISOR)\n" +
            "5. M6 — Emitir Orden de Pago:\n" +
            "        Ver comprobantes impagos → Calcular retenciones (IVA/IIBB/Ganancias)\n" +
            "        → Confirmar pago con medio de pago (Efectivo, Cheque, Transferencia)\n" +
            "6. M7 — Consultas:\n" +
            "        Cuenta corriente, documentos impagos, Libro IVA, retenciones, etc."
        );
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ta.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(560, 320));
        JOptionPane.showMessageDialog(this, sp, "Flujo del Sistema", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void initRubros() {
        rubros.add(new Rubro(1, "Medicamentos",            "Productos farmacéuticos y medicamentos"));
        rubros.add(new Rubro(2, "Insumos descartables",    "Material descartable de uso médico"));
        rubros.add(new Rubro(3, "Equipamiento médico",     "Equipos e instrumentos médicos"));
        rubros.add(new Rubro(4, "Limpieza e higiene",      "Productos de limpieza y sanitización"));
        rubros.add(new Rubro(5, "Servicios mantenimiento", "Servicios de mantenimiento y reparación"));
        rubros.add(new Rubro(6, "Papelería",               "Material de oficina y papelería"));
    }

    private static void initData() {
        Rubro rubMed    = rubros.get(0);
        Rubro rubInsumo = rubros.get(1);

        // Proveedor 1
        Proveedor prov1 = new Proveedor(
            "20-12345678-9", "Laboratorios SA", "LabSA",
            "Av. Corrientes 1234, CABA", "011-4444-5555", "ventas@labsa.com",
            CondicionIVA.RESPONSABLE_INSCRIPTO, "123-456789-0", new Date(90, 0, 1));
        prov1.setTopeMaximoDeuda(100000.0);
        prov1.agregarRubro(rubMed);

        // Proveedor 2
        Proveedor prov2 = new Proveedor(
            "30-98765432-1", "DistribMed SRL", "DistribMed",
            "Calle Falsa 456, Rosario", "0341-111-2222", "compras@distribmed.com.ar",
            CondicionIVA.RESPONSABLE_INSCRIPTO, "456-789012-3", new Date(95, 5, 15));
        prov2.setTopeMaximoDeuda(50000.0);
        prov2.agregarRubro(rubMed);
        prov2.agregarRubro(rubInsumo);

        for (Proveedor p : new Proveedor[]{prov1, prov2}) {
            OrdenCompraController.getInstance().agregarProveedor(p);
            FacturaController.getInstance().agregarProveedor(p);
            OrdenPagoController.getInstance().agregarProveedor(p);
            CuentaCorrienteController.getInstance().agregarProveedor(p);
        }

        // Productos
        Producto aspirina = new Producto("MED-001", "Aspirina 500mg x20",
                                         "caja", TipoIVA.IVA_21, rubMed);
        aspirina.agregarPrecioAcordado(new PrecioAcordado(150.0, new Date(125, 0, 1), null, prov1));
        aspirina.agregarPrecioAcordado(new PrecioAcordado(145.0, new Date(125, 0, 1), null, prov2));

        Producto guantes = new Producto("INS-001", "Guantes descartables x100",
                                        "caja", TipoIVA.IVA_21, rubInsumo);
        guantes.agregarPrecioAcordado(new PrecioAcordado(280.0, new Date(125, 0, 1), null, prov2));

        for (Producto prod : new Producto[]{aspirina, guantes}) {
            OrdenCompraController.getInstance().agregarProducto(prod);
        }

        // Impuestos M6
        OrdenPagoController.getInstance().agregarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        OrdenPagoController.getInstance().agregarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
        OrdenPagoController.getInstance().agregarImpuesto(new ImpuestoGanancias(3, 3.5, 5000.0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

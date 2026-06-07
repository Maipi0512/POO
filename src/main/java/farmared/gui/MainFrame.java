package farmared.gui;

import farmared.controllers.*;
import farmared.enums.CondicionIVA;
import farmared.enums.TipoIVA;
import farmared.modulos.m2_proveedores.*;
import farmared.modulos.m3_productos.*;
import farmared.modulos.m6_ordenes_pago.*;
import javax.swing.*;
import java.awt.*;
import java.util.Date;

public class MainFrame extends JFrame {

    static final String CUIT_DEMO    = "20-12345678-9";
    static final String CODIGO_PROD  = "MED-001";

    public MainFrame() {
        super("Farmared — Sistema de Gestión de Compras");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(960, 660);
        setLocationRelativeTo(null);

        initData();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("M4 · Órdenes de Compra", new PanelOrdenCompra());
        tabs.addTab("M5 · Comprobantes",       new PanelComprobantes());
        tabs.addTab("M6 · Órdenes de Pago",    new PanelOrdenPago());
        tabs.addTab("M7 · Consultas",           new PanelConsultas());
        add(tabs, BorderLayout.CENTER);

        JLabel status = new JLabel(
            "  Sistema inicializado · Proveedor: Laboratorios SA · Producto: MED-001",
            SwingConstants.LEFT);
        status.setBorder(BorderFactory.createEtchedBorder());
        add(status, BorderLayout.SOUTH);
    }

    private static void initData() {
        // M2
        Rubro rubMed = new Rubro(1, "Medicamentos", "Productos farmaceuticos");
        Proveedor prov = new Proveedor(
            CUIT_DEMO, "Laboratorios SA", "LabSA",
            "Av. Corrientes 1234", "011-4444-5555", "ventas@labsa.com",
            CondicionIVA.RESPONSABLE_INSCRIPTO, "123-456789-0",
            new Date(90, 0, 1)
        );
        prov.setTopeMaximoDeuda(100000.0);
        prov.agregarRubro(rubMed);

        OrdenCompraController.getInstance().agregarProveedor(prov);
        FacturaController.getInstance().agregarProveedor(prov);
        OrdenPagoController.getInstance().agregarProveedor(prov);
        CuentaCorrienteController.getInstance().agregarProveedor(prov);

        // M3
        Producto aspirina = new Producto(CODIGO_PROD, "Aspirina 500mg x20",
                                         "caja", TipoIVA.IVA_21, rubMed);
        PrecioAcordado precio = new PrecioAcordado(150.0, new Date(125, 0, 1), null, prov);
        aspirina.agregarPrecioAcordado(precio);
        OrdenCompraController.getInstance().agregarProducto(aspirina);

        // Impuestos M6
        OrdenPagoController.getInstance().agregarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        OrdenPagoController.getInstance().agregarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
        OrdenPagoController.getInstance().agregarImpuesto(new ImpuestoGanancias(3, 3.5, 5000.0));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}

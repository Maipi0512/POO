package farmared.ui.paneles;

import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m6_ordenes_pago.MedioPago;
import farmared.modulos.m6_ordenes_pago.OrdenPago;
import farmared.sistema.observador.NotificadorSistema;
import farmared.sistema.observador.ObservadorSistema;
import farmared.ui.AppContext;
import farmared.ui.controladores.OrdenPagoController;
import farmared.ui.dialogos.DialogMediosPago;
import farmared.ui.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PanelOrdenesPago extends JPanel implements ObservadorSistema {

    private final OrdenPagoController controlador =
            new OrdenPagoController(AppContext.getInstancia().getSistema());

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JTable tablaImpagos = new JTable();
    private final JTable tablaOP = new JTable();
    private final JLabel lblResumen = new JLabel("Seleccione comprobantes y prepare la OP.");

    private OrdenPago opEnCurso;
    private final Map<Comprobante, Double> seleccion = new LinkedHashMap<>();

    public PanelOrdenesPago() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tablaImpagos.setModel(new DefaultTableModel(
                new String[]{"Seleccionar", "Numero", "Tipo", "Items", "Total", "Saldo"}, 0
        ) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        });

        tablaOP.setModel(new DefaultTableModel(
                new String[]{"Numero", "Proveedor", "Bruto", "Retenciones", "Neto", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel superior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton cargar = new JButton("Cargar impagos");
        JButton preparar = new JButton("Preparar OP");
        JButton confirmar = new JButton("Confirmar pago (medios multiples)");
        superior.add(new JLabel("Proveedor:"));
        superior.add(comboProveedores);
        superior.add(cargar);
        superior.add(preparar);
        superior.add(confirmar);
        superior.add(lblResumen);

        cargar.addActionListener(e -> cargarImpagos());
        preparar.addActionListener(e -> prepararOP());
        confirmar.addActionListener(e -> confirmarPago());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tablaImpagos),
                new JScrollPane(tablaOP));
        split.setResizeWeight(0.45);

        add(superior, BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);

        NotificadorSistema.getInstancia().suscribir(this);
        cargarDatos();
    }

    public void cargarDatos() {
        comboProveedores.removeAllItems();
        for (var p : AppContext.getInstancia().getSistema().getProveedores()) {
            if (p.isActivo()) {
                comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
            }
        }

        DefaultTableModel model = (DefaultTableModel) tablaOP.getModel();
        model.setRowCount(0);
        for (OrdenPago op : controlador.listarEmitidas()) {
            model.addRow(new Object[]{
                    op.getNumero(),
                    op.getProveedor().getRazonSocial(),
                    UiUtil.formatearMoneda(op.getImporteBruto()),
                    UiUtil.formatearMoneda(op.getTotalRetenciones()),
                    UiUtil.formatearMoneda(op.getImporteNeto()),
                    op.getEstado()
            });
        }
    }

    private String obtenerCuitSeleccionado() {
        String item = (String) comboProveedores.getSelectedItem();
        if (item == null) throw new IllegalArgumentException("Seleccione un proveedor.");
        return item.split(" - ")[0];
    }

    private void cargarImpagos() {
        try {
            opEnCurso = null;
            seleccion.clear();
            List<Comprobante> impagos = controlador.listarImpagos(obtenerCuitSeleccionado());
            DefaultTableModel model = (DefaultTableModel) tablaImpagos.getModel();
            model.setRowCount(0);
            for (Comprobante c : impagos) {
                model.addRow(new Object[]{
                        Boolean.FALSE,
                        c.getNumero(),
                        c.getTipo(),
                        c.getDetalles().size(),
                        UiUtil.formatearMoneda(c.getImporteTotal()),
                        UiUtil.formatearMoneda(c.getSaldoPendiente())
                });
            }
            lblResumen.setText("Comprobantes impagos: " + impagos.size());
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void prepararOP() {
        try {
            seleccion.clear();
            DefaultTableModel model = (DefaultTableModel) tablaImpagos.getModel();
            List<Comprobante> impagos = controlador.listarImpagos(obtenerCuitSeleccionado());

            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean marcado = (Boolean) model.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado) && i < impagos.size()) {
                    Comprobante c = impagos.get(i);
                    seleccion.put(c, c.getSaldoPendiente());
                }
            }

            if (seleccion.isEmpty()) {
                throw new IllegalArgumentException("Seleccione al menos un comprobante.");
            }

            opEnCurso = controlador.preparar(obtenerCuitSeleccionado(), seleccion);
            lblResumen.setText(String.format(
                    "OP %s | %d comprobante(s) | Bruto: %s | Retenciones: %s | Neto: %s",
                    opEnCurso.getNumero(), seleccion.size(),
                    UiUtil.formatearMoneda(opEnCurso.getImporteBruto()),
                    UiUtil.formatearMoneda(opEnCurso.getTotalRetenciones()),
                    UiUtil.formatearMoneda(opEnCurso.getImporteNeto())
            ));
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private void confirmarPago() {
        if (opEnCurso == null) {
            UiUtil.mostrarError(this, "Primero prepare la OP.");
            return;
        }

        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        DialogMediosPago dialog = new DialogMediosPago(owner, opEnCurso.getImporteNeto());
        dialog.setVisible(true);

        if (!dialog.fueConfirmado()) return;

        try {
            List<MedioPago> medios = dialog.getMedios();
            controlador.confirmar(opEnCurso, medios);
            UiUtil.mostrarInfo(this, String.format(
                    "OP emitida: %s con %d medio(s) de pago",
                    opEnCurso.getNumero(), medios.size()
            ));
            opEnCurso = null;
            seleccion.clear();
            cargarImpagos();
            cargarDatos();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    @Override
    public void actualizar(String evento) {
        if ("COMPROBANTE_REGISTRADO".equals(evento) || "OP_EMITIDA".equals(evento)) {
            cargarDatos();
        }
    }
}

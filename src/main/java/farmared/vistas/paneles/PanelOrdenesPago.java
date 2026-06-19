package farmared.vistas.paneles;

import farmared.dto.ComprobanteDTO;
import farmared.dto.OrdenPagoDTO;
import farmared.dto.ProveedorDTO;
import farmared.modelo.modulos.m6_ordenes_pago.MedioPago;
import farmared.vistas.observador.NotificadorSistema;
import farmared.vistas.observador.ObservadorSistema;
import farmared.controladores.AppContext;
import farmared.controladores.OrdenPagoController;
import farmared.vistas.dialogos.DialogMediosPago;
import farmared.vistas.util.UiUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class PanelOrdenesPago extends JPanel implements ObservadorSistema {

    private final OrdenPagoController controlador =
            AppContext.getInstancia().getOrdenPagoCtrl();

    private final JComboBox<String> comboProveedores = new JComboBox<>();
    private final JTable tablaImpagos = new JTable();
    private final JTable tablaOP = new JTable();
    private final JLabel lblResumen = new JLabel("Seleccione comprobantes y prepare la OP.");

    private OrdenPagoDTO opEnCurso;
    private final Map<String, Double> seleccion = new LinkedHashMap<>();

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
        for (ProveedorDTO p : controlador.getProveedoresDTO()) {
            if (p.isActivo()) {
                comboProveedores.addItem(p.getCuit() + " - " + p.getRazonSocial());
            }
        }

        DefaultTableModel model = (DefaultTableModel) tablaOP.getModel();
        model.setRowCount(0);
        for (OrdenPagoDTO op : controlador.getOrdenesEmitidasDTO()) {
            model.addRow(new Object[]{
                    op.getNumero(),
                    op.getRazonSocialProveedor(),
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
            List<ComprobanteDTO> impagos = controlador.iniciarOrdenPagoDTO(obtenerCuitSeleccionado());
            DefaultTableModel model = (DefaultTableModel) tablaImpagos.getModel();
            model.setRowCount(0);
            for (ComprobanteDTO c : impagos) {
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
            List<ComprobanteDTO> impagos = controlador.iniciarOrdenPagoDTO(obtenerCuitSeleccionado());

            for (int i = 0; i < model.getRowCount(); i++) {
                Boolean marcado = (Boolean) model.getValueAt(i, 0);
                if (Boolean.TRUE.equals(marcado) && i < impagos.size()) {
                    ComprobanteDTO c = impagos.get(i);
                    seleccion.put(c.getNumero(), c.getSaldoPendiente());
                }
            }

            if (seleccion.isEmpty()) {
                throw new IllegalArgumentException("Seleccione al menos un comprobante.");
            }

            opEnCurso = controlador.seleccionarComprobantesDTO(obtenerCuitSeleccionado(), seleccion, new Date());
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
            controlador.confirmarPagoDTO(opEnCurso.getNumero(), medios);
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

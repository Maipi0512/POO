package farmared.vistas.dialogos;

import farmared.modelo.modulos.m6_ordenes_pago.*;
import farmared.vistas.util.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dialogo para registrar uno o mas medios de pago (RF-19).
 * Permite combinar efectivo, transferencia, cheque propio y cheque de terceros.
 */
public class DialogMediosPago extends JDialog {

    private final double netoAPagar;
    private final List<MedioPago> mediosRegistrados = new ArrayList<>();
    private final JTable tabla = new JTable();
    private final JLabel lblPendiente = new JLabel();
    private boolean confirmado;

    public DialogMediosPago(Frame owner, double netoAPagar) {
        super(owner, "Medios de pago", true);
        this.netoAPagar = Math.round(netoAPagar * 100.0) / 100.0;
        construir();
    }

    private void construir() {
        setLayout(new BorderLayout(8, 8));
        tabla.setModel(new javax.swing.table.DefaultTableModel(
                new String[]{"Tipo", "Importe", "Detalle"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        });

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton agregar = new JButton("Agregar medio");
        JButton quitar = new JButton("Quitar");
        JButton confirmar = new JButton("Confirmar pago");
        JButton cancelar = new JButton("Cancelar");
        botones.add(agregar);
        botones.add(quitar);
        botones.add(confirmar);
        botones.add(cancelar);
        botones.add(lblPendiente);

        agregar.addActionListener(e -> agregarMedio());
        quitar.addActionListener(e -> quitarMedio());
        confirmar.addActionListener(e -> confirmarPago());
        cancelar.addActionListener(e -> dispose());

        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(botones, BorderLayout.SOUTH);

        actualizarPendiente();
        pack();
        setMinimumSize(new Dimension(500, 300));
        setLocationRelativeTo(getOwner());
    }

    private double totalMedios() {
        return mediosRegistrados.stream().mapToDouble(MedioPago::getImporte).sum();
    }

    private void actualizarPendiente() {
        double pendiente = netoAPagar - totalMedios();
        lblPendiente.setText(String.format("Neto: %s | Cargado: %s | Pendiente: %s",
                UiUtil.formatearMoneda(netoAPagar),
                UiUtil.formatearMoneda(totalMedios()),
                UiUtil.formatearMoneda(pendiente)));
    }

    private void refrescarTabla() {
        var model = (javax.swing.table.DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
        for (MedioPago mp : mediosRegistrados) {
            model.addRow(new Object[]{mp.getClass().getSimpleName(),
                    UiUtil.formatearMoneda(mp.getImporte()), mp.obtenerDescripcion()});
        }
        actualizarPendiente();
    }

    private void agregarMedio() {
        double pendiente = Math.round((netoAPagar - totalMedios()) * 100.0) / 100.0;
        if (pendiente <= 0) {
            UiUtil.mostrarError(this, "Ya se cubrio el neto a pagar.");
            return;
        }

        String[] tipos = {"Efectivo", "Transferencia", "Cheque propio", "Cheque de terceros"};
        String tipo = (String) JOptionPane.showInputDialog(this, "Tipo de medio:", "Agregar",
                JOptionPane.QUESTION_MESSAGE, null, tipos, tipos[0]);
        if (tipo == null) return;

        String importeTxt = JOptionPane.showInputDialog(this,
                "Importe (pendiente " + UiUtil.formatearMoneda(pendiente) + "):",
                String.valueOf(pendiente));
        if (importeTxt == null) return;

        try {
            double importe = UiUtil.parsearDouble(importeTxt, "Importe");
            if (importe <= 0) throw new IllegalArgumentException("Importe invalido.");
            if (totalMedios() + importe > netoAPagar + 0.01) {
                throw new IllegalArgumentException("El importe supera el neto pendiente.");
            }

            Date hoy = new Date();
            int id = mediosRegistrados.size() + 1;
            MedioPago medio;

            switch (tipo) {
                case "Efectivo":
                    medio = new Efectivo(id, importe, hoy);
                    break;
                case "Transferencia":
                    String cbuO = JOptionPane.showInputDialog(this, "CBU origen:");
                    String cbuD = JOptionPane.showInputDialog(this, "CBU destino:");
                    String nroOp = JOptionPane.showInputDialog(this, "Nro. operacion:");
                    if (cbuO == null || cbuD == null || nroOp == null) return;
                    medio = new TransferenciaBancaria(id, importe, hoy, cbuO, cbuD, nroOp);
                    break;
                case "Cheque propio":
                    medio = crearCheque(new ChequePropio(id, importe, hoy,
                            pedir("Nro. cheque"), hoy, hoy,
                            pedir("Firmante"), pedir("Banco"), pedir("Cuenta origen")));
                    break;
                case "Cheque de terceros":
                    medio = crearCheque(new ChequeTerceros(id, importe, hoy,
                            pedir("Nro. cheque"), hoy, hoy,
                            pedir("Firmante"), pedir("Banco"), pedir("Endosante")));
                    break;
                default:
                    return;
            }

            mediosRegistrados.add(medio);
            refrescarTabla();
        } catch (Exception ex) {
            UiUtil.mostrarError(this, ex.getMessage());
        }
    }

    private String pedir(String campo) {
        String v = JOptionPane.showInputDialog(this, campo + ":");
        return v != null ? v : "";
    }

    private MedioPago crearCheque(Cheque cheque) {
        return cheque;
    }

    private void quitarMedio() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            UiUtil.mostrarError(this, "Seleccione un medio de pago.");
            return;
        }
        mediosRegistrados.remove(fila);
        refrescarTabla();
    }

    private void confirmarPago() {
        if (Math.abs(totalMedios() - netoAPagar) > 0.01) {
            UiUtil.mostrarError(this, "Los medios deben cubrir exactamente el neto: "
                    + UiUtil.formatearMoneda(netoAPagar));
            return;
        }
        confirmado = true;
        dispose();
    }

    public boolean fueConfirmado() {
        return confirmado;
    }

    public List<MedioPago> getMedios() {
        return new ArrayList<>(mediosRegistrados);
    }
}

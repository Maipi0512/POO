<<<<<<<< HEAD:src/main/java/farmared/vistas/util/UiUtil.java
﻿package farmared.vistas.util;
========
package farmared.GUI.util;
>>>>>>>> 4f7806ab87b6a3fe759880a16e996f93a8bf6870:src/main/java/farmared/GUI/util/UiUtil.java

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class UiUtil {

    private static final SimpleDateFormat FECHA = new SimpleDateFormat("dd/MM/yyyy");

    private UiUtil() {}

    public static void mostrarError(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void mostrarInfo(Component parent, String mensaje) {
        JOptionPane.showMessageDialog(parent, mensaje, "Informacion", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirmar(Component parent, String mensaje) {
        return JOptionPane.showConfirmDialog(parent, mensaje, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static String formatearFecha(Date fecha) {
        return fecha == null ? "-" : FECHA.format(fecha);
    }

    public static String formatearMoneda(double valor) {
        return String.format("$%.2f", valor);
    }

    public static void limpiarTabla(JTable tabla) {
        DefaultTableModel model = (DefaultTableModel) tabla.getModel();
        model.setRowCount(0);
    }

    public static double parsearDouble(String texto, String campo) {
        try {
            return Double.parseDouble(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo '" + campo + "' debe ser numerico.");
        }
    }
}

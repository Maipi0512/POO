package farmared.GUI.util;

import farmared.modelo.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;

import java.util.List;

/** Utilidad para calcular totales del carrito de compra en la interfaz. */
public final class CarritoUtil {

    private CarritoUtil() {}

    public static ResumenCarrito resumenComprobante(List<DetalleComprobante> detalles) {
        double neto = 0, iva = 0;
        for (DetalleComprobante d : detalles) {
            neto += d.getSubtotal();
            iva += d.getImporteIVA();
        }
        return new ResumenCarrito(detalles.size(), neto, iva);
    }

    public static ResumenCarrito resumenOC(List<DetalleOC> detalles) {
        double neto = 0;
        for (DetalleOC d : detalles) neto += d.getSubtotal();
        return new ResumenCarrito(detalles.size(), neto, 0);
    }

    public static final class ResumenCarrito {
        private final int cantidadItems;
        private final double neto;
        private final double iva;

        public ResumenCarrito(int cantidadItems, double neto, double iva) {
            this.cantidadItems = cantidadItems;
            this.neto = Math.round(neto * 100.0) / 100.0;
            this.iva = Math.round(iva * 100.0) / 100.0;
        }

        public int getCantidadItems() { return cantidadItems; }
        public double getNeto() { return neto; }
        public double getIva() { return iva; }
        public double getTotal() { return Math.round((neto + iva) * 100.0) / 100.0; }

        public String formatearComprobante() {
            return String.format("Carrito: %d item(s) | Neto: %s | IVA: %s | Total: %s",
                    cantidadItems, UiUtil.formatearMoneda(neto),
                    UiUtil.formatearMoneda(iva), UiUtil.formatearMoneda(getTotal()));
        }

        public String formatearOC() {
            return String.format("Carrito: %d item(s) | Total: %s",
                    cantidadItems, UiUtil.formatearMoneda(neto));
        }
    }
}

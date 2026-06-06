package farmared.modulos.m6_ordenes_pago;

import java.util.Date;

/** Cheque de tercero endosado al proveedor (RF-19). */
public class ChequeTerceros extends Cheque {

    private String endosante;

    public ChequeTerceros(int idMedioPago, double importe, Date fechaPago,
                          String numero, Date fechaEmisionCheque, Date fechaVencimiento,
                          String firmante, String banco, String endosante) {
        super(idMedioPago, importe, fechaPago, numero, fechaEmisionCheque, fechaVencimiento, firmante, banco);
        this.endosante = endosante;
    }

    @Override
    public String obtenerDescripcion() {
        return "Cheque de terceros #" + numero + " (de: " + endosante + ", " + banco + ")";
    }

    public String getEndosante() { return endosante; }
}

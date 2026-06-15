package farmared.modelo.modulos.m6_ordenes_pago;

import java.util.Date;

/** Cheque librado por Farmared contra su propia cuenta (RF-19). */
public class ChequePropio extends Cheque {

    private String cuentaOrigen;

    public ChequePropio(int idMedioPago, double importe, Date fechaPago,
                        String numero, Date fechaEmisionCheque, Date fechaVencimiento,
                        String firmante, String banco, String cuentaOrigen) {
        super(idMedioPago, importe, fechaPago, numero, fechaEmisionCheque, fechaVencimiento, firmante, banco);
        this.cuentaOrigen = cuentaOrigen;
    }

    @Override
    public String obtenerDescripcion() {
        return "Cheque propio #" + numero + " (" + banco + ")";
    }

    public String getCuentaOrigen() { return cuentaOrigen; }
}

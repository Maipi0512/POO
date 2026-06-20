package farmared.modelo.modulos.m6_ordenes_pago;

import java.util.Date;

/** Transferencia bancaria como medio de pago (RF-19). */
public class TransferenciaBancaria extends MedioPago {

    private String cbuOrigen;
    private String cbuDestino;
    private String numeroOperacion;

    public TransferenciaBancaria(int idMedioPago, double importe, Date fecha,
                                  String cbuOrigen, String cbuDestino, String numeroOperacion) {
        super(idMedioPago, importe, fecha);
        this.cbuOrigen = cbuOrigen;
        this.cbuDestino = cbuDestino;
        this.numeroOperacion = numeroOperacion;
    }

    @Override
    public boolean procesarPago() {
        return cbuOrigen != null && !cbuOrigen.isBlank()
            && cbuDestino != null && !cbuDestino.isBlank()
            && numeroOperacion != null && !numeroOperacion.isBlank()
            && getImporte() > 0;
    }

    @Override
    public String obtenerDescripcion() {
        return "Transferencia bancaria (Op. " + numeroOperacion + ")";
    }

    public String getCbuOrigen()       { return cbuOrigen; }
    public String getCbuDestino()      { return cbuDestino; }
    public String getNumeroOperacion() { return numeroOperacion; }
}

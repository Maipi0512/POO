package farmared.modelo.modulos.m6_ordenes_pago;

import java.util.Date;

/** Pago en efectivo (RF-19). Acreditacion inmediata. */
public class Efectivo extends MedioPago {

    public Efectivo(int idMedioPago, double importe, Date fecha) {
        super(idMedioPago, importe, fecha);
    }

    @Override
    public boolean procesarPago() { return true; }

    @Override
    public String obtenerDescripcion() { return "Efectivo"; }
}

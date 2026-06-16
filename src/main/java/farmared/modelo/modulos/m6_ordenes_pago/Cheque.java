package farmared.modelo.modulos.m6_ordenes_pago;

import java.util.Date;

/**
 * Clase abstracta base para cheques (RF-19).
 * Datos obligatorios de todo cheque: numero, fechas, firmante, banco.
 */
public abstract class Cheque extends MedioPago {

    protected String numero;
    protected Date fechaEmisionCheque;
    protected Date fechaVencimiento;
    protected String firmante;
    protected String banco;

    public Cheque(int idMedioPago, double importe, Date fechaPago,
                  String numero, Date fechaEmisionCheque, Date fechaVencimiento,
                  String firmante, String banco) {
        super(idMedioPago, importe, fechaPago);
        this.numero = numero;
        this.fechaEmisionCheque = fechaEmisionCheque;
        this.fechaVencimiento = fechaVencimiento;
        this.firmante = firmante;
        this.banco = banco;
    }

    @Override
    public boolean procesarPago() {
        return numero != null && !numero.isBlank()
            && fechaEmisionCheque != null
            && fechaVencimiento != null
            && firmante != null && !firmante.isBlank()
            && !fechaVencimiento.before(fechaEmisionCheque);
    }

    public String getNumeroCheque()     { return numero; }
    public Date getFechaEmisionCheque() { return fechaEmisionCheque; }
    public Date getFechaVencimiento()   { return fechaVencimiento; }
    public String getFirmante()         { return firmante; }
    public String getBanco()            { return banco; }
}

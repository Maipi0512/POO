package farmared.modelo.modulos.m6_ordenes_pago;

import java.util.Date;

/**
 * Clase abstracta base para los medios de pago de una Orden de Pago (RF-19).
 *
 * Jerarquia:
 *   MedioPago
 *     ├── Efectivo
 *     ├── TransferenciaBancaria
 *     └── Cheque
 *           ├── ChequePropio
 *           └── ChequeTerceros
 *
 * Una OrdenPago puede combinar multiples instancias de MedioPago.
 */
public abstract class MedioPago {

    protected int idMedioPago;
    protected double importe;
    protected Date fecha;

    public MedioPago(int idMedioPago, double importe, Date fecha) {
        this.idMedioPago = idMedioPago;
        this.importe = importe;
        this.fecha = fecha;
    }

    /** Procesa el pago. Retorna true si fue exitoso. */
    public abstract boolean procesarPago();

    /** Descripcion legible del medio para informes. */
    public abstract String obtenerDescripcion();

    public int getIdMedioPago()  { return idMedioPago; }
    public double getImporte()   { return importe; }
    public Date getFecha()       { return fecha; }

    @Override
    public String toString() {
        return obtenerDescripcion() + " $" + importe;
    }
}

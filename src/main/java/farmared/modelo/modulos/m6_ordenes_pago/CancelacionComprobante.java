package farmared.modelo.modulos.m6_ordenes_pago;

import farmared.modelo.modulos.m5_comprobantes.Comprobante;

/**
 * Registro de la cancelacion (pago) de un comprobante en una Orden de Pago (RF-18).
 * Llamado en DS4 pasos 12-15: getComprobante() y getImporteCancelado().
 */
public class CancelacionComprobante {

    private Comprobante comprobante;
    private double importeCancelado;
    private boolean esCancelacionTotal;

    public CancelacionComprobante(Comprobante comprobante,
                                   double importeCancelado, boolean esCancelacionTotal) {
        this.comprobante = comprobante;
        this.importeCancelado = importeCancelado;
        this.esCancelacionTotal = esCancelacionTotal;
    }

    public Comprobante getComprobante()      { return comprobante; }
    public double getImporteCancelado()      { return importeCancelado; }
    public boolean isEsCancelacionTotal()    { return esCancelacionTotal; }

    @Override
    public String toString() {
        return "Cancelacion{comp=" + comprobante.getNumero()
                + ", importe=$" + importeCancelado + ", total=" + esCancelacionTotal + "}";
    }
}

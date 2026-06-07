package farmared.modulos.m5_comprobantes;

import farmared.enums.EstadoComprobante;
import farmared.enums.TipoComprobante;
import farmared.modulos.m2_proveedores.Proveedor;
import java.util.Date;

/**
 * Nota de Credito del proveedor (RF-14).
 * REDUCE la deuda → afectarCuentaCorriente() devuelve valor negativo.
 */
public class NotaCredito extends Comprobante {

    public NotaCredito(String numero, Date fechaEmision, Date fechaRecepcion, Proveedor proveedor) {
        super(numero, TipoComprobante.NOTA_CREDITO, fechaEmision, fechaRecepcion, proveedor);
    }

    @Override
    public double afectarCuentaCorriente() {
        if (estado == EstadoComprobante.ANULADO) return 0.0;
        return -saldoPendiente;   // negativo
    }

    @Override
    public boolean esSumaDeuda() { return false; }
}

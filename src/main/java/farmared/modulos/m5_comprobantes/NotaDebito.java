package farmared.modulos.m5_comprobantes;

import farmared.enums.EstadoComprobante;
import farmared.enums.TipoComprobante;
import farmared.modulos.m1_proveedores.Proveedor;
import java.util.Date;

/**
 * Nota de Debito del proveedor (RF-14).
 * AUMENTA la deuda → afectarCuentaCorriente() devuelve valor positivo.
 */
public class NotaDebito extends Comprobante {

    public NotaDebito(String numero, Date fechaEmision, Date fechaRecepcion, Proveedor proveedor) {
        super(numero, TipoComprobante.NOTA_DEBITO, fechaEmision, fechaRecepcion, proveedor);
    }

    @Override
    public double afectarCuentaCorriente() {
        if (estado == EstadoComprobante.ANULADO
                || estado == EstadoComprobante.PAGADO_TOTAL) return 0.0;
        return saldoPendiente;   // positivo
    }

    @Override
    public boolean esSumaDeuda() { return true; }
}

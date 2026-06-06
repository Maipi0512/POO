package farmared.enums;

/**
 * Estado de pago de un Comprobante en la cuenta corriente del proveedor.
 * PENDIENTE → todavia no fue pagado.
 * PAGADO_PARCIAL → fue cancelado parcialmente por una o mas ordenes de pago.
 * PAGADO_TOTAL → saldo cero, no genera deuda vigente.
 * ANULADO → dado de baja, no afecta la cuenta corriente.
 */
public enum EstadoComprobante {
    PENDIENTE,
    PAGADO_PARCIAL,
    PAGADO_TOTAL,
    ANULADO
}

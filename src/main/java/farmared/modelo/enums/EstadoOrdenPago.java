package farmared.modelo.enums;

/**
 * Ciclo de vida de una Orden de Pago.
 * GENERADA → calculada pero pendiente de confirmacion de medios de pago.
 * EMITIDA → confirmada con medios de pago, actualiza saldo de comprobantes.
 * ANULADA → cancelada.
 */
public enum EstadoOrdenPago {
    GENERADA,
    EMITIDA,
    ANULADA
}

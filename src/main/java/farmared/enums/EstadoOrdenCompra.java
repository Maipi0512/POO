package farmared.enums;

/**
 * Ciclo de vida de una Orden de Compra.
 * EMITIDA → puede recibir facturas.
 * PARCIALMENTE_FACTURADA → al menos una factura asociada, pero no cubre el total.
 * FACTURADA → todos los items fueron facturados.
 * AUTORIZADA → emitida con autorizacion de supervisor (supero tope de deuda).
 * ANULADA → cancelada, no genera deuda.
 */
public enum EstadoOrdenCompra {
    EMITIDA,
    AUTORIZADA,
    PARCIALMENTE_FACTURADA,
    FACTURADA,
    ANULADA
}

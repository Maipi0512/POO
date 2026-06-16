package farmared.modelo.enums;

/**
 * Tipo legal del comprobante fiscal recibido del proveedor.
 * Factura A/B/C depende de la condicion IVA del emisor y receptor.
 */
public enum TipoComprobante {
    FACTURA_A,
    FACTURA_B,
    FACTURA_C,
    NOTA_CREDITO,
    NOTA_DEBITO
}

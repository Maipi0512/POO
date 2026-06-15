package farmared.modelo.enums;

/**
 * Tipos de impuesto que el sistema puede retener al pagar a un proveedor.
 * Cada tipo tiene su propia logica de calculo (ver jerarquia Impuesto).
 */
public enum TipoImpuesto {
    IVA,
    INGRESOS_BRUTOS,
    GANANCIAS
}

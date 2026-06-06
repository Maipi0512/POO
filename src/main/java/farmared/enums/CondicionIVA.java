package farmared.enums;

/**
 * Condicion tributaria del proveedor ante el fisco.
 * Determina como se calculan los impuestos en las transacciones.
 */
public enum CondicionIVA {
    RESPONSABLE_INSCRIPTO,
    MONOTRIBUTISTA,
    EXENTO,
    CONSUMIDOR_FINAL
}

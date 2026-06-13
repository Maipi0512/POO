package farmared.enums;

/**
 * Alicuota de IVA aplicable a un producto o servicio.
 * Cada valor lleva su porcentaje para usarlo directamente en calculos.
 */
public enum TipoIVA {
    IVA_21(21.0),
    IVA_10_5(10.5),
    IVA_27(27.0),
    EXENTO(0.0);

    // Porcentaje numerico asociado al tipo (ej: 21.0, 10.5, 0.0)
    private final double porcentaje;

    TipoIVA(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public double getPorcentaje() {
        return porcentaje;
    }
}

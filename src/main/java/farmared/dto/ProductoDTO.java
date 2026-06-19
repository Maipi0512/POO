package farmared.dto;

public final class ProductoDTO {
    private final String codigoInterno;
    private final String descripcion;
    private final String unidadMedida;
    private final String tipoIVA;
    private final RubroDTO rubro;
    private final boolean activo;

    public ProductoDTO(String codigoInterno, String descripcion, String unidadMedida,
                       String tipoIVA, RubroDTO rubro, boolean activo) {
        this.codigoInterno = codigoInterno;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.tipoIVA = tipoIVA;
        this.rubro = rubro;
        this.activo = activo;
    }

    public String getCodigoInterno() { return codigoInterno; }
    public String getDescripcion() { return descripcion; }
    public String getUnidadMedida() { return unidadMedida; }
    public String getTipoIVA() { return tipoIVA; }
    public RubroDTO getRubro() { return rubro; }
    public boolean isActivo() { return activo; }

    @Override
    public String toString() { return codigoInterno + " - " + descripcion; }
}

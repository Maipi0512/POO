package farmared.dto;

import java.util.List;

public final class ProveedorDTO {
    private final String cuit;
    private final String razonSocial;
    private final String nombreFantasia;
    private final String domicilioComercial;
    private final String telefono;
    private final String email;
    private final String condicionIVA;
    private final String numeroIngresosBrutos;
    private final double topeMaximoDeuda;
    private final double deudaVigente;
    private final boolean activo;
    private final List<RubroDTO> rubros;

    public ProveedorDTO(String cuit, String razonSocial, String nombreFantasia,
                        String domicilioComercial, String telefono, String email,
                        String condicionIVA, String numeroIngresosBrutos,
                        double topeMaximoDeuda, double deudaVigente, boolean activo,
                        List<RubroDTO> rubros) {
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.nombreFantasia = nombreFantasia;
        this.domicilioComercial = domicilioComercial;
        this.telefono = telefono;
        this.email = email;
        this.condicionIVA = condicionIVA;
        this.numeroIngresosBrutos = numeroIngresosBrutos;
        this.topeMaximoDeuda = topeMaximoDeuda;
        this.deudaVigente = deudaVigente;
        this.activo = activo;
        this.rubros = rubros != null ? List.copyOf(rubros) : List.of();
    }

    public String getCuit() { return cuit; }
    public String getRazonSocial() { return razonSocial; }
    public String getNombreFantasia() { return nombreFantasia; }
    public String getDomicilioComercial() { return domicilioComercial; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public String getCondicionIVA() { return condicionIVA; }
    public String getNumeroIngresosBrutos() { return numeroIngresosBrutos; }
    public double getTopeMaximoDeuda() { return topeMaximoDeuda; }
    public double getDeudaVigente() { return deudaVigente; }
    public boolean isActivo() { return activo; }
    public List<RubroDTO> getRubros() { return rubros; }

    @Override
    public String toString() { return razonSocial + " (" + cuit + ")"; }
}

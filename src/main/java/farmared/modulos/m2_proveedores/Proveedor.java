package farmared.modulos.m2_proveedores;

import farmared.enums.CondicionIVA;
import farmared.enums.TipoImpuesto;
import farmared.modulos.m5_comprobantes.Comprobante;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Proveedor de bienes o servicios (RF-01 a RF-04).
 *
 * Responsabilidades clave:
 *   - Mantener la cuenta corriente (lista de Comprobantes)
 *   - Calcular la deuda vigente mediante polimorfismo (afectarCuentaCorriente)
 *   - Validar si una nueva OC supera el tope de deuda (RF-12)
 *   - Gestionar certificados de no retencion (RF-09)
 */
public class Proveedor {

    private String cuit;
    private String razonSocial;
    private String nombreFantasia;
    private String domicilioComercial;
    private String telefono;
    private String email;
    private CondicionIVA condicionIVA;
    private String numeroIngresosBrutos;
    private Date fechaInicioActividades;
    private double topeMaximoDeuda;
    private boolean activo;

    private List<Rubro> rubros;
    private List<Comprobante> comprobantes;
    private List<CertificadoNoRetencion> certificados;

    public Proveedor(String cuit, String razonSocial, String nombreFantasia,
                     String domicilioComercial, String telefono, String email,
                     CondicionIVA condicionIVA, String numeroIngresosBrutos,
                     Date fechaInicioActividades) {
        this.cuit = cuit;
        this.razonSocial = razonSocial;
        this.nombreFantasia = nombreFantasia;
        this.domicilioComercial = domicilioComercial;
        this.telefono = telefono;
        this.email = email;
        this.condicionIVA = condicionIVA;
        this.numeroIngresosBrutos = numeroIngresosBrutos;
        this.fechaInicioActividades = fechaInicioActividades;
        this.topeMaximoDeuda = 0.0;
        this.activo = true;
        this.rubros = new ArrayList<>();
        this.comprobantes = new ArrayList<>();
        this.certificados = new ArrayList<>();
    }

    // =========================================================================
    // CUENTA CORRIENTE (RF-21, DS4)
    // =========================================================================

    /**
     * DS4 paso 4-8: loop afectarCuentaCorriente() → calcularDeudaVigente() → totalDeudaVigente.
     * Facturas/ND suman, NotasCredito restan.
     */
    public double obtenerCuentaCorriente() {
        double acumulado = 0.0;
        for (Comprobante c : comprobantes) {
            acumulado += c.afectarCuentaCorriente();
        }
        return calcularDeudaVigente(acumulado);
    }

    /** DS4 paso 7: self-call para normalizar el total acumulado. */
    private double calcularDeudaVigente(double suma) {
        return Math.round(suma * 100.0) / 100.0;
    }

    public void agregarComprobante(Comprobante comprobante) {
        comprobantes.add(comprobante);
    }

    // =========================================================================
    // CONTROL DE TOPE DE DEUDA (RF-04, RF-12)
    // =========================================================================

    /**
     * Valida si la nueva OC supera el tope de deuda (RF-12).
     * Regla: Total OC + Deuda Vigente <= Tope Maximo
     */
    public boolean validarNuevaOC(double importeTotalOC) {
        if (topeMaximoDeuda <= 0) return true;
        double deudaActual = obtenerCuentaCorriente();
        return (importeTotalOC + deudaActual) <= topeMaximoDeuda;
    }

    // =========================================================================
    // CERTIFICADOS DE NO RETENCION (RF-09)
    // =========================================================================

    public void agregarCertificado(CertificadoNoRetencion cert) {
        certificados.add(cert);
    }

    /**
     * Retorna true si hay un certificado vigente hoy para el tipo de impuesto dado.
     * Llamado en DS2 antes de calcular cada retencion.
     */
    public boolean tieneCertificadoVigente(TipoImpuesto tipo) {
        Date hoy = new Date();
        for (CertificadoNoRetencion cert : certificados) {
            if (cert.getTipoImpuesto() == tipo && cert.estaVigente(hoy)) return true;
        }
        return false;
    }

    // =========================================================================
    // RUBROS (RF-02)
    // =========================================================================

    public void agregarRubro(Rubro rubro) {
        if (!rubros.contains(rubro)) rubros.add(rubro);
    }

    public void quitarRubro(Rubro rubro) { rubros.remove(rubro); }

    // =========================================================================
    // GETTERS Y SETTERS
    // =========================================================================

    public String getCuit()                         { return cuit; }
    public String getRazonSocial()                  { return razonSocial; }
    public String getNombreFantasia()               { return nombreFantasia; }
    public String getDomicilioComercial()           { return domicilioComercial; }
    public String getTelefono()                     { return telefono; }
    public String getEmail()                        { return email; }
    public CondicionIVA getCondicionIVA()           { return condicionIVA; }
    public String getNumeroIngresosBrutos()         { return numeroIngresosBrutos; }
    public Date getFechaInicioActividades()         { return fechaInicioActividades; }
    public double getTopeMaximoDeuda()              { return topeMaximoDeuda; }
    public boolean isActivo()                       { return activo; }
    public List<Rubro> getRubros()                  { return new ArrayList<>(rubros); }
    public List<Comprobante> getComprobantes()      { return new ArrayList<>(comprobantes); }
    public List<CertificadoNoRetencion> getCertificados() { return new ArrayList<>(certificados); }

    public void setRazonSocial(String v)        { razonSocial = v; }
    public void setNombreFantasia(String v)     { nombreFantasia = v; }
    public void setDomicilioComercial(String v) { domicilioComercial = v; }
    public void setTelefono(String v)           { telefono = v; }
    public void setEmail(String v)              { email = v; }
    public void setCondicionIVA(CondicionIVA v) { condicionIVA = v; }
    public void setTopeMaximoDeuda(double v)    { topeMaximoDeuda = v; }
    public void setActivo(boolean v)            { activo = v; }

    @Override
    public String toString() {
        return "Proveedor{CUIT=" + cuit + ", razonSocial='" + razonSocial + "'}";
    }
}

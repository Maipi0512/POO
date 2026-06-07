package farmared.modulos.m1_usuarios;

import java.util.Date;

/**
 * Registro de una autorizacion otorgada por un Supervisor (RF-29).
 * Se genera cuando una operacion supera un limite del sistema:
 *   - OC que excede el tope de deuda del proveedor (RF-12)
 *   - Comprobante sin OC previa o con desvio de precio (RF-17)
 */
public class Autorizacion {

    private int idAutorizacion;
    private Usuario supervisor;
    private Date fechaHora;
    private String motivo;
    private String observaciones;

    public Autorizacion(int idAutorizacion, Usuario supervisor, String motivo) {
        if (!supervisor.esAutorizador()) {
            throw new IllegalArgumentException(
                "Solo un SUPERVISOR puede otorgar autorizaciones."
            );
        }
        this.idAutorizacion = idAutorizacion;
        this.supervisor = supervisor;
        this.motivo = motivo;
        this.fechaHora = new Date();
        this.observaciones = "";
    }

    public int getIdAutorizacion()     { return idAutorizacion; }
    public Usuario getSupervisor()     { return supervisor; }
    public Date getFechaHora()         { return fechaHora; }
    public String getMotivo()          { return motivo; }
    public String getObservaciones()   { return observaciones; }

    public void setObservaciones(String obs) { this.observaciones = obs; }

    @Override
    public String toString() {
        return "Autorizacion{#" + idAutorizacion
                + ", supervisor=" + supervisor.getNombre()
                + ", motivo='" + motivo + "'}";
    }
}

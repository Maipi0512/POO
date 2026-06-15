package farmared.modelo.modulos.m6_ordenes_pago;

import farmared.modelo.enums.EstadoOrdenPago;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m3_impuestos.Retencion;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orden de Pago para cancelar comprobantes de un proveedor (RF-18, RF-19, RF-20).
 *
 * Flujo DS2:
 *   new OrdenPago → agregarCancelacion() x N → agregarRetencion() x N
 *   → generar() [calcula neto] → procesarPago() → cerrarOrden()
 */
public class OrdenPago {

    private String numero;
    private Proveedor proveedor;
    private Date fechaEmision;
    private double importeBruto;
    private double totalRetenciones;
    private double importeNeto;
    private EstadoOrdenPago estado;

    private List<CancelacionComprobante> cancelaciones;
    private List<Retencion> retenciones;
    private List<MedioPago> mediosPago;

    public OrdenPago(String numero, Proveedor proveedor, Date fechaEmision) {
        this.numero = numero;
        this.proveedor = proveedor;
        this.fechaEmision = fechaEmision;
        this.estado = EstadoOrdenPago.GENERADA;
        this.cancelaciones = new ArrayList<>();
        this.retenciones = new ArrayList<>();
        this.mediosPago = new ArrayList<>();
    }

    public void agregarCancelacion(CancelacionComprobante c) {
        cancelaciones.add(c);
        importeBruto = Math.round((importeBruto + c.getImporteCancelado()) * 100.0) / 100.0;
    }

    public void agregarRetencion(Retencion r) {
        retenciones.add(r);
        totalRetenciones = Math.round((totalRetenciones + r.getImporte()) * 100.0) / 100.0;
    }

    /** Calcula el importe neto y pasa al estado GENERADA (DS2, paso generar). */
    public void generar() {
        importeNeto = Math.round((importeBruto - totalRetenciones) * 100.0) / 100.0;
        estado = EstadoOrdenPago.GENERADA;
    }

    /**
     * DS2: registra los medios de pago y valida que su suma cubra el importeNeto.
     * El controller ya llamo mp.procesarPago() en su loop antes de invocar este metodo.
     */
    public boolean procesarPago(List<MedioPago> lista) {
        double total = 0.0;
        for (MedioPago mp : lista) {
            mediosPago.add(mp);
            total += mp.getImporte();
        }
        return Math.abs(Math.round(total * 100.0) / 100.0 - importeNeto) < 0.01;
    }

    /** Cierra la OP y registra el pago en cada comprobante cancelado (DS2). */
    public void cerrarOrden() {
        estado = EstadoOrdenPago.EMITIDA;
        for (CancelacionComprobante c : cancelaciones) {
            c.getComprobante().registrarPago(c.getImporteCancelado());
        }
    }

    public void anular() { estado = EstadoOrdenPago.ANULADA; }

    /** Retorna las cancelaciones (DS4, paso obtenerCancelaciones). */
    public List<CancelacionComprobante> obtenerCancelaciones() {
        return new ArrayList<>(cancelaciones);
    }

    public String getNumero()               { return numero; }
    public Proveedor getProveedor()         { return proveedor; }
    public Date getFechaEmision()           { return fechaEmision; }
    public double getImporteBruto()         { return importeBruto; }
    public double getTotalRetenciones()     { return totalRetenciones; }
    public double getImporteNeto()          { return importeNeto; }
    public EstadoOrdenPago getEstado()      { return estado; }
    public List<Retencion> getRetenciones() { return new ArrayList<>(retenciones); }
    public List<MedioPago> getMediosPago()  { return new ArrayList<>(mediosPago); }

    @Override
    public String toString() {
        return "OP{" + numero + ", prov=" + proveedor.getRazonSocial()
                + ", neto=$" + importeNeto + ", estado=" + estado + "}";
    }
}

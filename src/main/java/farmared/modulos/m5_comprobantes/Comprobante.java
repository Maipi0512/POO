package farmared.modulos.m5_comprobantes;

import farmared.enums.EstadoComprobante;
import farmared.enums.TipoComprobante;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m1_usuarios.Autorizacion;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase abstracta base para documentos fiscales del proveedor (RF-14).
 *
 * Jerarquia:
 *   Comprobante
 *     ├── Factura      → afectarCuentaCorriente() POSITIVO (suma deuda)
 *     ├── NotaCredito  → afectarCuentaCorriente() NEGATIVO (reduce deuda)
 *     └── NotaDebito   → afectarCuentaCorriente() POSITIVO (suma deuda)
 *
 * afectarCuentaCorriente() es el metodo polimorfico que usa Proveedor (DS4).
 */
public abstract class Comprobante {

    protected String numero;
    protected TipoComprobante tipo;
    protected Date fechaEmision;
    protected Date fechaRecepcion;
    protected double importeNeto;
    protected double importeIVA;
    protected double importeTotal;
    protected double saldoPendiente;
    protected EstadoComprobante estado;
    protected Proveedor proveedor;
    protected List<DetalleComprobante> detalles;
    protected Autorizacion autorizacion;

    public Comprobante(String numero, TipoComprobante tipo,
                       Date fechaEmision, Date fechaRecepcion, Proveedor proveedor) {
        this.numero = numero;
        this.tipo = tipo;
        this.fechaEmision = fechaEmision;
        this.fechaRecepcion = fechaRecepcion;
        this.proveedor = proveedor;
        this.estado = EstadoComprobante.PENDIENTE;
        this.detalles = new ArrayList<>();
    }

    // =========================================================================
    // METODOS POLIMORFICOS
    // =========================================================================

    /**
     * Retorna el delta de deuda que aporta este comprobante.
     * Positivo = suma deuda, Negativo = reduce deuda, 0 = no afecta.
     */
    public abstract double afectarCuentaCorriente();

    /** true si este comprobante suma deuda (Factura, ND); false si la reduce (NC). */
    public abstract boolean esSumaDeuda();

    // =========================================================================
    // GESTION DE DETALLES
    // =========================================================================

    public void agregarDetalle(DetalleComprobante detalle) {
        detalles.add(detalle);
        recalcularTotales();
    }

    private void recalcularTotales() {
        importeNeto = 0.0;
        importeIVA  = 0.0;
        for (DetalleComprobante d : detalles) {
            importeNeto += d.getSubtotal();
            importeIVA  += d.getImporteIVA();
        }
        importeTotal   = Math.round((importeNeto + importeIVA) * 100.0) / 100.0;
        saldoPendiente = importeTotal;
    }

    /**
     * Registra un pago parcial o total sobre este comprobante.
     * Llamado desde OrdenPago.cerrarOrden() (DS2).
     */
    public void registrarPago(double montoCancelado) {
        saldoPendiente = Math.max(0.0,
            Math.round((saldoPendiente - montoCancelado) * 100.0) / 100.0);
        if (saldoPendiente == 0.0)        estado = EstadoComprobante.PAGADO_TOTAL;
        else if (montoCancelado > 0)      estado = EstadoComprobante.PAGADO_PARCIAL;
    }

    public void anular() {
        estado = EstadoComprobante.ANULADO;
        saldoPendiente = 0.0;
    }

    public List<DetalleComprobante> getDetalles() { return new ArrayList<>(detalles); }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getNumero()               { return numero; }
    public TipoComprobante getTipo()        { return tipo; }
    public Date getFechaEmision()           { return fechaEmision; }
    public Date getFechaRecepcion()         { return fechaRecepcion; }
    public double getImporteNeto()          { return importeNeto; }
    public double getImporteIVA()           { return importeIVA; }
    public double getImporteTotal()         { return importeTotal; }
    public double getSaldoPendiente()       { return saldoPendiente; }
    public EstadoComprobante getEstado()    { return estado; }
    public Proveedor getProveedor()         { return proveedor; }
    public Autorizacion getAutorizacion()   { return autorizacion; }

    public void setAutorizacion(Autorizacion a) { autorizacion = a; }

    @Override
    public String toString() {
        return tipo + "{" + numero + ", $" + importeTotal + ", estado=" + estado + "}";
    }
}

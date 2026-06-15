package farmared.modelo.modulos.m4_ordenes_compra;

import farmared.modelo.enums.EstadoOrdenCompra;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m8_usuarios.Autorizacion;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Orden de Compra emitida para un proveedor (RF-11, RF-12, RF-13).
 *
 * Flujo DS1:
 *   new OrdenCompra → agregarDetalle() x N → calcularTotal()
 *   → proveedor.validarNuevaOC(importeTotal) → emitir() o emitirConAutorizacion()
 */
public class OrdenCompra {

    private String numero;
    private Date fechaEmision;
    private Proveedor proveedor;
    private double importeTotal;
    private EstadoOrdenCompra estado;
    private List<DetalleOC> detalles;
    private Autorizacion autorizacion;

    public OrdenCompra(String numero, Proveedor proveedor) {
        this.numero = numero;
        this.proveedor = proveedor;
        this.fechaEmision = new Date();
        this.estado = EstadoOrdenCompra.EMITIDA;
        this.detalles = new ArrayList<>();
    }

    public void agregarDetalle(DetalleOC detalle) {
        detalles.add(detalle);
        calcularTotal();
    }

    /** Suma los subtotales de todos los detalles (DS1, paso calcularTotal). */
    public double calcularTotal() {
        importeTotal = 0.0;
        for (DetalleOC d : detalles) importeTotal += d.getSubtotal();
        importeTotal = Math.round(importeTotal * 100.0) / 100.0;
        return importeTotal;
    }

    /** DS1: Proveedor.validarNuevaOC — true si la OC no supera el tope de deuda. */
    public boolean validarTopeDeuda() {
        return proveedor.validarNuevaOC(importeTotal);
    }

    public void setEstado(EstadoOrdenCompra nuevoEstado) { estado = nuevoEstado; }

    public void emitir() { estado = EstadoOrdenCompra.EMITIDA; }

    public void emitirConAutorizacion(Autorizacion auth) {
        autorizacion = auth;
        estado = EstadoOrdenCompra.AUTORIZADA;
    }

    public void anular() {
        if (estado == EstadoOrdenCompra.FACTURADA)
            throw new IllegalStateException("No se puede anular una OC ya facturada.");
        estado = EstadoOrdenCompra.ANULADA;
    }

    public List<DetalleOC> getDetalles()    { return new ArrayList<>(detalles); }
    public String getNumero()               { return numero; }
    public Date getFechaEmision()           { return fechaEmision; }
    public Proveedor getProveedor()         { return proveedor; }
    public double getImporteTotal()         { return importeTotal; }
    public EstadoOrdenCompra getEstado()    { return estado; }
    public Autorizacion getAutorizacion()   { return autorizacion; }

    @Override
    public String toString() {
        return "OC{" + numero + ", prov=" + proveedor.getRazonSocial()
                + ", total=$" + importeTotal + ", estado=" + estado + "}";
    }
}

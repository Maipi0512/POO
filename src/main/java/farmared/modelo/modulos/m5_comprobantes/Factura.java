package farmared.modelo.modulos.m5_comprobantes;

import farmared.modelo.enums.EstadoComprobante;
import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Factura recibida del proveedor (RF-14, RF-15, RF-16, RF-17).
 * AUMENTA la deuda → afectarCuentaCorriente() devuelve valor positivo.
 * Puede asociarse a una o varias OC (RF-15).
 */
public class Factura extends Comprobante {

    private List<OrdenCompra> ordenesCompraAsociadas;
    private boolean tieneDesvio;

    public Factura(String numero, TipoComprobante tipo,
                   Date fechaEmision, Date fechaRecepcion, Proveedor proveedor) {
        super(numero, tipo, fechaEmision, fechaRecepcion, proveedor);
        this.ordenesCompraAsociadas = new ArrayList<>();
        this.tieneDesvio = false;
    }

    @Override
    public double afectarCuentaCorriente() {
        if (estado == EstadoComprobante.ANULADO
                || estado == EstadoComprobante.PAGADO_TOTAL) return 0.0;
        return saldoPendiente;   // positivo
    }

    @Override
    public boolean esSumaDeuda() { return true; }

    public void asociarOrdenCompra(OrdenCompra oc) {
        if (!ordenesCompraAsociadas.contains(oc)) ordenesCompraAsociadas.add(oc);
    }

    public List<OrdenCompra> getOrdenesCompraAsociadas() {
        return new ArrayList<>(ordenesCompraAsociadas);
    }

    public boolean isTieneDesvio()              { return tieneDesvio; }
    public void setTieneDesvio(boolean desvio)  { tieneDesvio = desvio; }
}

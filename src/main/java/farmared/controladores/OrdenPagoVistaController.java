package farmared.controladores;

import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m6_ordenes_pago.MedioPago;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** Controlador de vista para ordenes de pago — delega a OrdenPagoController. */
public class OrdenPagoVistaController {

    public List<Comprobante> listarImpagos(String cuit) {
        return OrdenPagoController.getInstance().iniciarOrdenPago(cuit);
    }

    public OrdenPago preparar(String cuit, Map<Comprobante, Double> seleccion) {
        return OrdenPagoController.getInstance().seleccionarComprobantes(cuit, seleccion, new Date());
    }

    public OrdenPago confirmar(OrdenPago op, List<MedioPago> medios) {
        return OrdenPagoController.getInstance().confirmarPago(op, medios);
    }

    public List<OrdenPago> listarEmitidas() {
        return OrdenPagoController.getInstance().getOrdenesEmitidas();
    }
}

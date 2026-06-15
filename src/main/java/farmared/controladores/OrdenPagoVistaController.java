package farmared.controladores;

import farmared.modelo.SistemaGestionCompras;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m6_ordenes_pago.MedioPago;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;

import java.util.Date;
import java.util.List;
import java.util.Map;

/** Controlador MVC del modulo de ordenes de pago. */
public class OrdenPagoVistaController {

    private final SistemaGestionCompras sistema;

    public OrdenPagoVistaController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public List<Comprobante> listarImpagos(String cuit) {
        return sistema.iniciarOrdenPago(cuit);
    }

    public OrdenPago preparar(String cuit, Map<Comprobante, Double> seleccion) {
        return sistema.prepararOrdenPago(cuit, seleccion, new Date());
    }

    public OrdenPago confirmar(OrdenPago op, List<MedioPago> medios) {
        return sistema.confirmarOrdenPago(op, medios);
    }

    public List<OrdenPago> listarEmitidas() {
        return sistema.reporteOrdenesPago();
    }
}

package farmared.controladores;

import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.modelo.SistemaGestionCompras;

import java.util.Date;
import java.util.List;

/** Controlador MVC del modulo de comprobantes. */
public class ComprobanteController {

    private final SistemaGestionCompras sistema;

    public ComprobanteController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public List<Comprobante> listar() {
        return sistema.getComprobantes();
    }

    public OrdenCompra buscarOC(String numero) {
        return sistema.buscarOrdenCompraPorNumero(numero);
    }

    public Comprobante registrar(String cuit, TipoComprobante tipo, List<DetalleComprobante> detalles,
                                  List<String> ocs, Usuario supervisor, String motivo) {
        Date hoy = new Date();

        if (tipo == TipoComprobante.NOTA_CREDITO) {
            return sistema.registrarNotaCredito(null, hoy, hoy, detalles, cuit);
        }
        if (tipo == TipoComprobante.NOTA_DEBITO) {
            return sistema.registrarNotaDebito(null, hoy, hoy, detalles, cuit);
        }

        boolean esFactura = tipo == TipoComprobante.FACTURA_A
                || tipo == TipoComprobante.FACTURA_B || tipo == TipoComprobante.FACTURA_C;

        if (esFactura && !ocs.isEmpty()) {
            OrdenCompra oc = buscarOC(ocs.get(0));
            if (oc != null) {
                if (!sistema.validarProductos(detalles, oc.getDetalles())) {
                    throw new IllegalStateException("Los productos no coinciden con la OC.");
                }
                if (!sistema.validarImpuestos(detalles)) {
                    throw new IllegalStateException("Los impuestos no son consistentes con el catalogo.");
                }
            }
        }

        return sistema.registrarFactura(null, tipo, hoy, hoy, detalles, cuit, ocs, supervisor, motivo);
    }

    public boolean requiereSupervisor(String cuit, List<DetalleComprobante> detalles,
                                       List<String> ocs) {
        if (ocs.isEmpty()) return false;
        OrdenCompra oc = buscarOC(ocs.get(0));
        if (oc == null) return false;
        return !sistema.validarPrecios(detalles, oc.getDetalles())
                || !sistema.validarImpuestos(detalles);
    }

    public List<Usuario> listarSupervisores() {
        return sistema.listarSupervisores();
    }
}

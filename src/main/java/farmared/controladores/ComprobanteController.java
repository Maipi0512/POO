package farmared.controladores;

import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m8_usuarios.Usuario;

import java.util.List;

/** Controlador del modulo de comprobantes — delega a FacturaController. */
public class ComprobanteController {

    public List<Comprobante> listar() {
        return FacturaController.getInstance().listar();
    }

    public OrdenCompra buscarOC(String numero) {
        return OrdenCompraController.getInstance().buscarOrdenCompraPorNumero(numero);
    }

    public Comprobante registrar(String cuit, TipoComprobante tipo,
                                  List<DetalleComprobante> detalles,
                                  List<String> ocs, Usuario supervisor, String motivo) {
        return FacturaController.getInstance().registrar(cuit, tipo, detalles, ocs, supervisor, motivo);
    }

    public boolean requiereSupervisor(String cuit, List<DetalleComprobante> detalles,
                                       List<String> ocs) {
        return FacturaController.getInstance().requiereSupervisor(cuit, detalles, ocs);
    }

    public List<Usuario> listarSupervisores() {
        return FacturaController.getInstance().listarSupervisores();
    }
}

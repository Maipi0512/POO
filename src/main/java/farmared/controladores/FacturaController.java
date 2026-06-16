package farmared.controladores;

import farmared.modelo.enums.EstadoComprobante;
import farmared.modelo.enums.EstadoOrdenCompra;
import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.modulos.m8_usuarios.Autorizacion;
import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m5_comprobantes.Factura;
import farmared.modelo.modulos.m5_comprobantes.NotaCredito;
import farmared.modelo.modulos.m5_comprobantes.NotaDebito;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DS3 — Registrar Comprobante (Factura / Nota Credito / Nota Debito).
 * Controlador singleton del modulo 5 (RF-14 a RF-17).
 */
public class FacturaController {

    private static FacturaController instancia;

    private final List<Proveedor>   proveedores;
    private final List<OrdenCompra> ordenesCompra;
    private final List<Comprobante> comprobantes;
    private final List<Usuario>     usuarios;
    private int contadorComprobante  = 1;
    private int contadorAutorizacion = 1;

    private FacturaController(List<Proveedor> proveedores, List<OrdenCompra> ordenesCompra,
                               List<Comprobante> comprobantes, List<Usuario> usuarios) {
        this.proveedores   = proveedores;
        this.ordenesCompra = ordenesCompra;
        this.comprobantes  = comprobantes;
        this.usuarios      = usuarios;
    }

    public static void inicializar(List<Proveedor> proveedores, List<OrdenCompra> ordenesCompra,
                                    List<Comprobante> comprobantes, List<Usuario> usuarios) {
        instancia = new FacturaController(proveedores, ordenesCompra, comprobantes, usuarios);
    }

    public static FacturaController getInstance() {
        if (instancia == null) throw new IllegalStateException("FacturaController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS3 — registro de comprobantes
    // =========================================================================

    /** Registro genérico: despacha según tipo. */
    public Comprobante registrar(String cuit, TipoComprobante tipo,
                                  List<DetalleComprobante> detalles,
                                  List<String> ocs, Usuario supervisor, String motivo) {
        Date hoy = new Date();
        if (tipo == TipoComprobante.NOTA_CREDITO)
            return registrarNotaCredito(null, hoy, hoy, detalles, cuit);
        if (tipo == TipoComprobante.NOTA_DEBITO)
            return registrarNotaDebito(null, hoy, hoy, detalles, cuit);
        return registrarFactura(null, tipo, hoy, hoy, detalles, cuit, ocs, supervisor, motivo);
    }

    public Factura registrarFactura(String numero, TipoComprobante tipo,
                                     Date fechaEmision, Date fechaRecepcion,
                                     List<DetalleComprobante> datos,
                                     String cuitProveedor, List<String> nrosOC,
                                     Usuario supervisor, String motivo) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuitProveedor);

        String nro = numero != null ? numero : "FAC-" + String.format("%08d", contadorComprobante++);
        Factura factura = new Factura(nro, tipo, fechaEmision, fechaRecepcion, prov);
        for (DetalleComprobante d : datos) factura.agregarDetalle(d);

        List<DetalleOC> detallesOC = new ArrayList<>();
        for (String nroOC : nrosOC) {
            OrdenCompra oc = buscarOrdenCompraPorNumero(nroOC);
            if (oc != null) {
                factura.asociarOrdenCompra(oc);
                detallesOC.addAll(oc.getDetalles());
            }
        }

        boolean hayDesvio = false;
        if (!detallesOC.isEmpty()) {
            hayDesvio = !validarProductos(factura.getDetalles(), detallesOC)
                     || !validarPrecios(factura.getDetalles(), detallesOC)
                     || !validarImpuestos(factura.getDetalles());
        }

        boolean sinOC = nrosOC.isEmpty();
        if (hayDesvio || sinOC) {
            if (supervisor == null || !supervisor.esAutorizador())
                throw new IllegalStateException(
                    sinOC ? "Compra directa sin OC requiere autorizacion."
                          : "Desvio de precio requiere autorizacion.");
            factura.setTieneDesvio(hayDesvio);
            String motiAut = motivo != null ? motivo
                    : (sinOC ? "Compra directa sin OC" : "Desvio de precio respecto a OC");
            factura.setAutorizacion(new Autorizacion(contadorAutorizacion++, supervisor, motiAut));
        }

        factura.calcularImpuestos();
        double deltaDeuda = factura.afectarCuentaCorriente();
        prov.agregarComprobante(factura);
        prov.actualizarDeuda(deltaDeuda);
        comprobantes.add(factura);
        actualizarEstadoOrdenesCompra(factura);
        return factura;
    }

    public NotaCredito registrarNotaCredito(String numero, Date fechaEmision, Date fechaRecepcion,
                                             List<DetalleComprobante> detalles, String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        String nro = numero != null ? numero : "NC-" + String.format("%08d", contadorComprobante++);
        NotaCredito nc = new NotaCredito(nro, fechaEmision, fechaRecepcion, prov);
        for (DetalleComprobante d : detalles) nc.agregarDetalle(d);
        prov.agregarComprobante(nc);
        comprobantes.add(nc);
        return nc;
    }

    public NotaDebito registrarNotaDebito(String numero, Date fechaEmision, Date fechaRecepcion,
                                           List<DetalleComprobante> detalles, String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        String nro = numero != null ? numero : "ND-" + String.format("%08d", contadorComprobante++);
        NotaDebito nd = new NotaDebito(nro, fechaEmision, fechaRecepcion, prov);
        for (DetalleComprobante d : detalles) nd.agregarDetalle(d);
        prov.agregarComprobante(nd);
        comprobantes.add(nd);
        return nd;
    }

    // =========================================================================
    // Validaciones RF-16
    // =========================================================================

    /** Retorna true si el comprobante requiere autorización de supervisor. */
    public boolean requiereSupervisor(String cuit, List<DetalleComprobante> detalles, List<String> ocs) {
        if (ocs == null || ocs.isEmpty()) return true;
        OrdenCompra oc = buscarOrdenCompraPorNumero(ocs.get(0));
        if (oc == null) return true;
        return !validarPrecios(detalles, oc.getDetalles()) || !validarImpuestos(detalles);
    }

    public boolean validarProductos(List<DetalleComprobante> factura, List<DetalleOC> oc) {
        for (DetalleComprobante df : factura) {
            boolean ok = false;
            for (DetalleOC doc : oc) {
                if (doc.getProducto().getCodigoInterno().equals(df.getProducto().getCodigoInterno())) {
                    ok = true; break;
                }
            }
            if (!ok) return false;
        }
        return true;
    }

    public boolean validarPrecios(List<DetalleComprobante> factura, List<DetalleOC> oc) {
        for (DetalleComprobante df : factura) {
            for (DetalleOC doc : oc) {
                if (doc.getProducto().getCodigoInterno().equals(df.getProducto().getCodigoInterno())) {
                    double dif = Math.abs(df.getPrecioUnitario() - doc.getPrecioUnitario())
                                 / doc.getPrecioUnitario() * 100;
                    if (dif > 0.01) return false;
                }
            }
        }
        return true;
    }

    public boolean validarImpuestos(List<DetalleComprobante> factura) {
        for (DetalleComprobante d : factura) {
            double alicuotaEsperada = d.getProducto().getTipoIVA().getPorcentaje();
            if (Math.abs(d.getAlicuotaIVA() - alicuotaEsperada) > 0.01) return false;
            double ivaEsperado = Math.round(d.getSubtotal() * (alicuotaEsperada / 100.0) * 100.0) / 100.0;
            if (Math.abs(d.getImporteIVA() - ivaEsperado) > 0.01) return false;
        }
        return true;
    }

    private void actualizarEstadoOrdenesCompra(Factura factura) {
        for (OrdenCompra oc : factura.getOrdenesCompraAsociadas()) {
            if (oc.getEstado() == EstadoOrdenCompra.ANULADA) continue;
            boolean algunaLineaFacturada = false;
            boolean todasCompletas = true;
            for (DetalleOC doc : oc.getDetalles()) {
                double cantFacturada = 0;
                for (DetalleComprobante dc : factura.getDetalles()) {
                    if (dc.getProducto().getCodigoInterno()
                            .equals(doc.getProducto().getCodigoInterno()))
                        cantFacturada += dc.getCantidad();
                }
                if (cantFacturada > 0) algunaLineaFacturada = true;
                if (cantFacturada < doc.getCantidad()) todasCompletas = false;
            }
            if (todasCompletas && algunaLineaFacturada)
                oc.actualizarEstado(EstadoOrdenCompra.FACTURADA);
            else if (algunaLineaFacturada)
                oc.actualizarEstado(EstadoOrdenCompra.PARCIALMENTE_FACTURADA);
        }
    }

    // =========================================================================
    // Búsquedas (loops DS3)
    // =========================================================================

    public Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }

    public OrdenCompra buscarOrdenCompraPorNumero(String numero) {
        for (OrdenCompra oc : ordenesCompra)
            if (oc.getNumero().equals(numero)) return oc;
        return null;
    }

    public List<Comprobante> listar() { return new ArrayList<>(comprobantes); }
    public List<Proveedor> getProveedores() { return new ArrayList<>(proveedores); }

    public List<Usuario> listarSupervisores() {
        List<Usuario> sup = new ArrayList<>();
        for (Usuario u : usuarios) if (u.esAutorizador()) sup.add(u);
        return sup;
    }

    public List<OrdenCompra> listarOCsPorProveedor(String cuit) {
        List<OrdenCompra> resultado = new ArrayList<>();
        for (OrdenCompra oc : ordenesCompra)
            if (oc.getProveedor().getCuit().equals(cuit)) resultado.add(oc);
        return resultado;
    }

    public List<Comprobante> listarPorProveedor(String cuit) {
        List<Comprobante> resultado = new ArrayList<>();
        for (Comprobante c : comprobantes)
            if (c.getProveedor().getCuit().equals(cuit)
                    && c.getEstado() != EstadoComprobante.ANULADO)
                resultado.add(c);
        return resultado;
    }
}

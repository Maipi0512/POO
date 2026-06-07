package farmared.controllers;

import farmared.enums.EstadoComprobante;
import farmared.enums.TipoComprobante;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m4_ordenes_compra.DetalleOC;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modulos.m5_comprobantes.Factura;
import farmared.modulos.m5_comprobantes.NotaCredito;
import farmared.modulos.m5_comprobantes.NotaDebito;
import farmared.modulos.m1_usuarios.Autorizacion;
import farmared.modulos.m1_usuarios.Usuario;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton controlador de Comprobantes (DS3 - Registrar Factura).
 *
 * Flujo DS3:
 *   registrarFactura(datos, idProveedor, nrosOC[])
 *     → buscarProveedorPorId → new Factura → loop DetalleComprobante → getDetalles
 *     → loop buscarOrdenCompraPorNumero → getDetalles → validarProductos → validarPrecios
 *     → calcularImpuestos → afectarCuentaCorriente → deltaDeuda
 */
public class FacturaController {

    private static FacturaController instancia;

    private List<Proveedor>   proveedores;
    private List<Comprobante> comprobantes;
    private List<OrdenCompra> ordenesCompra;

    private int contadorComprobante  = 1;
    private int contadorAutorizacion = 1;

    private FacturaController() {
        proveedores   = new ArrayList<>();
        comprobantes  = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
    }

    public static FacturaController getInstance() {
        if (instancia == null) {
            instancia = new FacturaController();
        }
        return instancia;
    }

    // =========================================================================
    // DS3: registrarFactura(datos, idProveedor, nrosOC[])
    // =========================================================================

    /** DS3 loop "en coleccion de proveedores" (paso 2-3). */
    public Proveedor buscarProveedorPorId(String idProveedor) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(idProveedor)) return p;
        return null;
    }

    /** DS3 loop "en coleccion de ordenes de compra" (paso 12-13). */
    public OrdenCompra buscarOrdenCompraPorNumero(String nroOC) {
        for (OrdenCompra oc : ordenesCompra)
            if (oc.getNumero().equals(nroOC)) return oc;
        return null;
    }

    /**
     * DS3 completo: registrarFactura(datos, idProveedor, nrosOC[]).
     * Pasos 2-23 del diagrama de secuencia DS3.
     */
    public Factura registrarFactura(String numero, TipoComprobante tipo,
                                    Date fechaEmision, Date fechaRecepcion,
                                    List<DetalleComprobante> datos,
                                    String idProveedor, List<String> nrosOC,
                                    Usuario supervisor, String motivo) {
        // DS3 paso 2-3
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null)
            throw new IllegalArgumentException("Proveedor no encontrado: " + idProveedor);

        // DS3 paso 4-5
        String nro = (numero != null) ? numero : "FAC-" + String.format("%08d", contadorComprobante++);
        Factura unaFactura = new Factura(nro, tipo, fechaEmision, fechaRecepcion, unProveedor);

        // DS3 paso 6-9: loop cada item en datos
        for (DetalleComprobante d : datos) {
            unaFactura.agregarDetalle(d);
        }

        // DS3 paso 10
        List<DetalleComprobante> detallesFactura = unaFactura.getDetalles();

        // DS3 paso 11-15: loop por cada nroOC, buscar OC y acumular detallesOCTotales
        List<DetalleOC> detallesOCTotales = new ArrayList<>();
        for (String nroOC : nrosOC) {
            OrdenCompra unaOrdenCompra = buscarOrdenCompraPorNumero(nroOC);
            if (unaOrdenCompra != null) {
                unaFactura.asociarOrdenCompra(unaOrdenCompra);
                detallesOCTotales.addAll(unaOrdenCompra.getDetalles()); // paso 14-15
            }
        }

        // DS3 paso 16-19: validarProductos y validarPrecios
        boolean hayDesvio = false;
        if (!detallesOCTotales.isEmpty()) {
            boolean prodOk = validarProductos(detallesFactura, detallesOCTotales); // 16-17
            boolean precOk = validarPrecios(detallesFactura, detallesOCTotales);   // 18-19
            hayDesvio = !prodOk || !precOk;
        }

        boolean sinOC = nrosOC.isEmpty();
        if (hayDesvio || sinOC) {
            if (supervisor == null || !supervisor.esAutorizador())
                throw new IllegalStateException(sinOC
                    ? "Compra directa sin OC requiere autorizacion de Supervisor."
                    : "Desvio de precio respecto a OC requiere autorizacion de Supervisor.");
            unaFactura.setTieneDesvio(hayDesvio);
            String motiAut = (motivo != null) ? motivo
                : (sinOC ? "Compra directa sin OC" : "Desvio de precio respecto a OC");
            unaFactura.setAutorizacion(new Autorizacion(contadorAutorizacion++, supervisor, motiAut));
        }

        // DS3 paso 20-21: calcularImpuestos (ya ocurrio al agregarDetalle)
        // DS3 paso 22-23: afectarCuentaCorriente → deltaDeuda
        unaFactura.afectarCuentaCorriente();
        unProveedor.agregarComprobante(unaFactura);
        comprobantes.add(unaFactura);
        return unaFactura;
    }

    // =========================================================================
    // DS3 paso 16-17: validarProductos
    // =========================================================================

    public boolean validarProductos(List<DetalleComprobante> detallesFactura, List<DetalleOC> detallesOC) {
        for (DetalleComprobante df : detallesFactura) {
            boolean ok = false;
            for (DetalleOC doc : detallesOC) {
                if (doc.getProducto().getCodigoInterno().equals(df.getProducto().getCodigoInterno())) {
                    ok = true;
                    break;
                }
            }
            if (!ok) return false;
        }
        return true;
    }

    // =========================================================================
    // DS3 paso 18-19: validarPrecios
    // =========================================================================

    public boolean validarPrecios(List<DetalleComprobante> detallesFactura, List<DetalleOC> detallesOC) {
        for (DetalleComprobante df : detallesFactura) {
            for (DetalleOC doc : detallesOC) {
                if (doc.getProducto().getCodigoInterno().equals(df.getProducto().getCodigoInterno())) {
                    double dif = Math.abs(df.getPrecioUnitario() - doc.getPrecioUnitario())
                                 / doc.getPrecioUnitario() * 100.0;
                    if (dif > 0.01) return false;
                }
            }
        }
        return true;
    }

    // =========================================================================
    // Notas de credito y debito
    // =========================================================================

    public NotaCredito registrarNotaCredito(String numero, Date fechaEmision, Date fechaRecepcion,
                                            List<DetalleComprobante> detalles, String idProveedor) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        String nro = (numero != null) ? numero : "NC-" + String.format("%08d", contadorComprobante++);
        NotaCredito nc = new NotaCredito(nro, fechaEmision, fechaRecepcion, unProveedor);
        for (DetalleComprobante d : detalles) nc.agregarDetalle(d);
        unProveedor.agregarComprobante(nc);
        comprobantes.add(nc);
        return nc;
    }

    public NotaDebito registrarNotaDebito(String numero, Date fechaEmision, Date fechaRecepcion,
                                          List<DetalleComprobante> detalles, String idProveedor) {
        Proveedor unProveedor = buscarProveedorPorId(idProveedor);
        if (unProveedor == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        String nro = (numero != null) ? numero : "ND-" + String.format("%08d", contadorComprobante++);
        NotaDebito nd = new NotaDebito(nro, fechaEmision, fechaRecepcion, unProveedor);
        for (DetalleComprobante d : detalles) nd.agregarDetalle(d);
        unProveedor.agregarComprobante(nd);
        comprobantes.add(nd);
        return nd;
    }

    // =========================================================================
    // RF-21 M7: Total de facturas recibidas por dia y por proveedor
    // =========================================================================

    /**
     * Retorna un mapa con clave "CUIT - dd/MM/yyyy" y valor {montoTotal, cantidadFacturas}.
     * Solo considera Facturas (no NC ni ND).
     */
    public Map<String, Map<String, Object>> totalFacturasPorDiaYProveedor() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Map<String, Map<String, Object>> resultado = new LinkedHashMap<>();
        for (Comprobante c : comprobantes) {
            if (!(c instanceof Factura)) continue;
            if (c.getEstado() == EstadoComprobante.ANULADO) continue;
            String clave = c.getProveedor().getCuit() + " - " + sdf.format(c.getFechaRecepcion());
            resultado.computeIfAbsent(clave, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("proveedor", c.getProveedor().getRazonSocial());
                m.put("fecha", sdf.format(c.getFechaRecepcion()));
                m.put("cantidad", 0);
                m.put("montoTotal", 0.0);
                return m;
            });
            Map<String, Object> fila = resultado.get(clave);
            fila.put("cantidad",   (int) fila.get("cantidad") + 1);
            fila.put("montoTotal", Math.round(((double) fila.get("montoTotal") + c.getImporteTotal()) * 100.0) / 100.0);
        }
        return resultado;
    }

    // =========================================================================
    // RF-26: Libro IVA Compras
    // =========================================================================

    public List<Map<String, Object>> generarLibroIVACompras() {
        List<Map<String, Object>> libro = new ArrayList<>();
        for (Comprobante c : comprobantes) {
            if (c.getEstado() == EstadoComprobante.ANULADO) continue;
            double base21 = 0, iva21 = 0, base10 = 0, iva10 = 0;
            for (DetalleComprobante d : c.getDetalles()) {
                if (d.getAlicuotaIVA() == 21.0)  { base21 += d.getSubtotal(); iva21 += d.getImporteIVA(); }
                else if (d.getAlicuotaIVA() == 10.5) { base10 += d.getSubtotal(); iva10 += d.getImporteIVA(); }
            }
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("cuit",         c.getProveedor().getCuit());
            linea.put("razonSocial",  c.getProveedor().getRazonSocial());
            linea.put("fechaEmision", c.getFechaEmision());
            linea.put("tipo",         c.getTipo().toString());
            linea.put("base21",       Math.round(base21 * 100.0) / 100.0);
            linea.put("iva21",        Math.round(iva21  * 100.0) / 100.0);
            linea.put("base10_5",     Math.round(base10 * 100.0) / 100.0);
            linea.put("iva10_5",      Math.round(iva10  * 100.0) / 100.0);
            linea.put("importeTotal", c.getImporteTotal());
            libro.add(linea);
        }
        return libro;
    }

    // =========================================================================
    // Registro de entidades (llamado por SistemaGestionCompras)
    // =========================================================================

    public void agregarProveedor(Proveedor p)   { if (!proveedores.contains(p)) proveedores.add(p); }
    public void agregarOrdenCompra(OrdenCompra oc) { if (!ordenesCompra.contains(oc)) ordenesCompra.add(oc); }

    public List<Comprobante> getComprobantes()   { return new ArrayList<>(comprobantes); }
    public List<Proveedor>   getProveedores()    { return new ArrayList<>(proveedores); }
    public List<OrdenCompra> getOrdenesCompra()  { return new ArrayList<>(ordenesCompra); }
}

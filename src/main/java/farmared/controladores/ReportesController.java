package farmared.controladores;

import farmared.modelo.enums.EstadoComprobante;
import farmared.modelo.enums.TipoImpuesto;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m5_comprobantes.Factura;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modelo.modulos.m3_impuestos.Retencion;
import farmared.dto.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DS4 — Obtener Cuenta Corriente del Proveedor.
 * Controlador singleton del modulo 7 — Consultas y Reportes (RF-21 a RF-27).
 */
public class ReportesController {

    private static ReportesController instancia;

    private final List<Proveedor>   proveedores;
    private final List<Comprobante> comprobantes;
    private final List<OrdenPago>   ordenesPago;
    private final List<Producto>    productos;
    private final List<OrdenCompra> ordenesCompra;

    private ReportesController(List<Proveedor> proveedores, List<Comprobante> comprobantes,
                                List<OrdenPago> ordenesPago, List<Producto> productos,
                                List<OrdenCompra> ordenesCompra) {
        this.proveedores   = proveedores;
        this.comprobantes  = comprobantes;
        this.ordenesPago   = ordenesPago;
        this.productos     = productos;
        this.ordenesCompra = ordenesCompra;
    }

    public static void inicializar(List<Proveedor> proveedores, List<Comprobante> comprobantes,
                                    List<OrdenPago> ordenesPago, List<Producto> productos,
                                    List<OrdenCompra> ordenesCompra) {
        instancia = new ReportesController(proveedores, comprobantes, ordenesPago, productos, ordenesCompra);
    }

    public static ReportesController getInstance() {
        if (instancia == null) throw new IllegalStateException("ReportesController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // DS4 — cuenta corriente detallada
    // =========================================================================

    /**
     * DS4: busca proveedor, obtiene sus comprobantes e historial de pagos.
     */
    public List<Comprobante> consultarCuentaCorriente(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        prov.obtenerCuentaCorriente();
        return prov.getComprobantes();
    }

    /** DS4: historial de pagos de un proveedor. */
    public List<OrdenPago> buscarPagosPorProveedor(String cuit) {
        List<OrdenPago> resultado = new ArrayList<>();
        for (OrdenPago op : ordenesPago)
            if (op.getProveedor().getCuit().equals(cuit)) resultado.add(op);
        return resultado;
    }

    // =========================================================================
    // Reportes adicionales (RF-22 a RF-27)
    // =========================================================================

    /** RF-22: Documentos impagos de un proveedor. */
    public List<Comprobante> listarDocumentosImpagos(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) return new ArrayList<>();
        List<Comprobante> resultado = new ArrayList<>();
        for (Comprobante c : prov.getComprobantes())
            if (c.getSaldoPendiente() > 0 && c.esSumaDeuda()) resultado.add(c);
        return resultado;
    }

    /** RF-24: Deuda vigente de todos los proveedores. */
    public Map<String, Double> consultarDeudaVigentePorProveedor() {
        Map<String, Double> resultado = new LinkedHashMap<>();
        for (Proveedor p : proveedores)
            resultado.put(p.getRazonSocial() + " (" + p.getCuit() + ")", p.obtenerCuentaCorriente());
        return resultado;
    }

    /** RF-25: Total retenido agrupado por tipo de impuesto. */
    public Map<TipoImpuesto, Double> reporteRetencionesPorTipo() {
        Map<TipoImpuesto, Double> resultado = new EnumMap<>(TipoImpuesto.class);
        for (TipoImpuesto t : TipoImpuesto.values()) resultado.put(t, 0.0);
        for (OrdenPago op : ordenesPago)
            for (Retencion r : op.getRetenciones())
                resultado.merge(r.getImpuesto().getTipo(), r.getImporte(), Double::sum);
        return resultado;
    }

    /** RF-26: Libro IVA Compras. */
    public List<Map<String, Object>> generarLibroIVACompras() {
        List<Map<String, Object>> libro = new ArrayList<>();
        for (Comprobante c : comprobantes) {
            if (c.getEstado() == EstadoComprobante.ANULADO) continue;
            double base21 = 0, iva21 = 0, base10 = 0, iva10 = 0;
            for (DetalleComprobante d : c.getDetalles()) {
                if (d.getAlicuotaIVA() == 21.0)   { base21 += d.getSubtotal(); iva21 += d.getImporteIVA(); }
                else if (d.getAlicuotaIVA() == 10.5) { base10 += d.getSubtotal(); iva10 += d.getImporteIVA(); }
            }
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("cuit",         c.getProveedor().getCuit());
            linea.put("razonSocial",  c.getProveedor().getRazonSocial());
            linea.put("fechaEmision", c.getFechaEmision());
            linea.put("tipo",         c.getTipo().toString());
            linea.put("base21",       round2(base21));
            linea.put("iva21",        round2(iva21));
            linea.put("base10_5",     round2(base10));
            linea.put("iva10_5",      round2(iva10));
            linea.put("importeTotal", c.getImporteTotal());
            libro.add(linea);
        }
        return libro;
    }

    /** RF-23: Facturas recibidas por día (cuit opcional). */
    public List<Factura> consultarFacturasPorDia(Date fecha, String cuitProveedor) {
        List<Factura> resultado = new ArrayList<>();
        for (Comprobante c : comprobantes) {
            if (!(c instanceof Factura)) continue;
            Factura f = (Factura) c;
            if (!mismaFecha(f.getFechaRecepcion(), fecha)) continue;
            if (cuitProveedor != null && !cuitProveedor.isBlank()
                    && !f.getProveedor().getCuit().equals(cuitProveedor)) continue;
            resultado.add(f);
        }
        return resultado;
    }

    /** RF-07: Compulsa de precios de un producto. */
    public List<PrecioAcordado> consultarCompulsaPrecios(String codigoProducto) {
        for (Producto p : productos)
            if (p.getCodigoInterno().equals(codigoProducto))
                return p.obtenerPreciosHistoricos();
        return new ArrayList<>();
    }

    // =========================================================================
    // Búsquedas (DS4)
    // =========================================================================

    public Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }

    public List<Proveedor> getProveedores() { return new ArrayList<>(proveedores); }
    public List<ProveedorDTO> getProveedoresDTO() { return DtoMapper.toProveedorDTOList(proveedores); }

    /** Reporte de todas las OC emitidas (opcionalmente filtradas por proveedor). */
    public List<OrdenCompra> listarOrdenesCompra(String cuitProveedor) {
        List<OrdenCompra> resultado = new ArrayList<>();
        for (OrdenCompra oc : ordenesCompra) {
            if (cuitProveedor == null || cuitProveedor.isBlank()
                    || oc.getProveedor().getCuit().equals(cuitProveedor))
                resultado.add(oc);
        }
        return resultado;
    }

    public List<OrdenCompraDTO> listarOrdenesCompraDTO(String cuitProveedor) {
        return DtoMapper.toOrdenCompraDTOList(listarOrdenesCompra(cuitProveedor));
    }

    /** Reporte de todas las OP emitidas (opcionalmente filtradas por proveedor). */
    public List<OrdenPago> listarOrdenesPago(String cuitProveedor) {
        List<OrdenPago> resultado = new ArrayList<>();
        for (OrdenPago op : ordenesPago) {
            if (cuitProveedor == null || cuitProveedor.isBlank()
                    || op.getProveedor().getCuit().equals(cuitProveedor))
                resultado.add(op);
        }
        return resultado;
    }

    public List<OrdenPagoDTO> listarOrdenesPagoDTO(String cuitProveedor) {
        return DtoMapper.toOrdenPagoDTOList(listarOrdenesPago(cuitProveedor));
    }

    public List<ComprobanteDTO> consultarCuentaCorrienteDTO(String cuitProveedor) {
        return DtoMapper.toComprobanteDTOList(consultarCuentaCorriente(cuitProveedor));
    }

    public List<ComprobanteDTO> listarDocumentosImpagosDTO(String cuitProveedor) {
        return DtoMapper.toComprobanteDTOList(listarDocumentosImpagos(cuitProveedor));
    }

    public List<ComprobanteDTO> consultarFacturasPorDiaDTO(Date fecha, String cuitProveedor) {
        List<Comprobante> temp = new ArrayList<>();
        for (Factura f : consultarFacturasPorDia(fecha, cuitProveedor)) {
            temp.add(f);
        }
        return DtoMapper.toComprobanteDTOList(temp);
    }

    public List<OrdenPagoDTO> buscarPagosPorProveedorDTO(String cuit) {
        return DtoMapper.toOrdenPagoDTOList(buscarPagosPorProveedor(cuit));
    }

    public List<PrecioAcordadoDTO> consultarCompulsaPreciosDTO(String codigoProducto) {
        return DtoMapper.toPrecioAcordadoDTOList(consultarCompulsaPrecios(codigoProducto));
    }

    private boolean mismaFecha(Date a, Date b) {
        if (a == null || b == null) return false;
        Calendar ca = Calendar.getInstance(); ca.setTime(a);
        Calendar cb = Calendar.getInstance(); cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
            && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    private double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}

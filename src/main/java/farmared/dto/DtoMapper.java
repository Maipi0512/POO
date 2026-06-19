package farmared.dto;

import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m3_impuestos.Impuesto;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modelo.modulos.m8_usuarios.Usuario;

import java.util.ArrayList;
import java.util.List;

public final class DtoMapper {

    public static PrecioAcordadoDTO toDTO(PrecioAcordado pa) {
        if (pa == null) return null;
        return new PrecioAcordadoDTO(
                pa.getPrecioUnitario(),
                pa.getFechaAcuerdo(),
                pa.getFechaFinVigencia(),
                pa.getProveedor() != null ? pa.getProveedor().getCuit() : null,
                pa.getProveedor() != null ? pa.getProveedor().getRazonSocial() : null,
                pa.estaVigente()
        );
    }

    public static List<PrecioAcordadoDTO> toPrecioAcordadoDTOList(List<PrecioAcordado> list) {
        if (list == null) return List.of();
        List<PrecioAcordadoDTO> res = new ArrayList<>();
        for (PrecioAcordado pa : list) res.add(toDTO(pa));
        return res;
    }

    public static RubroDTO toDTO(Rubro r) {
        if (r == null) return null;
        return new RubroDTO(r.getIdRubro(), r.getNombre(), r.getDescripcion());
    }

    public static List<RubroDTO> toRubroDTOList(List<Rubro> list) {
        if (list == null) return List.of();
        List<RubroDTO> res = new ArrayList<>();
        for (Rubro r : list) res.add(toDTO(r));
        return res;
    }

    public static ProveedorDTO toDTO(Proveedor p) {
        if (p == null) return null;
        return new ProveedorDTO(
                p.getCuit(),
                p.getRazonSocial(),
                p.getNombreFantasia(),
                p.getDomicilioComercial(),
                p.getTelefono(),
                p.getEmail(),
                p.getCondicionIVA() != null ? p.getCondicionIVA().name() : null,
                p.getNumeroIngresosBrutos(),
                p.getTopeMaximoDeuda(),
                p.obtenerCuentaCorriente(),
                p.isActivo(),
                toRubroDTOList(p.getRubros())
        );
    }

    public static List<ProveedorDTO> toProveedorDTOList(List<Proveedor> list) {
        if (list == null) return List.of();
        List<ProveedorDTO> res = new ArrayList<>();
        for (Proveedor p : list) res.add(toDTO(p));
        return res;
    }

    public static ProductoDTO toDTO(Producto p) {
        if (p == null) return null;
        return new ProductoDTO(
                p.getCodigoInterno(),
                p.getDescripcion(),
                p.getUnidadMedida(),
                p.getTipoIVA() != null ? p.getTipoIVA().name() : null,
                toDTO(p.getRubro()),
                p.isActivo()
        );
    }

    public static List<ProductoDTO> toProductoDTOList(List<Producto> list) {
        if (list == null) return List.of();
        List<ProductoDTO> res = new ArrayList<>();
        for (Producto p : list) res.add(toDTO(p));
        return res;
    }

    public static OrdenCompraDTO toDTO(OrdenCompra oc) {
        if (oc == null) return null;
        List<OrdenCompraDTO.DetalleOCDTO> detalles = new ArrayList<>();
        if (oc.getDetalles() != null) {
            for (var d : oc.getDetalles()) {
                detalles.add(new OrdenCompraDTO.DetalleOCDTO(
                        d.getNroLinea(),
                        d.getProducto().getCodigoInterno(),
                        d.getProducto().getDescripcion(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotal()
                ));
            }
        }
        return new OrdenCompraDTO(
                oc.getNumero(),
                oc.getFechaEmision(),
                oc.getProveedor() != null ? oc.getProveedor().getCuit() : null,
                oc.getProveedor() != null ? oc.getProveedor().getRazonSocial() : null,
                oc.getImporteTotal(),
                oc.getEstado() != null ? oc.getEstado().name() : null,
                oc.getAutorizacion() != null && oc.getAutorizacion().getSupervisor() != null ?
                        oc.getAutorizacion().getSupervisor().getUsername() : null,
                detalles
        );
    }

    public static List<OrdenCompraDTO> toOrdenCompraDTOList(List<OrdenCompra> list) {
        if (list == null) return List.of();
        List<OrdenCompraDTO> res = new ArrayList<>();
        for (OrdenCompra oc : list) res.add(toDTO(oc));
        return res;
    }

    public static ComprobanteDTO toDTO(Comprobante c) {
        if (c == null) return null;
        List<ComprobanteDTO.DetalleComprobanteDTO> detalles = new ArrayList<>();
        if (c.getDetalles() != null) {
            for (DetalleComprobante d : c.getDetalles()) {
                detalles.add(new ComprobanteDTO.DetalleComprobanteDTO(
                        d.getNroLinea(),
                        d.getProducto().getCodigoInterno(),
                        d.getProducto().getDescripcion(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getAlicuotaIVA(),
                        d.getSubtotal(),
                        d.getImporteIVA()
                ));
            }
        }
        return new ComprobanteDTO(
                c.getNumero(),
                c.getTipo() != null ? c.getTipo().name() : null,
                c.getFechaEmision(),
                c.getFechaRecepcion(),
                c.getImporteNeto(),
                c.getImporteIVA(),
                c.getImporteTotal(),
                c.getSaldoPendiente(),
                c.getEstado() != null ? c.getEstado().name() : null,
                c.getProveedor() != null ? c.getProveedor().getCuit() : null,
                c.getProveedor() != null ? c.getProveedor().getRazonSocial() : null,
                c.getAutorizacion() != null && c.getAutorizacion().getSupervisor() != null ?
                        c.getAutorizacion().getSupervisor().getUsername() : null,
                detalles
        );
    }

    public static List<ComprobanteDTO> toComprobanteDTOList(List<Comprobante> list) {
        if (list == null) return List.of();
        List<ComprobanteDTO> res = new ArrayList<>();
        for (Comprobante c : list) res.add(toDTO(c));
        return res;
    }

    public static OrdenPagoDTO toDTO(OrdenPago op) {
        if (op == null) return null;
        List<OrdenPagoDTO.CancelacionComprobanteDTO> cancelaciones = new ArrayList<>();
        if (op.obtenerCancelaciones() != null) {
            for (var can : op.obtenerCancelaciones()) {
                cancelaciones.add(new OrdenPagoDTO.CancelacionComprobanteDTO(
                        can.getComprobante().getNumero(),
                        can.getImporteCancelado(),
                        can.isEsCancelacionTotal()
                ));
            }
        }
        List<OrdenPagoDTO.RetencionDTO> retenciones = new ArrayList<>();
        if (op.getRetenciones() != null) {
            for (var ret : op.getRetenciones()) {
                retenciones.add(new OrdenPagoDTO.RetencionDTO(
                        ret.getImpuesto().getNombre(),
                        ret.getBase(),
                        ret.getPorcentajeAplicado(),
                        ret.getImporte()
                ));
            }
        }
        return new OrdenPagoDTO(
                op.getNumero(),
                op.getFechaEmision(),
                op.getProveedor() != null ? op.getProveedor().getCuit() : null,
                op.getProveedor() != null ? op.getProveedor().getRazonSocial() : null,
                op.getImporteBruto(),
                op.getTotalRetenciones(),
                op.getImporteNeto(),
                op.getEstado() != null ? op.getEstado().name() : null,
                cancelaciones,
                retenciones
        );
    }

    public static List<OrdenPagoDTO> toOrdenPagoDTOList(List<OrdenPago> list) {
        if (list == null) return List.of();
        List<OrdenPagoDTO> res = new ArrayList<>();
        for (OrdenPago op : list) res.add(toDTO(op));
        return res;
    }

    public static UsuarioDTO toDTO(Usuario u) {
        if (u == null) return null;
        return new UsuarioDTO(
                u.getIdUsuario(),
                u.getNombre(),
                u.getApellido(),
                u.getUsername(),
                u.getRol() != null ? u.getRol().name() : null
        );
    }

    public static List<UsuarioDTO> toUsuarioDTOList(List<Usuario> list) {
        if (list == null) return List.of();
        List<UsuarioDTO> res = new ArrayList<>();
        for (Usuario u : list) res.add(toDTO(u));
        return res;
    }

    public static ImpuestoDTO toDTO(Impuesto i) {
        if (i == null) return null;
        return new ImpuestoDTO(
                i.getIdImpuesto(),
                i.getNombre(),
                i.getTipo() != null ? i.getTipo().name() : null,
                i.getPorcentajeBase(),
                i.getMinimoNoImponible()
        );
    }

    public static List<ImpuestoDTO> toImpuestoDTOList(List<Impuesto> list) {
        if (list == null) return List.of();
        List<ImpuestoDTO> res = new ArrayList<>();
        for (Impuesto i : list) res.add(toDTO(i));
        return res;
    }
}

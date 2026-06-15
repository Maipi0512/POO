package farmared.modelo;

import farmared.modelo.enums.*;
import farmared.modelo.modulos.m1_proveedores.*;
import farmared.modelo.modulos.m2_productos.*;
import farmared.modelo.modulos.m3_impuestos.*;
import farmared.modelo.modulos.m4_ordenes_compra.*;
import farmared.modelo.modulos.m5_comprobantes.*;
import farmared.modelo.modulos.m6_ordenes_pago.*;
import farmared.modelo.modulos.m8_usuarios.*;
import farmared.modelo.modulos.m1_proveedores.CUIT;
import farmared.vistas.observador.NotificadorSistema;
import java.util.*;

/**
 * Clase fachada del sistema. Unico punto de entrada para todas las operaciones.
 * Coordina los 7 modulos funcionales (RF-01 al RF-29).
 *
 * Modulo  1 → Gestion de Proveedores
 * Modulo  2 → Catalogo de Productos
 * Modulo  3 → Impuestos y Retenciones
 * Modulo  4 → Ordenes de Compra
 * Modulo  5 → Comprobantes
 * Modulo  6 → Ordenes de Pago
 * Modulo  7 → Consultas y Reportes (metodos consultarXxx / reporteXxx)
 * Modulo  8 → Usuarios y Seguridad
 */
public class SistemaGestionCompras {

    private final String nombreSistema;
    private final String version;

    private final List<Proveedor>   proveedores;
    private final List<Producto>    productos;
    private final List<Rubro>       rubros;
    private final List<Impuesto>    impuestos;
    private final List<OrdenCompra> ordenesCompra;
    private final List<Comprobante> comprobantes;
    private final List<OrdenPago>   ordenesPago;
    private final List<Usuario>     usuarios;

    private int contadorOC          = 1;
    private int contadorOP          = 1;
    private int contadorComprobante = 1;
    private int contadorAutorizacion= 1;
    private int contadorRubro        = 1;

    public SistemaGestionCompras(String nombreSistema, String version) {
        this.nombreSistema = nombreSistema;
        this.version = version;
        proveedores   = new ArrayList<>();
        productos     = new ArrayList<>();
        rubros        = new ArrayList<>();
        impuestos     = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
        comprobantes  = new ArrayList<>();
        ordenesPago   = new ArrayList<>();
        usuarios      = new ArrayList<>();
    }

    // =========================================================================
    // MODULO 1 — GESTION DE PROVEEDORES (RF-01 a RF-04)
    // =========================================================================

    public void registrarProveedor(Proveedor proveedor) {
        new CUIT(proveedor.getCuit());
        for (Proveedor p : proveedores) {
            if (p.getCuit().equals(proveedor.getCuit()))
                throw new IllegalArgumentException("CUIT duplicado: " + proveedor.getCuit());
        }
        proveedores.add(proveedor);
        notificarCambio("PROVEEDOR_REGISTRADO");
    }

    public void modificarProveedor(String cuit, String razonSocial, String nombreFantasia,
                                    String domicilio, String telefono, String email,
                                    CondicionIVA condicionIVA, double topeDeuda) {
        Proveedor p = buscarProveedorPorId(cuit);
        if (p == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuit);
        if (!p.isActivo()) throw new IllegalStateException("No se puede modificar un proveedor dado de baja.");
        p.setRazonSocial(razonSocial);
        p.setNombreFantasia(nombreFantasia);
        p.setDomicilioComercial(domicilio);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setCondicionIVA(condicionIVA);
        p.setTopeMaximoDeuda(topeDeuda);
        notificarCambio("PROVEEDOR_MODIFICADO");
    }

    public void darBajaProveedor(String cuit) {
        Proveedor p = buscarProveedorPorId(cuit);
        if (p == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuit);
        p.setActivo(false);
        notificarCambio("PROVEEDOR_BAJA");
    }

    /** Loop "buscar en coleccion de proveedores" de los diagramas de secuencia. */
    public Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }

    public void registrarRubro(Rubro rubro) {
        rubros.add(rubro);
        if (rubro.getIdRubro() >= contadorRubro) contadorRubro = rubro.getIdRubro() + 1;
    }

    public Rubro registrarRubro(String nombre, String descripcion) {
        for (Rubro r : rubros) {
            if (r.getNombre().equalsIgnoreCase(nombre.trim()))
                throw new IllegalArgumentException("Ya existe un rubro con ese nombre.");
        }
        Rubro rubro = new Rubro(contadorRubro++, nombre.trim(), descripcion.trim());
        rubros.add(rubro);
        notificarCambio("RUBRO_REGISTRADO");
        return rubro;
    }

    public List<Rubro> getRubros() { return new ArrayList<>(rubros); }

    public void registrarCertificadoNoRetencion(String cuit, CertificadoNoRetencion certificado) {
        Proveedor prov = buscarProveedorPorId(cuit);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuit);
        prov.agregarCertificado(certificado);
    }

    // =========================================================================
    // MODULO 2 — CATALOGO DE PRODUCTOS (RF-05 a RF-07)
    // =========================================================================

    public void registrarProducto(Producto producto) {
        for (Producto p : productos) {
            if (p.getCodigoInterno().equals(producto.getCodigoInterno()))
                throw new IllegalArgumentException("Codigo duplicado: " + producto.getCodigoInterno());
        }
        productos.add(producto);
        notificarCambio("PRODUCTO_REGISTRADO");
    }

    public void darBajaProducto(String codigoInterno) {
        Producto p = buscarProductoPorCodigo(codigoInterno);
        if (p == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoInterno);
        p.setActivo(false);
        notificarCambio("PRODUCTO_BAJA");
    }

    /** Loop "buscar en coleccion de productos" del DS1. */
    public Producto buscarProductoPorCodigo(String codigo) {
        for (Producto p : productos)
            if (p.getCodigoInterno().equals(codigo)) return p;
        return null;
    }

    /** Productos con precio vigente para el proveedor (RF-06, RF-07). */
    public List<Producto> listarProductosPorProveedor(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) return Collections.emptyList();
        List<Producto> resultado = new ArrayList<>();
        for (Producto p : productos) {
            if (!p.isActivo()) continue;
            PrecioAcordado precio = p.obtenerUltimoPrecio(prov);
            if (precio != null && precio.estaVigente()) resultado.add(p);
        }
        return resultado;
    }

    public double obtenerPrecioVigente(String codigoProducto, String cuitProveedor) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (producto == null || prov == null) return -1;
        PrecioAcordado precio = producto.obtenerUltimoPrecio(prov);
        return (precio != null && precio.estaVigente()) ? precio.getPrecioUnitario() : -1;
    }

    /** Agrega un precio acordado a un producto existente (mismo producto, otro proveedor). */
    public void agregarPrecioAcordado(String codigoProducto, String cuitProveedor, double precioUnitario) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        if (producto == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuitProveedor);
        producto.agregarPrecioAcordado(new PrecioAcordado(precioUnitario, new Date(), null, prov));
    }

    // =========================================================================
    // MODULO 3 — IMPUESTOS (RF-08)
    // =========================================================================

    public void parametrizarImpuesto(Impuesto impuesto) { impuestos.add(impuesto); }

    // =========================================================================
    // MODULO 4 — ORDENES DE COMPRA (RF-11, RF-12, RF-13) — DS1
    // =========================================================================

    /**
     * Crea una OC vacia para el proveedor indicado (DS1, paso 1).
     */
    public OrdenCompra crearOrdenCompra(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuitProveedor);
        return new OrdenCompra("OC-" + String.format("%08d", contadorOC++), prov);
    }

    /**
     * Agrega un item a la OC buscando el precio vigente del producto (DS1, loop interno).
     */
    public void agregarItemOC(OrdenCompra oc, String codigoProducto, double cantidad, int nroLinea) {
        Producto producto = buscarProductoPorCodigo(codigoProducto);
        if (producto == null) throw new IllegalArgumentException("Producto no encontrado: " + codigoProducto);

        PrecioAcordado precio = producto.obtenerUltimoPrecio(oc.getProveedor());
        if (precio == null || !precio.estaVigente())
            throw new IllegalStateException("Sin precio vigente para " + codigoProducto);

        oc.agregarDetalle(new DetalleOC(nroLinea, producto, cantidad, precio.getPrecioUnitario()));
    }

    /**
     * Emite la OC (RF-11, RF-12). Si supera el tope necesita supervisor (RF-12).
     */
    public OrdenCompra emitirOrdenCompra(OrdenCompra oc, Usuario supervisor, String motivo) {
        oc.calcularTotal();
        if (oc.validarTopeDeuda()) {
            oc.emitir();
        } else {
            if (supervisor == null || !supervisor.esAutorizador())
                throw new IllegalStateException(
                    "La OC supera el tope de deuda. Se requiere autorizacion de Supervisor."
                );
            Autorizacion auth = new Autorizacion(contadorAutorizacion++, supervisor,
                motivo != null ? motivo : "Autorizacion OC por exceso de tope");
            oc.emitirConAutorizacion(auth);
        }
        ordenesCompra.add(oc);
        notificarCambio("OC_EMITIDA");
        return oc;
    }

    public void anularOrdenCompra(String numeroOC) {
        OrdenCompra oc = buscarOrdenCompraPorNumero(numeroOC);
        if (oc == null) throw new IllegalArgumentException("OC no encontrada: " + numeroOC);
        oc.anular();
    }

    public OrdenCompra buscarOrdenCompraPorNumero(String numero) {
        for (OrdenCompra oc : ordenesCompra)
            if (oc.getNumero().equals(numero)) return oc;
        return null;
    }

    // =========================================================================
    // MODULO 5 — COMPROBANTES (RF-14 a RF-17) — DS3
    // =========================================================================

    /**
     * Registra una factura (RF-14, RF-15, RF-16, RF-17). Implementa DS3.
     */
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
                          : "Desvio de precio requiere autorizacion."
                );
            factura.setTieneDesvio(hayDesvio);
            String motiAut = motivo != null ? motivo
                : (sinOC ? "Compra directa sin OC" : "Desvio de precio respecto a OC");
            factura.setAutorizacion(new Autorizacion(contadorAutorizacion++, supervisor, motiAut));
        }

        prov.agregarComprobante(factura);
        comprobantes.add(factura);
        actualizarEstadoOrdenesCompra(factura);
        notificarCambio("COMPROBANTE_REGISTRADO");
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
        notificarCambio("COMPROBANTE_REGISTRADO");
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
        notificarCambio("COMPROBANTE_REGISTRADO");
        return nd;
    }

    // Validaciones RF-16
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

    /** RF-16: valida alicuota e importe de IVA por linea segun el catalogo. */
    public boolean validarImpuestos(List<DetalleComprobante> factura) {
        for (DetalleComprobante d : factura) {
            double alicuotaEsperada = d.getProducto().getTipoIVA().getPorcentaje();
            if (Math.abs(d.getAlicuotaIVA() - alicuotaEsperada) > 0.01) return false;

            double ivaEsperado = Math.round(d.getSubtotal() * (alicuotaEsperada / 100.0) * 100.0) / 100.0;
            if (Math.abs(d.getImporteIVA() - ivaEsperado) > 0.01) return false;
        }
        return true;
    }

    /** Actualiza estado de OC asociadas (State: PARCIALMENTE_FACTURADA / FACTURADA). */
    private void actualizarEstadoOrdenesCompra(Factura factura) {
        for (OrdenCompra oc : factura.getOrdenesCompraAsociadas()) {
            if (oc.getEstado() == EstadoOrdenCompra.ANULADA) continue;

            boolean algunaLineaFacturada = false;
            boolean todasCompletas = true;

            for (DetalleOC doc : oc.getDetalles()) {
                double cantFacturada = 0;
                for (DetalleComprobante dc : factura.getDetalles()) {
                    if (dc.getProducto().getCodigoInterno()
                            .equals(doc.getProducto().getCodigoInterno())) {
                        cantFacturada += dc.getCantidad();
                    }
                }
                if (cantFacturada > 0) algunaLineaFacturada = true;
                if (cantFacturada < doc.getCantidad()) todasCompletas = false;
            }

            if (todasCompletas && algunaLineaFacturada) {
                oc.setEstado(EstadoOrdenCompra.FACTURADA);
            } else if (algunaLineaFacturada) {
                oc.setEstado(EstadoOrdenCompra.PARCIALMENTE_FACTURADA);
            }
        }
    }

    // =========================================================================
    // MODULO 6 — ORDENES DE PAGO (RF-18, RF-19, RF-20) — DS2
    // =========================================================================

    /**
     * PASO 1 DS2: Devuelve comprobantes con saldo pendiente del proveedor.
     */
    public List<Comprobante> iniciarOrdenPago(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        List<Comprobante> impagos = new ArrayList<>();
        for (Comprobante c : prov.getComprobantes()) {
            if (c.getSaldoPendiente() > 0 && c.getEstado() != EstadoComprobante.ANULADO)
                impagos.add(c);
        }
        return impagos;
    }

    /**
     * PASO 2 DS2: Crea la OP, carga cancelaciones y calcula retenciones.
     * Retorna la OP en estado GENERADA con importeNeto listo para informar al operador.
     */
    public OrdenPago prepararOrdenPago(String cuitProveedor,
                                        Map<Comprobante, Double> seleccion,
                                        Date fechaEmision) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");

        OrdenPago op = new OrdenPago("OP-" + String.format("%08d", contadorOP++), prov, fechaEmision);

        for (Map.Entry<Comprobante, Double> e : seleccion.entrySet()) {
            double imp = e.getValue();
            boolean total = Math.abs(imp - e.getKey().getSaldoPendiente()) < 0.01;
            op.agregarCancelacion(new CancelacionComprobante(e.getKey(), imp, total));
        }

        calcularRetenciones(op, prov, fechaEmision);
        op.generar();
        return op;
    }

    /**
     * PASO 3 DS2: Confirma medios de pago y cierra la OP.
     */
    public OrdenPago confirmarOrdenPago(OrdenPago op, List<MedioPago> mediosPago) {
        if (!op.procesarPago(mediosPago))
            throw new IllegalStateException(
                "Medios de pago invalidos o no cubren el neto $" + op.getImporteNeto()
            );
        op.cerrarOrden();
        ordenesPago.add(op);
        notificarCambio("OP_EMITIDA");
        return op;
    }

    /** Calcula retenciones por tipo de impuesto (RF-20, DS2 loop). */
    private void calcularRetenciones(OrdenPago op, Proveedor prov, Date fecha) {
        double base = op.getImporteBruto();
        for (Impuesto imp : impuestos) {
            if (prov.tieneCertificadoVigente(imp.getTipo())) continue;  // RF-09
            double importe = imp.calcularRetencion(base, prov);          // polimorfismo
            if (importe > 0) {
                op.agregarRetencion(new Retencion(imp, base, imp.determinarPorcentaje(base), importe));
            }
        }
    }

    // =========================================================================
    // MODULO 7 — CONSULTAS Y REPORTES (RF-21 a RF-27) — DS4
    // =========================================================================

    /** RF-21 / DS4: Cuenta corriente detallada de un proveedor. */
    public List<Comprobante> consultarCuentaCorriente(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) throw new IllegalArgumentException("Proveedor no encontrado.");
        prov.obtenerCuentaCorriente();
        return prov.getComprobantes();
    }

    /** RF-22: Documentos impagos de un proveedor. */
    public List<Comprobante> listarDocumentosImpagos(String cuitProveedor) {
        Proveedor prov = buscarProveedorPorId(cuitProveedor);
        if (prov == null) return Collections.emptyList();
        List<Comprobante> result = new ArrayList<>();
        for (Comprobante c : prov.getComprobantes())
            if (c.getSaldoPendiente() > 0 && c.esSumaDeuda()) result.add(c);
        return result;
    }

    /** RF-24: Deuda vigente de todos los proveedores. */
    public Map<String, Double> consultarDeudaVigentePorProveedor() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Proveedor p : proveedores)
            result.put(p.getRazonSocial() + " (" + p.getCuit() + ")", p.obtenerCuentaCorriente());
        return result;
    }

    /** RF-25: Total retenido agrupado por tipo de impuesto. */
    public Map<TipoImpuesto, Double> reporteRetencionesPorTipo() {
        Map<TipoImpuesto, Double> result = new EnumMap<>(TipoImpuesto.class);
        for (TipoImpuesto t : TipoImpuesto.values()) result.put(t, 0.0);
        for (OrdenPago op : ordenesPago)
            for (Retencion r : op.getRetenciones())
                result.merge(r.getImpuesto().getTipo(), r.getImporte(), Double::sum);
        return result;
    }

    /** RF-26: Libro IVA Compras. */
    public List<Map<String, Object>> generarLibroIVACompras() {
        List<Map<String, Object>> libro = new ArrayList<>();
        for (Comprobante c : comprobantes) {
            if (c.getEstado() == EstadoComprobante.ANULADO) continue;
            double base21 = 0, iva21 = 0, base10 = 0, iva10 = 0;
            for (DetalleComprobante d : c.getDetalles()) {
                if (d.getAlicuotaIVA() == 21.0) { base21 += d.getSubtotal(); iva21 += d.getImporteIVA(); }
                else if (d.getAlicuotaIVA() == 10.5) { base10 += d.getSubtotal(); iva10 += d.getImporteIVA(); }
            }
            Map<String, Object> linea = new LinkedHashMap<>();
            linea.put("cuit",          c.getProveedor().getCuit());
            linea.put("razonSocial",   c.getProveedor().getRazonSocial());
            linea.put("fechaEmision",  c.getFechaEmision());
            linea.put("tipo",          c.getTipo().toString());
            linea.put("base21",        Math.round(base21 * 100.0) / 100.0);
            linea.put("iva21",         Math.round(iva21  * 100.0) / 100.0);
            linea.put("base10_5",      Math.round(base10 * 100.0) / 100.0);
            linea.put("iva10_5",       Math.round(iva10  * 100.0) / 100.0);
            linea.put("importeTotal",  c.getImporteTotal());
            libro.add(linea);
        }
        return libro;
    }

    /** RF-27: Lista de OC emitidas. */
    public List<OrdenCompra> reporteOrdenesCompra() { return new ArrayList<>(ordenesCompra); }

    /** RF-27: Lista de OP emitidas. */
    public List<OrdenPago> reporteOrdenesPago() { return new ArrayList<>(ordenesPago); }

    /** RF-07: Compulsa de precios de un producto. */
    public List<PrecioAcordado> consultarCompulsaPrecios(String codigoProducto) {
        Producto p = buscarProductoPorCodigo(codigoProducto);
        return p != null ? p.obtenerPreciosHistoricos() : Collections.emptyList();
    }

    /** RF-23: Facturas recibidas por dia y proveedor (cuit opcional). */
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

    /** RF-23: Detalle de pagos realizados por proveedor. */
    public List<OrdenPago> consultarPagosPorProveedor(String cuitProveedor) {
        List<OrdenPago> resultado = new ArrayList<>();
        for (OrdenPago op : ordenesPago) {
            if (op.getProveedor().getCuit().equals(cuitProveedor)) resultado.add(op);
        }
        return resultado;
    }

    private boolean mismaFecha(Date a, Date b) {
        if (a == null || b == null) return false;
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR);
    }

    // =========================================================================
    // MODULO 8 — USUARIOS (RF-28)
    // =========================================================================

    public void registrarUsuario(Usuario usuario) { usuarios.add(usuario); }

    public Usuario autenticarUsuario(String username, String password) {
        for (Usuario u : usuarios) {
            if (u.validarCredenciales(username, password)) return u;
        }
        return null;
    }

    public List<Usuario> listarSupervisores() {
        List<Usuario> resultado = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u.esAutorizador()) resultado.add(u);
        }
        return resultado;
    }

    // =========================================================================
    // GETTERS
    // =========================================================================

    public String getNombreSistema()                  { return nombreSistema; }
    public String getVersion()                        { return version; }
    public List<Proveedor> getProveedores()           { return new ArrayList<>(proveedores); }
    public List<Producto> getProductos()              { return new ArrayList<>(productos); }
    public List<OrdenCompra> getOrdenesCompra()       { return new ArrayList<>(ordenesCompra); }
    public List<OrdenPago> getOrdenesPago()           { return new ArrayList<>(ordenesPago); }
    public List<Comprobante> getComprobantes()        { return new ArrayList<>(comprobantes); }
    public List<Impuesto> getImpuestos()              { return new ArrayList<>(impuestos); }

    private void notificarCambio(String evento) {
        NotificadorSistema.getInstancia().notificar(evento);
    }
}

package farmared;

import farmared.controladores.OrdenPagoController;
import farmared.enums.CondicionIVA;
import farmared.enums.EstadoOrdenPago;
import farmared.enums.TipoComprobante;
import farmared.enums.TipoIVA;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modulos.m5_comprobantes.Factura;
import farmared.modulos.m6_ordenes_pago.Efectivo;
import farmared.modulos.m6_ordenes_pago.Impuesto;
import farmared.modulos.m6_ordenes_pago.ImpuestoIVA;
import farmared.modulos.m6_ordenes_pago.ImpuestoIngresosBrutos;
import farmared.modulos.m6_ordenes_pago.MedioPago;
import farmared.modulos.m6_ordenes_pago.OrdenPago;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests para DS2 - Emitir Orden de Pago.
 *
 * Flujo:
 *   iniciarOrdenPago(cuit)
 *   → obtenerComprobantesImpagos() → List<Comprobante>
 *   → seleccionarComprobantes(cuit, seleccion, fecha)
 *       → new OrdenPago → agregarCancelacion() x N
 *       → calcularRetenciones() → agregarRetencion() x N → generar()
 *   → confirmarPago(op, medios)
 *       → procesarPago(medios) → cerrarOrden()
 *       → proveedor.registrarPago() x N
 */
public class DS2_EmitirOrdenPagoTest {

    private List<Proveedor>  proveedores;
    private List<Impuesto>   impuestos;
    private List<OrdenPago>  ordenesPago;

    private Proveedor proveedor;
    private Proveedor proveedorMonotributo;
    private Factura   factura1;
    private Factura   factura2;
    private Producto  producto;

    private ImpuestoIVA            ivaRetencion;
    private ImpuestoIngresosBrutos iibbRetencion;

    private OrdenPagoController ctrl;

    @Before
    public void setUp() {
        proveedores = new ArrayList<>();
        impuestos   = new ArrayList<>();
        ordenesPago = new ArrayList<>();

        Rubro rubMed = new Rubro(1, "Medicamentos", "");

        // Proveedor Responsable Inscripto (aplica retención IVA)
        proveedor = new Proveedor(
                "20-11111111-1", "LabSA", "Lab SA",
                "Av. Principal 100", "011-1111", "lab@sa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, "IB-001", new Date()
        );
        proveedor.setTopeMaximoDeuda(200000.0);
        proveedor.agregarRubro(rubMed);
        proveedores.add(proveedor);

        // Proveedor Monotributista (NO aplica retención IVA)
        proveedorMonotributo = new Proveedor(
                "27-22222222-2", "MiniSA", "Mini SA",
                "Calle 200", "011-2222", "mini@sa.com",
                CondicionIVA.MONOTRIBUTISTA, "IB-002", new Date()
        );
        proveedorMonotributo.setTopeMaximoDeuda(50000.0);
        proveedores.add(proveedorMonotributo);

        producto = new Producto("MED-001", "Ibuprofeno 600mg", "caja", TipoIVA.IVA_21, rubMed);
        producto.agregarPrecioAcordado(new PrecioAcordado(100.0, new Date(0), null, proveedor));

        // Factura de $1000 con saldo pendiente completo
        factura1 = new Factura("FAC-001", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        factura1.agregarDetalle(new DetalleComprobante(1, producto, 10.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        factura1.calcularImpuestos();
        proveedor.agregarComprobante(factura1);

        // Segunda factura de $500
        factura2 = new Factura("FAC-002", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        factura2.agregarDetalle(new DetalleComprobante(1, producto, 5.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        factura2.calcularImpuestos();
        proveedor.agregarComprobante(factura2);

        // Impuestos: IVA 10.5% sin mínimo no imponible, IIBB 3% sin mínimo
        ivaRetencion  = new ImpuestoIVA(1, 10.5, 0.0);
        iibbRetencion = new ImpuestoIngresosBrutos(2, 3.0, 0.0);
        impuestos.add(ivaRetencion);
        impuestos.add(iibbRetencion);

        OrdenPagoController.inicializar(proveedores, impuestos, ordenesPago);
        ctrl = OrdenPagoController.getInstance();
    }

    // -------------------------------------------------------------------------
    // Test 1: iniciarOrdenPago — retorna comprobantes con saldo pendiente
    // -------------------------------------------------------------------------
    @Test
    public void iniciarOrdenPago_conFacturasImpagos_retornaLista() {
        List<Comprobante> impagos = ctrl.iniciarOrdenPago("20-11111111-1");

        assertEquals(2, impagos.size());
        assertTrue(impagos.contains(factura1));
        assertTrue(impagos.contains(factura2));
    }

    // -------------------------------------------------------------------------
    // Test 2: iniciarOrdenPago — proveedor sin comprobantes → lista vacía
    // -------------------------------------------------------------------------
    @Test
    public void iniciarOrdenPago_sinComprobantes_retornaListaVacia() {
        List<Comprobante> impagos = ctrl.iniciarOrdenPago("27-22222222-2");

        assertNotNull(impagos);
        assertEquals(0, impagos.size());
    }

    // -------------------------------------------------------------------------
    // Test 3: iniciarOrdenPago — proveedor inexistente lanza excepción
    // -------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void iniciarOrdenPago_proveedorInexistente_lanzaExcepcion() {
        ctrl.iniciarOrdenPago("99-99999999-9");
    }

    // -------------------------------------------------------------------------
    // Test 4: seleccionarComprobantes — importeBruto es suma de montos seleccionados
    // -------------------------------------------------------------------------
    @Test
    public void seleccionarComprobantes_importeBrutoCorrrecto() {
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura1, factura1.getSaldoPendiente());
        seleccion.put(factura2, factura2.getSaldoPendiente());

        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());

        double brutoEsperado = factura1.getSaldoPendiente() + factura2.getSaldoPendiente();
        assertEquals(brutoEsperado, op.getImporteBruto(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 5: seleccionarComprobantes — retenciones calculadas para RI
    //         IVA 10.5% + IIBB 3% sobre importeBruto
    // -------------------------------------------------------------------------
    @Test
    public void seleccionarComprobantes_proveedorRI_calculaRetenciones() {
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura1, factura1.getSaldoPendiente());

        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());

        double base = factura1.getSaldoPendiente();
        double retencionEsperada = base * 0.105 + base * 0.03; // IVA + IIBB

        assertTrue("Debe haber retenciones", op.getTotalRetenciones() > 0);
        assertEquals(retencionEsperada, op.getTotalRetenciones(), 0.01);
        assertEquals(base - retencionEsperada, op.getImporteNeto(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 6: seleccionarComprobantes — monotributista no tiene retención IVA
    // -------------------------------------------------------------------------
    @Test
    public void seleccionarComprobantes_proveedorMonotributista_sinRetencionIVA() {
        // Crear factura para el monotributista
        Factura factMono = new Factura("FAC-M01", TipoComprobante.FACTURA_C, new Date(), new Date(), proveedorMonotributo);
        factMono.agregarDetalle(new DetalleComprobante(1, producto, 10.0, 100.0, 0.0));
        factMono.calcularImpuestos();
        proveedorMonotributo.agregarComprobante(factMono);

        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factMono, factMono.getSaldoPendiente());

        OrdenPago op = ctrl.seleccionarComprobantes("27-22222222-2", seleccion, new Date());

        // Solo IIBB aplica (3%), no IVA — porque es Monotributista
        double base = factMono.getSaldoPendiente();
        assertEquals(base * 0.03, op.getTotalRetenciones(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 7: confirmarPago — efectivo que cubre el neto → OP en estado EMITIDA
    // -------------------------------------------------------------------------
    @Test
    public void confirmarPago_efectivoCubreNeto_estadoEmitida() {
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura1, factura1.getSaldoPendiente());

        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());

        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, op.getImporteNeto(), new Date()));

        ctrl.confirmarPago(op, medios);

        assertEquals(EstadoOrdenPago.EMITIDA, op.getEstado());
        assertEquals(1, ordenesPago.size());
    }

    // -------------------------------------------------------------------------
    // Test 8: confirmarPago — medios que no cubren el neto → excepción
    // -------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void confirmarPago_mediosInsuficientes_lanzaExcepcion() {
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura1, factura1.getSaldoPendiente());

        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());

        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, 1.0, new Date())); // muy poco dinero

        ctrl.confirmarPago(op, medios);
    }

    // -------------------------------------------------------------------------
    // Test 9: confirmarPago — saldo del comprobante baja después del pago
    // -------------------------------------------------------------------------
    @Test
    public void confirmarPago_saldoFacturaActualizadoTrasPago() {
        double saldoAntes = factura1.getSaldoPendiente();
        assertTrue(saldoAntes > 0);

        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura1, saldoAntes);

        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        ctrl.confirmarPago(op, List.of(new Efectivo(1, op.getImporteNeto(), new Date())));

        assertEquals(0.0, factura1.getSaldoPendiente(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 10: pago parcial — saldo del comprobante queda reducido, no en cero
    // -------------------------------------------------------------------------
    @Test
    public void confirmarPago_pagoParcial_saldoReducido() {
        double saldoTotal = factura1.getSaldoPendiente();
        double aPagar     = saldoTotal / 2.0;

        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura1, aPagar);

        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        ctrl.confirmarPago(op, List.of(new Efectivo(1, op.getImporteNeto(), new Date())));

        double saldoDespues = factura1.getSaldoPendiente();
        assertTrue("El saldo debe haber bajado", saldoDespues < saldoTotal);
        assertTrue("El saldo no debe ser cero", saldoDespues > 0);
    }
}

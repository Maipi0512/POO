package farmared;

import farmared.controladores.FacturaController;
import farmared.controladores.OrdenCompraController;
import farmared.controladores.OrdenPagoController;
import farmared.controladores.ReportesController;
import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.EstadoOrdenCompra;
import farmared.modelo.enums.EstadoOrdenPago;
import farmared.modelo.enums.RolUsuario;
import farmared.modelo.enums.TipoComprobante;
import farmared.modelo.enums.TipoIVA;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m3_impuestos.Impuesto;
import farmared.modelo.modulos.m3_impuestos.ImpuestoIVA;
import farmared.modelo.modulos.m3_impuestos.ImpuestoIngresosBrutos;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modelo.modulos.m5_comprobantes.Comprobante;
import farmared.modelo.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modelo.modulos.m5_comprobantes.Factura;
import farmared.modelo.modulos.m6_ordenes_pago.Efectivo;
import farmared.modelo.modulos.m6_ordenes_pago.MedioPago;
import farmared.modelo.modulos.m6_ordenes_pago.OrdenPago;
import farmared.modelo.modulos.m8_usuarios.Usuario;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MainTest {

    // =========================================================================
    // Estado compartido — se reinicializa en cada test con setUp()
    // =========================================================================

    private static List<Proveedor>   proveedores;
    private static List<Producto>    productos;
    private static List<OrdenCompra> ordenesCompra;
    private static List<Comprobante> comprobantes;
    private static List<OrdenPago>   ordenesPago;
    private static List<Usuario>     usuarios;
    private static List<Impuesto>    impuestos;

    private static Proveedor proveedor;
    private static Producto  ibuprofeno;
    private static Producto  paracetamol;
    private static Usuario   supervisor;

    private static int passed = 0;
    private static int failed = 0;

    // =========================================================================
    // Main
    // =========================================================================

    public static void main(String[] args) {

        // DS1 - Generar Orden de Compra
        test("DS1 - crearOrdenCompra: proveedor valido retorna OC",              MainTest::ds1_crearOC_proveedorValido);
        test("DS1 - crearOrdenCompra: proveedor inexistente lanza excepcion",    MainTest::ds1_crearOC_proveedorInexistente);
        test("DS1 - agregarItem: subtotal correcto (10 x 150 = 1500)",           MainTest::ds1_agregarItem_subtotal);
        test("DS1 - agregarItem: varios items, subtotales correctos",            MainTest::ds1_agregarItem_varios);
        test("DS1 - agregarItem: sin precio vigente lanza excepcion",            MainTest::ds1_agregarItem_sinPrecio);
        test("DS1 - emitirOrdenCompra: dentro de tope -> EMITIDA",              MainTest::ds1_emitir_dentroDeTope);
        test("DS1 - emitirOrdenCompra: calcularTotal correcto",                  MainTest::ds1_emitir_calcularTotal);
        test("DS1 - emitirOrdenCompra: supera tope sin supervisor -> excepcion", MainTest::ds1_emitir_superaTopeSinSupervisor);
        test("DS1 - emitirOrdenCompra: supera tope con supervisor -> AUTORIZADA",MainTest::ds1_emitir_superaTopeConSupervisor);
        test("DS1 - anularOrdenCompra: estado -> ANULADA",                      MainTest::ds1_anular);

        // DS2 - Emitir Orden de Pago
        test("DS2 - iniciarOrdenPago: retorna comprobantes impagos",             MainTest::ds2_iniciar_conFacturas);
        test("DS2 - iniciarOrdenPago: proveedor inexistente lanza excepcion",    MainTest::ds2_iniciar_proveedorInexistente);
        test("DS2 - seleccionarComprobantes: importeBruto correcto",             MainTest::ds2_seleccionar_importeBruto);
        test("DS2 - seleccionarComprobantes: retenciones calculadas (RI)",       MainTest::ds2_seleccionar_retenciones);
        test("DS2 - confirmarPago: efectivo cubre neto -> EMITIDA",             MainTest::ds2_confirmar_exitoso);
        test("DS2 - confirmarPago: medios insuficientes lanza excepcion",        MainTest::ds2_confirmar_mediosInsuficientes);
        test("DS2 - confirmarPago: saldo de factura baja a 0 tras pago total",  MainTest::ds2_confirmar_saldoCero);

        // DS3 - Registrar Factura
        test("DS3 - registrarFactura: con OC sin desvios -> exitoso",            MainTest::ds3_conOC_exitoso);
        test("DS3 - registrarFactura: sin OC sin supervisor lanza excepcion",    MainTest::ds3_sinOC_sinSupervisor);
        test("DS3 - registrarFactura: sin OC con supervisor -> exitoso",         MainTest::ds3_sinOC_conSupervisor);
        test("DS3 - registrarFactura: desvio precio sin supervisor -> excepcion",MainTest::ds3_desvio_sinSupervisor);
        test("DS3 - registrarFactura: entrega parcial -> PARCIALMENTE_FACTURADA",MainTest::ds3_parcial);

        // DS4 - Cuenta Corriente
        test("DS4 - consultarCuentaCorriente: sin comprobantes -> deuda 0",      MainTest::ds4_sinComprobantes);
        test("DS4 - consultarCuentaCorriente: una factura -> deuda correcta",    MainTest::ds4_unaFactura);
        test("DS4 - consultarCuentaCorriente: dos facturas -> suma correcta",    MainTest::ds4_dosFacturas);
        test("DS4 - consultarCuentaCorriente: proveedor inexistente -> excepcion",MainTest::ds4_proveedorInexistente);
        test("DS4 - listarDocumentosImpagos: solo facturas con saldo > 0",       MainTest::ds4_listarImpagos);

        System.out.println("\n--- " + passed + " PASS / " + failed + " FAIL ---");
    }

    // =========================================================================
    // Setup — carga de datos manuales
    // =========================================================================

    private static void setUp() {
        proveedores   = new ArrayList<>();
        productos     = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
        comprobantes  = new ArrayList<>();
        ordenesPago   = new ArrayList<>();
        usuarios      = new ArrayList<>();
        impuestos     = new ArrayList<>();

        Rubro rubMed = new Rubro(1, "Medicamentos", "Farmaceuticos");

        proveedor = new Proveedor(
                "20-11111111-1", "LabSA", "Lab SA",
                "Av. Principal 100", "011-1111", "lab@sa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, "IB-001", new Date()
        );
        proveedor.setTopeMaximoDeuda(50000.0);
        proveedor.agregarRubro(rubMed);
        proveedores.add(proveedor);

        ibuprofeno = new Producto("MED-001", "Ibuprofeno 600mg", "caja", TipoIVA.IVA_21, rubMed);
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(150.0, new Date(0), null, proveedor));
        productos.add(ibuprofeno);

        paracetamol = new Producto("MED-002", "Paracetamol 500mg", "caja", TipoIVA.IVA_21, rubMed);
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(80.0, new Date(0), null, proveedor));
        productos.add(paracetamol);

        supervisor = new Usuario(1, "Carlos", "Rios", "crios", "pass", RolUsuario.SUPERVISOR);
        usuarios.add(supervisor);

        impuestos.add(new ImpuestoIVA(1, 10.5, 0.0));
        impuestos.add(new ImpuestoIngresosBrutos(2, 3.0, 0.0));

        OrdenCompraController.inicializar(proveedores, productos, ordenesCompra, usuarios);
        OrdenPagoController.inicializar(proveedores, impuestos, ordenesPago);
        FacturaController.inicializar(proveedores, ordenesCompra, comprobantes, usuarios);
        ReportesController.inicializar(proveedores, comprobantes, ordenesPago, productos);
    }

    // =========================================================================
    // DS1 - Generar Orden de Compra
    // =========================================================================

    private static void ds1_crearOC_proveedorValido() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        assertNotNull(oc, "OC no debe ser null");
        assertEquals(proveedor, oc.getProveedor(), "Proveedor incorrecto");
        assertTrue(oc.getNumero().startsWith("OC-"), "Numero debe empezar con OC-");
        assertEquals(0, oc.getDetalles().size(), "OC nueva debe tener 0 detalles");
    }

    private static void ds1_crearOC_proveedorInexistente() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        try {
            ctrl.crearOrdenCompra("99-99999999-9");
            throw new AssertionError("Debia lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void ds1_agregarItem_subtotal() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);
        assertEquals(1, oc.getDetalles().size(), "Debe haber 1 detalle");
        assertEquals(1500.0, oc.getDetalles().get(0).getSubtotal(), "Subtotal 10*150=1500");
    }

    private static void ds1_agregarItem_varios() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);
        ctrl.agregarItem(oc, "MED-002", 5.0, 2);
        assertEquals(2, oc.getDetalles().size(), "Debe haber 2 detalles");
        assertEquals(1500.0, oc.getDetalles().get(0).getSubtotal(), "Subtotal item 1");
        assertEquals(400.0,  oc.getDetalles().get(1).getSubtotal(), "Subtotal item 2");
    }

    private static void ds1_agregarItem_sinPrecio() {
        setUp();
        Rubro rub = new Rubro(2, "Sin precio", "");
        Producto sinPrecio = new Producto("SIN-001", "Producto sin precio", "u", TipoIVA.IVA_21, rub);
        productos.add(sinPrecio);
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        try {
            ctrl.agregarItem(oc, "SIN-001", 1.0, 1);
            throw new AssertionError("Debia lanzar IllegalStateException");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds1_emitir_dentroDeTope() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);
        ctrl.emitirOrdenCompra(oc, null, null);
        assertEquals(EstadoOrdenCompra.EMITIDA, oc.getEstado(), "Estado debe ser EMITIDA");
        assertEquals(1, ordenesCompra.size(), "Debe haber 1 OC en la lista");
    }

    private static void ds1_emitir_calcularTotal() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1); // 10*150 = 1500
        ctrl.agregarItem(oc, "MED-002", 5.0,  2); // 5*80  =  400
        ctrl.emitirOrdenCompra(oc, null, null);
        assertEquals(1900.0, oc.getImporteTotal(), "Total debe ser 1900");
    }

    private static void ds1_emitir_superaTopeSinSupervisor() {
        setUp();
        proveedor.setTopeMaximoDeuda(100.0);
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1); // $1500 > tope $100
        try {
            ctrl.emitirOrdenCompra(oc, null, null);
            throw new AssertionError("Debia lanzar IllegalStateException");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds1_emitir_superaTopeConSupervisor() {
        setUp();
        proveedor.setTopeMaximoDeuda(100.0);
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);
        ctrl.emitirOrdenCompra(oc, supervisor, "Autorizacion por exceso de tope");
        assertEquals(EstadoOrdenCompra.AUTORIZADA, oc.getEstado(), "Estado debe ser AUTORIZADA");
        assertNotNull(oc.getAutorizacion(), "Debe tener autorizacion");
    }

    private static void ds1_anular() {
        setUp();
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 5.0, 1);
        ctrl.emitirOrdenCompra(oc, null, null);
        ctrl.anularOrdenCompra(oc.getNumero());
        assertEquals(EstadoOrdenCompra.ANULADA, oc.getEstado(), "Estado debe ser ANULADA");
    }

    // =========================================================================
    // DS2 - Emitir Orden de Pago
    // =========================================================================

    private static Factura crearFacturaEnProveedor(String numero, double cantidad, double precio) {
        Factura f = new Factura(numero, TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f.agregarDetalle(new DetalleComprobante(1, ibuprofeno, cantidad, precio, TipoIVA.IVA_21.getPorcentaje()));
        f.calcularImpuestos();
        proveedor.agregarComprobante(f);
        return f;
    }

    private static void ds2_iniciar_conFacturas() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        Factura f2 = crearFacturaEnProveedor("FAC-002", 5.0, 100.0);
        OrdenPagoController ctrl = OrdenPagoController.getInstance();
        List<Comprobante> impagos = ctrl.iniciarOrdenPago("20-11111111-1");
        assertEquals(2, impagos.size(), "Debe retornar 2 comprobantes impagos");
        assertTrue(impagos.contains(f1), "Debe contener la factura 1");
        assertTrue(impagos.contains(f2), "Debe contener la factura 2");
    }

    private static void ds2_iniciar_proveedorInexistente() {
        setUp();
        OrdenPagoController ctrl = OrdenPagoController.getInstance();
        try {
            ctrl.iniciarOrdenPago("99-99999999-9");
            throw new AssertionError("Debia lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void ds2_seleccionar_importeBruto() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        Factura f2 = crearFacturaEnProveedor("FAC-002", 5.0, 100.0);
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(f1, f1.getSaldoPendiente());
        seleccion.put(f2, f2.getSaldoPendiente());
        OrdenPago op = OrdenPagoController.getInstance()
                .seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        double esperado = f1.getSaldoPendiente() + f2.getSaldoPendiente();
        assertEquals(esperado, op.getImporteBruto(), "ImporteBruto debe ser la suma de los saldos");
    }

    private static void ds2_seleccionar_retenciones() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(f1, f1.getSaldoPendiente());
        OrdenPago op = OrdenPagoController.getInstance()
                .seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        double base     = f1.getSaldoPendiente();
        double esperado = base * 0.105 + base * 0.03; // IVA 10.5% + IIBB 3%
        assertTrue(op.getTotalRetenciones() > 0, "Debe haber retenciones");
        assertEquals(esperado, op.getTotalRetenciones(), "Total retenciones incorrecto");
    }

    private static void ds2_confirmar_exitoso() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(f1, f1.getSaldoPendiente());
        OrdenPagoController ctrl = OrdenPagoController.getInstance();
        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, op.getImporteNeto(), new Date()));
        ctrl.confirmarPago(op, medios);
        assertEquals(EstadoOrdenPago.EMITIDA, op.getEstado(), "Estado debe ser EMITIDA");
        assertEquals(1, ordenesPago.size(), "Debe haber 1 orden de pago");
    }

    private static void ds2_confirmar_mediosInsuficientes() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(f1, f1.getSaldoPendiente());
        OrdenPagoController ctrl = OrdenPagoController.getInstance();
        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, 1.0, new Date())); // muy poco dinero
        try {
            ctrl.confirmarPago(op, medios);
            throw new AssertionError("Debia lanzar IllegalStateException");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds2_confirmar_saldoCero() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        double saldoAntes = f1.getSaldoPendiente();
        assertTrue(saldoAntes > 0, "Saldo inicial debe ser > 0");
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(f1, saldoAntes);
        OrdenPagoController ctrl = OrdenPagoController.getInstance();
        OrdenPago op = ctrl.seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, op.getImporteNeto(), new Date()));
        ctrl.confirmarPago(op, medios);
        assertEquals(0.0, f1.getSaldoPendiente(), "Saldo debe ser 0 tras pago total");
    }

    // =========================================================================
    // DS3 - Registrar Factura
    // =========================================================================

    private static void ds3_conOC_exitoso() {
        setUp();
        OrdenCompraController ocCtrl   = OrdenCompraController.getInstance();
        FacturaController     factCtrl = FacturaController.getInstance();

        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-11111111-1");
        ocCtrl.agregarItemConPrecio(oc, "MED-001", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);
        assertEquals(EstadoOrdenCompra.EMITIDA, oc.getEstado(), "OC debe estar EMITIDA antes de facturar");

        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 10.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));

        Comprobante comp = factCtrl.registrar(
                "20-11111111-1", TipoComprobante.FACTURA_A, detalles,
                List.of(oc.getNumero()), null, "Test DS3"
        );
        assertNotNull(comp, "Comprobante no debe ser null");
        assertEquals(1, comprobantes.size(), "Debe haber 1 comprobante registrado");
        assertEquals(EstadoOrdenCompra.FACTURADA, oc.getEstado(), "OC debe quedar FACTURADA");
        assertTrue(proveedor.obtenerCuentaCorriente() > 0, "Deuda del proveedor debe ser > 0");
    }

    private static void ds3_sinOC_sinSupervisor() {
        setUp();
        FacturaController factCtrl = FacturaController.getInstance();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        try {
            factCtrl.registrar("20-11111111-1", TipoComprobante.FACTURA_A, detalles,
                    new ArrayList<>(), null, "Test sin OC");
            throw new AssertionError("Debia lanzar IllegalStateException");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds3_sinOC_conSupervisor() {
        setUp();
        FacturaController factCtrl = FacturaController.getInstance();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        Comprobante comp = factCtrl.registrar("20-11111111-1", TipoComprobante.FACTURA_A, detalles,
                new ArrayList<>(), supervisor, "Compra directa autorizada");
        assertNotNull(comp, "Comprobante no debe ser null");
        assertEquals(1, comprobantes.size(), "Debe haber 1 comprobante registrado");
    }

    private static void ds3_desvio_sinSupervisor() {
        setUp();
        OrdenCompraController ocCtrl   = OrdenCompraController.getInstance();
        FacturaController     factCtrl = FacturaController.getInstance();

        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-11111111-1");
        ocCtrl.agregarItemConPrecio(oc, "MED-001", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);

        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 10.0, 200.0, TipoIVA.IVA_21.getPorcentaje())); // precio diferente

        try {
            factCtrl.registrar("20-11111111-1", TipoComprobante.FACTURA_A, detalles,
                    List.of(oc.getNumero()), null, "Test desvio");
            throw new AssertionError("Debia lanzar IllegalStateException por desvio de precio");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds3_parcial() {
        setUp();
        OrdenCompraController ocCtrl   = OrdenCompraController.getInstance();
        FacturaController     factCtrl = FacturaController.getInstance();

        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-11111111-1");
        ocCtrl.agregarItemConPrecio(oc, "MED-001", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);

        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje())); // solo 5 de 10

        factCtrl.registrar("20-11111111-1", TipoComprobante.FACTURA_A, detalles,
                List.of(oc.getNumero()), null, "Entrega parcial");

        assertEquals(EstadoOrdenCompra.PARCIALMENTE_FACTURADA, oc.getEstado(),
                "OC debe quedar PARCIALMENTE_FACTURADA");
    }

    // =========================================================================
    // DS4 - Cuenta Corriente
    // =========================================================================

    private static void ds4_sinComprobantes() {
        setUp();
        ReportesController ctrl = ReportesController.getInstance();
        List<Comprobante> resultado = ctrl.consultarCuentaCorriente("20-11111111-1");
        assertEquals(0, resultado.size(), "Sin comprobantes, lista debe ser vacia");
        assertEquals(0.0, proveedor.obtenerCuentaCorriente(), "Deuda debe ser 0");
    }

    private static void ds4_unaFactura() {
        setUp();
        Factura f = new Factura("FAC-001", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f.agregarDetalle(new DetalleComprobante(1, ibuprofeno, 10.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        f.calcularImpuestos();
        proveedor.agregarComprobante(f);

        ReportesController.getInstance().consultarCuentaCorriente("20-11111111-1");
        double deuda = proveedor.obtenerCuentaCorriente();
        assertEquals(f.getSaldoPendiente(), deuda, "Deuda debe igualar el saldo de la factura");
        assertTrue(deuda > 0, "Deuda debe ser > 0");
    }

    private static void ds4_dosFacturas() {
        setUp();
        Factura f1 = new Factura("FAC-001", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f1.agregarDetalle(new DetalleComprobante(1, ibuprofeno, 10.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        f1.calcularImpuestos();
        proveedor.agregarComprobante(f1);

        Factura f2 = new Factura("FAC-002", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f2.agregarDetalle(new DetalleComprobante(1, ibuprofeno, 5.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        f2.calcularImpuestos();
        proveedor.agregarComprobante(f2);

        ReportesController.getInstance().consultarCuentaCorriente("20-11111111-1");
        double deuda    = proveedor.obtenerCuentaCorriente();
        double esperada = f1.getSaldoPendiente() + f2.getSaldoPendiente();
        assertEquals(esperada, deuda, "Deuda debe ser la suma de ambas facturas");
    }

    private static void ds4_proveedorInexistente() {
        setUp();
        try {
            ReportesController.getInstance().consultarCuentaCorriente("99-99999999-9");
            throw new AssertionError("Debia lanzar IllegalArgumentException");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void ds4_listarImpagos() {
        setUp();
        Factura f1 = new Factura("FAC-001", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f1.agregarDetalle(new DetalleComprobante(1, ibuprofeno, 10.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        f1.calcularImpuestos();
        proveedor.agregarComprobante(f1);

        Factura f2 = new Factura("FAC-002", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f2.agregarDetalle(new DetalleComprobante(1, ibuprofeno, 5.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        f2.calcularImpuestos();
        proveedor.agregarComprobante(f2);
        f2.registrarPago(f2.getSaldoPendiente()); // pagada totalmente

        List<Comprobante> impagos = ReportesController.getInstance().listarDocumentosImpagos("20-11111111-1");
        assertEquals(1, impagos.size(), "Solo debe retornar 1 impago");
        assertTrue(impagos.contains(f1),  "Debe contener f1 (impaga)");
        assertTrue(!impagos.contains(f2), "No debe contener f2 (pagada)");
    }

    // =========================================================================
    // Runner
    // =========================================================================

    private static void test(String name, Runnable r) {
        try {
            r.run();
            System.out.println("PASS: " + name);
            passed++;
        } catch (AssertionError | Exception e) {
            System.out.println("FAIL: " + name + " -> " + e.getMessage());
            failed++;
        }
    }

    // =========================================================================
    // Assertions manuales (sin JUnit)
    // =========================================================================

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) throw new AssertionError(msg);
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!expected.equals(actual))
            throw new AssertionError(msg + " [esperado=" + expected + ", actual=" + actual + "]");
    }

    private static void assertEquals(double expected, double actual, String msg) {
        if (Math.abs(expected - actual) > 0.01)
            throw new AssertionError(msg + " [esperado=" + expected + ", actual=" + actual + "]");
    }

    private static void assertEquals(int expected, int actual, String msg) {
        if (expected != actual)
            throw new AssertionError(msg + " [esperado=" + expected + ", actual=" + actual + "]");
    }
}

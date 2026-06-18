package farmared;

import farmared.controladores.*;
import farmared.modelo.enums.*;
import farmared.modelo.modulos.m1_proveedores.*;
import farmared.modelo.modulos.m2_productos.*;
import farmared.modelo.modulos.m3_impuestos.*;
import farmared.modelo.modulos.m4_ordenes_compra.*;
import farmared.modelo.modulos.m5_comprobantes.*;
import farmared.modelo.modulos.m6_ordenes_pago.*;
import farmared.modelo.modulos.m8_usuarios.*;

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
    private static List<Rubro>       rubros;
    private static List<Producto>    productos;
    private static List<OrdenCompra> ordenesCompra;
    private static List<Comprobante> comprobantes;
    private static List<OrdenPago>   ordenesPago;
    private static List<Usuario>     usuarios;
    private static List<Impuesto>    impuestos;

    private static Rubro    rubMed;
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

        // --- DS1 - Generar Orden de Compra ---
        test("DS1 - crearOrdenCompra: proveedor valido retorna OC",               MainTest::ds1_crearOC_proveedorValido);
        test("DS1 - crearOrdenCompra: proveedor inexistente lanza excepcion",     MainTest::ds1_crearOC_proveedorInexistente);
        test("DS1 - agregarItem: subtotal correcto (10 x 150 = 1500)",            MainTest::ds1_agregarItem_subtotal);
        test("DS1 - agregarItem: varios items, subtotales correctos",             MainTest::ds1_agregarItem_varios);
        test("DS1 - agregarItem: sin precio vigente lanza excepcion",             MainTest::ds1_agregarItem_sinPrecio);
        test("DS1 - emitirOrdenCompra: dentro de tope -> EMITIDA",               MainTest::ds1_emitir_dentroDeTope);
        test("DS1 - emitirOrdenCompra: calcularTotal correcto",                   MainTest::ds1_emitir_calcularTotal);
        test("DS1 - emitirOrdenCompra: supera tope sin supervisor -> excepcion",  MainTest::ds1_emitir_superaTopeSinSupervisor);
        test("DS1 - emitirOrdenCompra: supera tope con supervisor -> AUTORIZADA", MainTest::ds1_emitir_superaTopeConSupervisor);
        test("DS1 - anularOrdenCompra: estado -> ANULADA",                        MainTest::ds1_anular);

        // --- DS2 - Emitir Orden de Pago ---
        test("DS2 - iniciarOrdenPago: retorna comprobantes impagos",              MainTest::ds2_iniciar_conFacturas);
        test("DS2 - iniciarOrdenPago: proveedor inexistente lanza excepcion",     MainTest::ds2_iniciar_proveedorInexistente);
        test("DS2 - seleccionarComprobantes: importeBruto correcto",              MainTest::ds2_seleccionar_importeBruto);
        test("DS2 - seleccionarComprobantes: retenciones calculadas (RI)",        MainTest::ds2_seleccionar_retenciones);
        test("DS2 - confirmarPago: efectivo cubre neto -> EMITIDA",              MainTest::ds2_confirmar_exitoso);
        test("DS2 - confirmarPago: medios insuficientes lanza excepcion",         MainTest::ds2_confirmar_mediosInsuficientes);
        test("DS2 - confirmarPago: saldo de factura baja a 0 tras pago total",   MainTest::ds2_confirmar_saldoCero);

        // --- DS3 - Registrar Factura ---
        test("DS3 - registrarFactura: con OC sin desvios -> exitoso",             MainTest::ds3_conOC_exitoso);
        test("DS3 - registrarFactura: sin OC sin supervisor lanza excepcion",     MainTest::ds3_sinOC_sinSupervisor);
        test("DS3 - registrarFactura: sin OC con supervisor -> exitoso",          MainTest::ds3_sinOC_conSupervisor);
        test("DS3 - registrarFactura: desvio precio sin supervisor -> excepcion", MainTest::ds3_desvio_sinSupervisor);
        test("DS3 - registrarFactura: entrega parcial -> PARCIALMENTE_FACTURADA", MainTest::ds3_parcial);

        // --- DS4 - Cuenta Corriente ---
        test("DS4 - consultarCuentaCorriente: sin comprobantes -> deuda 0",       MainTest::ds4_sinComprobantes);
        test("DS4 - consultarCuentaCorriente: una factura -> deuda correcta",     MainTest::ds4_unaFactura);
        test("DS4 - consultarCuentaCorriente: dos facturas -> suma correcta",     MainTest::ds4_dosFacturas);
        test("DS4 - consultarCuentaCorriente: proveedor inexistente -> excepcion",MainTest::ds4_proveedorInexistente);
        test("DS4 - listarDocumentosImpagos: solo facturas con saldo > 0",        MainTest::ds4_listarImpagos);

        // --- Proveedores (RF-01 a RF-09) ---
        test("PROV - registrarProveedor: CUIT valido -> queda en lista",          MainTest::prov_registrar_exitoso);
        test("PROV - registrarProveedor: CUIT invalido lanza excepcion",          MainTest::prov_registrar_cuitInvalido);
        test("PROV - registrarProveedor: CUIT duplicado lanza excepcion",         MainTest::prov_registrar_cuitDuplicado);
        test("PROV - modificarProveedor: datos actualizados correctamente",       MainTest::prov_modificar_exitoso);
        test("PROV - modificarProveedor: proveedor dado de baja lanza excepcion", MainTest::prov_modificar_dadoDeBaja);
        test("PROV - darBajaProveedor: queda inactivo",                           MainTest::prov_darBaja_exitoso);
        test("PROV - registrarRubro: nombre unico -> queda en lista",             MainTest::prov_rubro_exitoso);
        test("PROV - registrarRubro: nombre duplicado lanza excepcion",           MainTest::prov_rubro_duplicado);
        test("PROV - registrarCertificadoNoRetencion: certificado vigente",       MainTest::prov_certificado_exitoso);
        test("PROV - parametrizarImpuesto: impuesto agregado a lista",            MainTest::prov_impuesto_exitoso);

        // --- Productos (RF-05 a RF-07) ---
        test("PROD - registrarProducto: codigo unico -> queda en lista",          MainTest::prod_registrar_exitoso);
        test("PROD - registrarProducto: codigo duplicado lanza excepcion",        MainTest::prod_registrar_codigoDuplicado);
        test("PROD - darBajaProducto: queda inactivo",                            MainTest::prod_darBaja_exitoso);
        test("PROD - agregarPrecioAcordado: precio vigente consultable",          MainTest::prod_precio_exitoso);
        test("PROD - consultarCompulsaPrecios: retorna historial de precios",     MainTest::prod_compulsa_exitoso);

        // --- Usuarios (RF-28, RF-29) ---
        test("USER - autenticarUsuario: credenciales validas retorna usuario",    MainTest::user_autenticar_valido);
        test("USER - autenticarUsuario: credenciales invalidas retorna null",     MainTest::user_autenticar_invalido);
        test("USER - registrarUsuario: queda en lista de todos",                  MainTest::user_registrar_exitoso);

        // --- Reportes extras (RF-23 a RF-26) ---
        test("REP  - consultarDeudaVigentePorProveedor: mapa con deuda correcta", MainTest::rep_deudaVigente);
        test("REP  - reporteRetencionesPorTipo: contiene todos los tipos",        MainTest::rep_retencionesPorTipo);
        test("REP  - generarLibroIVACompras: linea por cada factura activa",      MainTest::rep_libroIVA);
        test("REP  - consultarFacturasPorDia: filtra por fecha correctamente",    MainTest::rep_facturasPorDia);

        // --- Nota de Credito / Nota de Debito ---
        test("NC   - registrarNotaCredito: queda en proveedor y comprobantes",    MainTest::fact_notaCredito_exitoso);
        test("ND   - registrarNotaDebito: queda en proveedor y comprobantes",     MainTest::fact_notaDebito_exitoso);

        System.out.println("\n--- " + passed + " PASS / " + failed + " FAIL ---");
    }

    // =========================================================================
    // Setup — carga de datos manuales
    // =========================================================================

    private static void setUp() {
        proveedores   = new ArrayList<>();
        rubros        = new ArrayList<>();
        productos     = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
        comprobantes  = new ArrayList<>();
        ordenesPago   = new ArrayList<>();
        usuarios      = new ArrayList<>();
        impuestos     = new ArrayList<>();

        rubMed = new Rubro(1, "Medicamentos", "Farmaceuticos");
        rubros.add(rubMed);

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

        ProveedorController.inicializar(proveedores, rubros, impuestos, usuarios);
        ProductoController.inicializar(productos, proveedores);
        UsuarioController.inicializar(usuarios);
        OrdenCompraController.inicializar(proveedores, productos, ordenesCompra, usuarios);
        OrdenPagoController.inicializar(proveedores, impuestos, ordenesPago);
        FacturaController.inicializar(proveedores, ordenesCompra, comprobantes, usuarios);
        ReportesController.inicializar(proveedores, comprobantes, ordenesPago, productos, ordenesCompra);
    }

    // =========================================================================
    // DS1 - Generar Orden de Compra
    // =========================================================================

    private static void ds1_crearOC_proveedorValido() {
        setUp();
        OrdenCompra oc = OrdenCompraController.getInstance().crearOrdenCompra("20-11111111-1");
        assertNotNull(oc, "OC no debe ser null");
        assertEquals(proveedor, oc.getProveedor(), "Proveedor incorrecto");
        assertTrue(oc.getNumero().startsWith("OC-"), "Numero debe empezar con OC-");
        assertEquals(0, oc.getDetalles().size(), "OC nueva debe tener 0 detalles");
    }

    private static void ds1_crearOC_proveedorInexistente() {
        setUp();
        try {
            OrdenCompraController.getInstance().crearOrdenCompra("99-99999999-9");
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
        OrdenCompra oc = OrdenCompraController.getInstance().crearOrdenCompra("20-11111111-1");
        try {
            OrdenCompraController.getInstance().agregarItem(oc, "SIN-001", 1.0, 1);
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
        ctrl.agregarItem(oc, "MED-001", 10.0, 1); // 10*150=1500
        ctrl.agregarItem(oc, "MED-002", 5.0,  2); // 5*80=400
        ctrl.emitirOrdenCompra(oc, null, null);
        assertEquals(1900.0, oc.getImporteTotal(), "Total debe ser 1900");
    }

    private static void ds1_emitir_superaTopeSinSupervisor() {
        setUp();
        proveedor.setTopeMaximoDeuda(100.0);
        OrdenCompraController ctrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);
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
        List<Comprobante> impagos = OrdenPagoController.getInstance().iniciarOrdenPago("20-11111111-1");
        assertEquals(2, impagos.size(), "Debe retornar 2 comprobantes impagos");
        assertTrue(impagos.contains(f1), "Debe contener la factura 1");
        assertTrue(impagos.contains(f2), "Debe contener la factura 2");
    }

    private static void ds2_iniciar_proveedorInexistente() {
        setUp();
        try {
            OrdenPagoController.getInstance().iniciarOrdenPago("99-99999999-9");
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
        assertEquals(f1.getSaldoPendiente() + f2.getSaldoPendiente(), op.getImporteBruto(),
                "ImporteBruto debe ser la suma de los saldos");
    }

    private static void ds2_seleccionar_retenciones() {
        setUp();
        Factura f1 = crearFacturaEnProveedor("FAC-001", 10.0, 100.0);
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(f1, f1.getSaldoPendiente());
        OrdenPago op = OrdenPagoController.getInstance()
                .seleccionarComprobantes("20-11111111-1", seleccion, new Date());
        double base     = f1.getSaldoPendiente();
        double esperado = base * 0.105 + base * 0.03;
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
        medios.add(new Efectivo(1, 1.0, new Date()));
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
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 10.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        Comprobante comp = factCtrl.registrar("20-11111111-1", TipoComprobante.FACTURA_A,
                detalles, List.of(oc.getNumero()), null, "Test DS3");
        assertNotNull(comp, "Comprobante no debe ser null");
        assertEquals(1, comprobantes.size(), "Debe haber 1 comprobante registrado");
        assertEquals(EstadoOrdenCompra.FACTURADA, oc.getEstado(), "OC debe quedar FACTURADA");
        assertTrue(proveedor.obtenerCuentaCorriente() > 0, "Deuda del proveedor debe ser > 0");
    }

    private static void ds3_sinOC_sinSupervisor() {
        setUp();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        try {
            FacturaController.getInstance().registrar("20-11111111-1", TipoComprobante.FACTURA_A,
                    detalles, new ArrayList<>(), null, "Test sin OC");
            throw new AssertionError("Debia lanzar IllegalStateException");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds3_sinOC_conSupervisor() {
        setUp();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        Comprobante comp = FacturaController.getInstance().registrar("20-11111111-1",
                TipoComprobante.FACTURA_A, detalles, new ArrayList<>(), supervisor, "Compra directa");
        assertNotNull(comp, "Comprobante no debe ser null");
        assertEquals(1, comprobantes.size(), "Debe haber 1 comprobante registrado");
    }

    private static void ds3_desvio_sinSupervisor() {
        setUp();
        OrdenCompraController ocCtrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-11111111-1");
        ocCtrl.agregarItemConPrecio(oc, "MED-001", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 10.0, 200.0, TipoIVA.IVA_21.getPorcentaje()));
        try {
            FacturaController.getInstance().registrar("20-11111111-1", TipoComprobante.FACTURA_A,
                    detalles, List.of(oc.getNumero()), null, "Test desvio");
            throw new AssertionError("Debia lanzar IllegalStateException por desvio de precio");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void ds3_parcial() {
        setUp();
        OrdenCompraController ocCtrl = OrdenCompraController.getInstance();
        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-11111111-1");
        ocCtrl.agregarItemConPrecio(oc, "MED-001", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        FacturaController.getInstance().registrar("20-11111111-1", TipoComprobante.FACTURA_A,
                detalles, List.of(oc.getNumero()), null, "Entrega parcial");
        assertEquals(EstadoOrdenCompra.PARCIALMENTE_FACTURADA, oc.getEstado(),
                "OC debe quedar PARCIALMENTE_FACTURADA");
    }

    // =========================================================================
    // DS4 - Cuenta Corriente
    // =========================================================================

    private static void ds4_sinComprobantes() {
        setUp();
        List<Comprobante> resultado = ReportesController.getInstance()
                .consultarCuentaCorriente("20-11111111-1");
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
        assertEquals(f1.getSaldoPendiente() + f2.getSaldoPendiente(),
                proveedor.obtenerCuentaCorriente(), "Deuda debe ser la suma de ambas facturas");
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
        f2.registrarPago(f2.getSaldoPendiente());
        List<Comprobante> impagos = ReportesController.getInstance().listarDocumentosImpagos("20-11111111-1");
        assertEquals(1, impagos.size(), "Solo debe retornar 1 impago");
        assertTrue(impagos.contains(f1),  "Debe contener f1 (impaga)");
        assertTrue(!impagos.contains(f2), "No debe contener f2 (pagada)");
    }

    // =========================================================================
    // Proveedores (RF-01 a RF-09)
    // =========================================================================

    private static void prov_registrar_exitoso() {
        setUp();
        ProveedorController ctrl = ProveedorController.getInstance();
        ctrl.registrarProveedor("30-22222222-2", "NuevoLab SA", "NuevoLab",
                "Calle Falsa 123", "011-9999", "nuevo@lab.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, "IB-002", 20000.0, List.of(rubMed));
        assertEquals(2, proveedores.size(), "Debe haber 2 proveedores");
        assertNotNull(ctrl.buscarProveedorPorId("30-22222222-2"), "Debe encontrarse por CUIT");
    }

    private static void prov_registrar_cuitInvalido() {
        setUp();
        try {
            ProveedorController.getInstance().registrarProveedor(
                    "30222222222", "NuevoLab", "NuevoLab", "", "", "",
                    CondicionIVA.MONOTRIBUTISTA, "IB-003", 10000.0, new ArrayList<>());
            throw new AssertionError("Debia lanzar IllegalArgumentException por CUIT invalido");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void prov_registrar_cuitDuplicado() {
        setUp();
        try {
            ProveedorController.getInstance().registrarProveedor(
                    "20-11111111-1", "Duplicado", "Dup", "", "", "",
                    CondicionIVA.MONOTRIBUTISTA, "IB-DUP", 5000.0, new ArrayList<>());
            throw new AssertionError("Debia lanzar IllegalArgumentException por CUIT duplicado");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void prov_modificar_exitoso() {
        setUp();
        ProveedorController.getInstance().modificarProveedor(
                "20-11111111-1", "LabSA Modificado", "LabMod",
                "Nueva Direccion 999", "011-0000", "nuevo@mail.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 99000.0, List.of(rubMed));
        Proveedor p = ProveedorController.getInstance().buscarProveedorPorId("20-11111111-1");
        assertEquals("LabSA Modificado", p.getRazonSocial(), "Razon social debe actualizarse");
        assertEquals(99000.0, p.getTopeMaximoDeuda(), "Tope debe actualizarse");
    }

    private static void prov_modificar_dadoDeBaja() {
        setUp();
        ProveedorController ctrl = ProveedorController.getInstance();
        ctrl.darBajaProveedor("20-11111111-1");
        try {
            ctrl.modificarProveedor("20-11111111-1", "X", "X", "", "", "",
                    CondicionIVA.MONOTRIBUTISTA, 1.0, new ArrayList<>());
            throw new AssertionError("Debia lanzar IllegalStateException");
        } catch (IllegalStateException e) { /* esperado */ }
    }

    private static void prov_darBaja_exitoso() {
        setUp();
        ProveedorController ctrl = ProveedorController.getInstance();
        ctrl.darBajaProveedor("20-11111111-1");
        assertTrue(!proveedor.isActivo(), "Proveedor debe quedar inactivo");
    }

    private static void prov_rubro_exitoso() {
        setUp();
        ProveedorController ctrl = ProveedorController.getInstance();
        Rubro r = ctrl.registrarRubro("Limpieza", "Insumos de limpieza");
        assertNotNull(r, "Rubro no debe ser null");
        assertEquals(2, rubros.size(), "Debe haber 2 rubros en la lista");
    }

    private static void prov_rubro_duplicado() {
        setUp();
        try {
            ProveedorController.getInstance().registrarRubro("Medicamentos", "Duplicado");
            throw new AssertionError("Debia lanzar IllegalArgumentException por nombre duplicado");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void prov_certificado_exitoso() {
        setUp();
        Date desde = new Date(0);
        Date hasta = new Date(Long.MAX_VALUE);
        CertificadoNoRetencion cert = new CertificadoNoRetencion(
                "CERT-001", TipoImpuesto.IVA, desde, hasta, 0.0);
        ProveedorController.getInstance().registrarCertificadoNoRetencion("20-11111111-1", cert);
        assertTrue(proveedor.tieneCertificadoVigente(TipoImpuesto.IVA),
                "Proveedor debe tener certificado IVA vigente");
    }

    private static void prov_impuesto_exitoso() {
        setUp();
        int antes = impuestos.size();
        ProveedorController.getInstance().parametrizarImpuesto(new ImpuestoIVA(99, 5.0, 0.0));
        assertEquals(antes + 1, impuestos.size(), "Debe haber un impuesto mas en la lista");
    }

    // =========================================================================
    // Productos (RF-05 a RF-07)
    // =========================================================================

    private static void prod_registrar_exitoso() {
        setUp();
        ProductoController ctrl = ProductoController.getInstance();
        Producto nuevo = new Producto("NEW-001", "Nuevo Producto", "u", TipoIVA.IVA_21, rubMed);
        ctrl.registrarProducto(nuevo);
        assertEquals(3, productos.size(), "Debe haber 3 productos");
        assertNotNull(ctrl.buscarProductoPorCodigo("NEW-001"), "Debe encontrarse por codigo");
    }

    private static void prod_registrar_codigoDuplicado() {
        setUp();
        try {
            ProductoController.getInstance().registrarProducto(
                    new Producto("MED-001", "Duplicado", "u", TipoIVA.IVA_21, rubMed));
            throw new AssertionError("Debia lanzar IllegalArgumentException por codigo duplicado");
        } catch (IllegalArgumentException e) { /* esperado */ }
    }

    private static void prod_darBaja_exitoso() {
        setUp();
        ProductoController.getInstance().darBajaProducto("MED-001");
        assertTrue(!ibuprofeno.isActivo(), "Producto debe quedar inactivo");
    }

    private static void prod_precio_exitoso() {
        setUp();
        ProductoController ctrl = ProductoController.getInstance();
        ctrl.agregarPrecioAcordado("MED-002", "20-11111111-1", 99.0);
        double precio = ctrl.obtenerPrecioVigente("MED-002", "20-11111111-1");
        assertEquals(99.0, precio, "Precio vigente debe ser el ultimo registrado");
    }

    private static void prod_compulsa_exitoso() {
        setUp();
        List<PrecioAcordado> historial = ProductoController.getInstance()
                .consultarCompulsaPrecios("MED-001");
        assertTrue(historial.size() >= 1, "Debe haber al menos un precio en el historial");
    }

    // =========================================================================
    // Usuarios (RF-28, RF-29)
    // =========================================================================

    private static void user_autenticar_valido() {
        setUp();
        Usuario u = UsuarioController.getInstance().autenticarUsuario("crios", "pass");
        assertNotNull(u, "Usuario debe ser encontrado con credenciales correctas");
        assertEquals("crios", u.getUsername(), "Username debe coincidir");
    }

    private static void user_autenticar_invalido() {
        setUp();
        Usuario u = UsuarioController.getInstance().autenticarUsuario("nadie", "mal");
        assertTrue(u == null, "Usuario debe ser null con credenciales incorrectas");
    }

    private static void user_registrar_exitoso() {
        setUp();
        Usuario nuevo = new Usuario(99, "Juan", "Perez", "jperez", "1234", RolUsuario.OPERADOR);
        UsuarioController.getInstance().registrarUsuario(nuevo);
        assertEquals(2, UsuarioController.getInstance().listarTodos().size(),
                "Debe haber 2 usuarios registrados");
    }

    // =========================================================================
    // Reportes extras (RF-23 a RF-26)
    // =========================================================================

    private static void rep_deudaVigente() {
        setUp();
        Factura f = new Factura("FAC-001", TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f.agregarDetalle(new DetalleComprobante(1, ibuprofeno, 5.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        f.calcularImpuestos();
        proveedor.agregarComprobante(f);
        Map<String, Double> deudas = ReportesController.getInstance()
                .consultarDeudaVigentePorProveedor();
        assertEquals(1, deudas.size(), "Debe haber 1 entrada en el mapa");
        double deuda = deudas.values().iterator().next();
        assertTrue(deuda > 0, "Deuda debe ser > 0");
    }

    private static void rep_retencionesPorTipo() {
        setUp();
        Map<TipoImpuesto, Double> mapa = ReportesController.getInstance().reporteRetencionesPorTipo();
        assertNotNull(mapa, "Mapa no debe ser null");
        assertTrue(mapa.containsKey(TipoImpuesto.IVA), "Debe contener clave IVA");
        assertTrue(mapa.containsKey(TipoImpuesto.INGRESOS_BRUTOS), "Debe contener clave IIBB");
        assertTrue(mapa.containsKey(TipoImpuesto.GANANCIAS), "Debe contener clave GANANCIAS");
    }

    private static void rep_libroIVA() {
        setUp();
        // Registrar una factura via FacturaController para que quede en la lista compartida
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 10.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        FacturaController.getInstance().registrar("20-11111111-1", TipoComprobante.FACTURA_A,
                detalles, new ArrayList<>(), supervisor, "Test libro IVA");
        List<Map<String, Object>> libro = ReportesController.getInstance().generarLibroIVACompras();
        assertEquals(1, libro.size(), "Debe haber 1 linea en el libro IVA");
        assertTrue(libro.get(0).containsKey("iva21"), "La linea debe tener campo iva21");
    }

    private static void rep_facturasPorDia() {
        setUp();
        Date hoy = new Date();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 3.0, 100.0, TipoIVA.IVA_21.getPorcentaje()));
        FacturaController.getInstance().registrar("20-11111111-1", TipoComprobante.FACTURA_A,
                detalles, new ArrayList<>(), supervisor, "Test por dia");
        List<Factura> resultado = ReportesController.getInstance()
                .consultarFacturasPorDia(hoy, "20-11111111-1");
        assertEquals(1, resultado.size(), "Debe encontrar 1 factura de hoy");
    }

    // =========================================================================
    // Nota de Credito / Nota de Debito
    // =========================================================================

    private static void fact_notaCredito_exitoso() {
        setUp();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 2.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        Comprobante nc = FacturaController.getInstance().registrar(
                "20-11111111-1", TipoComprobante.NOTA_CREDITO,
                detalles, new ArrayList<>(), null, "NC test");
        assertNotNull(nc, "Nota de credito no debe ser null");
        assertEquals(1, comprobantes.size(), "Debe haber 1 comprobante registrado");
        assertEquals(1, proveedor.getComprobantes().size(), "Proveedor debe tener 1 comprobante");
    }

    private static void fact_notaDebito_exitoso() {
        setUp();
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, ibuprofeno, 1.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));
        Comprobante nd = FacturaController.getInstance().registrar(
                "20-11111111-1", TipoComprobante.NOTA_DEBITO,
                detalles, new ArrayList<>(), null, "ND test");
        assertNotNull(nd, "Nota de debito no debe ser null");
        assertEquals(1, comprobantes.size(), "Debe haber 1 comprobante registrado");
        assertEquals(1, proveedor.getComprobantes().size(), "Proveedor debe tener 1 comprobante");
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

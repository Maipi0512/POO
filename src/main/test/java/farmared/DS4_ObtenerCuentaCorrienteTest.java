package farmared;

import farmared.controladores.ReportesController;
import farmared.enums.CondicionIVA;
import farmared.enums.TipoComprobante;
import farmared.enums.TipoIVA;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modulos.m5_comprobantes.Factura;
import farmared.modulos.m5_comprobantes.NotaCredito;
import farmared.modulos.m6_ordenes_pago.OrdenPago;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para DS4 - Obtener Cuenta Corriente del Proveedor.
 *
 * Flujo:
 *   consultarCuentaCorriente(cuit)
 *   → buscarProveedorPorId(cuit)
 *   → proveedor.obtenerCuentaCorriente()
 *       → calcularDeudaVigente()
 *           → loop comprobantes: afectarCuentaCorriente()  [polimorfismo]
 *   → proveedor.getComprobantes()
 */
public class DS4_ObtenerCuentaCorrienteTest {

    private List<Proveedor>   proveedores;
    private List<Comprobante> comprobantes;
    private List<OrdenPago>   ordenesPago;
    private List<Producto>    productos;

    private Proveedor proveedor;
    private Producto  producto;

    private ReportesController ctrl;

    @Before
    public void setUp() {
        proveedores  = new ArrayList<>();
        comprobantes = new ArrayList<>();
        ordenesPago  = new ArrayList<>();
        productos    = new ArrayList<>();

        Rubro rubMed = new Rubro(1, "Medicamentos", "");

        proveedor = new Proveedor(
                "20-11111111-1", "LabSA", "Lab SA",
                "Av. Principal 100", "011-1111", "lab@sa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, "IB-001", new Date()
        );
        proveedor.setTopeMaximoDeuda(200000.0);
        proveedor.agregarRubro(rubMed);
        proveedores.add(proveedor);

        producto = new Producto("MED-001", "Ibuprofeno 600mg", "caja", TipoIVA.IVA_21, rubMed);
        producto.agregarPrecioAcordado(new PrecioAcordado(100.0, new Date(0), null, proveedor));
        productos.add(producto);

        ReportesController.inicializar(proveedores, comprobantes, ordenesPago, productos);
        ctrl = ReportesController.getInstance();
    }

    // Helper: crea y registra una Factura en el proveedor
    private Factura crearFactura(String numero, double cantidad, double precio) {
        Factura f = new Factura(numero, TipoComprobante.FACTURA_A, new Date(), new Date(), proveedor);
        f.agregarDetalle(new DetalleComprobante(1, producto, cantidad, precio, TipoIVA.IVA_21.getPorcentaje()));
        f.calcularImpuestos();
        proveedor.agregarComprobante(f);
        return f;
    }

    // Helper: crea y registra una Nota de Crédito en el proveedor
    private NotaCredito crearNotaCredito(String numero, double cantidad, double precio) {
        NotaCredito nc = new NotaCredito(numero, new Date(), new Date(), proveedor);
        nc.agregarDetalle(new DetalleComprobante(1, producto, cantidad, precio, TipoIVA.IVA_21.getPorcentaje()));
        nc.calcularImpuestos();
        proveedor.agregarComprobante(nc);
        return nc;
    }

    // -------------------------------------------------------------------------
    // Test 1: sin comprobantes → deuda = 0
    // -------------------------------------------------------------------------
    @Test
    public void consultarCuentaCorriente_sinComprobantes_deudaCero() {
        List<Comprobante> resultado = ctrl.consultarCuentaCorriente("20-11111111-1");

        assertNotNull(resultado);
        assertEquals(0, resultado.size());
        assertEquals(0.0, proveedor.obtenerCuentaCorriente(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 2: una factura → deuda = importeTotal de la factura
    // -------------------------------------------------------------------------
    @Test
    public void consultarCuentaCorriente_unaFactura_deudaIgualAFactura() {
        Factura f = crearFactura("FAC-001", 10.0, 100.0);

        List<Comprobante> resultado = ctrl.consultarCuentaCorriente("20-11111111-1");

        assertEquals(1, resultado.size());
        double deuda = proveedor.obtenerCuentaCorriente();
        assertEquals(f.getSaldoPendiente(), deuda, 0.01);
        assertTrue(deuda > 0);
    }

    // -------------------------------------------------------------------------
    // Test 3: factura + nota de crédito → deuda se reduce
    // -------------------------------------------------------------------------
    @Test
    public void consultarCuentaCorriente_facturaYNC_deudaReducida() {
        Factura     f  = crearFactura("FAC-001", 10.0, 100.0);
        NotaCredito nc = crearNotaCredito("NC-001", 3.0, 100.0);

        ctrl.consultarCuentaCorriente("20-11111111-1");

        double deudaNeta = proveedor.obtenerCuentaCorriente();
        double esperada  = f.getSaldoPendiente() - nc.getSaldoPendiente();

        assertEquals(esperada, deudaNeta, 0.01);
        assertTrue("La NC debe reducir la deuda", deudaNeta < f.getSaldoPendiente());
    }

    // -------------------------------------------------------------------------
    // Test 4: dos facturas → deuda = suma de ambas
    // -------------------------------------------------------------------------
    @Test
    public void consultarCuentaCorriente_dosFacturas_deudaEsSuma() {
        Factura f1 = crearFactura("FAC-001", 10.0, 100.0);
        Factura f2 = crearFactura("FAC-002", 5.0,  100.0);

        ctrl.consultarCuentaCorriente("20-11111111-1");

        double deuda    = proveedor.obtenerCuentaCorriente();
        double esperada = f1.getSaldoPendiente() + f2.getSaldoPendiente();

        assertEquals(esperada, deuda, 0.01);
        assertEquals(2, proveedor.getComprobantes().size());
    }

    // -------------------------------------------------------------------------
    // Test 5: factura pagada parcialmente → deuda refleja saldo restante
    // -------------------------------------------------------------------------
    @Test
    public void consultarCuentaCorriente_facturaPagadaParcialmente_deudaBajoCorrectamente() {
        Factura f = crearFactura("FAC-001", 10.0, 100.0);
        double saldoOriginal = f.getSaldoPendiente();
        double pago = saldoOriginal / 2.0;

        f.registrarPago(pago);

        ctrl.consultarCuentaCorriente("20-11111111-1");

        double deuda = proveedor.obtenerCuentaCorriente();
        assertEquals(saldoOriginal - pago, deuda, 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 6: factura pagada totalmente → deuda = 0
    // -------------------------------------------------------------------------
    @Test
    public void consultarCuentaCorriente_facturaPagadaTotalmente_deudaCero() {
        Factura f = crearFactura("FAC-001", 10.0, 100.0);
        f.registrarPago(f.getSaldoPendiente());

        ctrl.consultarCuentaCorriente("20-11111111-1");

        assertEquals(0.0, proveedor.obtenerCuentaCorriente(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 7: proveedor inexistente → excepción
    // -------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void consultarCuentaCorriente_proveedorInexistente_lanzaExcepcion() {
        ctrl.consultarCuentaCorriente("99-99999999-9");
    }

    // -------------------------------------------------------------------------
    // Test 8: listarDocumentosImpagos → solo facturas con saldo > 0
    // -------------------------------------------------------------------------
    @Test
    public void listarDocumentosImpagos_soloFacturasSinPagar() {
        Factura f1 = crearFactura("FAC-001", 10.0, 100.0);
        Factura f2 = crearFactura("FAC-002", 5.0,  100.0);
        f2.registrarPago(f2.getSaldoPendiente()); // pagada totalmente

        List<Comprobante> impagos = ctrl.listarDocumentosImpagos("20-11111111-1");

        assertEquals(1, impagos.size());
        assertTrue(impagos.contains(f1));
        assertFalse(impagos.contains(f2));
    }
}

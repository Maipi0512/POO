package farmared;

import farmared.controladores.FacturaController;
import farmared.controladores.OrdenCompraController;
import farmared.enums.CondicionIVA;
import farmared.enums.EstadoOrdenCompra;
import farmared.enums.RolUsuario;
import farmared.enums.TipoComprobante;
import farmared.enums.TipoIVA;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.Comprobante;
import farmared.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modulos.m5_comprobantes.Factura;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para DS3 - Registrar Comprobante (Factura).
 *
 * Flujo completo:
 *   1. Proveedor tiene un Rubro
 *   2. Producto pertenece a ese Rubro y tiene PrecioAcordado con el Proveedor
 *   3. Se genera una OC (DS1) con ese producto
 *   4. Se emite la OC
 *   5. Se registra la Factura asociada a la OC (DS3)
 *   6. Se verifican resultados: factura creada, OC pasa a FACTURADA, deuda del proveedor sube
 */
public class DS3_RegistrarFacturaTest {

    // Listas compartidas (simulan AppContext)
    private List<Proveedor>   proveedores;
    private List<Producto>    productos;
    private List<OrdenCompra> ordenesCompra;
    private List<Comprobante> comprobantes;
    private List<Usuario>     usuarios;

    // Objetos de dominio
    private Proveedor proveedor;
    private Producto  producto;
    private Usuario   supervisor;

    private OrdenCompraController ocCtrl;
    private FacturaController     factCtrl;

    @Before
    public void setUp() {
        proveedores   = new ArrayList<>();
        productos     = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
        comprobantes  = new ArrayList<>();
        usuarios      = new ArrayList<>();

        // Crear rubro
        Rubro rubroMedicamentos = new Rubro(1, "Medicamentos", "Medicamentos en general");

        // Crear proveedor
        proveedor = new Proveedor(
                "20-12345678-1", "LabSA", "Lab SA",
                "Av. Siempre Viva 742", "0800-555-1234", "lab@sa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, "12345678", new Date()
        );
        proveedor.setTopeMaximoDeuda(100000.0);
        proveedor.agregarRubro(rubroMedicamentos);
        proveedores.add(proveedor);

        // Crear producto con IVA 21% en el rubro Medicamentos
        producto = new Producto("IBUP-500", "Ibuprofeno 500mg", "caja", TipoIVA.IVA_21, rubroMedicamentos);
        // Precio acordado: $150 sin vencimiento
        producto.agregarPrecioAcordado(new PrecioAcordado(150.0, new Date(0), null, proveedor));
        productos.add(producto);

        // Supervisor para autorizaciones
        supervisor = new Usuario(1, "Ana", "Lopez", "ana", "1234", RolUsuario.SUPERVISOR);
        usuarios.add(supervisor);

        // Inicializar controladores con las mismas listas
        OrdenCompraController.inicializar(proveedores, productos, ordenesCompra, usuarios);
        FacturaController.inicializar(proveedores, ordenesCompra, comprobantes, usuarios);

        ocCtrl   = OrdenCompraController.getInstance();
        factCtrl = FacturaController.getInstance();
    }

    // -------------------------------------------------------------------------
    // Test 1: flujo completo DS1 + DS3 — factura con OC, sin desvíos
    // -------------------------------------------------------------------------
    @Test
    public void registrarFactura_conOC_exitoso() {
        // DS1: generar OC
        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-12345678-1");
        ocCtrl.agregarItemConPrecio(oc, "IBUP-500", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);

        assertEquals(EstadoOrdenCompra.EMITIDA, oc.getEstado());
        assertEquals(1, ordenesCompra.size());

        // DS3: construir detalles a partir de la OC (igual que hace el panel)
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, producto, 10.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));

        List<String> nrosOC = List.of(oc.getNumero());

        Comprobante comp = factCtrl.registrar(
                "20-12345678-1", TipoComprobante.FACTURA_A, detalles, nrosOC, null, "Test DS3"
        );

        // Verifica que se creó la factura
        assertNotNull(comp);
        assertEquals(1, comprobantes.size());
        assertEquals(1, comp.getDetalles().size());

        // Verifica que la OC cambió de estado
        assertEquals(EstadoOrdenCompra.FACTURADA, oc.getEstado());

        // Verifica que la deuda del proveedor aumentó
        double deuda = proveedor.obtenerCuentaCorriente();
        assertTrue("La deuda del proveedor debe ser mayor a cero", deuda > 0);
    }

    // -------------------------------------------------------------------------
    // Test 2: factura sin OC → requiere supervisor
    // -------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void registrarFactura_sinOC_sinSupervisor_lanzaExcepcion() {
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, producto, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));

        // Sin OC y sin supervisor → debe lanzar IllegalStateException
        factCtrl.registrar(
                "20-12345678-1", TipoComprobante.FACTURA_A, detalles,
                new ArrayList<>(), null, "Test sin OC"
        );
    }

    // -------------------------------------------------------------------------
    // Test 3: factura sin OC con supervisor → registra correctamente
    // -------------------------------------------------------------------------
    @Test
    public void registrarFactura_sinOC_conSupervisor_exitoso() {
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, producto, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));

        Comprobante comp = factCtrl.registrar(
                "20-12345678-1", TipoComprobante.FACTURA_A, detalles,
                new ArrayList<>(), supervisor, "Compra directa autorizada"
        );

        assertNotNull(comp);
        assertEquals(1, comprobantes.size());
        assertTrue(comp instanceof Factura);
    }

    // -------------------------------------------------------------------------
    // Test 4: desvío de precio → requiere supervisor
    // -------------------------------------------------------------------------
    @Test
    public void registrarFactura_conDesvio_sinSupervisor_lanzaExcepcion() {
        // Emitir OC con precio $150
        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-12345678-1");
        ocCtrl.agregarItemConPrecio(oc, "IBUP-500", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);

        // La factura trae precio diferente ($200 en vez de $150) → desvío
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, producto, 10.0, 200.0, TipoIVA.IVA_21.getPorcentaje()));

        try {
            factCtrl.registrar(
                    "20-12345678-1", TipoComprobante.FACTURA_A, detalles,
                    List.of(oc.getNumero()), null, "Test desvio"
            );
            fail("Deberia haber lanzado IllegalStateException por desvio de precio");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("autorizacion"));
        }
    }

    // -------------------------------------------------------------------------
    // Test 5: OC parcialmente facturada (solo algunos items)
    // -------------------------------------------------------------------------
    @Test
    public void registrarFactura_parcial_ocQuedaParcialmenteFacturada() {
        // OC con 10 unidades
        OrdenCompra oc = ocCtrl.crearOrdenCompra("20-12345678-1");
        ocCtrl.agregarItemConPrecio(oc, "IBUP-500", 10.0, 1, 150.0);
        ocCtrl.emitirOrdenCompra(oc, null, null);

        // Factura solo 5 unidades
        List<DetalleComprobante> detalles = new ArrayList<>();
        detalles.add(new DetalleComprobante(1, producto, 5.0, 150.0, TipoIVA.IVA_21.getPorcentaje()));

        factCtrl.registrar(
                "20-12345678-1", TipoComprobante.FACTURA_A, detalles,
                List.of(oc.getNumero()), null, "Entrega parcial"
        );

        assertEquals(EstadoOrdenCompra.PARCIALMENTE_FACTURADA, oc.getEstado());
    }
}

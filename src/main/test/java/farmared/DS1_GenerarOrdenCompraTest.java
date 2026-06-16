package farmared;

import farmared.controladores.OrdenCompraController;
import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.EstadoOrdenCompra;
import farmared.modelo.enums.RolUsuario;
import farmared.modelo.enums.TipoIVA;
import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m4_ordenes_compra.OrdenCompra;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests para DS1 - Generar Orden de Compra.
 *
 * Flujo:
 *   crearOrdenCompra(idProveedor)
 *   → loop: agregarItem(oc, codigoProducto, cantidad)
 *       → obtenerUltimoPrecio(proveedor) → estaVigente()
 *       → new DetalleOC → calcularSubtotal()
 *       → agregarDetalle()
 *   → emitirOrdenCompra(oc)
 *       → calcularTotal()
 *       → proveedor.validarNuevaOC(importeTotal)
 *       → emitir() o emitirConAutorizacion()
 */
public class DS1_GenerarOrdenCompraTest {

    private List<Proveedor>   proveedores;
    private List<Producto>    productos;
    private List<OrdenCompra> ordenesCompra;
    private List<Usuario>     usuarios;

    private Proveedor proveedor;
    private Producto  ibuprofeno;
    private Producto  paracetamol;
    private Usuario   supervisor;

    private OrdenCompraController ctrl;

    @Before
    public void setUp() {
        proveedores   = new ArrayList<>();
        productos     = new ArrayList<>();
        ordenesCompra = new ArrayList<>();
        usuarios      = new ArrayList<>();

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

        OrdenCompraController.inicializar(proveedores, productos, ordenesCompra, usuarios);
        ctrl = OrdenCompraController.getInstance();
    }

    // -------------------------------------------------------------------------
    // Test 1: crearOrdenCompra — OC creada con numero y proveedor correctos
    // -------------------------------------------------------------------------
    @Test
    public void crearOrdenCompra_proveedorValido_retornaOC() {
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");

        assertNotNull(oc);
        assertEquals(proveedor, oc.getProveedor());
        assertNotNull(oc.getNumero());
        assertTrue(oc.getNumero().startsWith("OC-"));
        assertEquals(0, oc.getDetalles().size());
    }

    // -------------------------------------------------------------------------
    // Test 2: crearOrdenCompra — proveedor inexistente lanza excepcion
    // -------------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void crearOrdenCompra_proveedorInexistente_lanzaExcepcion() {
        ctrl.crearOrdenCompra("99-99999999-9");
    }

    // -------------------------------------------------------------------------
    // Test 3: agregarItem — detalle queda en OC con subtotal correcto
    // -------------------------------------------------------------------------
    @Test
    public void agregarItem_productoConPrecio_detalleAgregado() {
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);

        assertEquals(1, oc.getDetalles().size());
        assertEquals(1500.0, oc.getDetalles().get(0).getSubtotal(), 0.01);
        assertEquals(1, oc.getDetalles().get(0).getNroLinea());
    }

    // -------------------------------------------------------------------------
    // Test 4: agregarItem — multiples items, subtotales correctos en cada uno
    // -------------------------------------------------------------------------
    @Test
    public void agregarItem_variosItems_subtotalesCorrectos() {
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);
        ctrl.agregarItem(oc, "MED-002", 5.0, 2);

        assertEquals(2, oc.getDetalles().size());
        assertEquals(1500.0, oc.getDetalles().get(0).getSubtotal(), 0.01);
        assertEquals(400.0,  oc.getDetalles().get(1).getSubtotal(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 5: agregarItem — producto sin precio vigente lanza excepcion
    // -------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void agregarItem_sinPrecioVigente_lanzaExcepcion() {
        Rubro rub = new Rubro(2, "Sin precio", "");
        Producto sinPrecio = new Producto("SIN-001", "Producto sin precio", "u", TipoIVA.IVA_21, rub);
        productos.add(sinPrecio);

        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "SIN-001", 1.0, 1);
    }

    // -------------------------------------------------------------------------
    // Test 6: emitirOrdenCompra — dentro del tope, estado pasa a EMITIDA
    // -------------------------------------------------------------------------
    @Test
    public void emitirOrdenCompra_dentroDeTope_estadoEmitida() {
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);

        ctrl.emitirOrdenCompra(oc, null, null);

        assertEquals(EstadoOrdenCompra.EMITIDA, oc.getEstado());
        assertEquals(1, ordenesCompra.size());
    }

    // -------------------------------------------------------------------------
    // Test 7: emitirOrdenCompra — calcularTotal suma correctamente
    // -------------------------------------------------------------------------
    @Test
    public void emitirOrdenCompra_calcularTotal_correcto() {
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1); // 10 * 150 = 1500
        ctrl.agregarItem(oc, "MED-002", 5.0,  2); // 5  * 80  = 400

        ctrl.emitirOrdenCompra(oc, null, null);

        assertEquals(1900.0, oc.getImporteTotal(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Test 8: emitirOrdenCompra — supera tope sin supervisor lanza excepcion
    // -------------------------------------------------------------------------
    @Test(expected = IllegalStateException.class)
    public void emitirOrdenCompra_superaTopeSinSupervisor_lanzaExcepcion() {
        proveedor.setTopeMaximoDeuda(100.0);

        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1); // $1500 > tope $100

        ctrl.emitirOrdenCompra(oc, null, null);
    }

    // -------------------------------------------------------------------------
    // Test 9: emitirOrdenCompra — supera tope con supervisor → AUTORIZADA
    // -------------------------------------------------------------------------
    @Test
    public void emitirOrdenCompra_superaTopeConSupervisor_estadoAutorizada() {
        proveedor.setTopeMaximoDeuda(100.0);

        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 10.0, 1);

        ctrl.emitirOrdenCompra(oc, supervisor, "Autorizacion por exceso de tope");

        assertEquals(EstadoOrdenCompra.AUTORIZADA, oc.getEstado());
        assertNotNull(oc.getAutorizacion());
    }

    // -------------------------------------------------------------------------
    // Test 10: anularOrdenCompra — estado pasa a ANULADA
    // -------------------------------------------------------------------------
    @Test
    public void anularOrdenCompra_estadoAnulada() {
        OrdenCompra oc = ctrl.crearOrdenCompra("20-11111111-1");
        ctrl.agregarItem(oc, "MED-001", 5.0, 1);
        ctrl.emitirOrdenCompra(oc, null, null);

        ctrl.anularOrdenCompra(oc.getNumero());

        assertEquals(EstadoOrdenCompra.ANULADA, oc.getEstado());
    }
}

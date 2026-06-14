package farmared.sistema;

import farmared.enums.*;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m4_ordenes_compra.OrdenCompra;
import farmared.modulos.m5_comprobantes.DetalleComprobante;
import farmared.modulos.m5_comprobantes.Factura;
import farmared.modulos.m6_ordenes_pago.ImpuestoGanancias;
import farmared.modulos.m6_ordenes_pago.Efectivo;
import farmared.modulos.m6_ordenes_pago.TransferenciaBancaria;
import farmared.enums.EstadoOrdenCompra;
import farmared.ui.DatosIniciales;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SistemaGestionComprasTest {

    private SistemaGestionCompras sistema;

    @Before
    public void setUp() {
        sistema = new SistemaGestionCompras("Test", "1.0");
        DatosIniciales.cargar(sistema);
    }

    @Test
    public void debeCargarVariosProveedores() {
        assertEquals(4, sistema.getProveedores().size());
    }

    @Test
    public void mismoProductoTienePreciosDistintosPorProveedor() {
        double precioLab = sistema.obtenerPrecioVigente("MED-001", "20-12345678-9");
        double precioDist = sistema.obtenerPrecioVigente("MED-001", "30-98765432-1");
        assertEquals(150.0, precioLab, 0.01);
        assertEquals(142.0, precioDist, 0.01);
    }

    @Test
    public void listarProductosFiltraPorProveedor() {
        List<Producto> deLab = sistema.listarProductosPorProveedor("20-12345678-9");
        List<Producto> deClean = sistema.listarProductosPorProveedor("27-11223344-5");
        assertEquals(2, deLab.size());
        assertEquals(2, deClean.size());
    }

    @Test
    public void ordenCompraConMultiplesItemsCalculaTotalCorrecto() {
        OrdenCompra oc = sistema.crearOrdenCompra("20-12345678-9");
        sistema.agregarItemOC(oc, "MED-001", 10, 1);
        sistema.agregarItemOC(oc, "MED-002", 5, 2);
        sistema.emitirOrdenCompra(oc, null, null);

        assertEquals(2, oc.getDetalles().size());
        assertEquals(1975.0, oc.getImporteTotal(), 0.01); // 10*150 + 5*95
    }

    @Test
    public void facturaConMultiplesItemsTieneTotalCorrecto() {
        Producto p1 = sistema.buscarProductoPorCodigo("MED-001");
        Producto p2 = sistema.buscarProductoPorCodigo("MED-002");

        List<DetalleComprobante> detalles = Arrays.asList(
                new DetalleComprobante(1, p1, 10, 150.0, 21.0),
                new DetalleComprobante(2, p2, 5, 95.0, 21.0)
        );

        Factura factura = sistema.registrarFactura(
                "FAC-TEST-01", TipoComprobante.FACTURA_A,
                new Date(), new Date(), detalles,
                "20-12345678-9", Collections.emptyList(),
                sistema.listarSupervisores().get(0), "Test"
        );

        assertEquals(2, factura.getDetalles().size());
        assertEquals(1975.0, factura.getImporteNeto(), 0.01);
        assertEquals(2389.75, factura.getImporteTotal(), 0.01);
    }

    @Test
    public void validarPreciosDetectaDesvio() {
        Producto p = sistema.buscarProductoPorCodigo("MED-001");
        DetalleComprobante detFactura = new DetalleComprobante(1, p, 10, 200.0, 21.0);

        OrdenCompra oc = sistema.crearOrdenCompra("20-12345678-9");
        sistema.agregarItemOC(oc, "MED-001", 10, 1);

        assertFalse(sistema.validarPrecios(
                Collections.singletonList(detFactura), oc.getDetalles()));
    }

    @Test
    public void emisionOPCalculaRetenciones() {
        Producto p = sistema.buscarProductoPorCodigo("MED-001");
        Factura factura = sistema.registrarFactura(
                "FAC-OP-01", TipoComprobante.FACTURA_A,
                new Date(), new Date(),
                Collections.singletonList(new DetalleComprobante(1, p, 100, 150.0, 21.0)),
                "20-12345678-9", Collections.emptyList(),
                sistema.listarSupervisores().get(0), "Test"
        );

        Map<farmared.modulos.m5_comprobantes.Comprobante, Double> sel = new LinkedHashMap<>();
        sel.put(factura, factura.getSaldoPendiente());

        var op = sistema.prepararOrdenPago("20-12345678-9", sel, new Date());
        assertTrue(op.getTotalRetenciones() > 0);
        assertTrue(op.getImporteNeto() < op.getImporteBruto());

        sistema.confirmarOrdenPago(op, Collections.singletonList(
                new Efectivo(1, op.getImporteNeto(), new Date())));
        assertEquals(0.0, factura.getSaldoPendiente(), 0.01);
    }

    @Test
    public void validarImpuestosDetectaAlicuotaIncorrecta() {
        Producto p = sistema.buscarProductoPorCodigo("MED-001");
        DetalleComprobante det = new DetalleComprobante(1, p, 10, 150.0, 10.5);
        assertFalse(sistema.validarImpuestos(Collections.singletonList(det)));
    }

    @Test
    public void validarImpuestosAceptaLineaCorrecta() {
        Producto p = sistema.buscarProductoPorCodigo("MED-001");
        DetalleComprobante det = new DetalleComprobante(1, p, 10, 150.0, 21.0);
        assertTrue(sistema.validarImpuestos(Collections.singletonList(det)));
    }

    @Test
    public void registrarRubroDesdeSistema() {
        int antes = sistema.getRubros().size();
        sistema.registrarRubro("Papeleria", "Insumos de oficina");
        assertEquals(antes + 1, sistema.getRubros().size());
    }

    @Test
    public void modificarYDarBajaProveedor() {
        sistema.modificarProveedor("20-12345678-9", "Lab SA Modificado",
                "LabSA", "Nueva 123", "011", "a@b.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 120000);
        assertEquals("Lab SA Modificado",
                sistema.buscarProveedorPorId("20-12345678-9").getRazonSocial());

        sistema.darBajaProveedor("20-12345678-9");
        assertFalse(sistema.buscarProveedorPorId("20-12345678-9").isActivo());
    }

    @Test
    public void facturaAsociadaActualizaEstadoOC() {
        OrdenCompra oc = sistema.crearOrdenCompra("30-98765432-1");
        sistema.agregarItemOC(oc, "MED-001", 10, 1);
        sistema.emitirOrdenCompra(oc, null, null);

        Producto p = sistema.buscarProductoPorCodigo("MED-001");
        Factura factura = sistema.registrarFactura(
                "FAC-OC-01", TipoComprobante.FACTURA_A, new Date(), new Date(),
                Collections.singletonList(new DetalleComprobante(1, p, 10, 142.0, 21.0)),
                "30-98765432-1", Collections.singletonList(oc.getNumero()), null, null
        );

        assertNotNull(factura);
        assertEquals(EstadoOrdenCompra.FACTURADA, oc.getEstado());
    }

    @Test
    public void opConMultiplesMediosDePago() {
        Producto p = sistema.buscarProductoPorCodigo("MED-002");
        Factura factura = sistema.registrarFactura(
                "FAC-MULTI-01", TipoComprobante.FACTURA_A, new Date(), new Date(),
                Collections.singletonList(new DetalleComprobante(1, p, 5, 95.0, 21.0)),
                "20-12345678-9", Collections.emptyList(),
                sistema.listarSupervisores().get(0), "Test"
        );

        Map<farmared.modulos.m5_comprobantes.Comprobante, Double> sel = new LinkedHashMap<>();
        sel.put(factura, factura.getSaldoPendiente());
        var op = sistema.prepararOrdenPago("20-12345678-9", sel, new Date());

        double mitad = Math.round(op.getImporteNeto() / 2.0 * 100.0) / 100.0;
        double resto = Math.round((op.getImporteNeto() - mitad) * 100.0) / 100.0;

        List<farmared.modulos.m6_ordenes_pago.MedioPago> medios = Arrays.asList(
                new Efectivo(1, mitad, new Date()),
                new TransferenciaBancaria(2, resto, new Date(), "111", "222", "OP-001")
        );

        sistema.confirmarOrdenPago(op, medios);
        assertEquals(2, op.getMediosPago().size());
        assertEquals(0.0, factura.getSaldoPendiente(), 0.01);
    }

    @Test
    public void impuestoGananciasUsaEscalas() {
        ImpuestoGanancias gan = (ImpuestoGanancias) sistema.getImpuestos().stream()
                .filter(i -> i instanceof ImpuestoGanancias)
                .findFirst().orElseThrow(() -> new AssertionError("Sin impuesto ganancias"));
        assertEquals(2.0, gan.determinarPorcentaje(5000), 0.01);
        assertEquals(3.5, gan.determinarPorcentaje(20000), 0.01);
        assertEquals(5.0, gan.determinarPorcentaje(60000), 0.01);
    }
}

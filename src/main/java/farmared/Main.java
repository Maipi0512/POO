package farmared;

import farmared.enums.*;
import farmared.modulos.m5_comprobantes.*;
import farmared.modulos.m6_ordenes_pago.*;
import farmared.sistema.SistemaGestionCompras;
import farmared.ui.DatosIniciales;
import java.util.*;

/**
 * Demo por consola del flujo completo con varios proveedores e items multiples.
 */
public class Main {

    public static void main(String[] args) {

        SistemaGestionCompras sistema = new SistemaGestionCompras("FarmaRed", "1.0");
        DatosIniciales.cargar(sistema);

        System.out.println("Proveedores cargados: " + sistema.getProveedores().size());

        // OC con 2 items para Laboratorios SA
        System.out.println("\n--- OC multi-item (Lab SA) ---");
        var oc = sistema.crearOrdenCompra("20-12345678-9");
        sistema.agregarItemOC(oc, "MED-001", 50, 1);
        sistema.agregarItemOC(oc, "MED-002", 20, 2);
        sistema.emitirOrdenCompra(oc, null, null);
        System.out.println("Items: " + oc.getDetalles().size() + " | Total: $" + oc.getImporteTotal());

        // Factura con 2 items vinculada a la OC
        System.out.println("\n--- Factura multi-item ---");
        var p1 = sistema.buscarProductoPorCodigo("MED-001");
        var p2 = sistema.buscarProductoPorCodigo("MED-002");
        var detalles = Arrays.asList(
                new DetalleComprobante(1, p1, 50, 150.0, 21.0),
                new DetalleComprobante(2, p2, 20, 95.0, 21.0)
        );
        Factura factura = sistema.registrarFactura(
                null, TipoComprobante.FACTURA_A, new Date(), new Date(),
                detalles, "20-12345678-9",
                Collections.singletonList(oc.getNumero()), null, null
        );
        System.out.println("Items: " + factura.getDetalles().size()
                + " | Neto: $" + factura.getImporteNeto()
                + " | Total: $" + factura.getImporteTotal());

        // OP
        System.out.println("\n--- Orden de Pago ---");
        Map<Comprobante, Double> sel = new LinkedHashMap<>();
        sel.put(factura, factura.getSaldoPendiente());
        OrdenPago op = sistema.prepararOrdenPago("20-12345678-9", sel, new Date());
        sistema.confirmarOrdenPago(op, Collections.singletonList(
                new Efectivo(1, op.getImporteNeto(), new Date())));
        System.out.println("OP emitida: " + op.getNumero());

        // Compulsa: mismo producto, dos proveedores
        System.out.println("\n--- Compulsa MED-001 ---");
        sistema.consultarCompulsaPrecios("MED-001").forEach(pa ->
                System.out.println("  " + pa.getProveedor().getRazonSocial()
                        + " -> $" + pa.getPrecioUnitario()));
    }
}

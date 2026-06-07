package farmared;

import farmared.controllers.*;
import farmared.enums.*;
import farmared.modulos.m1_usuarios.*;
import farmared.modulos.m2_proveedores.*;
import farmared.modulos.m3_productos.*;
import farmared.modulos.m4_ordenes_compra.*;
import farmared.modulos.m5_comprobantes.*;
import farmared.modulos.m6_ordenes_pago.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        OrdenCompraController    ocCtrl   = OrdenCompraController.getInstance();
        FacturaController        factCtrl = FacturaController.getInstance();
        OrdenPagoController      opCtrl   = OrdenPagoController.getInstance();
        CuentaCorrienteController ccCtrl  = CuentaCorrienteController.getInstance();

        // =====================================================================
        // M1 - USUARIOS Y SEGURIDAD
        // =====================================================================
        Usuario operador   = new Usuario(1, "Ana",    "Lopez", "alopez", "pass", RolUsuario.OPERADOR);
        Usuario supervisor  = new Usuario(2, "Carlos", "Rios",  "crios",  "pass", RolUsuario.SUPERVISOR);

        // =====================================================================
        // M2 - PROVEEDORES
        // Cada controller mantiene su propia lista de proveedores.
        // Al registrar un proveedor se propaga a los 4 controllers.
        // =====================================================================
        Rubro rubMedicamentos = new Rubro(1, "Medicamentos", "Productos farmaceuticos");

        Proveedor prov = new Proveedor(
            "20-12345678-9", "Laboratorios SA", "LabSA",
            "Av. Corrientes 1234", "011-4444-5555", "ventas@labsa.com",
            CondicionIVA.RESPONSABLE_INSCRIPTO, "123-456789-0",
            new Date(90, 0, 1)
        );
        prov.setTopeMaximoDeuda(100000.0);
        prov.agregarRubro(rubMedicamentos);

        // Propagar proveedor a los 4 controllers
        ocCtrl.agregarProveedor(prov);
        factCtrl.agregarProveedor(prov);
        opCtrl.agregarProveedor(prov);
        ccCtrl.agregarProveedor(prov);

        // =====================================================================
        // M3 - PRODUCTOS Y SERVICIOS
        // OrdenCompraController gestiona el catalogo de productos.
        // =====================================================================
        Producto aspirina = new Producto("MED-001", "Aspirina 500mg x20",
                                         "caja", TipoIVA.IVA_21, rubMedicamentos);

        PrecioAcordado precio = new PrecioAcordado(150.0, new Date(125, 0, 1), null, prov);
        aspirina.agregarPrecioAcordado(precio);

        ocCtrl.agregarProducto(aspirina);

        // M3: Parametrizar impuestos retenibles (OrdenPagoController los administra)
        opCtrl.agregarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        opCtrl.agregarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
        opCtrl.agregarImpuesto(new ImpuestoGanancias(3, 3.5, 5000.0));

        // =====================================================================
        // M4 - ORDENES DE COMPRA (DS1)
        // =====================================================================
        System.out.println("\n--- M4: Generar Orden de Compra (DS1) ---");

        OrdenCompra oc = ocCtrl.crearOrdenCompra(prov.getCuit());
        ocCtrl.agregarItem(oc, "MED-001", 100);
        OrdenCompra ocEmitida = ocCtrl.emitirOrdenCompra(oc, null, null);
        // Propagar a FacturaController para que DS3 pueda asociar facturas a esta OC
        factCtrl.agregarOrdenCompra(ocEmitida);
        System.out.println("OC emitida: " + ocEmitida);

        // =====================================================================
        // M5 - COMPROBANTES RECIBIDOS (DS3)
        // =====================================================================
        System.out.println("\n--- M5: Registrar Factura (DS3) ---");

        DetalleComprobante det = new DetalleComprobante(1, aspirina, 100, 150.0, 21.0);
        Factura factura = factCtrl.registrarFactura(
            null, TipoComprobante.FACTURA_A,
            new Date(), new Date(),
            Collections.singletonList(det),
            prov.getCuit(),
            Collections.singletonList(ocEmitida.getNumero()),
            null, null
        );
        System.out.println("Factura registrada: " + factura);
        System.out.println("Deuda vigente: $" + prov.obtenerCuentaCorriente());

        // =====================================================================
        // M6 - ORDENES DE PAGO (DS2)
        // =====================================================================
        System.out.println("\n--- M6: Emitir Orden de Pago (DS2) ---");

        List<Comprobante> impagos = opCtrl.iniciarOrdenPago(prov.getCuit());
        System.out.println("Comprobantes impagos: " + impagos.size());

        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura, factura.getSaldoPendiente());
        OrdenPago op = opCtrl.seleccionarComprobantes(prov.getCuit(), seleccion, new Date());

        System.out.println("Bruto:             $" + op.getImporteBruto());
        System.out.println("Total retenciones: $" + op.getTotalRetenciones());
        System.out.println("Neto a pagar:      $" + op.getImporteNeto());

        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, op.getImporteNeto(), new Date()));
        OrdenPago opConfirmada = opCtrl.confirmarPago(op, medios);
        // Propagar a CuentaCorrienteController para que DS4 la incluya en historial
        ccCtrl.agregarOrdenPago(opConfirmada);
        System.out.println("OP emitida: " + opConfirmada);
        System.out.println("Retenciones: " + opConfirmada.getRetenciones());
        System.out.println("Deuda post-pago: $" + prov.obtenerCuentaCorriente());

        // =====================================================================
        // M7 - CONSULTAS GENERALES Y REPORTES
        // Las consultas se realizan directamente sobre los controllers especializados.
        // =====================================================================

        System.out.println("\n--- M7: Cuenta Corriente detallada (DS4) ---");
        Map<String, Object> cc = ccCtrl.consultarCuentaCorriente(prov.getCuit());
        System.out.println("Deuda vigente: $" + cc.get("totalDeudaVigente"));
        System.out.println("Pagos aplicados: " + cc.get("pagosAplicados"));

        System.out.println("\n--- M7: Documentos impagos ---");
        ccCtrl.listarDocumentosImpagos(prov.getCuit())
            .forEach(c -> System.out.println("  " + c + " | saldo: $" + c.getSaldoPendiente()));

        System.out.println("\n--- M7: Detalle de pagos realizados ---");
        ccCtrl.detallarPagosPorProveedor(prov.getCuit())
            .forEach(p2 -> System.out.println("  " + p2));

        System.out.println("\n--- M7: Compulsa de precios MED-001 ---");
        ocCtrl.consultarCompulsaPrecios("MED-001")
            .forEach(pa -> System.out.println("  " + pa));

        System.out.println("\n--- M7: Deuda vigente por proveedor ---");
        ccCtrl.consultarDeudaVigentePorProveedor()
            .forEach((k, v) -> System.out.println("  " + k + ": $" + v));

        System.out.println("\n--- M7: Retenciones por tipo de impuesto ---");
        opCtrl.reporteRetencionesPorTipo()
            .forEach((k, v) -> System.out.println("  " + k + ": $" + v));

        System.out.println("\n--- M7: Libro IVA Compras ---");
        factCtrl.generarLibroIVACompras()
            .forEach(linea -> System.out.println("  " + linea));

        System.out.println("\n--- M7: Facturas por dia y proveedor ---");
        factCtrl.totalFacturasPorDiaYProveedor()
            .forEach((k, v) -> System.out.println("  " + k + ": " + v));
    }
}

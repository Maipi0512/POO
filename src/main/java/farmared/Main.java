package farmared;

import farmared.enums.*;
import farmared.modulos.m1_proveedores.*;
import farmared.modulos.m2_productos.*;
import farmared.modulos.m3_impuestos.*;
import farmared.modulos.m4_ordenes_compra.*;
import farmared.modulos.m5_comprobantes.*;
import farmared.modulos.m6_ordenes_pago.*;
import farmared.modulos.m8_usuarios.*;
import farmared.sistema.SistemaGestionCompras;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        SistemaGestionCompras sistema = new SistemaGestionCompras("FarmaRed", "1.0");

        // === M1: CONFIGURAR RUBROS ===
        Rubro rubMedicamentos = new Rubro(1, "Medicamentos", "Productos farmaceuticos");
        sistema.registrarRubro(rubMedicamentos);

        // === M3: PARAMETRIZAR IMPUESTOS (RF-08) ===
        sistema.parametrizarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        sistema.parametrizarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
        sistema.parametrizarImpuesto(new ImpuestoGanancias(3, 3.5, 5000.0));

        // === M8: REGISTRAR USUARIOS (RF-28) ===
        Usuario operador  = new Usuario(1, "Ana",    "Lopez", "alopez", "pass", RolUsuario.OPERADOR);
        Usuario supervisor= new Usuario(2, "Carlos", "Rios",  "crios",  "pass", RolUsuario.SUPERVISOR);
        sistema.registrarUsuario(operador);
        sistema.registrarUsuario(supervisor);

        // === M1: REGISTRAR PROVEEDOR (RF-01, RF-02, RF-04) ===
        Proveedor prov = new Proveedor(
            "20-12345678-9", "Laboratorios SA", "LabSA",
            "Av. Corrientes 1234", "011-4444-5555", "ventas@labsa.com",
            CondicionIVA.RESPONSABLE_INSCRIPTO, "123-456789-0",
            new Date(90, 0, 1)
        );
        prov.setTopeMaximoDeuda(100000.0);
        prov.agregarRubro(rubMedicamentos);
        sistema.registrarProveedor(prov);

        // === M2: REGISTRAR PRODUCTO CON PRECIO ACORDADO (RF-05, RF-06) ===
        Producto aspirina = new Producto("MED-001", "Aspirina 500mg x20",
                                         "caja", TipoIVA.IVA_21, rubMedicamentos);
        aspirina.agregarPrecioAcordado(new PrecioAcordado(150.0, new Date(125, 0, 1), null, prov));
        sistema.registrarProducto(aspirina);

        // === M4: DS1 — GENERAR ORDEN DE COMPRA (RF-11, RF-12) ===
        System.out.println("\n--- M4: Generar Orden de Compra ---");
        OrdenCompra oc = sistema.crearOrdenCompra("20-12345678-9");
        sistema.agregarItemOC(oc, "MED-001", 100, 1);
        sistema.emitirOrdenCompra(oc, null, null);
        System.out.println("OC emitida: " + oc);

        // === M5: DS3 — REGISTRAR FACTURA (RF-14, RF-15, RF-16) ===
        System.out.println("\n--- M5: Registrar Factura ---");
        DetalleComprobante det = new DetalleComprobante(1, aspirina, 100, 150.0, 21.0);
        Factura factura = sistema.registrarFactura(
            null, TipoComprobante.FACTURA_A,
            new Date(), new Date(),
            Collections.singletonList(det),
            "20-12345678-9",
            Collections.singletonList(oc.getNumero()),
            null, null
        );
        System.out.println("Factura registrada: " + factura);
        System.out.println("Deuda vigente: $" + prov.obtenerCuentaCorriente());

        // === M6: DS2 — EMITIR ORDEN DE PAGO (RF-18, RF-19, RF-20) ===
        System.out.println("\n--- M6: Emitir Orden de Pago ---");

        // Paso 1: preparar (calcula retenciones, devuelve el neto exacto)
        Map<Comprobante, Double> seleccion = new LinkedHashMap<>();
        seleccion.put(factura, factura.getSaldoPendiente());
        OrdenPago op = sistema.prepararOrdenPago("20-12345678-9", seleccion, new Date());

        System.out.println("Bruto:             $" + op.getImporteBruto());
        System.out.println("Total retenciones: $" + op.getTotalRetenciones());
        System.out.println("Neto a pagar:      $" + op.getImporteNeto());

        // Paso 2: confirmar con el medio de pago por el neto exacto
        List<MedioPago> medios = new ArrayList<>();
        medios.add(new Efectivo(1, op.getImporteNeto(), new Date()));
        sistema.confirmarOrdenPago(op, medios);
        System.out.println("OP emitida: " + op);
        System.out.println("Retenciones: " + op.getRetenciones());
        System.out.println("Deuda post-pago: $" + prov.obtenerCuentaCorriente());

        // === M7: DS4 — CUENTA CORRIENTE (RF-21) ===
        System.out.println("\n--- M7: Cuenta Corriente ---");
        for (Comprobante c : sistema.consultarCuentaCorriente("20-12345678-9")) {
            System.out.println("  " + c + " | saldo: $" + c.getSaldoPendiente());
        }

        // === M7: RF-24 — DEUDA VIGENTE POR PROVEEDOR ===
        System.out.println("\n--- M7: Deuda vigente por proveedor ---");
        sistema.consultarDeudaVigentePorProveedor()
               .forEach((k, v) -> System.out.println("  " + k + ": $" + v));

        // === M7: RF-25 — RETENCIONES POR TIPO ===
        System.out.println("\n--- M7: Retenciones por tipo de impuesto ---");
        sistema.reporteRetencionesPorTipo()
               .forEach((k, v) -> System.out.println("  " + k + ": $" + v));

        // === M7: RF-26 — LIBRO IVA COMPRAS ===
        System.out.println("\n--- M7: Libro IVA Compras ---");
        for (Map<String, Object> linea : sistema.generarLibroIVACompras()) {
            System.out.println(linea);
        }
    }
}

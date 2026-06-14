package farmared.ui;

import farmared.enums.*;
import farmared.modulos.m2_proveedores.*;
import farmared.modulos.m3_productos.*;
import farmared.modulos.m6_ordenes_pago.*;
import farmared.modulos.m1_usuarios.*;
import farmared.sistema.SistemaGestionCompras;

import java.util.Date;

/**
 * Carga datos de ejemplo con varios proveedores, productos y precios distintos por proveedor.
 */
public final class DatosIniciales {

    private DatosIniciales() {}

    public static void cargar(SistemaGestionCompras sistema) {
        if (!sistema.getProveedores().isEmpty()) return;

        Rubro rubMedicamentos = new Rubro(1, "Medicamentos", "Productos farmaceuticos");
        Rubro rubLimpieza = new Rubro(2, "Limpieza e higiene", "Insumos de limpieza");
        Rubro rubEquipamiento = new Rubro(3, "Equipamiento medico", "Equipos e insumos clinicos");
        sistema.registrarRubro(rubMedicamentos);
        sistema.registrarRubro(rubLimpieza);
        sistema.registrarRubro(rubEquipamiento);

        sistema.parametrizarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        sistema.parametrizarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));

        ImpuestoGanancias ganancias = new ImpuestoGanancias(3, 3.5, 5000.0);
        ganancias.agregarEscala(new EscalaRetencion(0, 10000, 2.0));
        ganancias.agregarEscala(new EscalaRetencion(10000, 50000, 3.5));
        ganancias.agregarEscala(new EscalaRetencion(50000, 0, 5.0));
        sistema.parametrizarImpuesto(ganancias);

        sistema.registrarUsuario(new Usuario(1, "Ana", "Lopez", "alopez", "pass", RolUsuario.OPERADOR));
        sistema.registrarUsuario(new Usuario(2, "Carlos", "Rios", "crios", "pass", RolUsuario.SUPERVISOR));
        sistema.registrarUsuario(new Usuario(3, "Maria", "Gomez", "mgomez", "admin", RolUsuario.ADMINISTRADOR));

        // --- Proveedor 1: Laboratorios ---
        Proveedor labSA = crearProveedor(
                "20-12345678-9", "Laboratorios SA", "LabSA",
                "Av. Corrientes 1234", "011-4444-5555", "ventas@labsa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 150000.0, rubMedicamentos
        );
        sistema.registrarProveedor(labSA);

        // --- Proveedor 2: Distribuidora farmaceutica ---
        Proveedor farmaDist = crearProveedor(
                "30-98765432-1", "FarmaDistrib SRL", "FarmaDist",
                "Av. San Martin 500", "011-5555-6666", "compras@farmadist.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 200000.0, rubMedicamentos
        );
        sistema.registrarProveedor(farmaDist);

        // --- Proveedor 3: Limpieza ---
        Proveedor cleanCorp = crearProveedor(
                "27-11223344-5", "CleanCorp Argentina", "CleanCorp",
                "Calle Florida 200", "011-7777-8888", "info@cleancorp.com",
                CondicionIVA.MONOTRIBUTISTA, 50000.0, rubLimpieza
        );
        sistema.registrarProveedor(cleanCorp);

        // --- Proveedor 4: Equipamiento ---
        Proveedor medEquip = crearProveedor(
                "30-55667788-9", "MedEquip SA", "MedEquip",
                "Parque Industrial 15", "011-3333-2222", "ventas@medequip.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 300000.0, rubEquipamiento
        );
        sistema.registrarProveedor(medEquip);

        Date hoy = new Date();

        // Ibuprofeno: mismo producto, dos proveedores con precios distintos (RF-06)
        Producto ibuprofeno = new Producto("MED-001", "Ibuprofeno 600mg x30",
                "caja", TipoIVA.IVA_21, rubMedicamentos);
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(150.0, hoy, null, labSA));
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(142.0, hoy, null, farmaDist));
        sistema.registrarProducto(ibuprofeno);

        Producto paracetamol = new Producto("MED-002", "Paracetamol 500mg x20",
                "caja", TipoIVA.IVA_21, rubMedicamentos);
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(95.0, hoy, null, labSA));
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(88.0, hoy, null, farmaDist));
        sistema.registrarProducto(paracetamol);

        Producto alcohol = new Producto("LIM-001", "Alcohol en gel 500ml",
                "unidad", TipoIVA.IVA_21, rubLimpieza);
        alcohol.agregarPrecioAcordado(new PrecioAcordado(1200.0, hoy, null, cleanCorp));
        sistema.registrarProducto(alcohol);

        Producto detergente = new Producto("LIM-002", "Detergente hospitalario 5L",
                "bidon", TipoIVA.IVA_21, rubLimpieza);
        detergente.agregarPrecioAcordado(new PrecioAcordado(3500.0, hoy, null, cleanCorp));
        sistema.registrarProducto(detergente);

        Producto tensiometro = new Producto("EQP-001", "Tensiometro digital",
                "unidad", TipoIVA.IVA_10_5, rubEquipamiento);
        tensiometro.agregarPrecioAcordado(new PrecioAcordado(45000.0, hoy, null, medEquip));
        sistema.registrarProducto(tensiometro);

        Producto termometro = new Producto("EQP-002", "Termometro infrarrojo",
                "unidad", TipoIVA.IVA_10_5, rubEquipamiento);
        termometro.agregarPrecioAcordado(new PrecioAcordado(28000.0, hoy, null, medEquip));
        sistema.registrarProducto(termometro);
    }

    private static Proveedor crearProveedor(String cuit, String razon, String fantasia,
                                            String domicilio, String tel, String email,
                                            CondicionIVA condicion, double tope, Rubro... rubros) {
        Proveedor p = new Proveedor(cuit, razon, fantasia, domicilio, tel, email,
                condicion, "IB-" + cuit.substring(0, 5), new Date(90, 0, 1));
        p.setTopeMaximoDeuda(tope);
        for (Rubro r : rubros) p.agregarRubro(r);
        return p;
    }
}

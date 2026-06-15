package farmared.GUI;

import farmared.controladores.ProductoController;
import farmared.controladores.ProveedorController;
import farmared.controladores.UsuarioController;
import farmared.enums.CondicionIVA;
import farmared.enums.RolUsuario;
import farmared.enums.TipoIVA;
import farmared.modulos.m1_usuarios.Usuario;
import farmared.modulos.m2_proveedores.Proveedor;
import farmared.modulos.m2_proveedores.Rubro;
import farmared.modulos.m3_productos.PrecioAcordado;
import farmared.modulos.m3_productos.Producto;
import farmared.modulos.m6_ordenes_pago.EscalaRetencion;
import farmared.modulos.m6_ordenes_pago.ImpuestoGanancias;
import farmared.modulos.m6_ordenes_pago.ImpuestoIngresosBrutos;
import farmared.modulos.m6_ordenes_pago.ImpuestoIVA;

import java.util.Date;

/**
 * Carga datos de ejemplo: proveedores, productos, precios y transacciones demo
 * para que el sistema tenga datos listos al abrir (OCs, facturas, una OP pagada).
 */
public final class DatosIniciales {

    private DatosIniciales() {}

    public static void cargar() {
        ProveedorController pc = ProveedorController.getInstance();
        if (!pc.listarProveedores().isEmpty()) return;

        // =====================================================================
        // RUBROS
        // =====================================================================
        Rubro rubMedicamentos = new Rubro(1, "Medicamentos",       "Productos farmaceuticos");
        Rubro rubLimpieza     = new Rubro(2, "Limpieza e higiene", "Insumos de limpieza");
        Rubro rubEquipamiento = new Rubro(3, "Equipamiento medico","Equipos e insumos clinicos");
        pc.registrarRubro(rubMedicamentos);
        pc.registrarRubro(rubLimpieza);
        pc.registrarRubro(rubEquipamiento);

        // =====================================================================
        // IMPUESTOS
        // =====================================================================
        pc.parametrizarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        pc.parametrizarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
        ImpuestoGanancias ganancias = new ImpuestoGanancias(3, 3.5, 5000.0);
        ganancias.agregarEscala(new EscalaRetencion(0,     10000, 2.0));
        ganancias.agregarEscala(new EscalaRetencion(10000, 50000, 3.5));
        ganancias.agregarEscala(new EscalaRetencion(50000, 0,     5.0));
        pc.parametrizarImpuesto(ganancias);

        // =====================================================================
        // USUARIOS
        // =====================================================================
        UsuarioController uc = UsuarioController.getInstance();
        uc.registrarUsuario(new Usuario(1, "Ana",    "Lopez", "alopez", "pass",  RolUsuario.OPERADOR));
        uc.registrarUsuario(new Usuario(2, "Carlos", "Rios",  "crios",  "pass",  RolUsuario.SUPERVISOR));
        uc.registrarUsuario(new Usuario(3, "Maria",  "Gomez", "mgomez", "admin", RolUsuario.ADMINISTRADOR));

        Date hoy = new Date();

        // =====================================================================
        // PROVEEDORES
        // =====================================================================
        Proveedor labSA    = crearProv("20-12345678-9", "Laboratorios SA",      "LabSA",
                "Av. Corrientes 1234", "011-4444-5555", "ventas@labsa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 150000.0, rubMedicamentos);
        Proveedor farmaDist = crearProv("30-98765432-1", "FarmaDistrib SRL",    "FarmaDist",
                "Av. San Martin 500",  "011-5555-6666", "compras@farmadist.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 200000.0, rubMedicamentos);
        Proveedor cleanCorp = crearProv("27-11223344-5", "CleanCorp Argentina", "CleanCorp",
                "Calle Florida 200",   "011-7777-8888", "info@cleancorp.com",
                CondicionIVA.MONOTRIBUTISTA, 50000.0, rubLimpieza);
        Proveedor medEquip  = crearProv("30-55667788-9", "MedEquip SA",         "MedEquip",
                "Parque Industrial 15","011-3333-2222", "ventas@medequip.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 300000.0, rubEquipamiento);

        cargarProv(labSA);
        cargarProv(farmaDist);
        cargarProv(cleanCorp);
        cargarProv(medEquip);

        // =====================================================================
        // PRODUCTOS con precios acordados por proveedor
        // =====================================================================
        ProductoController prodCtrl = ProductoController.getInstance();

        Producto ibuprofeno = new Producto("MED-001", "Ibuprofeno 600mg x30",
                "caja", TipoIVA.IVA_21, rubMedicamentos);
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(150.0, hoy, null, labSA));
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(142.0, hoy, null, farmaDist));
        prodCtrl.registrarProducto(ibuprofeno);

        Producto paracetamol = new Producto("MED-002", "Paracetamol 500mg x20",
                "caja", TipoIVA.IVA_21, rubMedicamentos);
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(95.0, hoy, null, labSA));
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(88.0, hoy, null, farmaDist));
        prodCtrl.registrarProducto(paracetamol);

        Producto alcohol = new Producto("LIM-001", "Alcohol en gel 500ml",
                "unidad", TipoIVA.IVA_21, rubLimpieza);
        alcohol.agregarPrecioAcordado(new PrecioAcordado(1200.0, hoy, null, cleanCorp));
        prodCtrl.registrarProducto(alcohol);

        Producto detergente = new Producto("LIM-002", "Detergente hospitalario 5L",
                "bidon", TipoIVA.IVA_21, rubLimpieza);
        detergente.agregarPrecioAcordado(new PrecioAcordado(3500.0, hoy, null, cleanCorp));
        prodCtrl.registrarProducto(detergente);

        Producto tensiometro = new Producto("EQP-001", "Tensiometro digital",
                "unidad", TipoIVA.IVA_10_5, rubEquipamiento);
        tensiometro.agregarPrecioAcordado(new PrecioAcordado(45000.0, hoy, null, medEquip));
        prodCtrl.registrarProducto(tensiometro);

        Producto termometro = new Producto("EQP-002", "Termometro infrarrojo",
                "unidad", TipoIVA.IVA_10_5, rubEquipamiento);
        termometro.agregarPrecioAcordado(new PrecioAcordado(28000.0, hoy, null, medEquip));
        prodCtrl.registrarProducto(termometro);

    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    private static void cargarProv(Proveedor prov) {
        try {
            ProveedorController.getInstance().registrarProveedor(
                    prov.getCuit(), prov.getRazonSocial(), prov.getNombreFantasia(),
                    prov.getDomicilioComercial(), prov.getTelefono(), prov.getEmail(),
                    prov.getCondicionIVA(), prov.getNumeroIngresosBrutos(),
                    prov.getTopeMaximoDeuda(), prov.getRubros()
            );
        } catch (IllegalArgumentException ignored) {}
    }

    private static Proveedor crearProv(String cuit, String razon, String fantasia,
                                       String domicilio, String tel, String email,
                                       CondicionIVA condicion, double tope, Rubro... rubros) {
        Proveedor p = new Proveedor(cuit, razon, fantasia, domicilio, tel, email,
                condicion, "IB-" + cuit.substring(0, 5), new Date(631152000000L)); // 1990-01-01
        p.setTopeMaximoDeuda(tope);
        for (Rubro r : rubros) p.agregarRubro(r);
        return p;
    }
}

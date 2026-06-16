package farmared.vistas;

<<<<<<< HEAD
import farmared.modelo.enums.*;
import farmared.modelo.modulos.m1_proveedores.*;
import farmared.modelo.modulos.m2_productos.*;
import farmared.modelo.modulos.m3_impuestos.*;
import farmared.modelo.modulos.m8_usuarios.*;
=======
import farmared.controladores.ProductoController;
import farmared.controladores.ProveedorController;
import farmared.controladores.UsuarioController;
import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.RolUsuario;
import farmared.modelo.enums.TipoIVA;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.PrecioAcordado;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.modulos.m3_impuestos.EscalaRetencion;
import farmared.modelo.modulos.m3_impuestos.ImpuestoGanancias;
import farmared.modelo.modulos.m3_impuestos.ImpuestoIngresosBrutos;
import farmared.modelo.modulos.m3_impuestos.ImpuestoIVA;
import farmared.modelo.modulos.m8_usuarios.Usuario;
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

import java.util.Date;
import java.util.List;

/**
 * Carga datos de ejemplo usando los controladores directamente.
 */
public final class DatosIniciales {

    private DatosIniciales() {}

<<<<<<< HEAD
    public static void cargar(List<Proveedor> proveedores, List<Producto> productos, List<Rubro> rubros, List<Impuesto> impuestos, List<Usuario> usuarios) {
        if (!proveedores.isEmpty()) return;
=======
    public static void cargar() {
        ProveedorController pc = ProveedorController.getInstance();
        if (!pc.listarProveedores().isEmpty()) return;
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Rubro rubMedicamentos = new Rubro(1, "Medicamentos",        "Productos farmaceuticos");
        Rubro rubLimpieza     = new Rubro(2, "Limpieza e higiene",  "Insumos de limpieza");
        Rubro rubEquipamiento = new Rubro(3, "Equipamiento medico", "Equipos e insumos clinicos");
<<<<<<< HEAD
        rubros.add(rubMedicamentos);
        rubros.add(rubLimpieza);
        rubros.add(rubEquipamiento);

        impuestos.add(new ImpuestoIVA(1, 10.5, 0.0));
        impuestos.add(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
=======
        pc.registrarRubro(rubMedicamentos);
        pc.registrarRubro(rubLimpieza);
        pc.registrarRubro(rubEquipamiento);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        pc.parametrizarImpuesto(new ImpuestoIVA(1, 10.5, 0.0));
        pc.parametrizarImpuesto(new ImpuestoIngresosBrutos(2, 2.0, 1000.0));
        ImpuestoGanancias ganancias = new ImpuestoGanancias(3, 3.5, 5000.0);
        ganancias.agregarEscala(new EscalaRetencion(0,     10000, 2.0));
        ganancias.agregarEscala(new EscalaRetencion(10000, 50000, 3.5));
<<<<<<< HEAD
        ganancias.agregarEscala(new EscalaRetencion(50000, 0, 5.0));
        impuestos.add(ganancias);

        usuarios.add(new Usuario(1, "Ana", "Lopez", "alopez", "pass", RolUsuario.OPERADOR));
        usuarios.add(new Usuario(2, "Carlos", "Rios", "crios", "pass", RolUsuario.SUPERVISOR));
        usuarios.add(new Usuario(3, "Maria", "Gomez", "mgomez", "admin", RolUsuario.ADMINISTRADOR));

        // --- Proveedor 1: Laboratorios ---
        Proveedor labSA = crearProveedor(
                "20-12345678-9", "Laboratorios SA", "LabSA",
                "Av. Corrientes 1234", "011-4444-5555", "ventas@labsa.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 150000.0, rubMedicamentos
        );
        proveedores.add(labSA);

        // --- Proveedor 2: Distribuidora farmaceutica ---
        Proveedor farmaDist = crearProveedor(
                "30-98765432-1", "FarmaDistrib SRL", "FarmaDist",
                "Av. San Martin 500", "011-5555-6666", "compras@farmadist.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 200000.0, rubMedicamentos
        );
        proveedores.add(farmaDist);

        // --- Proveedor 3: Limpieza ---
        Proveedor cleanCorp = crearProveedor(
                "27-11223344-5", "CleanCorp Argentina", "CleanCorp",
                "Calle Florida 200", "011-7777-8888", "info@cleancorp.com",
                CondicionIVA.MONOTRIBUTISTA, 50000.0, rubLimpieza
        );
        proveedores.add(cleanCorp);

        // --- Proveedor 4: Equipamiento ---
        Proveedor medEquip = crearProveedor(
                "30-55667788-9", "MedEquip SA", "MedEquip",
                "Parque Industrial 15", "011-3333-2222", "ventas@medequip.com",
                CondicionIVA.RESPONSABLE_INSCRIPTO, 300000.0, rubEquipamiento
        );
        proveedores.add(medEquip);
=======
        ganancias.agregarEscala(new EscalaRetencion(50000, 0,     5.0));
        pc.parametrizarImpuesto(ganancias);

        UsuarioController uc = UsuarioController.getInstance();
        uc.registrarUsuario(new Usuario(1, "Ana",    "Lopez", "alopez", "pass",  RolUsuario.OPERADOR));
        uc.registrarUsuario(new Usuario(2, "Carlos", "Rios",  "crios",  "pass",  RolUsuario.SUPERVISOR));
        uc.registrarUsuario(new Usuario(3, "Maria",  "Gomez", "mgomez", "admin", RolUsuario.ADMINISTRADOR));
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Date hoy = new Date();

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

        ProductoController prodCtrl = ProductoController.getInstance();

        Producto ibuprofeno = new Producto("MED-001", "Ibuprofeno 600mg x30",
                "caja", TipoIVA.IVA_21, rubMedicamentos);
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(150.0, hoy, null, labSA));
        ibuprofeno.agregarPrecioAcordado(new PrecioAcordado(142.0, hoy, null, farmaDist));
<<<<<<< HEAD
        productos.add(ibuprofeno);
=======
        prodCtrl.registrarProducto(ibuprofeno);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Producto paracetamol = new Producto("MED-002", "Paracetamol 500mg x20",
                "caja", TipoIVA.IVA_21, rubMedicamentos);
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(95.0, hoy, null, labSA));
        paracetamol.agregarPrecioAcordado(new PrecioAcordado(88.0, hoy, null, farmaDist));
<<<<<<< HEAD
        productos.add(paracetamol);
=======
        prodCtrl.registrarProducto(paracetamol);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Producto alcohol = new Producto("LIM-001", "Alcohol en gel 500ml",
                "unidad", TipoIVA.IVA_21, rubLimpieza);
        alcohol.agregarPrecioAcordado(new PrecioAcordado(1200.0, hoy, null, cleanCorp));
<<<<<<< HEAD
        productos.add(alcohol);
=======
        prodCtrl.registrarProducto(alcohol);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Producto detergente = new Producto("LIM-002", "Detergente hospitalario 5L",
                "bidon", TipoIVA.IVA_21, rubLimpieza);
        detergente.agregarPrecioAcordado(new PrecioAcordado(3500.0, hoy, null, cleanCorp));
<<<<<<< HEAD
        productos.add(detergente);
=======
        prodCtrl.registrarProducto(detergente);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Producto tensiometro = new Producto("EQP-001", "Tensiometro digital",
                "unidad", TipoIVA.IVA_10_5, rubEquipamiento);
        tensiometro.agregarPrecioAcordado(new PrecioAcordado(45000.0, hoy, null, medEquip));
<<<<<<< HEAD
        productos.add(tensiometro);
=======
        prodCtrl.registrarProducto(tensiometro);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42

        Producto termometro = new Producto("EQP-002", "Termometro infrarrojo",
                "unidad", TipoIVA.IVA_10_5, rubEquipamiento);
        termometro.agregarPrecioAcordado(new PrecioAcordado(28000.0, hoy, null, medEquip));
<<<<<<< HEAD
        productos.add(termometro);
=======
        prodCtrl.registrarProducto(termometro);
>>>>>>> 448923d989d2835e785bae01461f31fbc44ebc42
    }

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
                condicion, "IB-" + cuit.substring(0, 5), new Date(631152000000L));
        p.setTopeMaximoDeuda(tope);
        for (Rubro r : rubros) p.agregarRubro(r);
        return p;
    }
}

package farmared.controladores;

import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.TipoImpuesto;
import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.modelo.modulos.m1_proveedores.CertificadoNoRetencion;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m3_impuestos.Impuesto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Controlador singleton — Modulo 1 y 3: Proveedores, Rubros, Impuestos (RF-01 a RF-10).
 */
public class ProveedorController {

    private static final Pattern CUIT_FORMATO = Pattern.compile("^\\d{2}-\\d{8}-\\d$");

    private static ProveedorController instancia;

    private final List<Proveedor> proveedores;
    private final List<Rubro>     rubros;
    private final List<Impuesto>  impuestos;
    private final List<Usuario>   usuarios;
    private int contadorRubro = 1;

    private ProveedorController(List<Proveedor> proveedores, List<Rubro> rubros,
                                 List<Impuesto> impuestos, List<Usuario> usuarios) {
        this.proveedores = proveedores;
        this.rubros      = rubros;
        this.impuestos   = impuestos;
        this.usuarios    = usuarios;
    }

    public static void inicializar(List<Proveedor> proveedores, List<Rubro> rubros,
                                    List<Impuesto> impuestos, List<Usuario> usuarios) {
        instancia = new ProveedorController(proveedores, rubros, impuestos, usuarios);
    }

    public static ProveedorController getInstance() {
        if (instancia == null) throw new IllegalStateException("ProveedorController no inicializado.");
        return instancia;
    }

    // =========================================================================
    // RF-01: Registrar proveedor
    // =========================================================================

    public void registrarProveedor(String cuit, String razonSocial, String nombreFantasia,
                                    String domicilio, String telefono, String email,
                                    CondicionIVA condicionIVA, String ingresosBrutos,
                                    double topeDeuda, List<Rubro> rubrosSeleccionados) {
        validarCuit(cuit);
        for (Proveedor p : proveedores) {
            if (p.getCuit().equals(cuit))
                throw new IllegalArgumentException("CUIT duplicado: " + cuit);
        }
        Proveedor prov = new Proveedor(cuit, razonSocial, nombreFantasia, domicilio,
                telefono, email, condicionIVA, ingresosBrutos, new Date());
        prov.setTopeMaximoDeuda(topeDeuda);
        if (rubrosSeleccionados != null)
            for (Rubro r : rubrosSeleccionados) prov.agregarRubro(r);
        proveedores.add(prov);
    }

    // RF-02: Modificar proveedor
    public void modificarProveedor(String cuit, String razonSocial, String nombreFantasia,
                                    String domicilio, String telefono, String email,
                                    CondicionIVA condicionIVA, double topeDeuda,
                                    List<Rubro> nuevosRubros) {
        Proveedor p = buscarProveedorPorId(cuit);
        if (p == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuit);
        if (!p.isActivo()) throw new IllegalStateException("No se puede modificar un proveedor dado de baja.");
        p.setRazonSocial(razonSocial);
        p.setNombreFantasia(nombreFantasia);
        p.setDomicilioComercial(domicilio);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setCondicionIVA(condicionIVA);
        p.setTopeMaximoDeuda(topeDeuda);
        if (nuevosRubros != null && !nuevosRubros.isEmpty()) p.reemplazarRubros(nuevosRubros);
    }

    // RF-03: Dar de baja
    public void darBajaProveedor(String cuit) {
        Proveedor p = buscarProveedorPorId(cuit);
        if (p == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuit);
        p.setActivo(false);
    }

    // RF-09: Certificado de no retención
    public void registrarCertificadoNoRetencion(String cuit, CertificadoNoRetencion cert) {
        Proveedor p = buscarProveedorPorId(cuit);
        if (p == null) throw new IllegalArgumentException("Proveedor no encontrado: " + cuit);
        p.agregarCertificado(cert);
    }

    // RF-02: Rubros
    public Rubro registrarRubro(String nombre, String descripcion) {
        for (Rubro r : rubros) {
            if (r.getNombre().equalsIgnoreCase(nombre.trim()))
                throw new IllegalArgumentException("Ya existe un rubro con ese nombre.");
        }
        Rubro r = new Rubro(contadorRubro++, nombre.trim(), descripcion.trim());
        rubros.add(r);
        return r;
    }

    public void registrarRubro(Rubro rubro) {
        rubros.add(rubro);
        if (rubro.getIdRubro() >= contadorRubro) contadorRubro = rubro.getIdRubro() + 1;
    }

    // RF-08: Impuestos
    public void parametrizarImpuesto(Impuesto impuesto) {
        impuestos.add(impuesto);
    }

    // =========================================================================
    // Búsquedas (loop DS1-DS4)
    // =========================================================================

    public Proveedor buscarProveedorPorId(String cuit) {
        for (Proveedor p : proveedores)
            if (p.getCuit().equals(cuit)) return p;
        return null;
    }

    public List<Proveedor> listarProveedores() { return new ArrayList<>(proveedores); }
    public List<Rubro>     listarRubros()       { return new ArrayList<>(rubros); }
    public List<Impuesto>  listarImpuestos()    { return new ArrayList<>(impuestos); }

    public List<Usuario> listarSupervisores() {
        List<Usuario> sup = new ArrayList<>();
        for (Usuario u : usuarios) if (u.esAutorizador()) sup.add(u);
        return sup;
    }

    public boolean tieneCertificadoVigente(String cuit, TipoImpuesto tipo) {
        Proveedor p = buscarProveedorPorId(cuit);
        return p != null && p.tieneCertificadoVigente(tipo);
    }

    private void validarCuit(String cuit) {
        if (cuit == null || !CUIT_FORMATO.matcher(cuit.trim()).matches())
            throw new IllegalArgumentException("CUIT invalido. Formato esperado: xx-xxxxxxxx-x");
    }
}

package farmared.controladores;

import farmared.modelo.modulos.m1_proveedores.CUIT;
import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.SistemaGestionCompras;

import java.util.Date;
import java.util.List;

/** Controlador MVC del modulo de proveedores y rubros. */
public class ProveedorController {

    private final SistemaGestionCompras sistema;

    public ProveedorController(SistemaGestionCompras sistema) {
        this.sistema = sistema;
    }

    public List<Proveedor> listar() {
        return sistema.getProveedores();
    }

    public List<Rubro> listarRubros() {
        return sistema.getRubros();
    }

    public void registrarProveedor(String cuit, String razonSocial, String nombreFantasia,
                                   String domicilio, String telefono, String email,
                                   CondicionIVA condicionIVA, String ingresosBrutos,
                                   double topeDeuda, List<Rubro> rubrosSeleccionados) {
        new CUIT(cuit);

        Proveedor prov = new Proveedor(
                cuit, razonSocial, nombreFantasia, domicilio, telefono, email,
                condicionIVA, ingresosBrutos, new Date()
        );
        prov.setTopeMaximoDeuda(topeDeuda);
        for (Rubro r : rubrosSeleccionados) prov.agregarRubro(r);

        sistema.registrarProveedor(prov);
    }

    public void modificarProveedor(String cuit, String razonSocial, String nombreFantasia,
                                    String domicilio, String telefono, String email,
                                    CondicionIVA condicionIVA, double topeDeuda) {
        sistema.modificarProveedor(cuit, razonSocial, nombreFantasia, domicilio,
                telefono, email, condicionIVA, topeDeuda);
    }

    public void darBajaProveedor(String cuit) {
        sistema.darBajaProveedor(cuit);
    }

    public Rubro registrarRubro(String nombre, String descripcion) {
        return sistema.registrarRubro(nombre, descripcion);
    }
}

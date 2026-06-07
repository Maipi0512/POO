package farmared.modulos.m3_productos;

import farmared.modulos.m2_proveedores.Proveedor;
import java.util.Date;

/**
 * Precio pactado entre un Producto y un Proveedor especifico (RF-06).
 * Un mismo Producto puede tener muchos PrecioAcordado (uno por proveedor).
 * Al emitir una OC, el sistema busca el precio vigente (DS1, paso estaVigente).
 */
public class PrecioAcordado {

    private double precioUnitario;
    private Date fechaAcuerdo;
    private Date fechaFinVigencia;   // null = sin vencimiento
    private Proveedor proveedor;

    public PrecioAcordado(double precioUnitario, Date fechaAcuerdo,
                           Date fechaFinVigencia, Proveedor proveedor) {
        this.precioUnitario = precioUnitario;
        this.fechaAcuerdo = fechaAcuerdo;
        this.fechaFinVigencia = fechaFinVigencia;
        this.proveedor = proveedor;
    }

    /**
     * Retorna true si este precio esta activo a la fecha de hoy.
     * Llamado en DS1, paso estaVigente().
     */
    public boolean estaVigente() {
        Date hoy = new Date();
        boolean inicioOk = !hoy.before(fechaAcuerdo);
        boolean finOk    = (fechaFinVigencia == null) || !hoy.after(fechaFinVigencia);
        return inicioOk && finOk;
    }

    public double getPrecioUnitario()  { return precioUnitario; }
    public Date getFechaAcuerdo()      { return fechaAcuerdo; }
    public Date getFechaFinVigencia()  { return fechaFinVigencia; }
    public Proveedor getProveedor()    { return proveedor; }

    @Override
    public String toString() {
        return "Precio{$" + precioUnitario + ", prov=" + proveedor.getRazonSocial()
                + ", vigente=" + estaVigente() + "}";
    }
}

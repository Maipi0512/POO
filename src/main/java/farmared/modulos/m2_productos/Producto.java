package farmared.modulos.m2_productos;

import farmared.enums.TipoIVA;
import farmared.modulos.m1_proveedores.Proveedor;
import farmared.modulos.m1_proveedores.Rubro;
import java.util.ArrayList;
import java.util.List;

/**
 * Item del catalogo centralizado de productos y servicios (RF-05, RF-06, RF-07).
 * Pertenece a exactamente un Rubro.
 * Puede tener multiples PrecioAcordado (uno por proveedor, con historico).
 */
public class Producto {

    private String codigoInterno;
    private String descripcion;
    private String unidadMedida;
    private TipoIVA tipoIVA;
    private Rubro rubro;
    private boolean activo;

    private List<PrecioAcordado> preciosAcordados;

    public Producto(String codigoInterno, String descripcion,
                    String unidadMedida, TipoIVA tipoIVA, Rubro rubro) {
        this.codigoInterno = codigoInterno;
        this.descripcion = descripcion;
        this.unidadMedida = unidadMedida;
        this.tipoIVA = tipoIVA;
        this.rubro = rubro;
        this.activo = true;
        this.preciosAcordados = new ArrayList<>();
    }

    public void agregarPrecioAcordado(PrecioAcordado precio) {
        preciosAcordados.add(precio);
    }

    /**
     * Retorna el precio vigente para el proveedor dado (RF-06).
     * Llamado en DS1, paso obtenerUltimoPrecio(Proveedor).
     * Si hay varios vigentes del mismo proveedor, devuelve el mas reciente.
     */
    public PrecioAcordado obtenerUltimoPrecio(Proveedor proveedor) {
        PrecioAcordado resultado = null;
        for (PrecioAcordado pa : preciosAcordados) {
            if (pa.getProveedor().equals(proveedor) && pa.estaVigente()) {
                if (resultado == null || pa.getFechaAcuerdo().after(resultado.getFechaAcuerdo())) {
                    resultado = pa;
                }
            }
        }
        return resultado;
    }

    /** Retorna todos los precios (historicos y vigentes) para la compulsa (RF-07). */
    public List<PrecioAcordado> obtenerPreciosHistoricos() {
        return new ArrayList<>(preciosAcordados);
    }

    public String getCodigoInterno()           { return codigoInterno; }
    public String getDescripcion()             { return descripcion; }
    public String getUnidadMedida()            { return unidadMedida; }
    public TipoIVA getTipoIVA()                { return tipoIVA; }
    public Rubro getRubro()                    { return rubro; }
    public boolean isActivo()                  { return activo; }
    public List<PrecioAcordado> getPreciosAcordados() { return preciosAcordados; }

    public void setActivo(boolean activo)          { this.activo = activo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    @Override
    public String toString() {
        return "Producto{" + codigoInterno + " - " + descripcion + "}";
    }
}

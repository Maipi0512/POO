package farmared.modulos.m3_impuestos;

import farmared.enums.TipoImpuesto;
import farmared.modulos.m1_proveedores.Proveedor;

/** Retencion de Ingresos Brutos. Aplica a todos los proveedores (RF-08, RF-10). */
public class ImpuestoIngresosBrutos extends Impuesto {

    public ImpuestoIngresosBrutos(int idImpuesto, double porcentajeBase, double minimoNoImponible) {
        super(idImpuesto, "Ingresos Brutos", TipoImpuesto.INGRESOS_BRUTOS, porcentajeBase, minimoNoImponible);
    }

    @Override
    public double calcularRetencion(double base, Proveedor prov) {
        if (base <= minimoNoImponible) return 0.0;
        return base * (determinarPorcentaje(base) / 100.0);
    }
}

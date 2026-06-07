package farmared.modulos.m6_ordenes_pago;

import farmared.enums.CondicionIVA;
import farmared.enums.TipoImpuesto;
import farmared.modulos.m2_proveedores.Proveedor;

/** Retencion de Ganancias. No aplica a Monotributistas (RF-08, RF-10). */
public class ImpuestoGanancias extends Impuesto {

    public ImpuestoGanancias(int idImpuesto, double porcentajeBase, double minimoNoImponible) {
        super(idImpuesto, "Ganancias", TipoImpuesto.GANANCIAS, porcentajeBase, minimoNoImponible);
    }

    @Override
    public double calcularRetencion(double base, Proveedor prov) {
        if (prov.getCondicionIVA() == CondicionIVA.MONOTRIBUTISTA) return 0.0;
        if (base <= minimoNoImponible) return 0.0;
        return base * (determinarPorcentaje(base) / 100.0);
    }
}

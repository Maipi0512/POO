package farmared.modelo.modulos.m3_impuestos;

import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.TipoImpuesto;
import farmared.modelo.modulos.m1_proveedores.Proveedor;

/** Retencion de IVA. Solo aplica a Responsables Inscriptos (RF-08, RF-10). */
public class ImpuestoIVA extends Impuesto {

    public ImpuestoIVA(int idImpuesto, double porcentajeBase, double minimoNoImponible) {
        super(idImpuesto, "IVA Retencion", TipoImpuesto.IVA, porcentajeBase, minimoNoImponible);
    }

    @Override
    public double calcularRetencion(double base, Proveedor prov) {
        if (prov.getCondicionIVA() != CondicionIVA.RESPONSABLE_INSCRIPTO) return 0.0;
        if (base <= minimoNoImponible) return 0.0;
        return base * (determinarPorcentaje(base) / 100.0);
    }
}

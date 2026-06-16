package farmared.modelo.modulos.m2_productos;

import java.util.Objects;

/** Value Object para montos monetarios (>= 0). */
public final class Precio {

    private final double monto;

    public Precio(double monto) {
        if (monto < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
        this.monto = Math.round(monto * 100.0) / 100.0;
    }

    public double getMonto() {
        return monto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Precio)) return false;
        return Double.compare(monto, ((Precio) o).monto) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monto);
    }
}

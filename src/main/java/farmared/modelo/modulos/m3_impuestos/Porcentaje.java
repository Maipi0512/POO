package farmared.modelo.modulos.m3_impuestos;

import java.util.Objects;

/** Value Object para porcentajes entre 0 y 100. */
public final class Porcentaje {

    private final double valor;

    public Porcentaje(double valor) {
        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        }
        this.valor = valor;
    }

    public double getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Porcentaje)) return false;
        return Double.compare(valor, ((Porcentaje) o).valor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
}

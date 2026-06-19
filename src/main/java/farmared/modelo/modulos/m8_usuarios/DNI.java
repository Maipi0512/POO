package farmared.modelo.modulos.m8_usuarios;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object para DNI.
 * Inmutable, valida tamaño especifico (7 u 8 digitos).
 */
public final class DNI {

    private static final Pattern FORMATO = Pattern.compile("^\\d{7,8}$");

    private final String valor;

    public DNI(String valor) {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("DNI invalido. Se esperan entre 7 y 8 digitos.");
        }
        this.valor = valor.trim();
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DNI)) return false;
        return valor.equals(((DNI) o).valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}

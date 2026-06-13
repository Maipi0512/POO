package farmared.dominio.vo;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object para CUIT (patron del diagrama de clases).
 * Inmutable, valida formato xx-xxxxxxxx-x.
 */
public final class CUIT {

    private static final Pattern FORMATO = Pattern.compile("^\\d{2}-\\d{8}-\\d$");

    private final String valor;

    public CUIT(String valor) {
        if (valor == null || !FORMATO.matcher(valor.trim()).matches()) {
            throw new IllegalArgumentException("CUIT invalido. Formato esperado: xx-xxxxxxxx-x");
        }
        this.valor = valor.trim();
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CUIT)) return false;
        return valor.equals(((CUIT) o).valor);
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

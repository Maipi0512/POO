package farmared.dominio.vo;

import farmared.modelo.modulos.m1_proveedores.CUIT;
import farmared.modelo.modulos.m2_productos.Precio;
import farmared.modelo.modulos.m3_impuestos.Porcentaje;
import org.junit.Test;

import static org.junit.Assert.*;

public class ValueObjectsTest {

    @Test
    public void cuitValidoSeCreaCorrectamente() {
        CUIT cuit = new CUIT("20-12345678-9");
        assertEquals("20-12345678-9", cuit.getValor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cuitInvalidoLanzaExcepcion() {
        new CUIT("20123456789");
    }

    @Test
    public void precioNoPuedeSerNegativo() {
        assertEquals(10.0, new Precio(10.0).getMonto(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void precioNegativoLanzaExcepcion() {
        new Precio(-1);
    }

    @Test
    public void porcentajeValidoEntre0y100() {
        assertEquals(21.0, new Porcentaje(21).getValor(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void porcentajeFueraDeRangoLanzaExcepcion() {
        new Porcentaje(150);
    }
}

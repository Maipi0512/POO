package farmared.dominio.vo;

import farmared.modelo.modulos.m1_proveedores.CUIT;
import farmared.modelo.modulos.m2_productos.Precio;
import farmared.modelo.modulos.m3_impuestos.Porcentaje;
import farmared.modelo.modulos.m8_usuarios.DNI;
import farmared.modelo.modulos.m1_proveedores.Proveedor;
import farmared.modelo.modulos.m1_proveedores.Rubro;
import farmared.modelo.modulos.m2_productos.Producto;
import farmared.modelo.enums.CondicionIVA;
import farmared.modelo.enums.TipoIVA;
import java.util.Date;
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
    public void precioValidoSeCreaCorrectamente() {
        assertEquals(10.0, new Precio(10.0).getMonto(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void precioNegativoLanzaExcepcion() {
        new Precio(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void precioCeroLanzaExcepcion() {
        new Precio(0);
    }

    @Test
    public void porcentajeValidoEntre0y100() {
        assertEquals(21.0, new Porcentaje(21).getValor(), 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void porcentajeFueraDeRangoLanzaExcepcion() {
        new Porcentaje(150);
    }

    @Test
    public void dniValidoSeCreaCorrectamente() {
        DNI dni8 = new DNI("12345678");
        DNI dni7 = new DNI("1234567");
        assertEquals("12345678", dni8.getValor());
        assertEquals("1234567", dni7.getValor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void dniInvalidoCortoLanzaExcepcion() {
        new DNI("123456");
    }

    @Test(expected = IllegalArgumentException.class)
    public void dniInvalidoLargoLanzaExcepcion() {
        new DNI("123456789");
    }

    @Test(expected = IllegalArgumentException.class)
    public void dniInvalidoCaracteresLanzaExcepcion() {
        new DNI("1234567A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void proveedorCuitInvalidoLanzaExcepcion() {
        new Proveedor("1234", "Razon Social", "Fantasia", "Calle 123", "123", "a@a.com", CondicionIVA.RESPONSABLE_INSCRIPTO, "123", new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void proveedorRazonSocialVaciaLanzaExcepcion() {
        new Proveedor("20-12345678-9", "  ", "Fantasia", "Calle 123", "123", "a@a.com", CondicionIVA.RESPONSABLE_INSCRIPTO, "123", new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void proveedorDomicilioComercialVacioLanzaExcepcion() {
        new Proveedor("20-12345678-9", "Razon", "Fantasia", "", "123", "a@a.com", CondicionIVA.RESPONSABLE_INSCRIPTO, "123", new Date());
    }

    @Test(expected = IllegalArgumentException.class)
    public void productoCodigoVacioLanzaExcepcion() {
        new Producto("", "Descripcion", "Unidad", TipoIVA.IVA_21, new Rubro(1, "Rubro", "Desc"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void productoDescripcionVaciaLanzaExcepcion() {
        new Producto("COD-1", " ", "Unidad", TipoIVA.IVA_21, new Rubro(1, "Rubro", "Desc"));
    }
}


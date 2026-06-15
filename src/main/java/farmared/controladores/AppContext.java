package farmared.controladores;

import farmared.modelo.modulos.m8_usuarios.Usuario;
import farmared.controladores.OrdenCompraController;
import farmared.controladores.FacturaController;
import farmared.controladores.OrdenPagoController;
import farmared.controladores.ReportesController;
import farmared.modelo.SistemaGestionCompras;

/**
 * Singleton que centraliza el acceso a los controladores de módulo y al usuario logueado.
 * Los controladores son los puntos de entrada por módulo (DS1-DS4).
 */
public final class AppContext {

    private static AppContext instancia;

    private final SistemaGestionCompras sistema;
    private Usuario usuarioActual;

    private AppContext() {
        sistema = new SistemaGestionCompras("FarmaRed", "1.0");
        OrdenCompraController.inicializar(sistema);
        FacturaController.inicializar(sistema);
        OrdenPagoController.inicializar(sistema);
        ReportesController.inicializar(sistema);
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) {
            instancia = new AppContext();
        }
        return instancia;
    }

    // Acceso a controladores de módulo (DS1-DS4)
    public OrdenCompraController getOrdenCompraCtrl() { return OrdenCompraController.getInstance(); }
    public FacturaController getFacturaCtrl()         { return FacturaController.getInstance(); }
    public OrdenPagoController getOrdenPagoCtrl()     { return OrdenPagoController.getInstance(); }
    public ReportesController getReportesCtrl()       { return ReportesController.getInstance(); }

    /** Acceso directo al sistema para operaciones de proveedores/productos/usuarios. */
    public SistemaGestionCompras getSistema() { return sistema; }

    public Usuario getUsuarioActual()                 { return usuarioActual; }
    public void setUsuarioActual(Usuario u)           { usuarioActual = u; }
    public void cerrarSesion()                        { usuarioActual = null; }
}

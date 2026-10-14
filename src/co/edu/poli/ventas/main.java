package co.edu.poli.ventas;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import co.edu.poli.ventas.procesador.ProcesadorVentas;

/**
 * Segunda clase con metodo main del proyecto. Al ejecutarse, lee los archivos
 * planos generados por {@link GenerateInfoFiles} y crea los dos reportes
 * solicitados en los puntos 3 y 4 del enunciado:
 * <ol>
 * <li>{@code data/reporte_vendedores.csv}: vendedores con el dinero
 * recaudado, de mayor a menor.</li>
 * <li>{@code data/reporte_productos.csv}: productos vendidos con su nombre y
 * precio, ordenados por cantidad vendida de mayor a menor.</li>
 * </ol>
 * <p>
 * El programa no solicita informacion al usuario. Al finalizar muestra un
 * mensaje de exito, junto con las advertencias sobre datos erroneos que se
 * hayan descartado, o un mensaje de error si algo sale mal.
 * </p>
 * <p>
 * El nombre de la clase esta en minuscula porque asi lo exige el enunciado
 * del proyecto.
 * </p>
 *
 * @author Grupo 14 - Herramientas de Programacion Movil
 */
public class main {

    /** Carpeta donde se encuentran los archivos de entrada y los reportes. */
    private static final Path CARPETA_DATOS = Paths.get("data");

    /** Archivo de entrada con la informacion de los productos. */
    private static final Path ARCHIVO_PRODUCTOS = CARPETA_DATOS.resolve("productos.txt");

    /** Archivo de entrada con la informacion de los vendedores. */
    private static final Path ARCHIVO_VENDEDORES = CARPETA_DATOS.resolve("vendedores.txt");

    /** Carpeta de entrada con los archivos de ventas de los vendedores. */
    private static final Path CARPETA_VENTAS = CARPETA_DATOS.resolve("ventas");

    /** Archivo de salida con el reporte de vendedores. */
    private static final Path REPORTE_VENDEDORES = CARPETA_DATOS.resolve("reporte_vendedores.csv");

    /** Archivo de salida con el reporte de productos. */
    private static final Path REPORTE_PRODUCTOS = CARPETA_DATOS.resolve("reporte_productos.csv");

    /**
     * Metodo principal: carga los productos y los vendedores, procesa todos
     * los archivos de ventas y genera los dos reportes CSV.
     *
     * @param args argumentos de linea de comandos; no se utilizan, pues el
     *             programa no solicita informacion al usuario
     */
    public static void main(String[] args) {
        try {
            ProcesadorVentas procesador = new ProcesadorVentas();

            procesador.cargarProductos(ARCHIVO_PRODUCTOS);
            procesador.cargarVendedores(ARCHIVO_VENDEDORES);
            procesador.procesarArchivosVentas(CARPETA_VENTAS);

            procesador.generarReporteVendedores(REPORTE_VENDEDORES);
            procesador.generarReporteProductos(REPORTE_PRODUCTOS);

            mostrarResumen(procesador);
        } catch (IOException excepcion) {
            System.err.println("Ocurrio un error generando los reportes: " + excepcion.getMessage());
        }
    }

    /**
     * Muestra en consola el mensaje de finalizacion exitosa, el resumen del
     * procesamiento y las advertencias sobre datos descartados.
     *
     * @param procesador procesador con los resultados de la ejecucion
     */
    private static void mostrarResumen(ProcesadorVentas procesador) {
        System.out.println("Reportes generados con exito.");
        System.out.println("Vendedores en el reporte: " + procesador.getCantidadVendedores());
        System.out.println("Productos vendidos en el reporte: " + procesador.getCantidadProductosVendidos());
        System.out.println("Archivos de ventas procesados: " + procesador.getArchivosVentasProcesados());
        System.out.println("Reportes creados: " + REPORTE_VENDEDORES + " y " + REPORTE_PRODUCTOS);

        List<String> advertencias = procesador.getAdvertencias();
        if (!advertencias.isEmpty()) {
            System.out.println();
            System.out.println("Se descartaron " + advertencias.size() + " datos con errores:");
            for (String advertencia : advertencias) {
                System.out.println("  - " + advertencia);
            }
        }
    }
}

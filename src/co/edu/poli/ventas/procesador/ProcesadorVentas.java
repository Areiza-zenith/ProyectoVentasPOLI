package co.edu.poli.ventas.procesador;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.edu.poli.ventas.modelo.Producto;
import co.edu.poli.ventas.modelo.Vendedor;

/**
 * Procesa los archivos planos de entrada del proyecto (productos, vendedores
 * y ventas) y genera los dos reportes solicitados en el enunciado:
 * <ol>
 * <li>El reporte de vendedores, con el dinero recaudado por cada uno,
 * ordenado de mayor a menor.</li>
 * <li>El reporte de productos vendidos, ordenado por cantidad vendida de
 * mayor a menor.</li>
 * </ol>
 * <p>
 * Ademas de cumplir los requisitos minimos, esta clase implementa dos de los
 * elementos extra del enunciado:
 * </p>
 * <ul>
 * <li>Permite procesar mas de un archivo de ventas por vendedor: las ventas
 * de todos los archivos de un mismo vendedor se acumulan en un solo
 * total.</li>
 * <li>Detecta archivos con formato erroneo o informacion incoherente (lineas
 * mal formadas, productos inexistentes, cantidades o precios negativos,
 * vendedores no registrados). Esa informacion se descarta y se registra como
 * advertencia, sin detener el procesamiento del resto de los datos.</li>
 * </ul>
 * <p>
 * Los vendedores se identifican por su numero de documento, ya que los
 * archivos de ventas generados por {@code GeneradorArchivos} usan el tipo de
 * documento {@code CC} por defecto, que puede no coincidir con el tipo
 * registrado en el archivo de vendedores.
 * </p>
 *
 * @author Grupo 14 - Herramientas de Programacion Movil
 */
public class ProcesadorVentas {

    /** Separador de campos usado en todos los archivos planos del proyecto. */
    private static final String SEPARADOR = ";";

    /** Extension de los archivos de ventas que se procesan. */
    private static final String EXTENSION_VENTAS = ".txt";

    /** Productos disponibles, indexados por su identificador. */
    private final Map<Integer, Producto> productosPorId = new HashMap<>();

    /** Vendedores registrados, indexados por su numero de documento. */
    private final Map<Long, Vendedor> vendedoresPorDocumento = new HashMap<>();

    /** Dinero recaudado por cada vendedor, indexado por su numero de documento. */
    private final Map<Long, Double> totalRecaudadoPorVendedor = new HashMap<>();

    /** Unidades vendidas de cada producto, indexadas por su identificador. */
    private final Map<Integer, Integer> unidadesVendidasPorProducto = new HashMap<>();

    /** Mensajes de advertencia sobre datos erroneos que fueron descartados. */
    private final List<String> advertencias = new ArrayList<>();

    /** Cantidad de archivos de ventas procesados correctamente. */
    private int archivosVentasProcesados;

    /**
     * Carga el archivo de productos, con el formato
     * {@code IDProducto;NombreProducto;PrecioPorUnidadProducto}. Las lineas
     * mal formadas, con identificador repetido o con precio negativo se
     * descartan y se registran como advertencia.
     *
     * @param archivoProductos ruta del archivo de productos
     * @throws IOException si el archivo no existe, no se puede leer o no
     *                      contiene ningun producto valido
     */
    public void cargarProductos(Path archivoProductos) throws IOException {
        List<String> lineas = leerLineasObligatorias(archivoProductos);

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            String ubicacion = archivoProductos.getFileName() + ", linea " + (i + 1);
            String[] campos = linea.split(SEPARADOR);

            if (campos.length < 3) {
                registrarAdvertencia(ubicacion, "formato invalido, se esperaba ID;Nombre;Precio");
                continue;
            }

            try {
                int idProducto = Integer.parseInt(campos[0].trim());
                String nombreProducto = campos[1].trim();
                double precioPorUnidad = Double.parseDouble(campos[2].trim());

                if (precioPorUnidad < 0) {
                    registrarAdvertencia(ubicacion, "el precio no puede ser negativo");
                } else if (productosPorId.containsKey(idProducto)) {
                    registrarAdvertencia(ubicacion, "el ID de producto " + idProducto + " esta repetido");
                } else {
                    productosPorId.put(idProducto, new Producto(idProducto, nombreProducto, precioPorUnidad));
                }
            } catch (NumberFormatException excepcion) {
                registrarAdvertencia(ubicacion, "el ID o el precio no son numeros validos");
            }
        }

        if (productosPorId.isEmpty()) {
            throw new IOException("El archivo " + archivoProductos + " no contiene productos validos.");
        }
    }

    /**
     * Carga el archivo de vendedores, con el formato
     * {@code TipoDocumento;NumeroDocumento;Nombres;Apellidos}. Las lineas mal
     * formadas o con documento repetido se descartan y se registran como
     * advertencia.
     *
     * @param archivoVendedores ruta del archivo de vendedores
     * @throws IOException si el archivo no existe, no se puede leer o no
     *                      contiene ningun vendedor valido
     */
    public void cargarVendedores(Path archivoVendedores) throws IOException {
        List<String> lineas = leerLineasObligatorias(archivoVendedores);

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty()) {
                continue;
            }
            String ubicacion = archivoVendedores.getFileName() + ", linea " + (i + 1);
            String[] campos = linea.split(SEPARADOR);

            if (campos.length < 4) {
                registrarAdvertencia(ubicacion, "formato invalido, se esperaba Tipo;Numero;Nombres;Apellidos");
                continue;
            }

            try {
                String tipoDocumento = campos[0].trim();
                long numeroDocumento = Long.parseLong(campos[1].trim());
                String nombres = campos[2].trim();
                String apellidos = campos[3].trim();

                if (vendedoresPorDocumento.containsKey(numeroDocumento)) {
                    registrarAdvertencia(ubicacion, "el documento " + numeroDocumento + " esta repetido");
                } else {
                    Vendedor vendedor = new Vendedor(tipoDocumento, numeroDocumento, nombres, apellidos);
                    vendedoresPorDocumento.put(numeroDocumento, vendedor);
                    totalRecaudadoPorVendedor.put(numeroDocumento, 0.0);
                }
            } catch (NumberFormatException excepcion) {
                registrarAdvertencia(ubicacion, "el numero de documento no es un numero valido");
            }
        }

        if (vendedoresPorDocumento.isEmpty()) {
            throw new IOException("El archivo " + archivoVendedores + " no contiene vendedores validos.");
        }
    }

    /**
     * Procesa todos los archivos de ventas ({@code .txt}) de la carpeta
     * indicada. Un mismo vendedor puede tener varios archivos; sus ventas se
     * acumulan en el mismo total. Debe llamarse despues de
     * {@link #cargarProductos(Path)} y {@link #cargarVendedores(Path)}.
     *
     * @param carpetaVentas carpeta que contiene los archivos de ventas
     * @throws IOException si la carpeta no existe o no contiene archivos de
     *                      ventas
     */
    public void procesarArchivosVentas(Path carpetaVentas) throws IOException {
        File[] archivos = carpetaVentas.toFile()
                .listFiles((carpeta, nombre) -> nombre.toLowerCase(Locale.ROOT).endsWith(EXTENSION_VENTAS));

        if (archivos == null) {
            throw new IOException("No existe la carpeta de ventas " + carpetaVentas
                    + ". Ejecute primero GenerateInfoFiles.");
        }
        if (archivos.length == 0) {
            throw new IOException("La carpeta " + carpetaVentas + " no contiene archivos de ventas.");
        }

        Arrays.sort(archivos);
        for (File archivo : archivos) {
            procesarArchivoVentas(archivo.toPath());
        }
    }

    /**
     * Procesa un unico archivo de ventas. La primera linea debe identificar
     * al vendedor ({@code TipoDocumento;NumeroDocumento}) y las siguientes
     * contienen una venta cada una ({@code IDProducto;Cantidad;}). Si la
     * primera linea es invalida o el vendedor no esta registrado, se descarta
     * el archivo completo.
     *
     * @param archivoVentas ruta del archivo de ventas
     * @throws IOException si ocurre un error al leer el archivo
     */
    private void procesarArchivoVentas(Path archivoVentas) throws IOException {
        List<String> lineas = leerLineas(archivoVentas);
        String nombreArchivo = archivoVentas.getFileName().toString();

        if (lineas.isEmpty() || lineas.get(0).trim().isEmpty()) {
            registrarAdvertencia(nombreArchivo, "archivo vacio, se descarta");
            return;
        }

        Long numeroDocumento = leerDocumentoVendedor(lineas.get(0));
        if (numeroDocumento == null) {
            registrarAdvertencia(nombreArchivo, "la primera linea debe ser TipoDocumento;NumeroDocumento, se descarta");
            return;
        }
        if (!vendedoresPorDocumento.containsKey(numeroDocumento)) {
            registrarAdvertencia(nombreArchivo, "el vendedor " + numeroDocumento + " no esta registrado, se descarta");
            return;
        }

        for (int i = 1; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (!linea.isEmpty()) {
                procesarLineaVenta(linea, numeroDocumento, nombreArchivo + ", linea " + (i + 1));
            }
        }
        archivosVentasProcesados++;
    }

    /**
     * Extrae el numero de documento del vendedor de la primera linea de un
     * archivo de ventas.
     *
     * @param primeraLinea primera linea del archivo de ventas
     * @return el numero de documento, o {@code null} si la linea es invalida
     */
    private Long leerDocumentoVendedor(String primeraLinea) {
        String[] campos = primeraLinea.trim().split(SEPARADOR);
        if (campos.length < 2) {
            return null;
        }
        try {
            return Long.parseLong(campos[1].trim());
        } catch (NumberFormatException excepcion) {
            return null;
        }
    }

    /**
     * Procesa una linea de venta ({@code IDProducto;Cantidad;}) y la suma al
     * total del vendedor y a las unidades vendidas del producto. Si la linea
     * es invalida, se descarta y se registra como advertencia.
     *
     * @param linea           linea de venta a procesar
     * @param numeroDocumento documento del vendedor que hizo la venta
     * @param ubicacion       archivo y linea, usados en las advertencias
     */
    private void procesarLineaVenta(String linea, long numeroDocumento, String ubicacion) {
        String[] campos = linea.split(SEPARADOR);
        if (campos.length < 2) {
            registrarAdvertencia(ubicacion, "formato invalido, se esperaba IDProducto;Cantidad;");
            return;
        }

        try {
            int idProducto = Integer.parseInt(campos[0].trim());
            int cantidad = Integer.parseInt(campos[1].trim());
            Producto producto = productosPorId.get(idProducto);

            if (producto == null) {
                registrarAdvertencia(ubicacion, "el producto " + idProducto + " no existe");
            } else if (cantidad <= 0) {
                registrarAdvertencia(ubicacion, "la cantidad debe ser mayor a cero");
            } else {
                double valorVenta = cantidad * producto.getPrecioPorUnidad();
                totalRecaudadoPorVendedor.merge(numeroDocumento, valorVenta, Double::sum);
                unidadesVendidasPorProducto.merge(idProducto, cantidad, Integer::sum);
            }
        } catch (NumberFormatException excepcion) {
            registrarAdvertencia(ubicacion, "el ID de producto o la cantidad no son numeros validos");
        }
    }

    /**
     * Genera el reporte de vendedores en formato CSV, con una linea por
     * vendedor ({@code NombreCompleto;DineroRecaudado}), ordenado por dinero
     * recaudado de mayor a menor. Incluye a todos los vendedores registrados,
     * aunque no tengan ventas.
     *
     * @param archivoReporte ruta del archivo de reporte a crear
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void generarReporteVendedores(Path archivoReporte) throws IOException {
        List<Vendedor> vendedoresOrdenados = new ArrayList<>(vendedoresPorDocumento.values());
        vendedoresOrdenados.sort(Comparator
                .comparing((Vendedor vendedor) -> totalRecaudadoPorVendedor.get(vendedor.getNumeroDocumento()))
                .reversed()
                .thenComparing(Vendedor::getNombreCompleto));

        try (BufferedWriter escritor = Files.newBufferedWriter(archivoReporte, StandardCharsets.UTF_8)) {
            for (Vendedor vendedor : vendedoresOrdenados) {
                double totalRecaudado = totalRecaudadoPorVendedor.get(vendedor.getNumeroDocumento());
                escritor.write(vendedor.getNombreCompleto() + SEPARADOR + formatearDinero(totalRecaudado));
                escritor.newLine();
            }
        }
    }

    /**
     * Genera el reporte de productos vendidos en formato CSV, con una linea
     * por producto ({@code NombreProducto;PrecioPorUnidad}), ordenado por
     * cantidad de unidades vendidas de mayor a menor. Solo incluye productos
     * con al menos una unidad vendida.
     *
     * @param archivoReporte ruta del archivo de reporte a crear
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void generarReporteProductos(Path archivoReporte) throws IOException {
        List<Producto> productosVendidos = new ArrayList<>();
        for (Integer idProducto : unidadesVendidasPorProducto.keySet()) {
            productosVendidos.add(productosPorId.get(idProducto));
        }
        productosVendidos.sort(Comparator
                .comparing((Producto producto) -> unidadesVendidasPorProducto.get(producto.getId()))
                .reversed()
                .thenComparing(Producto::getNombre));

        try (BufferedWriter escritor = Files.newBufferedWriter(archivoReporte, StandardCharsets.UTF_8)) {
            for (Producto producto : productosVendidos) {
                escritor.write(producto.getNombre() + SEPARADOR + formatearDinero(producto.getPrecioPorUnidad()));
                escritor.newLine();
            }
        }
    }

    /**
     * Obtiene las advertencias registradas durante el procesamiento.
     *
     * @return la lista de advertencias, en el orden en que se detectaron
     */
    public List<String> getAdvertencias() {
        return advertencias;
    }

    /**
     * Obtiene la cantidad de archivos de ventas procesados correctamente.
     *
     * @return la cantidad de archivos de ventas procesados
     */
    public int getArchivosVentasProcesados() {
        return archivosVentasProcesados;
    }

    /**
     * Obtiene la cantidad de vendedores registrados.
     *
     * @return la cantidad de vendedores cargados del archivo de vendedores
     */
    public int getCantidadVendedores() {
        return vendedoresPorDocumento.size();
    }

    /**
     * Obtiene la cantidad de productos distintos con al menos una venta.
     *
     * @return la cantidad de productos vendidos
     */
    public int getCantidadProductosVendidos() {
        return unidadesVendidasPorProducto.size();
    }

    /**
     * Lee todas las lineas de un archivo de entrada obligatorio.
     *
     * @param archivo ruta del archivo a leer
     * @return las lineas del archivo
     * @throws IOException si el archivo no existe o no se puede leer
     */
    private List<String> leerLineasObligatorias(Path archivo) throws IOException {
        if (!Files.exists(archivo)) {
            throw new IOException("No existe el archivo " + archivo + ". Ejecute primero GenerateInfoFiles.");
        }
        return leerLineas(archivo);
    }

    /**
     * Lee todas las lineas de un archivo de texto en UTF-8.
     *
     * @param archivo ruta del archivo a leer
     * @return las lineas del archivo
     * @throws IOException si ocurre un error de lectura
     */
    private List<String> leerLineas(Path archivo) throws IOException {
        List<String> lineas = new ArrayList<>();
        try (BufferedReader lector = Files.newBufferedReader(archivo, StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                lineas.add(linea);
            }
        }
        return lineas;
    }

    /**
     * Registra una advertencia sobre un dato descartado.
     *
     * @param ubicacion archivo (y linea) donde se encontro el problema
     * @param mensaje   descripcion del problema
     */
    private void registrarAdvertencia(String ubicacion, String mensaje) {
        advertencias.add(ubicacion + ": " + mensaje);
    }

    /**
     * Da formato a un valor de dinero con dos decimales y punto decimal,
     * para que no se confunda con el separador de campos.
     *
     * @param valor valor a formatear
     * @return el valor con dos decimales
     */
    private String formatearDinero(double valor) {
        return String.format(Locale.US, "%.2f", valor);
    }
}

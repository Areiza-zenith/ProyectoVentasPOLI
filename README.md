# Proyecto Ventas POLI — Entrega final (Semanas 7 y 8)

**Módulo:** Conceptos Fundamentales de Programación
**Entrega:** Generación y clasificación de datos
**Integrantes:** Cristian Fabián Areiza Umbacia, Daniela Herrera Pulido, Jhonathan Urrego Cordero, Joseph Benavides Diaz, Kevin Leonardo Vanegas Osorio

## Descripción

Programa en Java 8 (proyecto de Eclipse) que genera archivos planos de prueba
con productos, vendedores y ventas, y a partir de ellos crea dos reportes en
formato CSV: el dinero recaudado por cada vendedor y los productos vendidos
ordenados por cantidad.

El proyecto tiene dos (y solo dos) clases con método `main`:

1. `co.edu.poli.ventas.GenerateInfoFiles`: genera los archivos de entrada.
2. `co.edu.poli.ventas.main`: lee los archivos de entrada y genera los reportes.

Ninguno de los dos programas solicita información al usuario. Ambos muestran
un mensaje de finalización exitosa o un mensaje de error.

## Estructura del proyecto

```
ProyectoVentasPOLI/
├── .project
├── .classpath
├── README.md
├── conslusion.txt
├── src/
│   └── co/edu/poli/ventas/
│       ├── GenerateInfoFiles.java      (primera clase con main: genera los archivos)
│       ├── main.java                   (segunda clase con main: genera los reportes)
│       ├── generador/
│       │   └── GeneradorArchivos.java  (métodos de generación de archivos de prueba)
│       ├── procesador/
│       │   └── ProcesadorVentas.java   (lectura, validación, acumulación y reportes)
│       └── modelo/
│           ├── Producto.java
│           └── Vendedor.java
└── data/
    ├── productos.txt
    ├── vendedores.txt
    ├── ventas/
    │   └── ventas_<nombre>_<documento>.txt
    ├── reporte_vendedores.csv
    └── reporte_productos.csv
```

## Cómo importar el proyecto en Eclipse

1. Abrir Eclipse para *Java Developers*.
2. `File > Import... > General > Existing Projects into Workspace`.
3. Seleccionar la carpeta `ProyectoVentasPOLI`.
4. Verificar que el proyecto use un JRE compatible con Java 8.

## Cómo ejecutar

1. Ejecutar `co.edu.poli.ventas.GenerateInfoFiles` (clic derecho →
   `Run As > Java Application`). Genera en `data/`:
   - `productos.txt`: 15 productos (`ID;Nombre;Precio`).
   - `vendedores.txt`: 8 vendedores (`TipoDocumento;NúmeroDocumento;Nombres;Apellidos`).
   - `ventas/`: un archivo por vendedor, con `TipoDocumento;NúmeroDocumento`
     en la primera línea y una venta por línea (`IDProducto;Cantidad;`),
     entre 5 y 15 ventas por vendedor.
2. Ejecutar `co.edu.poli.ventas.main`. Genera en `data/`:
   - `reporte_vendedores.csv`: `NombreCompleto;DineroRecaudado`, de mayor a menor.
   - `reporte_productos.csv`: `NombreProducto;Precio`, ordenado por cantidad
     vendida de mayor a menor.

El repositorio ya incluye un juego de datos de ejemplo en `data/`, por lo que
`main` puede ejecutarse directamente.

## Métodos de generación (`GeneradorArchivos`)

- `createProductsFile(int productsCount)`
- `createSalesManInfoFile(int salesmanCount)`
- `createSalesMenFile(int randomSalesCount, String name, long id)`

Como `createSalesMenFile` no recibe el tipo de documento, el archivo de ventas
usa `CC` por defecto. Por eso la clase `main` identifica a cada vendedor por
su número de documento.

## Elementos extra implementados

- **Más de un archivo por vendedor:** las ventas de todos los archivos de un
  mismo vendedor se suman en un solo total.
- **Detección de datos erróneos:** se descartan y se informan en consola las
  líneas con formato inválido, productos inexistentes, cantidades o precios
  negativos y archivos de vendedores no registrados, sin detener el programa.

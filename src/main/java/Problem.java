import java.util.Random;
import java.util.Scanner;

public class Problem {

    // TODO: añadir bucle y menú de opciones

    public static void main(String[] args) {
        Random random = new Random();
        int semilla = random.nextInt();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Introduce el número de estaciones, el número de bicis, el número de furgonetas y el tipo " +
                "demanda (0: equilibrada o 1: punta):"); // Por defecto, la semilla es aleatoria

        int numeroEstaciones = scanner.nextInt();
        int numeroBicisTotal = scanner.nextInt();
        int numeroFurgonetas = scanner.nextInt();
        TipoDemanda tipoDemanda = TipoDemanda.values()[scanner.nextInt()];

        System.out.println("Introduce 0 para utilizar el algoritmo Hill Climbing o 1 para Simulated Annealing:");
        int algoritmoSeleccionado = scanner.nextInt();

        System.out.println("Introduce 0 para utilizar el primer generador de solución inicial o 1 para el segundo:");
        int generadorSeleccionado = scanner.nextInt();

        // TODO: añadir la seleccion del conjunto de operadores

        System.out.println("Introduce 0 para utilizar el primer heurístico o 1 para el segundo:");
        int heuristicoSeleccionado = scanner.nextInt();

        mostrarValoresSeleccionados(numeroEstaciones, numeroBicisTotal, numeroFurgonetas, tipoDemanda,
                algoritmoSeleccionado, generadorSeleccionado, heuristicoSeleccionado);

        // Empezamos solucion inicial ------------------------------
        Solution solucionInicial = new Solution(numeroEstaciones, numeroBicisTotal, numeroFurgonetas, tipoDemanda,
                semilla);
        System.out.println("EMPEZAMOS LA GENERACION");
        solucionInicial.generadorSolucion1();
        System.out.println("FINAL DE LA GENERACION");
        System.out.println("****************************************************");
        System.out.println(String.format("BENEFICIOS - COSTE POR FALLOS: '%s'", solucionInicial.getBeneficios()));
    }

    private static void mostrarMenu() {
        System.out.println("***********************************");
        System.out.println("********    B I C I N G    ********");
        System.out.println("***********************************");
        System.out.println(" ");
        System.out.println("Problema de búsqueda local");
        System.out.println(" ");
        System.out.println("Miembros del equipo:");
        System.out.println("   - Federico Rubinstein");
        System.out.println("   - Roger González Herrera");
        System.out.println("   - Luis Oriol Soler Cruz");
        System.out.println(" ");
        System.out.println(" ");
//        System.out.println("Introduce 0 para iniciar un nuevo problema Bicing o 1 para salir");
    }

    private static void mostrarValoresSeleccionados(int numeroEstaciones, int numeroBicisTotal, int numeroFurgonetas,
                                                    TipoDemanda tipoDemanda, int algoritmoSeleccionado,
                                                    int generadorSeleccionado, int heuristicoSeleccionado) {
        System.out.println("Estaciones: " + numeroEstaciones);
        System.out.println("Bicis: " + numeroBicisTotal);
        System.out.println("Furgonetas: " + numeroFurgonetas);
        System.out.println("Tipo demanda: " + tipoDemanda.toString());
        System.out.println("Algoritmo: " + algoritmoSeleccionado);
        System.out.println("Generador: " + generadorSeleccionado);
        System.out.println("Heuristico: " + heuristicoSeleccionado);
    }
}
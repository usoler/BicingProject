import IA.Bicing.Estaciones;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static java.lang.StrictMath.sqrt;

public class BicingProblem {

    // TODO: añadir bucle y menú de opciones

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        mostrarMenu();
        int end = scanner.nextInt();
        while (end != 1) {
            Random random = new Random();

            System.out.println("Introduce el número de estaciones, el número de bicis, el número de furgonetas y el tipo " +
                    "demanda (0: equilibrada o 1: punta):"); // Por defecto, la semilla es aleatoria
            // TODO: añadir la seleccion de la semilla

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





            int test = 10;

            // Empezamos solucion inicial ------------------------------
            int k = 1;
            for (int ki = 0; ki < 5; ++ki) {
                if(ki == 1) k = 5;
                if(ki == 2) k = 10;
                if(ki == 3) k = 25;
                if(ki == 4) k = 125;
                String nombreFichero = "Experimento3_k_" + Integer.toString(k);
                String pathname = "C:\\Users\\Fede\\Desktop\\code\\GitKraken\\BicingProject\\src\\main\\resources\\" + nombreFichero + ".csv";
                File csvFile = new File(pathname);
                FileWriter writer = new FileWriter(csvFile);

                CSVUtils.writeLine(writer, Arrays.asList("Iteracions", "Seed", "k", "Lambda", "Beneficis"));

                double lambda = 10.0;
                for(int li = 0; li < 5; ++li) {
                    lambda /= 10.0;
                    lambda = BigDecimal.valueOf(lambda).setScale(4, RoundingMode.HALF_UP).doubleValue();
                    for (int i = 0; i < test; ++i) {
                        int semilla = random.nextInt();
                        for (int j = 0; j < 100; ++j) {
                            BicingSolution solucionInicial = new BicingSolution(numeroEstaciones, numeroBicisTotal, numeroFurgonetas, tipoDemanda,
                                    semilla);
                            //System.out.println("EMPEZAMOS LA GENERACION");

                            //if (generadorSeleccionado == 0) {
                            solucionInicial.generadorSolucion1();
//                    } else {
//                        solucionInicial.generadorSolucion2();
//                    }
                            int beneficios = Bicing_Search(solucionInicial, algoritmoSeleccionado, heuristicoSeleccionado, k, lambda);
                            CSVUtils.writeLine(writer, Arrays.asList(Integer.toString(i+1), Integer.toString(semilla),
                                    Integer.toString(k), Double.toString(lambda), Integer.toString(beneficios)));
                        }
                    }
                }
                writer.flush();
                writer.close();
            }

            mostrarMenu();
            end = scanner.nextInt();
        }
    }

    private static void mostrarMenu() {
//        System.out.println("***********************************");
//        System.out.println("********    B I C I N G    ********");
//        System.out.println("***********************************");
//        System.out.println(" ");
//        System.out.println("Problema de búsqueda local");
//        System.out.println(" ");
//        System.out.println("Miembros del equipo:");
//        System.out.println("   - Federico Rubinstein");
//        System.out.println("   - Roger González Herrera");
//        System.out.println("   - Luis Oriol Soler Cruz");
        System.out.println(" ");
        System.out.println(" ");
        System.out.println("Introduce 0 para iniciar un nuevo problema Bicing o 1 para salir");
    }

    private static int Bicing_Search(BicingSolution solution, int algoritmoSeleccionado, int heuristicoSeleccionado, int k, double lamb) {
        try {
            HeuristicFunction heuristicFunction;
            if (heuristicoSeleccionado == 0) {
                heuristicFunction = new BicingHeuristicFunction1();
            } else {
                heuristicFunction = new BicingHeuristicFunction2();
            }

            SuccessorFunction successorFunction;
            Search search;
            if (algoritmoSeleccionado == 0) {
                successorFunction = new BicingSuccessorFunction1();
                search = new HillClimbingSearch();
            } else {
                successorFunction = new BicingSuccessorFunction2();
                //Antes: steps: 10000, stiter: 100, k: 5, lamb: 0.001
                search = new SimulatedAnnealingSearch(10000, 100, k, lamb);
            }

            Problem problem = new Problem(solution, successorFunction, new BicingGoalTest(), heuristicFunction);

            SearchAgent agent = new SearchAgent(problem, search); // TODO: ignorar System.out.println para calcular el tiempo correctamente


            //System.out.println(String.format("Time = '%s' ms", (endTime - startTime)));

//            if (algoritmoSeleccionado == 0) {
//                printActions(agent.getActions());
//                printInstrumentation(agent.getInstrumentation());
//            }

            //System.out.print(((BicingSolution) search.getGoalState()).toString());
            BicingSolution goalSolution = ((BicingSolution) search.getGoalState());
            return goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo();
//            System.out.println(String.format("FINAL: BENEFICIOS - COSTE POR FALLOS: '%s'", goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo()));
//            System.out.println(String.format("FINAL: BENEFICIOS: '%s'", goalSolution.getBeneficioPorAcierto()));
//            System.out.println(String.format("FINAL: COSTE POR FALLOS: '%s'", goalSolution.getPenalizacionPorFallo()));
//            System.out.println(String.format("FINAL: COSTE POR TRANSPORTE: '%s'", goalSolution.getCosteTransporte()));
//            System.out.println(String.format("FINAL: TOTAL GANADO (1): '%s'", goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo()));
//            System.out.println(String.format("FINAL: TOTAL GANADO (2): '%s'", goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo() - goalSolution.getCosteTransporte()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }


    private static void printActions(List actions) {
        for (int i = 0; i < actions.size(); i++) {
            String action = (String) actions.get(i);
            System.out.println(action);
        }
    }

    private static void printInstrumentation(Properties properties) {
        Iterator keys = properties.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String property = properties.getProperty(key);
            System.out.println(key + " : " + property);
        }
    }

    private static void printCoords(BicingSolution solution) {
        Estaciones estaciones = solution.getEstaciones();
        for (int i = 0; i < estaciones.size(); ++i) {
            System.out.println(String.format("Estacion #%s", i));
            int x = estaciones.get(i).getCoordX();
            int y = estaciones.get(i).getCoordY();
            System.out.println(String.format("x = '%s', y = '%s'", x, y));
        }
    }

    private static void printBeneficiosMaximosPosibles(BicingSolution solution) {
        Estaciones estaciones = solution.getEstaciones();
        int beneficios = 0;
        for (int i = 0; i < estaciones.size(); ++i) {
            int demanda = estaciones.get(i).getDemanda();
            int bicisDisponibles = estaciones.get(i).getNumBicicletasNext();
            if (demanda > bicisDisponibles) {
                beneficios += (demanda - bicisDisponibles);
            }
        }

        System.out.println(String.format("BENEFICIOS POSIBLES A OBTENER: '%s'", beneficios));
    }

    private static void printInfoEstaciones(BicingSolution solution) {
        Estaciones estaciones = solution.getEstaciones();
        for (int i = 0; i < estaciones.size(); ++i) {
            int demanda = estaciones.get(i).getDemanda();
            int bicisDisponibles = estaciones.get(i).getNumBicicletasNext();

            System.out.println(String.format("Estacion con id '%s'", i));
            System.out.println(String.format("Demanda = '%s'", demanda));
            System.out.println(String.format("BicisDisponibles = '%s'", bicisDisponibles));
        }
    }
}
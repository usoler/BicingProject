import IA.Bicing.Estaciones;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.util.*;

public class BicingProblem {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        mostrarMenu();
        int end = scanner.nextInt();
        while (end != 1) {
            Random random = new Random();

            System.out.println("Introduce el número de estaciones, el número de bicis, el número de furgonetas y el tipo " +
                    "demanda (0: equilibrada o 1: punta):"); // Por defecto, la semilla es aleatoria

            int numeroEstaciones = scanner.nextInt();
            int numeroBicisTotal = scanner.nextInt();
            int numeroFurgonetas = scanner.nextInt();
            TipoDemanda tipoDemanda = TipoDemanda.values()[scanner.nextInt()];

            System.out.println("Introduce un valor para la semilla o 0 para obtener una aleatoriamente:");
            int semilla = scanner.nextInt();
            if (semilla == 0) {
                semilla = random.nextInt();
            }

            System.out.println("Introduce 0 para utilizar el algoritmo Hill Climbing o 1 para Simulated Annealing:");
            int algoritmoSeleccionado = scanner.nextInt();

            System.out.println("Introduce 0 para utilizar el primer generador de solución inicial o 1 para el segundo:");
            int generadorSeleccionado = scanner.nextInt();

            System.out.println("Introduce 0 para utilizar el conjunto de operadores optimo o 1 para usarlos todos:");
            boolean esConjuntoOptimo = false;
            if (scanner.nextInt() == 0) {
                esConjuntoOptimo = true;
            }

            System.out.println("Introduce 0 para utilizar el primer heurístico o 1 para el segundo:");
            int heuristicoSeleccionado = scanner.nextInt();


            // Empezamos solucion inicial ------------------------------
            BicingSolution solucionInicial = new BicingSolution(numeroEstaciones, numeroBicisTotal, numeroFurgonetas, tipoDemanda,
                    semilla);
            System.out.println("EMPEZAMOS LA GENERACION");

            if (generadorSeleccionado == 0) {
                solucionInicial.generadorSolucion1();
            } else {
                solucionInicial.generadorSolucion2();
            }

            if (esConjuntoOptimo) {
                solucionInicial.setEstaUsandoConjuntoOperadoresOptimo(true);
            }

            System.out.println("FINAL DE LA GENERACION");
            System.out.println("****************************************************");
            printInfoEstaciones(solucionInicial);
            printBeneficiosMaximosPosibles(solucionInicial);
            System.out.println(String.format("BENEFICIOS - COSTE POR FALLOS: '%s'", solucionInicial.getBeneficioPorAcierto() - solucionInicial.getPenalizacionPorFallo()));
            System.out.println(String.format("BENEFICIOS: '%s'", solucionInicial.getBeneficioPorAcierto()));
            System.out.println(String.format("COSTE POR FALLOS: '%s'", solucionInicial.getPenalizacionPorFallo()));
            System.out.println(String.format("COSTE POR TRANSPORTE: '%s'", solucionInicial.getCosteTransporte()));

            Bicing_Search(solucionInicial, algoritmoSeleccionado, heuristicoSeleccionado);
            printCoords(solucionInicial);

            mostrarMenu();
            end = scanner.nextInt();
        }
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
        System.out.println("Introduce 0 para iniciar un nuevo problema Bicing o 1 para salir");
    }

    private static void Bicing_Search(BicingSolution solution, int algoritmoSeleccionado, int heuristicoSeleccionado) {
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
                search = new SimulatedAnnealingSearch(10000, 100, 5, 0.001);
            }

            Problem problem = new Problem(solution, successorFunction, new BicingGoalTest(), heuristicFunction);

            long startTime = System.currentTimeMillis();
            SearchAgent agent = new SearchAgent(problem, search); // TODO: ignorar todos los System.out.println del proyecto para calcular el tiempo correctamente
            long endTime = System.currentTimeMillis();

            System.out.println(String.format("Time = '%s' ms", (endTime - startTime)));

            if (algoritmoSeleccionado == 0) {
                printActions(agent.getActions());
                printInstrumentation(agent.getInstrumentation());
            }
            System.out.print(((BicingSolution) search.getGoalState()).toString());
            BicingSolution goalSolution = ((BicingSolution) search.getGoalState());
            System.out.println(String.format("FINAL: BENEFICIOS - COSTE POR FALLOS: '%s'", goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo()));
            System.out.println(String.format("FINAL: BENEFICIOS: '%s'", goalSolution.getBeneficioPorAcierto()));
            System.out.println(String.format("FINAL: COSTE POR FALLOS: '%s'", goalSolution.getPenalizacionPorFallo()));
            System.out.println(String.format("FINAL: COSTE POR TRANSPORTE: '%s'", goalSolution.getCosteTransporte()));
            System.out.println(String.format("FINAL: TOTAL GANADO: '%s'", goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo() - goalSolution.getCosteTransporte()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
import IA.Bicing.Estaciones;
import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class BicingProblem {

    // TODO: añadir bucle y menú de opciones

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        mostrarMenu();
        //int end = scanner.nextInt();
        int end = 0;
        while (end != 1) {
            Random random = new Random();

            //System.out.println("Introduce el número de estaciones, el número de bicis, el número de furgonetas y el tipo " +
            //        "demanda (0: equilibrada o 1: punta):"); // Por defecto, la semilla es aleatoria
            // TODO: añadir la seleccion de la semilla

            //int numeroEstaciones = scanner.nextInt();
            //int numeroBicisTotal = scanner.nextInt();
            //int numeroFurgonetas = scanner.nextInt();
            //TipoDemanda tipoDemanda = TipoDemanda.values()[0];
            TipoDemanda tipoDemanda = TipoDemanda.EQUILIBRADA;

            //System.out.println("Introduce 0 para utilizar el algoritmo Hill Climbing o 1 para Simulated Annealing:");
            //int algoritmoSeleccionado = scanner.nextInt();

            //System.out.println("Introduce 0 para utilizar el primer generador de solución inicial o 1 para el segundo:");
            //int generadorSeleccionado = scanner.nextInt();

            // TODO: añadir la seleccion del conjunto de operadores

            //System.out.println("Introduce 0 para utilizar el primer heurístico o 1 para el segundo:");
            //int heuristicoSeleccionado = scanner.nextInt();

            int test = 10;
            //int[] beneficiosIni = new int[test];
            //int[] beneficios = new int[test];
            int[] numEstaciones = new int[test];
            long[][] tiempo = new long[test][test];
            // Empezamos solucion inicial ------------------------------
            for (int i = 0; i < test; ++i) {
                System.out.println(i);
                //int beneficiosj = 0;
                //int beneficiosInij = 0;
                int numEstacionesj = 25;
                int numBicisj = 1250;
                int numFurgosj = 5;
                //long tiempoj = 0;
                int semilla = random.nextInt();

                for (int j = 0; j < test; ++j) {
                    System.out.println(String.format("Segon bucle: '%s'", j));
                    long startTime = System.currentTimeMillis();

                    BicingSolution solucionInicial = new BicingSolution(numEstacionesj, numBicisj, numFurgosj, TipoDemanda.EQUILIBRADA,
                            semilla);

                    solucionInicial.generadorSolucion1();


                    //beneficiosInij += (solucionInicial.getBeneficioPorAcierto() - solucionInicial.getPenalizacionPorFallo());
                    //beneficiosj += Bicing_Search(solucionInicial, algoritmoSeleccionado, heuristicoSeleccionado);
                    Bicing_Search(solucionInicial, 0, 0); // HillClimbing & Heuristico1

                    long endTime = System.currentTimeMillis();
                    //tiempoj += (endTime - startTime);
                    tiempo[i][j] = (endTime - startTime);
                    numEstacionesj += 25;
                    numBicisj += 1250;
                    numFurgosj += 5;
                }
                //beneficiosIni[i] = beneficiosInij / 10;
                //beneficios[i] = beneficiosj / 10;
                //tiempo[i] = tiempoj / 10;
                numEstaciones[i] = 25 * (i + 1);
            }
            //printCoords(solucionInicial);
            String nombreFichero = "tiempoEjecucion";
            //String pathname = "C:\\Users\\Fede\\Desktop\\code\\GitKraken\\BicingProject\\src\\main\\resources\\" + nombreFichero + ".csv";
            String pathname = "/Users/luisoriolsolercruz/Documents/GitKraken/BicingProject/src/main/resources/" + nombreFichero + ".csv";
            File csvFile = new File(pathname);
            FileWriter writer = new FileWriter(csvFile);


            CSVUtils.writeLine(writer, Arrays.asList(nombreFichero));
            CSVUtils.writeLine(writer, Arrays.asList("Experiment", "Num estacions", "Temps d'Execució (ms)"));
            //int beneficiosAcumuladosIni = 0;
            //int beneficiosAcumulados = 0;
            //int tiempoAcumulado = 0;

            List<String> csvList = null;
            for (int i = 0; i < test; ++i) {
                System.out.println(String.format("Experiment: '%s'", i + 1));
                for (int j = 0; j < test; ++j) {
                    //System.out.println(String.format("BENEFICIOSini - COSTE POR FALLOSini: '%s'", beneficiosIni[i]));
                    //System.out.println(String.formmat("BENEFICIOS - COSTE POR FALLOS: '%s'", beneficios[i]));
                    System.out.println(String.format("NUM ESTACIONS: '%s', TEMPS EXECUCIO: '%s'", numEstaciones[j], tiempo[i][j]));
                    csvList = new ArrayList<>();
                    csvList.add(0, String.valueOf(i + 1));
                    csvList.add(1, String.valueOf(numEstaciones[j]));
                    csvList.add(2, String.valueOf(tiempo[i][j]));

                    CSVUtils.writeLine(writer, csvList);
                    //beneficiosAcumuladosIni += beneficiosIni[i];
                    //beneficiosAcumulados += beneficios[i];
                    //tiempoAcumulado += tiempo[i];
                }
            }

            /*double mediaBeneficiosIni = (double) beneficiosAcumuladosIni / test;
            double mediaBeneficios = (double) beneficiosAcumulados / test;
            double mediaTiempo = (double) tiempoAcumulado / test;
            double desvTipusBenIni = 0;
            double desvTipusBen = 0;
            double desvTipusTiem = 0;
            for (int i = 0; i < test; ++i) {
                desvTipusBenIni += Math.pow((beneficiosIni[i] - mediaBeneficiosIni), 2);
                desvTipusBen += Math.pow((beneficios[i] - mediaBeneficios), 2);
                desvTipusTiem += Math.pow((tiempo[i] - mediaTiempo), 2);
            }
            desvTipusBenIni = sqrt(desvTipusBenIni / test);
            desvTipusBen = sqrt(desvTipusBen / test);
            desvTipusTiem = sqrt(desvTipusTiem / test);

            desvTipusBenIni = BigDecimal.valueOf(desvTipusBenIni).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusBen = BigDecimal.valueOf(desvTipusBen).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusTiem = BigDecimal.valueOf(desvTipusTiem).setScale(3, RoundingMode.HALF_UP).doubleValue();

            CSVUtils.writeLine(writer, Arrays.asList("Mitjana (desv. típica)", mediaBeneficiosIni + " (" + desvTipusBenIni + ")",
                    mediaBeneficios + " (" + desvTipusBen + ")", mediaTiempo + " (" + desvTipusTiem + ")"));*/
            writer.flush();
            writer.close();

            //mostrarMenu();
            //end = scanner.nextInt();
            end = 1;
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

    private static int Bicing_Search(BicingSolution solution, int algoritmoSeleccionado, int heuristicoSeleccionado) {
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
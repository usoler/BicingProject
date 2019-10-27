import aima.search.framework.*;
import aima.search.informed.HillClimbingSearch;
import aima.search.informed.SimulatedAnnealingSearch;
import javafx.util.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;

import static java.lang.StrictMath.sqrt;

public class BicingProblem {

    public static void main(String[] args) throws IOException {
        int end = 0;
        while (end != 1) {
            Random random = new Random();

            int numeroEstaciones = 25;
            int numeroBicisTotal = 1250;
            int numeroFurgonetas = 5;
            TipoDemanda tipoDemanda = TipoDemanda.EQUILIBRADA;

            int test = 10;

            // Hill Climbing
            int[] beneficiosH1HC = new int[test];
            int[] beneficiosH2HC = new int[test];
            double[] distanciaH1HC = new double[test];
            double[] distanciaH2HC = new double[test];
            long[] tiempoH1HC = new long[test];
            long[] tiempoH2HC = new long[test];

            // Simulated Annealing
            int[] beneficiosH1SA = new int[test];
            int[] beneficiosH2SA = new int[test];
            double[] distanciaH1SA = new double[test];
            double[] distanciaH2SA = new double[test];
            long[] tiempoH1SA = new long[test];
            long[] tiempoH2SA = new long[test];


            // Empezamos solucion inicial ------------------------------
            for (int i = 0; i < test; ++i) {
                System.out.println(i);
                for (int j = 0; j < test; ++j) {
                    System.out.println(String.format("Iteracio: '%s'", j));
                    int semilla = random.nextInt();

                    long startTimeGenerator = System.currentTimeMillis(); // START TIMER GENERATOR
                    BicingSolution solucionInicial = new BicingSolution(numeroEstaciones, numeroBicisTotal,
                            numeroFurgonetas, tipoDemanda, semilla);
                    solucionInicial.generadorSolucion1();
                    long endTimeGenerator = System.currentTimeMillis(); // END TIMER GENERATOR

                    long startTimeSearch = System.currentTimeMillis();
                    Pair<Integer, Double> values = Bicing_Search(solucionInicial, 0, 0);
                    long endTimeSearch = System.currentTimeMillis();
                    beneficiosH1HC[i] += values.getKey();
                    distanciaH1HC[i] += values.getValue();
                    tiempoH1HC[i] += ((endTimeGenerator - startTimeGenerator) + (endTimeSearch - startTimeSearch));

                    startTimeSearch = System.currentTimeMillis();
                    values = Bicing_Search(solucionInicial, 0, 1);
                    endTimeSearch = System.currentTimeMillis();
                    beneficiosH2HC[i] += values.getKey();
                    distanciaH2HC[i] += values.getValue();
                    tiempoH2HC[i] += ((endTimeGenerator - startTimeGenerator) + (endTimeSearch - startTimeSearch));

                    startTimeSearch = System.currentTimeMillis();
                    values = Bicing_Search(solucionInicial, 1, 0);
                    endTimeSearch = System.currentTimeMillis();
                    beneficiosH1SA[i] += values.getKey();
                    distanciaH1SA[i] += values.getValue();
                    tiempoH1SA[i] = ((endTimeGenerator - startTimeGenerator) + (endTimeSearch - startTimeSearch));

                    startTimeSearch = System.currentTimeMillis();
                    values = Bicing_Search(solucionInicial, 1, 1);
                    endTimeSearch = System.currentTimeMillis();
                    beneficiosH2SA[i] += values.getKey();
                    distanciaH2SA[i] += values.getValue();
                    tiempoH2SA[i] = ((endTimeGenerator - startTimeGenerator) + (endTimeSearch - startTimeSearch));
                }

                beneficiosH1HC[i] /= 10;
                distanciaH1HC[i] /= 10.0;
                tiempoH1HC[i] /= 10;

                beneficiosH2HC[i] /= 10;
                distanciaH2HC[i] /= 10.0;
                tiempoH2HC[i] /= 10;

                beneficiosH1SA[i] /= 10;
                distanciaH1SA[i] /= 10.0;
                tiempoH1SA[i] /= 10;

                beneficiosH2SA[i] /= 10;
                distanciaH2SA[i] /= 10.0;
                tiempoH2SA[i] /= 10;
            }


            // CSV WRITER
            String filenameHC = "hillClimbing";
            String filenameSA = "simulatedAnnealing";

            String pathnameHC = "/Users/luisoriolsolercruz/Documents/GitKraken/BicingProject/src/main/resources/" + filenameHC + ".csv";
            String pathnameSA = "/Users/luisoriolsolercruz/Documents/GitKraken/BicingProject/src/main/resources/" + filenameSA + ".csv";

            File csvFileHC = new File(pathnameHC);
            File csvFileSA = new File(pathnameSA);
            FileWriter writerHC = new FileWriter(csvFileHC);
            FileWriter writerSA = new FileWriter(csvFileSA);


            CSVUtils.writeLine(writerHC, Arrays.asList(filenameHC));
            CSVUtils.writeLine(writerHC, Arrays.asList("Experiment", "Beneficis", "Distancia", "Temps (ms)", "Beneficis", "Distancia", "Temps (ms)"));
            CSVUtils.writeLine(writerSA, Arrays.asList(filenameSA));
            CSVUtils.writeLine(writerSA, Arrays.asList("Experiment", "Beneficis", "Distancia", "Temps (ms)", "Beneficis", "Distancia", "Temps (ms)"));

            int beneficiosAcumuladosH1HC = 0;
            int beneficiosAcumuladosH2HC = 0;
            double distanciaAcumuladaH1HC = 0.0;
            double distanciaAcumuladaH2HC = 0.0;
            long tiempoAcumuladoH1HC = 0;
            long tiempoAcumuladoH2HC = 0;

            int beneficiosAcumuladosH1SA = 0;
            int beneficiosAcumuladosH2SA = 0;
            double distanciaAcumuladaH1SA = 0.0;
            double distanciaAcumuladaH2SA = 0.0;
            long tiempoAcumuladoH1SA = 0;
            long tiempoAcumuladoH2SA = 0;

            for (int i = 0; i < test; ++i) {
                System.out.println(String.format("Escrivint linea: '%s'", i));
                CSVUtils.writeLine(writerHC, Arrays.asList(Integer.toString(i + 1),
                        Integer.toString(beneficiosH1HC[i]), Double.toString(distanciaH1HC[i]), Long.toString(tiempoH1HC[i]),
                        Integer.toString(beneficiosH2HC[i]), Double.toString(distanciaH2HC[i]), Long.toString(tiempoH2HC[i])));

                CSVUtils.writeLine(writerSA, Arrays.asList(Integer.toString(i + 1),
                        Integer.toString(beneficiosH1SA[i]), Double.toString(distanciaH1SA[i]), Long.toString(tiempoH1SA[i]),
                        Integer.toString(beneficiosH2SA[i]), Double.toString(distanciaH2SA[i]), Long.toString(tiempoH2SA[i])));

                beneficiosAcumuladosH1HC += beneficiosH1HC[i];
                distanciaAcumuladaH1HC += distanciaH1HC[i];
                tiempoAcumuladoH1HC += tiempoH1HC[i];

                beneficiosAcumuladosH2HC += beneficiosH2HC[i];
                distanciaAcumuladaH2HC += distanciaH2HC[i];
                tiempoAcumuladoH2HC += tiempoH2HC[i];

                beneficiosAcumuladosH1SA += beneficiosH1SA[i];
                distanciaAcumuladaH1SA += distanciaH1SA[i];
                tiempoAcumuladoH1SA += tiempoH1SA[i];

                beneficiosAcumuladosH2SA += beneficiosH2SA[i];
                distanciaAcumuladaH2SA += distanciaH2SA[i];
                tiempoAcumuladoH2SA += tiempoH2SA[i];
            }

            double mediaBeneficiosH1HC = (double) beneficiosAcumuladosH1HC / 10.0;
            double mediaBeneficiosH2HC = (double) beneficiosAcumuladosH2HC / 10.0;
            double mediaBeneficiosH1SA = (double) beneficiosAcumuladosH1SA / 10.0;
            double mediaBeneficiosH2SA = (double) beneficiosAcumuladosH2SA / 10.0;

            double mediaDistanciaH1HC = distanciaAcumuladaH1HC / 10.0;
            double mediaDistanciaH2HC = distanciaAcumuladaH2HC / 10.0;
            double mediaDistanciaH1SA = distanciaAcumuladaH1SA / 10.0;
            double mediaDistanciaH2SA = distanciaAcumuladaH2SA / 10.0;

            double mediaTiempoH1HC = (double) tiempoAcumuladoH1HC / 10.0;
            double mediaTiempoH2HC = (double) tiempoAcumuladoH2HC / 10.0;
            double mediaTiempoH1SA = (double) tiempoAcumuladoH1SA / 10.0;
            double mediaTiempoH2SA = (double) tiempoAcumuladoH2SA / 10.0;

            double desvTipusBeneficiosH1HC = 0.0;
            double desvTipusBeneficiosH2HC = 0.0;
            double desvTipusBeneficiosH1SA = 0.0;
            double desvTipusBeneficiosH2SA = 0.0;

            double desvTipusDistanciaH1HC = 0.0;
            double desvTipusDistanciaH2HC = 0.0;
            double desvTipusDistanciaH1SA = 0.0;
            double desvTipusDistanciaH2SA = 0.0;

            double desvTipusTiempoH1HC = 0.0;
            double desvTipusTiempoH2HC = 0.0;
            double desvTipusTiempoH1SA = 0.0;
            double desvTipusTiempoH2SA = 0.0;

            for (int i = 0; i < test; ++i) {
                System.out.println(String.format("Desv tipus: '%s'", i));
                desvTipusBeneficiosH1HC += Math.pow((beneficiosH1HC[i] - mediaBeneficiosH1HC), 2);
                desvTipusBeneficiosH2HC += Math.pow((beneficiosH2HC[i] - mediaBeneficiosH2HC), 2);
                desvTipusBeneficiosH1SA += Math.pow((beneficiosH1SA[i] - mediaBeneficiosH1SA), 2);
                desvTipusBeneficiosH2SA += Math.pow((beneficiosH2SA[i] - mediaBeneficiosH2SA), 2);

                desvTipusDistanciaH1HC += Math.pow((distanciaH1HC[i] - mediaDistanciaH1HC), 2);
                desvTipusDistanciaH2HC += Math.pow((distanciaH2HC[i] - mediaDistanciaH2HC), 2);
                desvTipusDistanciaH1SA += Math.pow((distanciaH1SA[i] - mediaDistanciaH1SA), 2);
                desvTipusDistanciaH2SA += Math.pow((distanciaH2SA[i] - mediaDistanciaH2SA), 2);

                desvTipusTiempoH1HC += Math.pow((tiempoH1HC[i] - mediaTiempoH1HC), 2);
                desvTipusTiempoH2HC += Math.pow((tiempoH2HC[i] - mediaTiempoH2HC), 2);
                desvTipusTiempoH1SA += Math.pow((tiempoH1SA[i] - mediaTiempoH1SA), 2);
                desvTipusTiempoH2SA += Math.pow((tiempoH2SA[i] - mediaTiempoH2SA), 2);
            }

            desvTipusBeneficiosH1HC = sqrt(desvTipusBeneficiosH1HC / test);
            desvTipusBeneficiosH2HC = sqrt(desvTipusBeneficiosH2HC / test);
            desvTipusBeneficiosH1SA = sqrt(desvTipusBeneficiosH1SA / test);
            desvTipusBeneficiosH2SA = sqrt(desvTipusBeneficiosH2SA / test);

            desvTipusDistanciaH1HC = sqrt(desvTipusDistanciaH1HC / test);
            desvTipusDistanciaH2HC = sqrt(desvTipusDistanciaH2HC / test);
            desvTipusDistanciaH1SA = sqrt(desvTipusDistanciaH1SA / test);
            desvTipusDistanciaH2SA = sqrt(desvTipusDistanciaH2SA / test);

            desvTipusTiempoH1HC = sqrt(desvTipusTiempoH1HC / test);
            desvTipusTiempoH2HC = sqrt(desvTipusTiempoH2HC / test);
            desvTipusTiempoH1SA = sqrt(desvTipusTiempoH1SA / test);
            desvTipusTiempoH2SA = sqrt(desvTipusTiempoH2SA / test);

            desvTipusBeneficiosH1HC = BigDecimal.valueOf(desvTipusBeneficiosH1HC).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusBeneficiosH2HC = BigDecimal.valueOf(desvTipusBeneficiosH2HC).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusBeneficiosH1SA = BigDecimal.valueOf(desvTipusBeneficiosH1SA).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusBeneficiosH2SA = BigDecimal.valueOf(desvTipusBeneficiosH2SA).setScale(3, RoundingMode.HALF_UP).doubleValue();

            desvTipusDistanciaH1HC = BigDecimal.valueOf(desvTipusDistanciaH1HC).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusDistanciaH2HC = BigDecimal.valueOf(desvTipusDistanciaH2HC).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusDistanciaH1SA = BigDecimal.valueOf(desvTipusDistanciaH1SA).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusDistanciaH2SA = BigDecimal.valueOf(desvTipusDistanciaH2SA).setScale(3, RoundingMode.HALF_UP).doubleValue();

            desvTipusTiempoH1HC = BigDecimal.valueOf(desvTipusTiempoH1HC).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusTiempoH2HC = BigDecimal.valueOf(desvTipusTiempoH2HC).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusTiempoH1SA = BigDecimal.valueOf(desvTipusTiempoH1SA).setScale(3, RoundingMode.HALF_UP).doubleValue();
            desvTipusTiempoH2SA = BigDecimal.valueOf(desvTipusTiempoH2SA).setScale(3, RoundingMode.HALF_UP).doubleValue();

            CSVUtils.writeLine(writerHC, Arrays.asList("Mitjana (desv. típica)",
                    mediaBeneficiosH1HC + " (" + desvTipusBeneficiosH1HC + ")", mediaDistanciaH1HC + " (" + desvTipusDistanciaH1HC + ")", mediaTiempoH1HC + " (" + desvTipusTiempoH1HC + ")",
                    mediaBeneficiosH2HC + " (" + desvTipusBeneficiosH2HC + ")", mediaDistanciaH2HC + " (" + desvTipusDistanciaH2HC + ")", mediaTiempoH2HC + " (" + desvTipusTiempoH2HC + ")"));

            CSVUtils.writeLine(writerSA, Arrays.asList("Mitjana (desv. típica)",
                    mediaBeneficiosH1SA + " (" + desvTipusBeneficiosH1SA + ")", mediaDistanciaH1SA + " (" + desvTipusDistanciaH1SA + ")", mediaTiempoH1SA + " (" + desvTipusTiempoH1SA + ")",
                    mediaBeneficiosH2SA + " (" + desvTipusBeneficiosH2SA + ")", mediaDistanciaH2SA + " (" + desvTipusDistanciaH2SA + ")", mediaTiempoH2SA + " (" + desvTipusTiempoH2SA + ")"));

            writerHC.flush();
            writerHC.close();

            writerSA.flush();
            writerSA.close();

            end = 1;
        }
    }

    private static Pair<Integer, Double> Bicing_Search(BicingSolution solution, int algoritmoSeleccionado, int heuristicoSeleccionado) {
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

            BicingSolution goalSolution = ((BicingSolution) search.getGoalState());

            return new Pair(goalSolution.getBeneficioPorAcierto() - goalSolution.getPenalizacionPorFallo(), goalSolution.getDistanciaRecorrida());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
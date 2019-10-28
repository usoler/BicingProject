import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BicingSuccessorFunction2 implements SuccessorFunction {
    private static final int NUM_OF_OPERATORS = 4;
    private static final int NUM_MAX_DESTINOS = 2;
    private static final int NUM_MAX_BICIS = 30;

    @Override
    public List getSuccessors(Object state) {
        ArrayList<Successor> successorRandom = new ArrayList<>();
        BicingSolution solution = (BicingSolution) state;
        BicingSolution nuevaSolution = new BicingSolution(solution);
        Random generatorRandom = new Random();

        int contador = 10; // Para salir de un bucle despues de 1000 iteraciones

        int operadorRandom;
        if (solution.getEstaUsandoConjuntoOperadoresOptimo()) {
            operadorRandom = generatorRandom.nextInt(NUM_OF_OPERATORS - 1);
        } else {
            operadorRandom = generatorRandom.nextInt(NUM_OF_OPERATORS);
        }

        int idFurgonetaRandom;
        int idEstacionRandom;
        String actionMessage;
        switch (operadorRandom) {
            case 0: // moverFurgoneta
                idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                idEstacionRandom = generatorRandom.nextInt(solution.getEstaciones().size());

                while ((contador > 0) && (!nuevaSolution.moverFurgoneta(idFurgonetaRandom, idEstacionRandom))) { // En caso de no ser un sucesor valido, buscamnos otro
                    nuevaSolution = new BicingSolution(solution);
                    idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                    idEstacionRandom = generatorRandom.nextInt(solution.getEstaciones().size());
                    --contador;
                }

                actionMessage = String.format("Furgoneta con id = '%s' movida a estacion con id = '%s'",
                        idFurgonetaRandom, idEstacionRandom);
                successorRandom.add(new Successor(actionMessage, nuevaSolution));
                break;

            case 1: // intercambiarFurgonetas
                idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                int idFurgonetaAIntercambiarRandom = generatorRandom.nextInt(solution.getAsignaciones().length);

                while ((contador > 0) && (!nuevaSolution.intercambiarFurgonetas(idFurgonetaRandom, idFurgonetaAIntercambiarRandom))) {
                    nuevaSolution = new BicingSolution(solution);
                    idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                    idFurgonetaAIntercambiarRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                    --contador;
                }

                actionMessage = String.format("Furgoneta con id = '%s' intercambiada por furgoneta " +
                        "con id = '%s'", idFurgonetaRandom, idFurgonetaAIntercambiarRandom);
                successorRandom.add(new Successor(actionMessage, nuevaSolution));
                break;

            case 2: // cargarFurgoneta
                idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                int numPrimerasBicisRandom = generatorRandom.nextInt(NUM_MAX_BICIS);
                int numSegundasBicisRandom = generatorRandom.nextInt(NUM_MAX_BICIS);

                while ((contador > 0) && (!nuevaSolution.cargarFurgoneta(idFurgonetaRandom, numPrimerasBicisRandom, numSegundasBicisRandom))) {
                    nuevaSolution = new BicingSolution(solution);
                    idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                    numPrimerasBicisRandom = generatorRandom.nextInt(NUM_MAX_BICIS);
                    numSegundasBicisRandom = generatorRandom.nextInt(NUM_MAX_BICIS);
                    --contador;
                }

                if (numSegundasBicisRandom == 0) { // Para mostrar el mensaje correcto del swap
                    numPrimerasBicisRandom = numSegundasBicisRandom;
                    numSegundasBicisRandom = 0;
                }

                actionMessage = String.format("Furgoneta con id = '%s' cargada con '%s' bicis " +
                                "para el destino1 y '%s' para el destino2",
                        idFurgonetaRandom, numPrimerasBicisRandom, numSegundasBicisRandom);
                successorRandom.add(new Successor(actionMessage, nuevaSolution));
                break;

            case 3: // cambiarEstacionDestino
                idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                idEstacionRandom = generatorRandom.nextInt(solution.getEstaciones().size());
                int destinoUnoODos = generatorRandom.nextInt(NUM_MAX_DESTINOS);

                while ((contador > 0) && (!nuevaSolution.cambiarEstacionDestino(idFurgonetaRandom, destinoUnoODos, idEstacionRandom))) {
                    nuevaSolution = new BicingSolution(solution);
                    idFurgonetaRandom = generatorRandom.nextInt(solution.getAsignaciones().length);
                    idEstacionRandom = generatorRandom.nextInt(solution.getEstaciones().size());
                    destinoUnoODos = generatorRandom.nextInt(NUM_MAX_DESTINOS);
                    --contador;
                }

                actionMessage = String.format("Furgoneta con id = '%s', y origen '%s', cambiado el destino '%s' " +
                                "al destino con id = '%s'", idFurgonetaRandom,
                        nuevaSolution.getAsignaciones()[idFurgonetaRandom], destinoUnoODos + 1, idEstacionRandom);
                successorRandom.add(new Successor(actionMessage, nuevaSolution));
                break;
            default:
                break;
        }

        return successorRandom;
    }
}

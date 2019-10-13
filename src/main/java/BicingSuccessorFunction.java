import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

public class BicingSuccessorFunction implements SuccessorFunction {
    private static final int NUM_MAX_DESTINOS = 2;

    @Override
    public List getSuccessors(Object state) {
        ArrayList<Successor> successors = new ArrayList<>();
        BicingSolution solution = (BicingSolution) state;

        int numFurgonetas = solution.getAsignaciones().length;
        int numEstaciones = solution.getEstaciones().size();

        for (int i = 0; i < numFurgonetas; ++i) { // O(|F|) * O(|F| + |E|)
            // Sucesores generados por el operador 'moverFurgoneta'
            for (int j = 0; j < numEstaciones; ++j) { // O(|E|)
                BicingSolution nuevaSolution = new BicingSolution(solution);
                if (nuevaSolution.moverFurgoneta(i, j)) {
                    String actionMessage = String.format("Furgoneta con id = '%s' movida a estacion con id = '%s'",
                            i, j);
                    successors.add(new Successor(actionMessage, nuevaSolution));
                }
            }

            // Sucesores generados por el operador 'cambiarEstacionDestino'
            for (int j = 0; j < numEstaciones; ++j) { // O(|E|)
                for (int k = 0; k < NUM_MAX_DESTINOS; ++k) {
                    BicingSolution nuevaSolution = new BicingSolution(solution);
                    if (nuevaSolution.cambiarEstacionDestino(i, k, j)) {
                        String actionMessage = String.format("Furgoneta con id = '%s' cambiado el destino '%s' " +
                                "al destino con id = '%s'", i, k + 1, j);
                        successors.add(new Successor(actionMessage, nuevaSolution));
                    }
                }
            }

            // Sucesores generados por el operador 'intercambiarFurgonetas'
            for (int j = i + 1; j < numFurgonetas; ++j) { // O(|F|)
                BicingSolution nuevaSolution = new BicingSolution(solution);
                if (nuevaSolution.intercambiarFurgonetas(i, j)) {
                    String actionMessage = String.format("Furgoneta con id = '%s' intercambiada por furgoneta " +
                            "con id = '%s'", i, j);
                    successors.add(new Successor(actionMessage, nuevaSolution));
                }
            }
        }

        return successors;
    }
}

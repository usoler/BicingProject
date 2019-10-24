import aima.search.framework.Successor;
import aima.search.framework.SuccessorFunction;

import java.util.ArrayList;
import java.util.List;

public class BicingSuccessorFunction implements SuccessorFunction {
    private static final int NUM_MAX_DESTINOS = 2;
    private static final int NUM_MAX_BICIS = 30;

    @Override
    public List getSuccessors(Object state) {
        ArrayList<Successor> successors = new ArrayList<>();
        BicingSolution solution = (BicingSolution) state;

        int numFurgonetas = solution.getAsignaciones().length;
        int numEstaciones = solution.getEstaciones().size();

        boolean[] estacionesOcupadas = new boolean[numEstaciones];
        estacionesOcupadas = calcularEstacionesOcupadas(estacionesOcupadas, solution.getAsignaciones()); // O(F)
        int contadorEspacio = 0;

        for (int i = 0; i < numFurgonetas; ++i) { // O(|F|) * O(|F| + |E|)
            // Sucesores generados por el operador 'moverFurgoneta'
            for (int j = 0; j < numEstaciones; ++j) { // O(|E|)
                ++contadorEspacio;
                BicingSolution nuevaSolution = new BicingSolution(solution);
                if ((!estacionesOcupadas[j]) && (nuevaSolution.moverFurgoneta(i, j))) { // O(1)
                    String actionMessage = String.format("Furgoneta con id = '%s' movida a estacion con id = '%s'",
                            i, j);
                    successors.add(new Successor(actionMessage, nuevaSolution));
                }
            }

            // Sucesores generados por el operador 'cambiarEstacionDestino'
            for (int j = 0; j < numEstaciones; ++j) { // O(|E|)
                for (int k = 0; k < NUM_MAX_DESTINOS; ++k) {
                    ++contadorEspacio;
                    BicingSolution nuevaSolution = new BicingSolution(solution);
                    if (nuevaSolution.cambiarEstacionDestino(i, k, j)) {
                        String actionMessage = String.format("Furgoneta con id = '%s', y origen '%s', cambiado el destino '%s' " +
                                "al destino con id = '%s'", i, nuevaSolution.getAsignaciones()[i], k + 1, j);
                        successors.add(new Successor(actionMessage, nuevaSolution));
                    }
                }
            }

            // Sucesores generados por el operador 'intercambiarFurgonetas'
            for (int j = i + 1; j < numFurgonetas; ++j) { // O(|F|)
                ++contadorEspacio;
                BicingSolution nuevaSolution = new BicingSolution(solution);
                if (nuevaSolution.intercambiarFurgonetas(i, j)) {
                    String actionMessage = String.format("Furgoneta con id = '%s' intercambiada por furgoneta " +
                            "con id = '%s'", i, j);
                    successors.add(new Successor(actionMessage, nuevaSolution));
                }
            }

            // Sucesores generados por el operador 'cargarFurgoneta'
            for (int j = 0; j <= NUM_MAX_BICIS; ++j) {
                for (int k = 0; k <= NUM_MAX_BICIS; ++k) {
                    ++contadorEspacio;
                    BicingSolution nuevaSolution = new BicingSolution(solution);
                    if (nuevaSolution.cargarFurgoneta(i, j, k)) {
                        String actionMessage = String.format("Furgoneta con id = '%s' cargada con '%s' bicis " +
                                "para el destino1 y '%s' para el destino2", i, j, k);
                        successors.add(new Successor(actionMessage, nuevaSolution));
                    }
                }
            }
        }

        printSuccessors(successors);

        System.out.println("--------------------------------------------------------");
        System.out.println("--------------------------------------------------------");
        System.out.println(String.format("Numero de espacio: %s", contadorEspacio));
        System.out.println(String.format("Numero de sucesores: %s", successors.size()));
        System.out.println("--------------------------------------------------------");
        System.out.println("--------------------------------------------------------");

        return successors;
    }

    private boolean[] calcularEstacionesOcupadas(boolean[] estacionesOcupadas, int[] asignaciones) { // O(|F|)
        for (int i = 0; i < asignaciones.length; ++i) {
            if (asignaciones[i] != -1) {
                estacionesOcupadas[asignaciones[i]] = true;
            }
        }

        return estacionesOcupadas;
    }

    private void printSuccessors(ArrayList<Successor> successors) {
        for (int i = 0; i < successors.size(); ++i) {
            System.out.println("***********************************");
            System.out.println(String.format("Sucesor: '%s'", i));
            Successor successor = successors.get(i);
            BicingSolution solution = (BicingSolution) successor.getState();
            System.out.println(String.format("Beneficios sucesor: '%s'", solution.getBeneficioPorAcierto() - solution.getPenalizacionPorFallo()));
            System.out.println(String.format("Coste transporte sucesor: '%s'", solution.getCosteTransporte()));
            System.out.println("***********************************");
        }
    }
}
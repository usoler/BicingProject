import aima.search.framework.HeuristicFunction;

// Tiene en cuenta MAXIMIZAR los beneficios obtenidos al trasladar bicis
public class BicingHeuristicFunction1 implements HeuristicFunction {
    @Override
    public double getHeuristicValue(Object state) {
        BicingSolution solution = (BicingSolution) state;

        return (solution.getPenalizacionPorFallo() - 10 * solution.getBeneficioPorAcierto());
    }
}
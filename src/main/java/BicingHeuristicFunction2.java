import aima.search.framework.HeuristicFunction;

// Tiene en cuenta MAXIMIZAR los beneficios obtenidos al trasladar bicis - los costes de transporte
public class BicingHeuristicFunction2 implements HeuristicFunction {
    @Override
    public double getHeuristicValue(Object state) {
        BicingSolution solution = (BicingSolution) state;

        return (solution.getCosteTransporte() - (solution.getBeneficioPorAcierto() - solution.getPenalizacionPorFallo())); // TODO: ponderar
        // return (2 * solution.getCosteTransporte() - (solution.getBeneficioPorAcierto() - solution.getPenalizacionPorFallo()));       // Mejora 1
        // return (2 * solution.getCosteTransporte() - (2 * solution.getBeneficioPorAcierto() - solution.getPenalizacionPorFallo()));   // Mejora 2
    }
}
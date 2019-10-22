import aima.search.framework.HeuristicFunction;

// Tiene en cuenta MAXIMIZAR los beneficios obtenidos al trasladar bicis - los costes de transporte
public class BicingHeuristicFunction2 implements HeuristicFunction {
    @Override
    public double getHeuristicValue(Object state) {
        BicingSolution solution = (BicingSolution) state;

        int beneficios = 0;

        if (solution.getBeneficios() < 0) {
            beneficios = Math.abs(solution.getBeneficios());
        } else {
            beneficios = -solution.getBeneficios();
        }
        double costes = 0;
        if (solution.getCosteTransporte() < 0) {
            costes = Math.abs(solution.getCosteTransporte());
        } else {
            costes = -solution.getCosteTransporte();
        }
        return beneficios - costes;
    }
}
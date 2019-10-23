import aima.search.framework.HeuristicFunction;

// Tiene en cuenta MAXIMIZAR los beneficios obtenidos al trasladar bicis
public class BicingHeuristicFunction1 implements HeuristicFunction {
    @Override
    public double getHeuristicValue(Object state) {
        BicingSolution solution = (BicingSolution) state;

        int beneficios = 0;

      /*  if (solution.getBeneficios() < 0) {
            beneficios = Math.abs(solution.getBeneficios());
        } else {
            beneficios = -solution.getBeneficios();
        }*/

        return beneficios;
    }
}
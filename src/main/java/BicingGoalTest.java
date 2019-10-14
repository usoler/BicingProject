import aima.search.framework.GoalTest;

public class BicingGoalTest implements GoalTest {
    @Override
    public boolean isGoalState(Object state) {
        BicingSolution solution = (BicingSolution) state;
        return (solution.isGoalState());
    }
}

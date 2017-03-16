package TaskManager;

import UnitManagement.ScoutingUnit;
import UnitManagement.UnitManager;
import bwapi.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author vahanito
 * @since 16.01.2017
 */
public class TaskManager {

    private List<Task> tasks;

    private void orderTasksForOptimalValue(ScoutingUnit unit) {
        /* ToDo: calculateTravelTime by mala byt funkcia MapManagera. V UnitManageri som ju ani nenasiel
        if (tasks.isEmpty()) {
            return;
        }
        double maxValue = Double.MIN_VALUE;
        List<List<Task>> allPermutations = listPermutations(tasks);
        for (List<Task> permutation : allPermutations) {
            double currValue = 0;
            int ticks = 0;
            Position startingPosition = unit.getUnit().getPosition();
            for (Task task : permutation) {
                double travelTime = UnitManager.calculateTravelTime(unit, startingPosition, task.getPosition());
                currValue = task.functionValue(ticks, travelTime);
                ticks += travelTime;
                startingPosition = task.getPosition();
            }
            if (currValue > maxValue) {
                tasks = permutation;
            }
        }
        */
    }

    public static List<List<Task>> listPermutations(List<Task> elementsList) {
        if (elementsList.size() == 0) {
            List<List<Task>> result = Collections.emptyList();
            return result;
        }

        List<List<Task>> combinationList = new ArrayList<>();
        Task firstElement = elementsList.remove(0);

        List<List<Task>> recursiveReturn = listPermutations(elementsList);
        for (List<Task> list : recursiveReturn) {
            for (int i = 0; i <= list.size(); i++) {
                List<Task> temp = new ArrayList<>(list);
                temp.add(i, firstElement);
                combinationList.add(temp);
            }
        }
        return combinationList;
    }
}

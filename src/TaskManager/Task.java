package TaskManager;

import UnitManagement.ScoutingUnit;
import UnitManagement.UnitManager;
import bwapi.Position;

/**
 * @author vahanito
 * @since 16.01.2017
 */
public class Task {

    private int priority;
    private int priorityChange;
    private ScoutingUnit assignedUnit;
    private Position position;
    private boolean inProgress;
    private boolean cyclicTask;
    private int numberOfAttempts;

    public Task(int priority, int priorityChange, Position position, boolean cyclicTask) {
        this.priority = priority;
        this.priorityChange = priorityChange;
        this.position = position;
        this.cyclicTask = cyclicTask;
        this.assignedUnit = null;
        this.inProgress = false;
        this.numberOfAttempts = 0;
    }

    public double functionValue(int ticks) {
        /* ToDo: calculateTravelTime by mala byt funkcia MapManagera, v UnitManageri som ju ani nenasiel
        int priorityValueInTicksTime = priority + priorityChange*ticks;
        double travelTime = UnitManager.calculateTravelTime(assignedUnit, assignedUnit.getUnit().getPosition(), position);
        //double travelTime = position.getDistance(assignedUnit.getUnit().getPoint())/assignedUnit.getUnit().getVelocityX();
        return priorityValueInTicksTime - travelTime;
        */
        return -1;
    }

    public double functionValue(int ticks, double travelTime) {
        int priorityValueInTicksTime = priority + priorityChange*ticks;
        return priorityValueInTicksTime - travelTime;
    }

    public Position getPosition() {
        return position;
    }

    public void assignUnit(ScoutingUnit unit) {
        this.assignedUnit = unit;
    }
}

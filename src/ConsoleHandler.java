import MODQlearning.QExecutor;
import MODaStar.AStarModule;
import ScoutModule.Scout_module;
import UnitManagement.ActionManager;

/**
 * Created by Silent1 on 09.01.2017.
 */
public class ConsoleHandler {

    Scout_module scoutBot;

    public ConsoleHandler(Scout_module pScoutBot) {
        scoutBot=pScoutBot;
    }

    /**
     * In-game commands used for test and demonstration
     *
     * @param pMessage
     */
    public void messageHandler(String pMessage) {
        switch (pMessage) {
            case "add": addScoutingUnit();
                break;
            case "scbase": scoutBase_selectedUnits();
                break;
            case "rethome": returnHome_selectedUnits();
                break;
            case "qrun": QExecutor.EXECUTE=true;
        }
    }

    public void addScoutingUnit() {
        scoutBot.getUnitManager().addSelectedUnit();
    }

    public void scoutBase_selectedUnits() {
        scoutBot.getActionManager().scoutBase_selectedUnits(scoutBot.getUnitManager().getAllScoutingUnits());
    }

    public void returnHome_selectedUnits() {
        scoutBot.getActionManager().returnHome_selectedUnits(scoutBot.getUnitManager().getAllScoutingUnits());
    }

}

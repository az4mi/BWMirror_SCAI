import MODQlearning.QExecutor;
import MODaStar.AStarModule;
import MapManager.MapManager;
import ScoutModule.Scout_module;
import UnitManagement.ActionManager;
import bwapi.Position;
import bwapi.Unit;

/**
 * Created by Silent1 on 09.01.2017.
 */
public class ConsoleHandler {

    Scout_module scoutBot;
    MapManager mapManager;

    public ConsoleHandler(Scout_module pScoutBot,MapManager mapManager) {
        scoutBot=pScoutBot;
        this.mapManager = mapManager;
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
            break;
            case "layeradd": mapManager.addLayersMethod();
            break;
            case "calc":
                Unit u = scoutBot.getUnitManager().getAllScoutingUnits().get(0).getUnit();
                int frames = scoutBot.getMapManager().calculateFrames(u,u.getPosition(),new Position(u.getPosition().getX(),u.getPosition().getY()+100));
                System.out.println("frames: "+frames);
                break;
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

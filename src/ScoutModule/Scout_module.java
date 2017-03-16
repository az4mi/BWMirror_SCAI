package ScoutModule;

import MODQlearning.QExecutor;
import UnitManagement.*;
import bwapi.*;
import MapManager.*;
import bwta.BWTA;

import java.util.Calendar;
import java.util.Random;

/**
 * Created by Silent1 on 25.10.2016.
 */
public class Scout_module {

    public static boolean DEBUG=false;

    public static final int SAFETY_LEVEL=2;

    public static final int MAP_REFRESH_FRAME_COUNT=30;

    public static final int UNIT_DANGERCHECK_FRAME_COUNT=33;

    private Game game;
    private UnitManager unitManager;
    private MapManager mapManager;
    private ActionManager actionManager;

    public Scout_module(Game pGame) {
        game=pGame;
        unitManager=new UnitManager(game);
        mapManager=new MapManager(game);
        actionManager=new ActionManager(mapManager,game);
    }

    public void onStart() {
        initializeModules();
    }

    public void onFrame() {

//        /* AUTOTEST */
//        if(game.getFrameCount()==70) {
//            System.out.println("Starting initializations...");
//            TEST_initializeAll();
//            System.out.println("Initializations done !");
//        }
//        if(game.getFrameCount()==90) {
//            System.out.println("Scouting base...");
//            TEST_scoutBase();
//        }

        manageAll();
        drawAll();
    }

    public void initializeModules() {
        mapManager.initializeAll();
    }

    public void TEST_initializeAll() {
        mapManager.initializeAll();
        unitManager.TEST_initScoutingUnits();
    }

    public void TEST_scoutBase() {
        System.out.println("Scouting unit = "+unitManager.getGroundScoutingUnits().get(0).getUnit().getType().toString());
        actionManager.scoutBase(unitManager.getGroundScoutingUnits().get(0));
    }





    public void drawAll() {
        mapManager.drawAll();
        unitManager.drawAll();
    }

    public void manageAll() {
        mapManager.manageAll(game);
        unitManager.manageAll(mapManager);
    }

    //Test public
    public void helloWorld(Game pGame) {
        Random rnd=new Random();
        int x;
        int y;
        for(Unit u:pGame.getAllUnits()) {
            if(u.canMove()&&!u.isMoving()) {
                x=rnd.nextInt(5000);
                y=rnd.nextInt(5000);
                u.move(new Position(x,y),false);
            }
        }
    }





    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public UnitManager getUnitManager() {
        return unitManager;
    }

    public void setUnitManager(UnitManager unitManager) {
        this.unitManager = unitManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public void setMapManager(MapManager mapManager) {
        this.mapManager = mapManager;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public void setActionManager(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

}

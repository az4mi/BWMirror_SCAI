package MODaStar;

import bwapi.*;

import java.util.List;

/**
 * Top-level class used to execute path builder (AStarPathCalculator) with given grid map
 *
 * Created by Misho on 27.1.2016.
 */
public class AStarModule {

    private GridMap gridMap;

    /* ------------------- Constructors ------------------- */

    public AStarModule() {

    }

    public AStarModule(GridMap pGridMap) {
        gridMap=pGridMap;
    }


    /* ------------------- main methods ------------------- */

    /**
     * Executes path calculator
     *
     * @param pStartPosition
     * @param pDestination
     * @param pStartingHealth
     * @param pLevelOfSafety
     * @param pAirPath
     * @param pGame
     * @param pColor
     * @return
     */
    public AStarPathCalculator buildPath(Position pStartPosition, Position pDestination, int pStartingHealth, int pLevelOfSafety, boolean pAirPath, Game pGame, Color pColor) {
        AStarPathCalculator p=new AStarPathCalculator(pStartPosition,pDestination,pStartingHealth,pLevelOfSafety,pAirPath,gridMap,pGame,pColor);
        p.start();
        return p;
    }

    /* ------------------- initialising methods ------------------- */

    public void initializeAll(Game game) {}

    /* ------------------- real-time management methods ------------------- */


    /* ------------------- data structure operation methods ------------------- */


    /* ------------------- other methods ------------------- */


    /* ------------------- Drawing functions ------------------- */

    public void drawAll(List<Unit> pUnits, Game pGame) {
        drawGridMap(Color.Blue,pGame);
        drawBlockUnderUnits(pUnits ,pGame);
    }

    public void drawGridMap(Color pColor, Game pGame) {
        gridMap.drawGridMap(pColor,pGame);
    }

    public void drawBlockUnderUnits(List<Unit> pUnits, Game pGame) {
        Block b;
        for(Unit u:pUnits) {
            b=gridMap.getBlockByPosition_blockMap(u.getPosition());
            b.drawBlock(Color.Orange, pGame);
        }
    }


    /* ------------------- Getters and Setters ------------------- */

    public GridMap getGridMap() {
        return gridMap;
    }

}

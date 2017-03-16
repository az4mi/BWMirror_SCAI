package MapManager;

import MODaStar.AStarModule;
import MODaStar.AStarPathCalculator;
import MODaStar.Block;
import MODaStar.GridMap;
import ScoutModule.Scout_module;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the GEO data, positions and fields
 */
public class MapManager {

    /**
     * Size of the edge of grid block
     */
    public static final int GRIDEDGESIZE=18; //18

    private AStarModule aStarModule;

    private HeatMap heatMap;

    /**
     * Postiton of my base
     */
    private Position myBasePosition;

    /**
     * List of enemy base positons
     */
    private PotentialField enemyBasePosition;

    /**
     * List of expansion positons
     */
    private ArrayList<PotentialField> expansionPositions;

    /**
     * List of danger fields
     */
    private ArrayList<PotentialField> dangerFields;

    /**
     * List of retreat fields
     */
    private ArrayList<PotentialField> retreatFields;

    private ArrayList<ScoutingArea> scoutingAreas;

    private ArrayList<ScoutingArea> armyArea;

    private AStarPathCalculator staticPathCalculator;

    private Game game;

    //private GraphicsExtended graphicsEx;


    /* ------------------- Constructors ------------------- */

    /**
     * Creates the instance of MapManager and initializes variables
     */
    public MapManager(Game pGame) {
        heatMap=new HeatMap(pGame);
        expansionPositions=new ArrayList<>();
        dangerFields=new ArrayList<>();
        retreatFields=new ArrayList<>();
        scoutingAreas=new ArrayList<>();
        game=pGame;
        aStarModule=new AStarModule(new GridMap(MapManager.GRIDEDGESIZE,game));
        myBasePosition=pGame.self().getStartLocation().toPosition();//BWTA.getStartLocation(game.self()).getPosition();
    }

    public MapManager(Game pGame, AStarModule pAStarModule, HeatMap pHeatMap) {
        expansionPositions=new ArrayList<>();
        dangerFields=new ArrayList<>();
        retreatFields=new ArrayList<>();
        scoutingAreas=new ArrayList<>();
        aStarModule=pAStarModule;
        heatMap=pHeatMap;
        game=pGame;
        myBasePosition=pGame.self().getStartLocation().toPosition();//BWTA.getStartLocation(game.self()).getPosition();
    }

    /**
     * Creates the instance of MapManager with given lists of enemy base positions and expansion positions
     *
     * @param pEnemyBasePositions
     * @param pExpansionPositions
     */
    public MapManager(ArrayList pEnemyBasePositions, ArrayList pExpansionPositions,Game pGame) {
        expansionPositions=pExpansionPositions;
        dangerFields=new ArrayList<>();
        retreatFields=new ArrayList<>();
        game=pGame;
        myBasePosition=pGame.self().getStartLocation().toPosition();//BWTA.getStartLocation(game.self()).getPosition();
    }


    /* ------------------- initialising methods ------------------- */

    public void initializeAll() {
        initializeHeatMap(game);
        initializeEnemyBasePosition();
        initializeAStarModule(game);
//        initializeScoutingAreas();
    }

    public void initializeScoutingAreas() {
        /* Initialize enemy base area */
        Position eBasePosition= enemyBasePosition.getPosition();
        PotentialField centerBlock=heatMap.getHeatBlockContainingPosition(eBasePosition);
        int cRow=centerBlock.getRow();
        int cCol=centerBlock.getColumn();
        ScoutingArea eBaseArea=new ScoutingArea();

        for(int i=cRow-1;i<=cRow+1;i++) {
            if(i>=0&&i<heatMap.getColumns()) {
                for(int j=cCol-1;j<=cCol+1;j++) {
                    if(j>=0&&j<heatMap.getColumns()) {
                        eBaseArea.insert(heatMap.getHeatBlock(i,j));
                    }
                }
            }
        }
        System.out.println("eBase area size = " + eBaseArea.size() + " block");
        for(PotentialField pf:eBaseArea.getFieldArray()) {
            System.out.println("Field ["+pf.getRow()+";"+pf.getColumn()+"]");
        }
        eBaseArea.setID(ScoutingArea.BASEAREA);
        scoutingAreas.add(eBaseArea);

//        if(staticPathCalculator==null) {
//            staticPathCalculator = buildPath(pGame.getAllUnits().firstOf(UnitType.Terran_SCV), enemyBasePosition.get(0).getPosition(), 1, false, pGame, BWColor.Green);
//        }

        //ToDo: initialization of other paths
    }

    public ScoutingArea getEnemyArmyArea() {
        ScoutingArea area=new ScoutingArea();
        PotentialField centerBlock;
        int cRow;
        int cCol;
        for(PotentialField pf:dangerFields) {
            centerBlock=heatMap.getHeatBlockContainingPosition(pf.getPosition());
            cRow=centerBlock.getRow();
            cCol=centerBlock.getColumn();
            for(int i=cRow-1;i<=cRow+1;i++) {
                if(i>=0&&i<heatMap.getColumns()) {
                    for(int j=cCol-1;j<=cCol+1;j++) {
                        if(j>=0&&j<heatMap.getColumns()) {
                            if(!area.getFieldArray().contains(heatMap.getHeatBlock(i,j))) {
                                area.insert(heatMap.getHeatBlock(i,j));
                            }
                        }
                    }
                }
            }
        }
        if(area.getFieldArray().size()>0) {
            return area;
        }
        return null;
    }

    public ScoutingArea getLeastVisitedEnemyArmyArea()  {
        ScoutingArea area=new ScoutingArea();
        PotentialField centerBlock;
        int cRow;
        int cCol;
        double heatLvl=Double.MIN_VALUE;
        int index=-1;
        for(int i=0;i<dangerFields.size();i++) {
            if(dangerFields.get(i).getHeat()>heatLvl) {
                heatLvl=dangerFields.get(i).getHeat();
                index=i;
            }

        }
        if(index!=-1) {
            centerBlock = heatMap.getHeatBlockContainingPosition(dangerFields.get(index).getPosition());
            cRow=centerBlock.getRow();
            cCol=centerBlock.getColumn();
            for(int i=cRow-1;i<=cRow+1;i++) {
                if(i>=0&&i<heatMap.getColumns()) {
                    for(int j=cCol-1;j<=cCol+1;j++) {
                        if(j>=0&&j<heatMap.getColumns()) {
                            if(!area.getFieldArray().contains(heatMap.getHeatBlock(i,j))) {
                                area.insert(heatMap.getHeatBlock(i,j));
                            }
                        }
                    }
                }
            }
        }
        if(area.getFieldArray().size()>0) {
            return area;
        }
        return null;
    }

    /**
     * Initializes chokepoints from the map
     *
     */
    public void initializeChokePoints() {
//        chokePoints=game.getMap().getChokePoints();
//
//        for(ChokePoint choke:chokePoints) {
//            /*CONSOLE LOG*/
//            System.out.println("Added chokepoint at position :"+choke.getCenter().toString());
//            /*END CONSOLE LOG*/
//        }

    }

    public void initializeEnemyBasePosition() {
        List<TilePosition> startPositions=game.getStartLocations();
        for(TilePosition tp: startPositions) {
            if(!game.isVisible(tp)) {
                enemyBasePosition=new PotentialField(game,tp.getX()*TilePosition.SIZE_IN_PIXELS,tp.getY()*TilePosition.SIZE_IN_PIXELS,150);
            }
        }
//        List<BaseLocation> bases=BWTA.getStartLocations();
//        for(BaseLocation b:bases) {
//            if(b.isStartLocation()&&!game.isVisible(b.getTilePosition())) {
//                enemyBasePosition=new PotentialField(game,b.getTilePosition().getX()*TilePosition.SIZE_IN_PIXELS,b.getTilePosition().getY()*TilePosition.SIZE_IN_PIXELS,150);
//            }
//        }
    }

    public void initializeHeatMap(Game game) {
        heatMap.initializeHeatMap(500, game);
    }

    public void initializeAStarModule(Game game) {
        aStarModule.initializeAll(game);
    }


    /* ------------------- real-time management methods ------------------- */

    public void manageAll(Game game) {
        manageHeatMap(game);
        refreshDangerField(game);
        manageInitializationBaseArea(game);
        manageDangerFields(game);
    }

    public void refreshDangerField(Game pGame) {
        if(pGame.getFrameCount()%Scout_module.MAP_REFRESH_FRAME_COUNT==0) {
            refreshMap(pGame);
        }
    }

    public void refreshMap(Game pGame) {
        for(Unit pUnit:pGame.getAllUnits()) {
            if (pUnit.getPlayer().isEnemy(game.self()) && pUnit.getType().canAttack()&&!pUnit.getType().isWorker()) {
                PotentialField p = getDangerFieldByID(pUnit.getID());
                if (p != null) {
                    if(p.getX()!=pUnit.getPosition().getX()&&p.getY()!=pUnit.getPosition().getY()) {
                        aStarModule.getGridMap().refreshGridMap(p);
                        p.setX(pUnit.getPosition().getX());
                        p.setY(pUnit.getPosition().getY());
                        aStarModule.getGridMap().updateGridMap(p);
                    }
                } else {
                    PotentialField pf = new PotentialField(pGame, pUnit);
                    dangerFields.add(pf);
                    aStarModule.getGridMap().updateGridMap(pf);
                }
            }
        }


        for(PotentialField pf:dangerFields) {

            if(pf.isVisible(game.getAllUnits())) {
                if(pGame.enemy().allUnitCount()<1) {
                    aStarModule.getGridMap().refreshGridMap(pf);
                    dangerFields.remove(pf);
                }
                for(Unit u:game.enemy().getUnits()) {
                    if(u.getID()==pf.getId()) {
                        if(!u.exists()||!u.isVisible()) {
                            aStarModule.getGridMap().refreshGridMap(pf);
                            dangerFields.remove(pf);
                        }
                    }
                }
            }
        }
    }

    public void manageInitializationBaseArea(Game pGame) {
        /* Initialize base-to-base area */
        if(staticPathCalculator!=null&&staticPathCalculator.finished) {
            ArrayList<Block> path=staticPathCalculator.getBlockPathArray();
            ScoutingArea basePathArea=new ScoutingArea();

            for(Block b:path) {
                PotentialField pf=heatMap.getHeatBlockContainingPosition(b.getPosition());
                if(!basePathArea.getFieldArray().contains(pf)) {
                    basePathArea.insert(pf);
                }
            }
            basePathArea.setID(ScoutingArea.PATHAREA);
            scoutingAreas.add(basePathArea);
            staticPathCalculator=null;
        }
    }

    public void manageHeatMap(Game game) {
        heatMap.heatManagement(game);
    }

    public void manageDangerFields(Game pGame) {
        for(PotentialField pf:dangerFields) {

            if(game.isVisible(pf.getPosition().toTilePosition())) {
                pf.setHeat(0);
            } else {
                pf.increaseHeat();
            }
        }
    }

    public void manageAStarModule(Game game) {

    }

    /* ------------------- data structure operation methods ------------------- */

    /**
     * Adds expansion position to the list
     *
     * @param expansionPosition
     */
    public void addExpansionPosition(Game game, Position expansionPosition) {
        PotentialField expansionPF=new PotentialField(game,expansionPosition,UnitType.Terran_Command_Center.sightRange());
        if(!expansionPositions.contains(expansionPF)) {
            expansionPositions.add(expansionPF);
        }
    }

    /**
     * Removes expansion position from the list
     *
     * @param expansionPosition
     */
    public void removeExpansionPosition(Game game, Position expansionPosition) {
        PotentialField pf=new PotentialField(game,expansionPosition,UnitType.Terran_Command_Center.sightRange());
        if(expansionPositions.contains(pf)) {
            expansionPositions.remove(pf);
        }
    }

//    /**
//     * Adds enemy base position to the list
//     *
//     * @param basePosition
//     */
//    public void addEnemyBasePosition(Game game, Position basePosition) {
//        PotentialField basePF=new PotentialField(game, basePosition, UnitType.Terran_Command_Center.sightRange());
//        if(!enemyBasePosition.contains(basePF)) {
//            enemyBasePosition.add(basePF);
//        }
//    }

//    /**
//     * Removes base position from the list
//     *
//     * @param basePosition
//     */
//    public void removeBasePosition(Game game, Position basePosition) {
//        PotentialField basePF=new PotentialField(game, basePosition, UnitType.UnitTypes.Terran_Command_Center.getSightRange());
//        if(enemyBasePosition.contains(basePF)) {
//            enemyBasePosition.remove(basePF);
//        }
//    }

    /**
     * Adds danger field to the list
     *
     * @param pDangerField
     */
    public void addDangerField(PotentialField pDangerField) {
        dangerFields.add(pDangerField);
    }

    /**
     * Removes danger field from the list
     *
     * @param pDangerField
     */
    public void removeDangerField(PotentialField pDangerField) {
        if(dangerFields.contains(pDangerField)) {
            dangerFields.remove(pDangerField);
        }
    }

    /**
     * Adds retreat field to the list
     *
     * @param pRetreatField
     */
    public void addRetreatField(PotentialField pRetreatField) {
        retreatFields.add(pRetreatField);
    }

    /**
     * Removes retreat field from the list
     *
     * @param pRetreatField
     */
    public void removeRetreatField(PotentialField pRetreatField) {
        if(retreatFields.contains(pRetreatField)) {
            retreatFields.remove(pRetreatField);
        }
    }

    public PotentialField getDangerFieldByID(int pID) {
        for(PotentialField pf:dangerFields) {
            if(pf.getId()==pID) {
                return pf;
            }
        }
        return null;
    }


    /* ------------------- other methods ------------------- */

    public int calculateDangerForPath(Unit scout, ArrayList<Block> path){
        int danger = 0;
        double scoutSpeed = unitSpeed(scout);

        for (Block block :
                path) {
            danger += block.getDamage();
            for (Unit unit :
                    game.enemy().getUnits()) {
                if (unit.getType().canAttack() && isRanged(unit)){
                    if(canUnitComeToABlock(block,unit, (int) (block.getDestination_distance()/scoutSpeed))){
                        danger += unit.getPlayer().damage(unit.getType().groundWeapon());
                        // TODO increase complexity of the danger calculation
                    }
                }
            }
        }

        return danger;
    }

    public boolean isRanged(Unit unit){
        return unit.getType().groundWeapon().maxRange() > 0;
    }

    /**
     * retruns true if units can come to a block in stated frames
     * @param block
     * @param unit
     * @param frames number of a block in a path
     * @return
     */
    public boolean canUnitComeToABlock(Block block, Unit unit, int frames){
        double unitSpeed = unitSpeed(unit);
        double distance = distance(block.getPosition(),unit.getPosition());
        double framesNeeded = distance / unitSpeed; // how many drames unit needs to travel the distance
        return framesNeeded <= frames; // if units needs more than
    }

    public double unitSpeed(Unit unit){
        return unit.getPlayer().topSpeed(unit.getType());
    }

    public double distance(Position p1,Position p2){
        return Point2D.distance(p1.getX(),p1.getY(),p2.getX(),p2.getY());
    }

    public boolean containsPotentialFieldWithID(int pID) {
        for(PotentialField pf:dangerFields) {
            if(pf.getId()==pID) {
                return true;
            }
        }
        return false;
    }

    public AStarPathCalculator buildPath(Unit pUnit, Position pDestination, int pLevelOfSafety, boolean pAirPath, Game game, Color pColor) {
        return aStarModule.buildPath(pUnit.getPosition(), pDestination, pUnit.getHitPoints(), pLevelOfSafety, pAirPath, game,pColor);
    }

    public AStarPathCalculator buildPath(Unit pUnit,Position pStart, Position pDestination, int pLevelOfSafety, boolean pAirPath, Game game, Color pColor) {
        return aStarModule.buildPath(pStart, pDestination, pUnit.getHitPoints(), pLevelOfSafety, pAirPath, game,pColor);
    }

    /**
     * Returns count of the expansions
     *
     * @return
     */
    public int getExpanzionCount() {
        return expansionPositions.size();
    }

    /**
     * Returns count of the danger fields
     *
     * @return
     */
    public int getDangerFieldsCount() {
        return dangerFields.size();
    }

    /**
     * Returns count of the retreat fields
     *
     * @return
     */
    public int getRetreatFieldsCount() {
        return retreatFields.size();
    }

    /**
     * Returns string information about map
     *
     * @return
     */
    public String toString() {
        return  "\nExpansion locations = "+getExpanzionCount()+
                "\nDanger locations = "+getDangerFieldsCount()+
                "\nRetreat locations = "+getRetreatFieldsCount();
    }


    /* ------------------- Drawing functions ------------------- */

    public void drawAll() {
        drawBasePosition();
        drawExpansionPositions();
        //drawHeatMap(game);
        drawDangerFields();
        drawDangerGrid();
    }

    private void drawHeatMap(Game game) {
        heatMap.drawHeatMap(game);
    }

    /**
     * Draws base positions on the map
     */
    public void drawBasePosition() {
        enemyBasePosition.showGraphicsCircular(Color.Purple);
        game.drawCircleMap(myBasePosition,160,Color.Brown);
    }

    /**
     * Draws expansion positions on the map
     */
    public void drawExpansionPositions() {
        for(PotentialField pf:expansionPositions) {
            pf.showGraphicsCircular(Color.Yellow);
        }
    }

//    public void drawHeatMap(Game game) {
//        heatMap.drawHeatMap(game);
//    }

    public void drawDangerFields() {
        for(PotentialField pf:dangerFields) {
            pf.showGraphicsCircular(Color.Orange);
        }
    }

    public void drawDangerGrid() {
        aStarModule.getGridMap().drawDangerGrid(Color.Red,game);
    }

    /* ------------------- Getters and Setters ------------------- */

    public ArrayList<ScoutingArea> getScoutingArea(int pID) {
        ArrayList<ScoutingArea> arSc=new ArrayList<>();
        for(ScoutingArea sc:scoutingAreas) {
            if(sc.getID()==pID) {
                arSc.add(new ScoutingArea(sc));
            }
        }
        return arSc;
    }

    public AStarModule getaStarModule() {
        return aStarModule;
    }

    public void setaStarModule(AStarModule aStarModule) {
        this.aStarModule = aStarModule;
    }

    public HeatMap getHeatMap() {
        return heatMap;
    }

    public void setHeatMap(HeatMap heatMap) {
        this.heatMap = heatMap;
    }

    public Position getMyBasePosition() {
        return myBasePosition;
    }

    public void setMyBasePosition(Position myBasePosition) {
        this.myBasePosition = myBasePosition;
    }

    public Position getEnemyBasePosition() {
        return enemyBasePosition.getPosition();
    }

    public ArrayList<PotentialField> getExpansionPositions() {
        return expansionPositions;
    }

    public void setExpansionPositions(ArrayList<PotentialField> expansionPositions) {
        this.expansionPositions = expansionPositions;
    }

    public ArrayList<PotentialField> getDangerFields() {
        return dangerFields;
    }

    public void setDangerFields(ArrayList<PotentialField> dangerFields) {
        this.dangerFields = dangerFields;
    }

    public ArrayList<PotentialField> getRetreatFields() {
        return retreatFields;
    }

    public void setRetreatFields(ArrayList<PotentialField> retreatFields) {
        this.retreatFields = retreatFields;
    }
}

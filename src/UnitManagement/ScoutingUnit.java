package UnitManagement;

import MODaStar.AStarPathCalculator;
import MODaStar.Block;
import MapManager.*;
import ScoutModule.Scout_module;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Random;

/**
 * ScoutingUnit represents exact unit set for scouting tasks. Management of movement is independent of other modules.
 * ScoutingUnit provides reactive behavior and triggers other detection methods.
 */
public class ScoutingUnit {

    public static final int START_SAFEZONERADIUS=500;

    public static final int END_SAFEZONERADIUS=800;

    public static final int LOCALCHECK_PATH_SIZE=61;

    private Unit unit;

    private Position finalDestination;

    private AStarPathCalculator aStarPathCalculator;

    private AStarPathCalculator safeMicroPathCalculator;

    private AStarPathCalculator normalMicroPathCalculator;

    private AStarPathCalculator riskMicroPathCalculator;

    private ArrayList<Block> path;

    private ScoutingArea scoutingArea;

    private ArrayList<Block> safeMicroPath;

    private ArrayList<Block> normalMicroPath;

    private ArrayList<Block> riskMicroPath;

    private int microPathChooser=1; // 1 - safePath, 2 - normalPath, 3 - riskPath

    private boolean hasOrder;

    private boolean finishedOrder;

    private boolean hasTask;

    private Block microDestinationBlock;

    private int safety_level=Scout_module.SAFETY_LEVEL;

    private boolean readyToLearn;

    private boolean isAlive;


    /* ------------------- Constructors ------------------- */

    public ScoutingUnit(Unit pUnit) {
        path=new ArrayList<>();

        safeMicroPath =new ArrayList<>();
        normalMicroPath=new ArrayList<>();
        riskMicroPath=new ArrayList<>();

        unit=pUnit;
        hasOrder=false;
        finishedOrder=true;
        hasTask=false;

        readyToLearn=false;
        isAlive=true;
    }


    /* ------------------- main functionality methods ------------------- */

    public void scout(AStarPathCalculator pAStarPathCalculator, boolean pIsTask) {
        finishedOrder=false;
        hasOrder=true;
        hasTask=pIsTask;
        aStarPathCalculator=pAStarPathCalculator;
        finalDestination=aStarPathCalculator.getDestinationPosition();
    }

    public void chooseSafePath() {
        microPathChooser=1;
    }

    public void chooseNormalPath() {
        microPathChooser=2;
    }

    public void chooseRiskPath() {
        microPathChooser=3;
    }

    public boolean isReadyForQLearning() {
        if(safeMicroPath!=null&&normalMicroPath!=null&&riskMicroPath!=null&&readyToLearn) {
            readyToLearn=false;
            return true;
        }
        return false;
    }

    //public void micro(AStarPathCalculator pMicroPathCalculator) {safeMicroPathCalculator=pMicroPathCalculator; }


    /* ------------------- initialising methods ------------------- */


    /* ------------------- real-time management methods ------------------- */

    public void enemyDetected(Unit pUnit, MapManager pMapManager, Game pGame) {
        //do podmienky      safeMicroPathCalculator==null&&safeMicroPath.isEmpty()&&
        if(unit.getPosition().getDistance(pUnit.getPosition())<unit.getType().sightRange()+pUnit.getType().sightRange()) {
            pMapManager.refreshMap(pGame);
            for(Block b:path) {
                b.setDamage(pMapManager.getaStarModule().getGridMap().getBlockMap()[b.getRow()][b.getColumn()].getDamage());
            }
            if(unit.getType().isFlyer()) {
                if(pUnit.getType().groundWeapon().targetsAir()||pUnit.getType().airWeapon().targetsAir()) {
                    micro(pMapManager,pGame);
                }
            } else {
                if(pUnit.getType().groundWeapon().targetsGround()||pUnit.getType().airWeapon().targetsGround()) {
                    micro(pMapManager,pGame);
                }
            }
        }
    }

    public void manageAll(MapManager pMapManager, Game pGame) {
        manageMovement();
        managePathCalculator();

        manageSafeMicroPathCalculator();
        manageNormalMicroPathCalculator();
        manageRiskMicroPathCalculator();

        localDangerCheck(pMapManager, pGame);
        manageIdle();
        manageScoutingArea(pGame, pMapManager);
        manageFinishedSignalization();
        manageIsAlive();
    }

    public void manageIsAlive() {
        if(unit.exists()) {
            isAlive=true;
        } else {
            isAlive=false;
        }
    }

    public void managePathCalculator() {
        if(aStarPathCalculator!=null) {
            if(aStarPathCalculator.finished) {
                microDestinationBlock=null;
                safeMicroPath =new ArrayList<>();
                path=aStarPathCalculator.getBlockPathArray();
                if(path==null) {
                    System.out.println("Zmierni podmienky");
                    path=new ArrayList<>();
                    hasOrder=false;
                }
                aStarPathCalculator=null;
            }
        }
    }

    public void manageSafeMicroPathCalculator() {
        if(safeMicroPathCalculator !=null) {
            if(safeMicroPathCalculator.finished) {
                if(safeMicroPathCalculator.getBlockPathArray()!=null) {
                    safeMicroPath = safeMicroPathCalculator.getBlockPathArray();
                    for (int i = 0; i < 3; i++) {
                        safeMicroPath.remove(safeMicroPath.size() - 1);
                    }
                }
                safeMicroPathCalculator = null;
            }
        }
    }

    public void manageNormalMicroPathCalculator() {
        if(normalMicroPathCalculator !=null) {
            if(normalMicroPathCalculator.finished) {
                if(normalMicroPathCalculator.getBlockPathArray()!=null) {
                    normalMicroPath = normalMicroPathCalculator.getBlockPathArray();
                    for (int i = 0; i < 3; i++) {
                        normalMicroPath.remove(normalMicroPath.size() - 1);
                    }
                }
                normalMicroPathCalculator = null;
            }
        }
    }

    public void manageRiskMicroPathCalculator() {
        if(riskMicroPathCalculator !=null) {
            if(riskMicroPathCalculator.finished) {
                if(riskMicroPathCalculator.getBlockPathArray()!=null) {
                    riskMicroPath = riskMicroPathCalculator.getBlockPathArray();
                    for (int i = 0; i < 3; i++) {
                        riskMicroPath.remove(riskMicroPath.size() - 1);
                    }
                }
                riskMicroPathCalculator = null;
            }
        }
    }

    public void manageMovement() {
        if(!safeMicroPath.isEmpty()&&path.isEmpty()) {
            if(!normalMicroPath.isEmpty()&&path.isEmpty()) {
                if(!riskMicroPath.isEmpty()&&path.isEmpty()) {
                    switch (microPathChooser) {
                        case 1: manageSafeMicroPath();
                            break;
                        case 2: manageNormalMicroPath();
                            break;
                        case 3: manageRiskMicroPath();
                            break;
                    }
                }
            }
        } else {
            managePath();
        }
    }

    public void manageSafeMicroPath() {
        if(safeMicroPath.size()>0) {
            hasOrder=true;
            if(unit.getPosition().getDistance(safeMicroPath.get(safeMicroPath.size()-1).getPosition())<90) {
                unit.move(safeMicroPath.get(safeMicroPath.size() - 1).getPosition(), false);
                safeMicroPath.remove(safeMicroPath.size() - 1);
            }
        }
    }

    public void manageNormalMicroPath() {
        if(normalMicroPath.size()>0) {
            hasOrder=true;
            if(unit.getPosition().getDistance(normalMicroPath.get(normalMicroPath.size()-1).getPosition())<90) {
                unit.move(normalMicroPath.get(normalMicroPath.size() - 1).getPosition(), false);
                normalMicroPath.remove(normalMicroPath.size() - 1);
            }
        }
    }

    public void manageRiskMicroPath() {
        if(riskMicroPath.size()>0) {
            hasOrder=true;
            if(unit.getPosition().getDistance(riskMicroPath.get(riskMicroPath.size()-1).getPosition())<90) {
                unit.move(riskMicroPath.get(riskMicroPath.size() - 1).getPosition(), false);
                riskMicroPath.remove(riskMicroPath.size() - 1);
            }
        }
    }

    public void managePath() {
        if(path.size()>0) {
            hasOrder=true;
            finishedOrder=false;
            if(unit.getPosition().getDistance(path.get(path.size()-1).getPosition())<90) {
                unit.move(path.get(path.size() - 1).getPosition(), false);
                path.remove(path.size() - 1);
                if(path.isEmpty()) {
                    hasOrder=false;
                    hasTask=false;
                }
            } else {
                switch (microPathChooser) {
                    case 1: manageSafeMicroPath();
                        break;
                    case 2: manageNormalMicroPath();
                        break;
                    case 3: manageRiskMicroPath();
                        break;
                }
            }
        }
    }

    public void manageIdle() {
        if(safeMicroPath.size()>0&&unit.isIdle()) {
            unit.rightClick(safeMicroPath.get(safeMicroPath.size()-1).getPosition(),false);
        } else if(path.size()>0&&unit.isIdle()) {
            unit.rightClick(path.get(path.size()-1).getPosition(),false);
        }
    }

    public void manageFinishedSignalization() {
        if(hasOrder&&path!=null) {
            if(unit.getPosition().getDistance(path.get(0).getPosition())<100) {
                path=new ArrayList<>();
                safeMicroPath =new ArrayList<>();
                normalMicroPath=new ArrayList<>();
                riskMicroPath=new ArrayList<>();
                finishedOrder=true;
                hasOrder=false;
            }
        }
    }

    public void manageScoutingArea(Game pGame, MapManager pMapManager) {
        if(scoutingArea!=null&&scoutingArea.size()>0) {
            if(aStarPathCalculator==null&&path.size()>0) {
                if(pGame.isVisible(path.get(0).getPosition().toTilePosition())) {
                    scoutingArea.remove(scoutingArea.getNearestField(this,pGame));
                    aStarPathCalculator=pMapManager.buildPath(unit,scoutingArea.getNearestField(this,pGame).getPosition(), safety_level, unit.getType().isFlyer(),pGame, Color.Yellow);
                }
            } else if(aStarPathCalculator==null) {
                PotentialField field=scoutingArea.getNearestField(this,pGame);
                if(pGame.isVisible(field.getPosition().toTilePosition())) {
                    scoutingArea.remove(field);
                } else {
                    aStarPathCalculator=pMapManager.buildPath(unit,field.getPosition(),safety_level, unit.getType().isFlyer(),pGame,Color.Yellow);
                }
            }
        }
    }

    /* ------------------- data structure operation methods ------------------- */


    /* ------------------- other methods ------------------- */

    public int getLongestPathSize() {
        if(safeMicroPath!=null&&normalMicroPath!=null&&riskMicroPath!=null) {
            int sf=safeMicroPath.size();
            int nr=normalMicroPath.size();
            int rs=riskMicroPath.size();

            if(sf>nr) {
                if(sf>rs) {
                    return sf;
                } else {
                    return rs;
                }
            } else if(nr<rs) {
                return nr;
            }
        }
        return -1;
    }

    public double getSafePathDistanceRatio() {
        if(safeMicroPath!=null&&normalMicroPath!=null&&riskMicroPath!=null) {
            return (double)safeMicroPath.size()/(double)getLongestPathSize();
        }
        return -1;
    }

    public double getNormalPathDistanceRatio() {
        if(safeMicroPath!=null&&normalMicroPath!=null&&riskMicroPath!=null) {
            return (double)normalMicroPath.size()/(double)getLongestPathSize();
        }
        return -1;
    }

    public double getRiskPathDistanceRatio() {
        if(safeMicroPath!=null&&normalMicroPath!=null&&riskMicroPath!=null) {
            return (double)riskMicroPath.size()/(double)getLongestPathSize();
        }
        return -1;
    }

    public double getSafePathDangerRatio(MapManager pMapManager) {
        return pMapManager.calculateDangerForPath(unit,safeMicroPath);
    }


    public double getNormalPathDangerRatio(MapManager pMapManager) {
        return pMapManager.calculateDangerForPath(unit,normalMicroPath);
    }


    public double getRiskPathDangerRatio(MapManager pMapManager) {
        return pMapManager.calculateDangerForPath(unit,riskMicroPath);
    }

    public void scoutingAreaTEST(MapManager pMapManager, Game pGame) {

        if(scoutingArea.size()>0) {
            System.out.println("Scouting area fields = "+scoutingArea.size());
            PotentialField pf=scoutingArea.getNearestField(this,pGame);
            aStarPathCalculator=pMapManager.buildPath(unit,pf.getPosition(), safety_level, false, pGame, Color.Blue);
            scoutingArea.remove(pf);
            System.out.println("Scouting area fields = " + scoutingArea.size());
        }

    }

    public void micro(MapManager pMapManager,Game pGame) {
        Block safeEND=null;
        Block safeSTART=null;
        int removeStart=0;
        int removeEND=0;
        boolean safeDistance=true;
        boolean interruptMicro=true;
        int i;

        readyToLearn=true;

        if(microDestinationBlock==null) {
            if(path.size()>ScoutingUnit.LOCALCHECK_PATH_SIZE+5) {
                for(i=path.size()-1;i>path.size()-ScoutingUnit.LOCALCHECK_PATH_SIZE;i--) {
                    if(path.get(i).isInPotentialField()||pMapManager.getaStarModule().getGridMap().getBlockMap()[path.get(i).getRow()][path.get(i).getColumn()].isInPotentialField()) {
                        if((unit.getType().isFlyer()&&pMapManager.getaStarModule().getGridMap().getBlockMap()[path.get(i).getRow()][path.get(i).getColumn()].isAirDamage())||!unit.getType().isFlyer()&&pMapManager.getaStarModule().getGridMap().getBlockMap()[path.get(i).getRow()][path.get(i).getColumn()].isGroundDamage()) {
                            for(int k=i;k<path.size();k++) {
                                safeDistance=true;
                                for(PotentialField pf:pMapManager.getDangerFields()) {
                                    if(path.get(k).getPosition().getDistance(pf.getPosition())<pf.getRadius()+ScoutingUnit.START_SAFEZONERADIUS) {
                                        safeDistance=false;
                                        break;
                                    }
                                }
                                if(safeDistance) {
                                    safeSTART=path.get(k);
                                    removeStart=k;
                                    safeSTART.setColor(Color.Orange);
                                    break;
                                }
                            }
                            if(!safeDistance){
                                safeSTART=path.get(path.size()-1);
                                removeStart=path.size()-1;
                                safeSTART.setColor(Color.Orange);
                            }


                            for(int u=i;u>0;u--) {
                                safeDistance=true;
                                for(PotentialField pf:pMapManager.getDangerFields()) {
                                    if(path.get(u).getPosition().getDistance(pf.getPosition())<pf.getRadius()+ScoutingUnit.END_SAFEZONERADIUS) {
                                        safeDistance=false;
                                        break;
                                    }
                                }
                                if(safeDistance) {
                                    safeEND=path.get(u);
                                    removeEND=u;
                                    safeEND.setColor(Color.Orange);
                                    break;
                                }
                            }
                            if(!safeDistance) {
                                safeEND=path.get(1);
                                removeEND=1;
                                safeEND.setColor(Color.Orange);
                            }

                            break;
                        }
                    }
                }

                if(safeSTART!=null&&safeEND!=null) {
                    microDestinationBlock=safeEND;

                    safeMicroPathCalculator=pMapManager.buildPath(unit,safeSTART.getPosition(),microDestinationBlock.getPosition(),1, unit.getType().isFlyer(),pGame,Color.Green);
                    normalMicroPathCalculator=pMapManager.buildPath(unit,safeSTART.getPosition(),microDestinationBlock.getPosition(),2, unit.getType().isFlyer(),pGame,Color.White);
                    riskMicroPathCalculator=pMapManager.buildPath(unit,safeSTART.getPosition(),microDestinationBlock.getPosition(),3, unit.getType().isFlyer(),pGame,Color.Red);

                    removeFromPath(removeEND,removeStart);
                }

            } else if(path.size()>0) {
                for(i=path.size()-1;i>0;i--) {
                    if((unit.getType().isFlyer()&&pMapManager.getaStarModule().getGridMap().getBlockMap()[path.get(i).getRow()][path.get(i).getColumn()].isAirDamage())||!unit.getType().isFlyer()&&pMapManager.getaStarModule().getGridMap().getBlockMap()[path.get(i).getRow()][path.get(i).getColumn()].isGroundDamage()) {
                        if (path.get(i).isInPotentialField() || pMapManager.getaStarModule().getGridMap().getBlockMap()[path.get(i).getRow()][path.get(i).getColumn()].isInPotentialField()) {
                            microDestinationBlock=path.get(0);

                            safeMicroPathCalculator =pMapManager.buildPath(unit,unit.getPosition(),microDestinationBlock.getPosition(),1, unit.getType().isFlyer(),pGame,Color.Green);
                            normalMicroPathCalculator=pMapManager.buildPath(unit,unit.getPosition(),microDestinationBlock.getPosition(),2, unit.getType().isFlyer(),pGame,Color.White);
                            riskMicroPathCalculator=pMapManager.buildPath(unit,unit.getPosition(),microDestinationBlock.getPosition(),3, unit.getType().isFlyer(),pGame,Color.Red);

                            Block b=path.get(0);
                            path.clear();
                            path.add(b);
                            break;
                        }
                    }
                }
            }
        } else {
            for(PotentialField pf:pMapManager.getDangerFields()) {
                if(unit.getPosition().getDistance(pf.getPosition())<pf.getRadius()+300) {
                    interruptMicro=false;
                }
            }
            if(interruptMicro&&!unit.isUnderAttack()) {
                microDestinationBlock=null;

                safeMicroPath =new ArrayList<>();
                normalMicroPath=new ArrayList<>();
                riskMicroPath=new ArrayList<>();

                aStarPathCalculator=pMapManager.buildPath(unit,path.get(0).getPosition(),safety_level,unit.getType().isFlyer(),pGame,Color.Purple);
            } else if(microDestinationBlock!=null&&unit.getPosition().getDistance(microDestinationBlock.getPosition())>250) {
                safeMicroPathCalculator = pMapManager.buildPath(unit,microDestinationBlock.getPosition(),1,unit.getType().isFlyer(),pGame,Color.Green);
                normalMicroPathCalculator=pMapManager.buildPath(unit,microDestinationBlock.getPosition(),2,unit.getType().isFlyer(),pGame,Color.White);
                riskMicroPathCalculator=pMapManager.buildPath(unit,microDestinationBlock.getPosition(),3,unit.getType().isFlyer(),pGame,Color.Red);
            } else if(microDestinationBlock!=null&&microDestinationBlock.isInPotentialField()||pMapManager.getaStarModule().getGridMap().getBlockMap()[microDestinationBlock.getRow()][microDestinationBlock.getColumn()].isInPotentialField()) {
                microDestinationBlock=null;
                micro(pMapManager,pGame);
            } else {
                microDestinationBlock=null;
            }
        }
    }

    /**
     * Ak je v lokalnom dosahu nejake nebezpecenstvo, vrati prvu bezpecnu poziciu za nebezpecnou zonou
     * @return
     */
    public void localDangerCheck(MapManager pMapManager, Game pGame) {
        if(pGame.getFrameCount()%Scout_module.UNIT_DANGERCHECK_FRAME_COUNT==0&&unit.exists()) {
            micro(pMapManager, pGame);
        }
    }

    public void removeFromPath(int pStartIndex, int pEndIndex) {
        for(int i=pStartIndex;i<=pEndIndex;i++) {
            path.remove(pStartIndex);
        }
    }

    public boolean hasOrder() {
        return hasOrder;
    }

    public boolean finishedOrder() {
        return finishedOrder;
    }


    /* ------------------- Drawing functions ------------------- */

    public void drawAll(Game pGame) {
        if(unit.exists()) {
            drawPath(pGame);

            drawSafeMicroPath(pGame);
            drawNormalMicroPath(pGame);
            drawRiskMicroPath(pGame);
            //drawScoutingArea();
        }
    }

//    public void drawScoutingArea(GraphicsExtended graphicsEx) {
//        if(scoutingArea!=null&&unit.isExists()) {
//            scoutingArea.drawScoutingArea(graphicsEx);
//        }
//    }

    public void drawPath(Game pGame) {
        if(path!=null&&path.size()>0&&unit.exists()) {
            for(Block b:path) {
                pGame.drawCircleMap(b.getPosition(),5,b.getColor());
            }
        }
    }

    public void drawSafeMicroPath(Game pGame) {
        if(safeMicroPath !=null&& safeMicroPath.size()>0&&unit.exists()) {
            for(Block b : safeMicroPath) {
                pGame.drawCircleMap(b.getPosition(),5,b.getColor());
            }
        }
    }

    public void drawNormalMicroPath(Game pGame) {
        if(normalMicroPath !=null&& normalMicroPath.size()>0&&unit.exists()) {
            for(Block b : normalMicroPath) {
                pGame.drawCircleMap(b.getPosition(),5,b.getColor());
            }
        }
    }

    public void drawRiskMicroPath(Game pGame) {
        if(riskMicroPath !=null&& riskMicroPath.size()>0&&unit.exists()) {
            for(Block b : riskMicroPath) {
                pGame.drawCircleMap(b.getPosition(),5,b.getColor());
            }
        }
    }


    /* ------------------- Getters and Setters ------------------- */

    public int getSafety_level() {
        return safety_level;
    }

    public void setSafety_level(int safety_level) {
        this.safety_level = safety_level;
    }

    public boolean isHasTask() {
        return hasTask;
    }

    public void setHasTask(boolean hasTask) {
        this.hasTask = hasTask;
    }

    public ArrayList<Block> getSafeMicroPath() {
        return safeMicroPath;
    }

    public void setSafeMicroPath(ArrayList<Block> safeMicroPath) {
        this.safeMicroPath = safeMicroPath;
    }

    public ScoutingArea getScoutingArea() {
        return scoutingArea;
    }

    public void setScoutingArea(ScoutingArea scoutingArea) {
        this.scoutingArea = scoutingArea;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public ArrayList<Block> getPath() {
        return path;
    }

    public void setPath(ArrayList<Block> path) {
        this.path = path;
    }

    public boolean isHasOrder() {
        return hasOrder;
    }

    public void setHasOrder(boolean hasOrder) {
        this.hasOrder = hasOrder;
    }

    public boolean isFinishedOrder() {
        return finishedOrder;
    }

    public void setFinishedOrder(boolean finishedOrder) {
        this.finishedOrder = finishedOrder;
    }

    public Position getFinalDestination() {
        return finalDestination;
    }

    public void setFinalDestination(Position finalDestination) {
        this.finalDestination = finalDestination;
    }

    public int getMicroPathChooser() {
        return microPathChooser;
    }

    public void setMicroPathChooser(int microPathChooser) {
        this.microPathChooser = microPathChooser;
    }

    public boolean getIsAlive() {
        return isAlive;
    }
}

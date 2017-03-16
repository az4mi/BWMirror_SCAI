package MODQlearning;

import ScoutModule.Scout_module;
import UnitManagement.ScoutingUnit;
import bwapi.*;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Silent1 on 17.01.2017.
 */
public class QExecutor {

    public static boolean DEBUG=false;
    public static boolean EXECUTE=false;

    /* Q-learning */
    private Scout_module scout_module;
    private Game game;
    private QLearning qlearning;
    private State lastState = null;
    private Action executingAction = null;
    private int startState = 0;
    private ScoutingUnit actualScoutingUnit;
    private boolean running=false;
    private boolean nextScenario=true;
    private int nextUnit=1;

    private int reward;
    private int rewardDiscount = 0;
    /* Q-learning */

    private Position safePosition_1;
    private Position safePosition_2;
    private Position safePosition_3;
    private Position safePosition_4;
    private Position safePosition_5;
    private Position safePosition_6;
    private Position safePosition_7;
    private Position safePosition_8;
    private Position safePosition_9;
    private Position safePosition_10;
    private Position safePosition_11;
    private Position safePosition_12;

    private Position endPosition;

    private ScoutingUnit scUnit_1;
    private ScoutingUnit scUnit_2;
    private ScoutingUnit scUnit_3;
    private ScoutingUnit scUnit_4;
    private ScoutingUnit scUnit_5;
    private ScoutingUnit scUnit_6;
    private ScoutingUnit scUnit_7;
    private ScoutingUnit scUnit_8;
    private ScoutingUnit scUnit_9;
    private ScoutingUnit scUnit_10;
    private ScoutingUnit scUnit_11;
    private ScoutingUnit scUnit_12;

    private ScoutingUnit endUnit;

    private boolean isDead;

    private List<Position> safePositions;
    private List<ScoutingUnit> scoutingUnits;

    private int execFrameCount=0;

    public QExecutor(Scout_module pScout_module) {
        scout_module=pScout_module;
        game=pScout_module.getGame();
        safePositions=new LinkedList<>();
        scoutingUnits=new LinkedList<>();
        qlearning=new QLearning();
        reward=2000;
        isDead=false;
    }

    public void resetQLearning(ScoutingUnit pScoutingUnit) {
        reward=2000;                                                                                                        // Reset reward
        qlearning.loadMatrixIO();                                                                                           // New initialization of QMatrix from file
        actualScoutingUnit=pScoutingUnit;
        running=false;
    }

    public void onFrame() {
        //cameraLockOnActualUnit();

        if(actualScoutingUnit!=null) {
            isDead = !actualScoutingUnit.getUnit().exists();
        }

        /* type `qrun` into chat */
        if(QExecutor.EXECUTE) {

            executeAll();

            if(running) {
                decrementReward();
            }

        /* update every XX frames, when scoutingUnit is ready */
            if(actualScoutingUnit.isReadyForQLearning()) {
                update();
            }

        /* when scoutingUnit finished learning */
//            if(finishedLearning()) {
//                if(running) {
//                    qlearning.saveMatrixIO();// Save QMatrix
//                    running=false;
//                    nextUnit++;
//                    nextScenario=true;
//                }
//            }
        }

    }

    public boolean isDead() {
        if(!actualScoutingUnit.getUnit().isMoving()&&actualScoutingUnit.getUnit().getHitPoints()<actualScoutingUnit.getUnit().getInitialHitPoints()&&actualScoutingUnit.getUnit().getPosition().getDistance(actualScoutingUnit.getFinalDestination())>150) {
            return true;
        }
        return false;
    }

    public boolean isSuccesfull() {
        if(actualScoutingUnit.getFinalDestination()!=null) {
                if(actualScoutingUnit.getUnit().getDistance(actualScoutingUnit.getFinalDestination())<100) {
                    return true;
                }
        }
        return false;
    }

    public boolean finishedLearning() {
        if(actualScoutingUnit.getFinalDestination()!=null) {
            if(actualScoutingUnit.getUnit().getDistance(actualScoutingUnit.getFinalDestination())<100) {
                return true;
            }
        }
        if(!actualScoutingUnit.getUnit().exists()) {
            reward=-999;
            return true;
        }
        return false;
    }

    public void decrementReward() {
        reward--;
    }

    public void update()
    {
        if(DEBUG) {
            System.out.println(":: Q-update ::");
        }

        State currentState = detectState(actualScoutingUnit);

        if (lastState != null) {

            if (isSuccesfull()) {
                if(DEBUG) {
                    System.out.println(":: Unit won ::");
                }

                reward += 100;
                reward -= rewardDiscount;
                qlearning.experience(lastState, executingAction, currentState, reward);

                qlearning.saveMatrixIO();
                running=false;
                nextUnit++;
                nextScenario=true;

            } else if (isDead) {
                if(DEBUG) {
                    System.out.println(":: Unit lose ::");
                }
                reward -= 200;
                reward -= rewardDiscount;
                qlearning.experience(lastState, executingAction, currentState, reward);

                qlearning.saveMatrixIO();
                running=false;
                nextUnit++;
                nextScenario=true;
            } else {
                if(DEBUG) {
                    System.out.println(":: Unit making progress ::");
                }
                qlearning.experience(lastState, executingAction, currentState, 0);
            }
        }

        executingAction = qlearning.estimateBestActionIn(currentState);
        lastState = currentState;

        /* Execute next action */
        if(DEBUG) {
            System.out.println(":: Unit is making action ::");
        }

        executingAction.executeAction(actualScoutingUnit);
    }

//    public void updateOnEnd() {
//
//        State currentState = detectState(actualScoutingUnit);
//
//        double currentStateValue = currentState.getValue(game, unit);
//
//        if (game.enemy().getUnits().isEmpty()) {
//            currentStateValue = 0;
//            for (Unit myUnit : game.self().getUnits()) {
//                currentStateValue += myUnit.getType().maxHitPoints() + myUnit.getHitPoints() + myUnit.getShields();
//            }
//        }
//
//        if (lastState != null) {
//            double reward = (currentStateValue - lastStateValue) * 1000;
//            qlearning.experience(lastState, executingAction, currentState, reward);
//        }
//    }

    private State detectState(ScoutingUnit pScoutingUnit) {

        if(DEBUG) {
            System.out.println(":: Detecting state ::");
        }

        double HP_bound1=0.4;
        double HP_bound2=0.7;

        double RATIO_bound1=0.4;
        double RATIO_bound2=0.7;

        double DANGER_bound1=0.4;
        double DANGER_bound2=0.7;



        int HP;

        int SAFEPATH; //Pomer najbezpecnejsej cesty k najdlhsej
        int NORMALPATH; //Pomer normalnej cesty k najdlhsej
        int RISKPATH; //Pomer najriskantnejsej cesty k najdlhsej

        double DANGER;

        int SAFEDANGER;
        int NORMALDANGER;
        int RISKDANGER;

        String code="";

        if(pScoutingUnit.getUnit().getHitPoints()<pScoutingUnit.getUnit().getHitPoints()*HP_bound1) {
            HP=1; //Malo HP
        } else if(pScoutingUnit.getUnit().getHitPoints()<pScoutingUnit.getUnit().getHitPoints()*HP_bound2) {
            HP=2; //Stredne vela HP
        } else {
            HP=3; //Skoro full HP
        }



        if(pScoutingUnit.getSafePathDistanceRatio()<RATIO_bound1) {
            SAFEPATH=1; //O vela kratsia cesta
        } else if(pScoutingUnit.getSafePathDistanceRatio()<RATIO_bound2) {
            SAFEPATH=2; //Trochu kratsia cesta
        } else {
            SAFEPATH=3; //Skoro rovnaka cesta
        }

        if(pScoutingUnit.getNormalPathDistanceRatio()<RATIO_bound1) {
            NORMALPATH=1; //O vela kratsia cesta
        } else if(pScoutingUnit.getNormalPathDistanceRatio()<RATIO_bound2) {
            NORMALPATH=2; //Trochu kratsia cesta
        } else {
            NORMALPATH=3; //Skoro rovnaka cesta
        }

        if(pScoutingUnit.getRiskPathDistanceRatio()<RATIO_bound1) {
            RISKPATH=1; //O vela kratsia cesta
        } else if(pScoutingUnit.getRiskPathDistanceRatio()<RATIO_bound2) {
            RISKPATH=2; //Trochu kratsia cesta
        } else {
            RISKPATH=3; //Skoro rovnaka cesta
        }



        DANGER=pScoutingUnit.getSafePathDangerRatio(scout_module.getMapManager());

        if(DANGER<DANGER_bound1) {
            SAFEDANGER=1;
        } else if(DANGER<DANGER_bound2) {
            SAFEDANGER=2;
        } else {
            SAFEDANGER=3;
        }

        DANGER=pScoutingUnit.getNormalPathDangerRatio(scout_module.getMapManager());

        if(DANGER<DANGER_bound1) {
            NORMALDANGER=1;
        } else if(DANGER<DANGER_bound2) {
            NORMALDANGER=2;
        } else {
            NORMALDANGER=3;
        }

        DANGER=pScoutingUnit.getRiskPathDangerRatio(scout_module.getMapManager());

        if(DANGER<DANGER_bound1) {
            RISKDANGER=1;
        } else if(DANGER<DANGER_bound2) {
            RISKDANGER=2;
        } else {
            RISKDANGER=3;
        }

        code=""+HP+SAFEPATH+NORMALPATH+RISKPATH+SAFEDANGER+NORMALDANGER+RISKDANGER;

        State state=new State(code,HP,SAFEPATH,NORMALPATH,RISKPATH,SAFEDANGER,NORMALDANGER,RISKDANGER);

        if(DEBUG) {
            System.out.println(":: Detected state = "+state+" ::");
        }

        return state;
    }

    public void executeAll() {
        if(!running) {
            if(nextScenario) {
                switch (nextUnit) {
                    case 1:
                        if(DEBUG) {
                            System.out.println(":: Executing scenario 1 ::");
                        }
                        resetQLearning(scUnit_1);
                        execute_1();
                        running = true;
                        nextScenario = false;
                        break;
                    case 2:
                        if(DEBUG) {
                            System.out.println(":: Executing scenario 2 ::");
                        }
                        resetQLearning(scUnit_2);
                        execute_2();
                        running = true;
                        nextScenario = false;
                        break;
                    case 3:
                        if(DEBUG) {
                            System.out.println(":: Executing scenario 3 ::");
                        }
                        resetQLearning(scUnit_3);
                        execute_3();
                        running = true;
                        nextScenario = false;
                        break;
                    case 4:
                        resetQLearning(scUnit_4);
                        execute_4();
                        running = true;
                        nextScenario = false;
                        break;
                    case 5:
                        resetQLearning(scUnit_5);
                        execute_5();
                        running = true;
                        nextScenario = false;
                        break;
                    case 6:
                        resetQLearning(scUnit_6);
                        execute_6();
                        running = true;
                        nextScenario = false;
                        break;
                    case 7:
                        resetQLearning(scUnit_7);
                        execute_7();
                        running = true;
                        nextScenario = false;
                        break;
                    case 8:
                        resetQLearning(scUnit_8);
                        execute_8();
                        running = true;
                        nextScenario = false;
                        break;
                    case 9:
                        resetQLearning(scUnit_9);
                        execute_9();
                        running = true;
                        nextScenario = false;
                        break;
                    case 10:
                        resetQLearning(scUnit_10);
                        execute_10();
                        running = true;
                        nextScenario = false;
                        break;
                    case 11:
                        resetQLearning(scUnit_11);
                        execute_11();
                        running = true;
                        nextScenario = false;
                        break;
                    case 12:
                        resetQLearning(scUnit_12);
                        execute_12();
                        running = true;
                        nextScenario = false;
                        break;
                    case 13:
                        execute_END();
                        nextUnit=1;
                }
            }
        }
    }

    public void initializeAll() {
        initializeSafePositions();
        initializeScoutingUnits();
    }

    public void initializeScoutingUnits() {
        for(Unit u:game.getAllUnits()) {
            if(u.getPlayer()==game.self()) {
                if(QExecutor.DEBUG) {
                    System.out.println("ID = " + u.getID());
                }

                if(u.getPosition().getDistance(270,258)<200) {
                    if(u.canMove()) {
                        endUnit=new ScoutingUnit(u);
                        scoutingUnits.add(endUnit);
                        scout_module.getUnitManager().addScoutingUnit(endUnit);
                        if(QExecutor.DEBUG) {
                            System.out.println("EndUnit initialized.");
                        }
                    }
                } else if(u.getPosition().getDistance(258,1054)<100) {
                    scUnit_1=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_1);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_1);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 1 initialized.");
                    }
                } else if(u.getPosition().getDistance(3204,1064)<100) {
                    scUnit_2=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_2);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_2);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 2 initialized.");
                    }
                } else if(u.getPosition().getDistance(5961,1057)<100) {
                    scUnit_3=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_3);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_3);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 3 initialized.");
                    }
                } else if(u.getPosition().getDistance(319,2288)<100) {
                    scUnit_4=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_4);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_4);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 4 initialized.");
                    }
                } else if(u.getPosition().getDistance(3113,2329)<100) {
                    scUnit_5=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_5);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_5);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 5 initialized.");
                    }
                } else if(u.getPosition().getDistance(5842,2286)<100) {
                    scUnit_6=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_6);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_6);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 6 initialized.");
                    }
                } else if(u.getPosition().getDistance(232,3566)<100) {
                    scUnit_7=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_7);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_7);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 7 initialized.");
                    }
                } else if(u.getPosition().getDistance(2681,3556)<100) {
                    scUnit_8=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_8);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_8);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 8 initialized.");
                    }
                } else if(u.getPosition().getDistance(5676,3556)<100) {
                    scUnit_9=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_9);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_9);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 9 initialized.");
                    }
                } else if(u.getPosition().getDistance(163,4748)<100) {
                    scUnit_10=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_10);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_10);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 10 initialized.");
                    }
                } else if(u.getPosition().getDistance(2883,4765)<100) {
                    scUnit_11=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_11);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_11);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 11 initialized.");
                    }
                } else if(u.getPosition().getDistance(5754,4745)<100) {
                    scUnit_12=new ScoutingUnit(u);
                    scoutingUnits.add(scUnit_12);
                    scout_module.getUnitManager().addScoutingUnit(scUnit_12);
                    if(QExecutor.DEBUG) {
                        System.out.println("ScoutingUnit 12 initialized.");
                    }
                }
            }
        }
        if(QExecutor.DEBUG) {
            System.out.println("Scouting units count = "+scoutingUnits.size());
        }
    }

    public void initializeSafePositions() {
        for(Unit u:game.getAllUnits()) {
            if(u.getPlayer()==game.self()) {
                if(u.getType()==UnitType.Terran_Bunker) {
                    if(u.getPosition().getDistance(1008,258)<200) {
                        endPosition=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("EndPosition initialized.");
                        }
                    } else if(u.getPosition().getDistance(1648,1056)<100) {
                        safePosition_1=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 1 initialized.");
                        }
                    } else if(u.getPosition().getDistance(4656,1056)<100) {
                        safePosition_2=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 2 initialized.");
                        }
                    } else if(u.getPosition().getDistance(7632,1088)<100) {
                        safePosition_3=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 3 initialized.");
                        }
                    } else if(u.getPosition().getDistance(1936,2304)<100) {
                        safePosition_4=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 4 initialized.");
                        }
                    } else if(u.getPosition().getDistance(4848,2304)<100) {
                        safePosition_5=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 5 initialized.");
                        }
                    } else if(u.getPosition().getDistance(8016,2304)<100) {
                        safePosition_6=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 6 initialized.");
                        }
                    } else if(u.getPosition().getDistance(1712,3584)<100) {
                        safePosition_7=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 7 initialized.");
                        }
                    } else if(u.getPosition().getDistance(4752,3520)<100) {
                        safePosition_8=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 8 initialized.");
                        }
                    } else if(u.getPosition().getDistance(7920,3584)<100) {
                        safePosition_9=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 9 initialized.");
                        }
                    } else if(u.getPosition().getDistance(1808,4800)<100) {
                        safePosition_10=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 10 initialized.");
                        }
                    } else if(u.getPosition().getDistance(4720,4800)<100) {
                        safePosition_11=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 11 initialized.");
                        }
                    } else if(u.getPosition().getDistance(7696,4768)<100) {
                        safePosition_12=u.getPosition();
                        safePositions.add(u.getPosition());
                        if(QExecutor.DEBUG) {
                            System.out.println("SafePosition 12 initialized.");
                        }
                    }
                }
            }
        }
        if(QExecutor.DEBUG) {
            System.out.println("Safepositions = "+safePositions.size());
        }
    }

    public void execute_1() {
        scout_module.getActionManager().scoutPosition(safePosition_1,scUnit_1);
    }

    public void execute_2() {
        scout_module.getActionManager().scoutPosition(safePosition_2,scUnit_2);
    }

    public void execute_3() {
        scout_module.getActionManager().scoutPosition(safePosition_3,scUnit_3);
    }

    public void execute_4() {
        scout_module.getActionManager().scoutPosition(safePosition_4,scUnit_4);
    }

    public void execute_5() {
        scout_module.getActionManager().scoutPosition(safePosition_5,scUnit_5);
    }

    public void execute_6() {
        scout_module.getActionManager().scoutPosition(safePosition_6,scUnit_6);
    }

    public void execute_7() {
        scout_module.getActionManager().scoutPosition(safePosition_7,scUnit_7);
    }

    public void execute_8() {
        scout_module.getActionManager().scoutPosition(safePosition_8,scUnit_8);
    }

    public void execute_9() {
        scout_module.getActionManager().scoutPosition(safePosition_9,scUnit_9);
    }

    public void execute_10() {
        scout_module.getActionManager().scoutPosition(safePosition_10,scUnit_10);
    }

    public void execute_11() {
        scout_module.getActionManager().scoutPosition(safePosition_11,scUnit_11);
    }

    public void execute_12() {
        scout_module.getActionManager().scoutPosition(safePosition_12,scUnit_12);
    }

    public void execute_END() {
        scout_module.getActionManager().scoutPosition(endPosition,endUnit);
    }

    public void drawAll() {
        drawSafePositions();
        drawSelectedIDs();
        drawActualScoutingUnit();
    }

    public void drawSelectedIDs() {
        for(Unit u: game.getAllUnits()) {
            if(u.isSelected()) {
                game.drawTextMap(u.getPosition(),""+u.getID());
                game.drawTextMap(u.getX(),u.getY()+50,""+u.getPosition().toString());
            }
        }
    }

    public void drawSafePositions() {
        for(Position pos:safePositions) {
            game.drawCircleMap(pos,130, Color.White);
        }
    }

    public void drawActualScoutingUnit() {
        game.drawCircleMap(actualScoutingUnit.getUnit().getPosition(),80,Color.Orange);
    }

    public void drawScoutingUnits() {
        for(ScoutingUnit scu:scoutingUnits) {
            game.drawBoxMap(scu.getUnit().getPosition().getX()-20,scu.getUnit().getPosition().getY()-20,scu.getUnit().getPosition().getX()+20,scu.getUnit().getPosition().getY()+20,Color.Blue);
        }
    }

    public void showAll() {
        showReward();
        showActualUnitStats();
    }

    public void showReward() {
        game.drawTextScreen(20,20,"Reward     = "+Integer.toString(reward));
    }

    public void showActualUnitStats() {
        game.drawTextScreen(20,40,"IsAlive    = "+actualScoutingUnit.getUnit().exists());
        game.drawTextScreen(20,60,"Unit HP    = "+actualScoutingUnit.getUnit().getHitPoints());
        game.drawTextScreen(20,80,"Unit path  = "+actualScoutingUnit.getMicroPathChooser());
    }

    public void cameraLockOnActualUnit() {
        if(actualScoutingUnit!=null) {
            game.setScreenPosition(actualScoutingUnit.getUnit().getX()-200,actualScoutingUnit.getUnit().getY()-200);
        }
    }
}

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

    public static boolean DEBUG = true;
    public static boolean EXECUTE = false;

    /* Q-learning */
    private Scout_module scout_module;
    private Game game;
    private QLearning qlearning;
    private State lastState = null;
    private Action executingAction = null;
    private int startState = 0;
    private ScoutingUnit actualScoutingUnit;
    private boolean running = false;
    private boolean nextScenario = true;
    private int nextUnit = 1;

    private static int testCounter = 1;
    private static int numberOfTests = 5;
    private static final long[] timeArray = new long[12];
    private static final LinkedList<Integer> hpArray = new LinkedList<>();
    private boolean finished = false;
    private static int resetCounter = 0;
    private static int scoutIteration = 0;

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

    private long startTimeCase1;
    private long startTimeCase2;
    private long startTimeCase3;
    private long startTimeCase4;
    private long startTimeCase5;
    private long startTimeCase6;
    private long startTimeCase7;
    private long startTimeCase8;
    private long startTimeCase9;
    private long startTimeCase10;
    private long startTimeCase11;
    private long startTimeCase12;

    private boolean isDead;

    private List<Position> safePositions;
    private List<ScoutingUnit> scoutingUnits;

    private int execFrameCount = 0;

    public QExecutor(Scout_module pScout_module) {
        scout_module = pScout_module;
        game = pScout_module.getGame();
        safePositions = new LinkedList<>();
        scoutingUnits = new LinkedList<>();
        qlearning = new QLearning();
        reward = 2000;
        isDead = false;
    }

    public void resetQLearning(ScoutingUnit pScoutingUnit) {
        reward = 2000;                                                                                                        // Reset reward
        qlearning.loadMatrixIO();                                                                                           // New initialization of QMatrix from file
        actualScoutingUnit = pScoutingUnit;
        running = false;
    }

    public void onFrame() {
        //cameraLockOnActualUnit();

        /* type `qrun` into chat */
        if (QExecutor.EXECUTE) {

            if (actualScoutingUnit != null) {
                if (isSuccesfull()) {
                    hpArray.add(scoutIteration,actualScoutingUnit.getUnit().getHitPoints());

                    if (DEBUG) {
                        System.out.println(":: Unit won ::");
                        System.out.println("Unit `"+scoutIteration+"` HP = " + hpArray.get(scoutIteration));
                    }

                    scoutIteration++;
                    update();


                } else if (isDead()) {
                    hpArray.add(scoutIteration,0);

                    if (DEBUG) {
                        System.out.println(":: Unit lose ::");
                        System.out.println("Unit `"+scoutIteration+"` HP = " + hpArray.get(scoutIteration));
                    }

                    scoutIteration++;
                    update();
                }
            }


            executeAll();

            if (running) {
                decrementReward();
            }

        /* update every XX frames, when scoutingUnit is ready */
            if (actualScoutingUnit.isReadyForQLearning()) {
                update();
            }

        }

    }

    public boolean isDead() {
        if (!actualScoutingUnit.getUnit().exists()) {
            return true;
        }
        return false;
    }

    public boolean isSuccesfull() {
        if (actualScoutingUnit.getFinalDestination() != null) {
            if (actualScoutingUnit.getUnit().getDistance(actualScoutingUnit.getFinalDestination()) < 100) {
                return true;
            }
        }
        return false;
    }

    public boolean finishedLearning() {
        if (actualScoutingUnit.getFinalDestination() != null) {
            if (actualScoutingUnit.getUnit().getDistance(actualScoutingUnit.getFinalDestination()) < 100) {
                return true;
            }
        }
        if (!actualScoutingUnit.getUnit().exists()) {
            reward = -999;
            return true;
        }
        return false;
    }

    public void decrementReward() {
        reward--;
    }

    public void update() {
        State currentState = detectState(actualScoutingUnit);

        if (lastState != null) {

            if (isSuccesfull()) {

                if (DEBUG) {
                    System.out.println(":: Updating reward ::");
                }

                reward += 100;
                reward -= rewardDiscount;
                qlearning.experience(lastState, executingAction, currentState, reward);

                qlearning.saveMatrixIO();
                running = false;
                nextUnit++;
                nextScenario = true;

            } else if (isDead()) {

                if (DEBUG) {
                    System.out.println(":: Updating reward ::");
                }

                reward -= 200;
                reward -= rewardDiscount;
                qlearning.experience(lastState, executingAction, currentState, reward);

                qlearning.saveMatrixIO();
                running = false;
                nextUnit++;
                nextScenario = true;
            } else {
                if (DEBUG) {
                    System.out.println(":: State update (qlearning.experience) ::");
                }
                qlearning.experience(lastState, executingAction, currentState, 0);
            }
        }

        executingAction = qlearning.estimateBestActionIn(currentState);
        lastState = currentState;

        /* Execute next action */
        if (DEBUG) {
            System.out.println(":: Unit is executing next action ::");
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

        if (DEBUG) {
            System.out.println(":: Detecting state ::");
        }

        double HP_bound1 = 0.4;
        double HP_bound2 = 0.7;

        double RATIO_bound1 = 0.4;
        double RATIO_bound2 = 0.7;

        double DANGER_bound1 = 0.4;
        double DANGER_bound2 = 0.7;


        int HP;

        int SAFEPATH; //Pomer najbezpecnejsej cesty k najdlhsej
        int NORMALPATH; //Pomer normalnej cesty k najdlhsej
        int RISKPATH; //Pomer najriskantnejsej cesty k najdlhsej

        double DANGER;

        int SAFEDANGER;
        int NORMALDANGER;
        int RISKDANGER;

        String code = "";

        if (pScoutingUnit.getUnit().getHitPoints() < pScoutingUnit.getUnit().getHitPoints() * HP_bound1) {
            HP = 1; //Malo HP
        } else if (pScoutingUnit.getUnit().getHitPoints() < pScoutingUnit.getUnit().getHitPoints() * HP_bound2) {
            HP = 2; //Stredne vela HP
        } else {
            HP = 3; //Skoro full HP
        }


        if (pScoutingUnit.getSafePathDistanceRatio() < RATIO_bound1) {
            SAFEPATH = 1; //O vela kratsia cesta
        } else if (pScoutingUnit.getSafePathDistanceRatio() < RATIO_bound2) {
            SAFEPATH = 2; //Trochu kratsia cesta
        } else {
            SAFEPATH = 3; //Skoro rovnaka cesta
        }

        if (pScoutingUnit.getNormalPathDistanceRatio() < RATIO_bound1) {
            NORMALPATH = 1; //O vela kratsia cesta
        } else if (pScoutingUnit.getNormalPathDistanceRatio() < RATIO_bound2) {
            NORMALPATH = 2; //Trochu kratsia cesta
        } else {
            NORMALPATH = 3; //Skoro rovnaka cesta
        }

        if (pScoutingUnit.getRiskPathDistanceRatio() < RATIO_bound1) {
            RISKPATH = 1; //O vela kratsia cesta
        } else if (pScoutingUnit.getRiskPathDistanceRatio() < RATIO_bound2) {
            RISKPATH = 2; //Trochu kratsia cesta
        } else {
            RISKPATH = 3; //Skoro rovnaka cesta
        }


        DANGER = pScoutingUnit.getSafePathDangerRatio(scout_module.getMapManager());

        if (DANGER < DANGER_bound1) {
            SAFEDANGER = 1;
        } else if (DANGER < DANGER_bound2) {
            SAFEDANGER = 2;
        } else {
            SAFEDANGER = 3;
        }

        DANGER = pScoutingUnit.getNormalPathDangerRatio(scout_module.getMapManager());

        if (DANGER < DANGER_bound1) {
            NORMALDANGER = 1;
        } else if (DANGER < DANGER_bound2) {
            NORMALDANGER = 2;
        } else {
            NORMALDANGER = 3;
        }

        DANGER = pScoutingUnit.getRiskPathDangerRatio(scout_module.getMapManager());

        if (DANGER < DANGER_bound1) {
            RISKDANGER = 1;
        } else if (DANGER < DANGER_bound2) {
            RISKDANGER = 2;
        } else {
            RISKDANGER = 3;
        }

        code = "" + HP + SAFEPATH + NORMALPATH + RISKPATH + SAFEDANGER + NORMALDANGER + RISKDANGER;

        State state = new State(code, HP, SAFEPATH, NORMALPATH, RISKPATH, SAFEDANGER, NORMALDANGER, RISKDANGER);

        if (DEBUG) {
            System.out.println(":: Detected state = " + state + " ::");
        }

        return state;
    }

    public void executeAll() {
        if (!running) {
            if (nextScenario) {

                switch (nextUnit) {
                    case 1:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 1 ::");
                        }

                        startTimeCase1 = System.nanoTime();

                        resetQLearning(scUnit_1);
                        execute_1();
                        running = true;
                        nextScenario = false;


                        break;

                    case 2:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 2 ::");
                        }

                        timeArray[0] += System.nanoTime() - startTimeCase1;
                        startTimeCase2 = System.nanoTime();

                        resetQLearning(scUnit_2);
                        execute_2();
                        running = true;
                        nextScenario = false;


                        break;
                    case 3:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 3 ::");
                        }

                        timeArray[1] += System.nanoTime() - startTimeCase2;
                        startTimeCase3 = System.nanoTime();

                        resetQLearning(scUnit_3);
                        execute_3();
                        running = true;
                        nextScenario = false;


                        break;
                    case 4:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 4 ::");
                        }


                        timeArray[2] += System.nanoTime() - startTimeCase4;
                        startTimeCase5 = System.nanoTime();

                        resetQLearning(scUnit_4);
                        execute_4();
                        running = true;
                        nextScenario = false;


                        break;
                    case 5:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 5 ::");
                        }


                        timeArray[3] += System.nanoTime() - startTimeCase4;
                        startTimeCase5 = System.nanoTime();

                        resetQLearning(scUnit_5);
                        execute_5();
                        running = true;
                        nextScenario = false;


                        break;
                    case 6:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 6 ::");
                        }


                        timeArray[4] += System.nanoTime() - startTimeCase5;
                        startTimeCase6 = System.nanoTime();

                        resetQLearning(scUnit_6);
                        execute_6();
                        running = true;
                        nextScenario = false;


                        break;
                    case 7:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 7 ::");
                        }


                        timeArray[5] += System.nanoTime() - startTimeCase6;
                        startTimeCase7 = System.nanoTime();

                        resetQLearning(scUnit_7);
                        execute_7();
                        running = true;
                        nextScenario = false;


                        break;
                    case 8:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 8 ::");
                        }


                        timeArray[6] += System.nanoTime() - startTimeCase7;
                        startTimeCase8 = System.nanoTime();

                        resetQLearning(scUnit_8);
                        execute_8();
                        running = true;
                        nextScenario = false;


                        break;
                    case 9:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 9 ::");
                        }


                        timeArray[7] += System.nanoTime() - startTimeCase8;
                        startTimeCase9 = System.nanoTime();

                        resetQLearning(scUnit_9);
                        execute_9();
                        running = true;
                        nextScenario = false;


                        break;
                    case 10:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 10 ::");
                        }


                        timeArray[8] += System.nanoTime() - startTimeCase9;
                        startTimeCase10 = System.nanoTime();

                        resetQLearning(scUnit_10);
                        execute_10();
                        running = true;
                        nextScenario = false;


                        break;
                    case 11:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 11 ::");
                        }


                        timeArray[9] += System.nanoTime() - startTimeCase10;
                        startTimeCase11 = System.nanoTime();

                        resetQLearning(scUnit_11);
                        execute_11();
                        running = true;
                        nextScenario = false;


                        break;
                    case 12:
                        if (DEBUG) {
                            System.out.println(":: Executing scenario 12 ::");
                        }


                        timeArray[10] += System.nanoTime() - startTimeCase11;
                        startTimeCase12 = System.nanoTime();

                        resetQLearning(scUnit_12);
                        execute_12();
                        running = true;
                        nextScenario = false;


                        break;

                    case 13:
                        if (DEBUG) {
                            System.out.println(":: Executing ending scenario 13 ::");
                        }


                        timeArray[11] += System.nanoTime() - startTimeCase12;
                        execute_END();

                        nextUnit = 1;
                        scoutIteration=0;



                        for (int i = 0; i < hpArray.size(); i++) {
                            System.out.println("Unit" + i + ":  " + hpArray.get(i));
                        }


                        if (testCounter == numberOfTests) {
                            double converted = 0;
                            int scenario = 1;
                            int sumHP = 0;

                            System.out.println("\n:: Q-LEARNING ::\n");

                            for (int i = 0; i < 12; i++) {
                                double convertedOnce = (double) timeArray[i] / 1000000000.0;
                                converted += convertedOnce;

                                int HP = hpArray.get(i);
                                sumHP += HP;

                                System.out.format("Case %d  elapsed time:  %.4f seconds/tests \n", scenario, (convertedOnce / testCounter));
                                System.out.format("Unit %d:  %.2f HP \n", scenario, hpArray.get(i) / testCounter);
                                System.out.println("");

                                scenario++;
                            }
                            System.out.format("Average elapsed time:  %.4f seconds/tests \n", converted);
                            System.out.format("Average elapsed time:  %.4f seconds/(scenarios*tests) \n", converted / (12 * testCounter));
                            System.out.format("Average unit HP:  %.2f HP \n", sumHP / (12 * testCounter));
                            System.out.println("\nTests finished : " + testCounter + "\nScenarios finished : " + 12 * testCounter + "\n\n");
                            System.exit(0);
                        }

                        testCounter++;
                        finished = true;
                }

            }
        }
    }

    public void initializeAll() {
        initializeSafePositions();
        initializeScoutingUnits();
    }

    public void initializeScoutingUnits() {
        for (Unit u : game.getAllUnits()) {
            if (u.getPlayer() == game.self()) {
                if (u.getPosition().getDistance(270, 258) < 200) {
                    if(endUnit==null) {
                        if (u.canMove()) {
                            endUnit = new ScoutingUnit(u);
                            scoutingUnits.add(endUnit);
                            scout_module.getUnitManager().addScoutingUnit(endUnit);
                            if (QExecutor.DEBUG) {
                                System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                                System.out.println("EndUnit initialized.");
                            }
                        }
                    }
                } else if (u.getPosition().getDistance(258, 1054) < 100) {
                    if(scUnit_1==null) {
                        scUnit_1 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_1);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_1);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 1 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(3204, 1064) < 100) {
                    if(scUnit_2==null) {
                        scUnit_2 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_2);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_2);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 2 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(5961, 1057) < 100) {
                    if(scUnit_3==null) {
                        scUnit_3 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_3);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_3);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 3 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(319, 2288) < 100) {
                    if(scUnit_4==null) {
                        scUnit_4 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_4);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_4);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 4 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(3113, 2329) < 100) {
                    if(scUnit_5==null) {
                        scUnit_5 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_5);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_5);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 5 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(5842, 2286) < 100) {
                    if(scUnit_6==null) {
                        scUnit_6 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_6);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_6);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 6 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(232, 3566) < 100) {
                    if(scUnit_7==null) {
                        scUnit_7 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_7);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_7);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 7 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(2681, 3556) < 50) {
                    if(scUnit_8==null) {
                        scUnit_8 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_8);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_8);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 8 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(5676, 3556) < 100) {
                    if(scUnit_9==null) {
                        scUnit_9 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_9);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_9);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 9 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(163, 4748) < 100) {
                    if(scUnit_10==null) {
                        scUnit_10 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_10);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_10);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 10 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(2883, 4765) < 100) {
                    if(scUnit_11==null) {
                        scUnit_11 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_11);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_11);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 11 initialized.");
                        }
                    }
                } else if (u.getPosition().getDistance(5754, 4745) < 100) {
                    if(scUnit_12==null) {
                        scUnit_12 = new ScoutingUnit(u);
                        scoutingUnits.add(scUnit_12);
                        scout_module.getUnitManager().addScoutingUnit(scUnit_12);
                        if (QExecutor.DEBUG) {
                            System.out.println("ID = " + u.getID()+" Type = "+u.getType().toString());
                            System.out.println("ScoutingUnit 12 initialized.");
                        }
                    }
                }
            }
        }
        if (QExecutor.DEBUG) {
            System.out.println("Scouting units count = " + scoutingUnits.size());
        }
    }

    public void initializeSafePositions() {
        for (Unit u : game.getAllUnits()) {
            if (u.getPlayer() == game.self()) {
                if (u.getType() == UnitType.Terran_Bunker) {
                    if (u.getPosition().getDistance(1008, 258) < 200) {
                        if(endPosition==null) {
                            endPosition = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("EndPosition initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(1648, 1056) < 100) {
                        if(safePosition_1==null) {
                            safePosition_1 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 1 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(4656, 1056) < 100) {
                        if(safePosition_2==null) {
                            safePosition_2 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 2 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(7632, 1088) < 100) {
                        if(safePosition_3==null) {
                            safePosition_3 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 3 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(1936, 2304) < 100) {
                        if(safePosition_4==null) {
                            safePosition_4 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 4 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(4848, 2304) < 100) {
                        if(safePosition_5==null) {
                            safePosition_5 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 5 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(8016, 2304) < 100) {
                        if(safePosition_6==null) {
                            safePosition_6 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 6 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(1712, 3584) < 100) {
                        if(safePosition_7==null) {
                            safePosition_7 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 7 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(4752, 3520) < 100) {
                        if(safePosition_8==null) {
                            safePosition_8 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 8 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(7920, 3584) < 100) {
                        if(safePosition_9==null) {
                            safePosition_9 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 9 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(1808, 4800) < 100) {
                        if(safePosition_10==null) {
                            safePosition_10 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 10 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(4720, 4800) < 100) {
                        if(safePosition_11==null) {
                            safePosition_11 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 11 initialized.");
                            }
                        }
                    } else if (u.getPosition().getDistance(7696, 4768) < 100) {
                        if(safePosition_12==null) {
                            safePosition_12 = u.getPosition();
                            safePositions.add(u.getPosition());
                            if (QExecutor.DEBUG) {
                                System.out.println("SafePosition 12 initialized.");
                            }
                        }
                    }
                }
            }
        }
        if (QExecutor.DEBUG) {
            System.out.println("Safepositions = " + safePositions.size());
        }
    }

    public void execute_1() {
        scout_module.getActionManager().scoutPosition(safePosition_1, scUnit_1);
    }

    public void execute_2() {
        scout_module.getActionManager().scoutPosition(safePosition_2, scUnit_2);
    }

    public void execute_3() {
        scout_module.getActionManager().scoutPosition(safePosition_3, scUnit_3);
    }

    public void execute_4() {
        scout_module.getActionManager().scoutPosition(safePosition_4, scUnit_4);
    }

    public void execute_5() {
        scout_module.getActionManager().scoutPosition(safePosition_5, scUnit_5);
    }

    public void execute_6() {
        scout_module.getActionManager().scoutPosition(safePosition_6, scUnit_6);
    }

    public void execute_7() {
        scout_module.getActionManager().scoutPosition(safePosition_7, scUnit_7);
    }

    public void execute_8() {
        scout_module.getActionManager().scoutPosition(safePosition_8, scUnit_8);
    }

    public void execute_9() {
        scout_module.getActionManager().scoutPosition(safePosition_9, scUnit_9);
    }

    public void execute_10() {
        scout_module.getActionManager().scoutPosition(safePosition_10, scUnit_10);
    }

    public void execute_11() {
        scout_module.getActionManager().scoutPosition(safePosition_11, scUnit_11);
    }

    public void execute_12() {
        scout_module.getActionManager().scoutPosition(safePosition_12, scUnit_12);
    }

    public void execute_END() {
        scout_module.getActionManager().scoutPosition(endPosition, endUnit);
    }

    public void drawAll() {
//        drawSafePositions();
//        drawSelectedIDs();
//        drawActualScoutingUnit();
    }

    public void drawSelectedIDs() {
        for (Unit u : game.getAllUnits()) {
            if (u.isSelected()) {
                game.drawTextMap(u.getPosition(), "" + u.getID());
                game.drawTextMap(u.getX(), u.getY() + 50, "" + u.getPosition().toString());
                if(u.getID()==endUnit.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "endUnit");
                } else if(u.getID()==scUnit_1.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 1");
                } else if(u.getID()==scUnit_2.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 2");
                } else if(u.getID()==scUnit_3.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 3");
                } else if(u.getID()==scUnit_4.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 4");
                } else if(u.getID()==scUnit_5.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 5");
                } else if(u.getID()==scUnit_6.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 6");
                } else if(u.getID()==scUnit_7.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 7");
                } else if(u.getID()==scUnit_8.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 8");
                } else if(u.getID()==scUnit_9.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 9");
                } else if(u.getID()==scUnit_10.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 10");
                } else if(u.getID()==scUnit_11.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 11");
                } else if(u.getID()==scUnit_12.getUnit().getID()) {
                    game.drawTextMap(u.getX(), u.getY() + 70, "scUnit 12");
                }
            }
        }
    }

    public void drawSafePositions() {
        for (Position pos : safePositions) {
            game.drawCircleMap(pos, 130, Color.White);
            if(pos.getX()==safePosition_1.getX()&&pos.getY()==safePosition_1.getY()) {
                game.drawTextMap(pos,"SafePosition 1");
            } else if(pos.getX()==safePosition_2.getX()&&pos.getY()==safePosition_2.getY()) {
                game.drawTextMap(pos,"SafePosition 2");
            } else if(pos.getX()==safePosition_3.getX()&&pos.getY()==safePosition_3.getY()) {
                game.drawTextMap(pos,"SafePosition 3");
            } else if(pos.getX()==safePosition_4.getX()&&pos.getY()==safePosition_4.getY()) {
                game.drawTextMap(pos,"SafePosition 4");
            } else if(pos.getX()==safePosition_5.getX()&&pos.getY()==safePosition_5.getY()) {
                game.drawTextMap(pos,"SafePosition 5");
            } else if(pos.getX()==safePosition_6.getX()&&pos.getY()==safePosition_6.getY()) {
                game.drawTextMap(pos,"SafePosition 6");
            } else if(pos.getX()==safePosition_7.getX()&&pos.getY()==safePosition_7.getY()) {
                game.drawTextMap(pos,"SafePosition 7");
            } else if(pos.getX()==safePosition_8.getX()&&pos.getY()==safePosition_8.getY()) {
                game.drawTextMap(pos,"SafePosition 8");
            } else if(pos.getX()==safePosition_9.getX()&&pos.getY()==safePosition_9.getY()) {
                game.drawTextMap(pos,"SafePosition 9");
            } else if(pos.getX()==safePosition_10.getX()&&pos.getY()==safePosition_10.getY()) {
                game.drawTextMap(pos,"SafePosition 10");
            } else if(pos.getX()==safePosition_11.getX()&&pos.getY()==safePosition_11.getY()) {
                game.drawTextMap(pos,"SafePosition 11");
            } else if(pos.getX()==safePosition_12.getX()&&pos.getY()==safePosition_12.getY()) {
                game.drawTextMap(pos,"SafePosition 12");
            } else if(pos.getX()==endPosition.getX()&&pos.getY()==endPosition.getY()) {
                game.drawTextMap(pos,"endPosition");
            }
        }
    }

    public void drawActualScoutingUnit() {
        game.drawCircleMap(actualScoutingUnit.getUnit().getPosition(), 80, Color.Orange);
    }

    public void drawScoutingUnits() {
        for (ScoutingUnit scu : scoutingUnits) {
            if(scu.getUnit().getID()==endUnit.getUnit().getID()) {
                System.out.println("true");
                game.drawTextMap(scu.getUnit().getPosition(),"EndUnit");
            }
        }
    }

    public void showAll() {
        showReward();
        showActualUnitStats();
    }

    public void showReward() {
        game.drawTextScreen(20, 20, "Reward     = " + Integer.toString(reward));
    }

    public void showActualUnitStats() {
        game.drawTextScreen(20, 40, "IsAlive    = " + actualScoutingUnit.getUnit().exists());
        game.drawTextScreen(20, 60, "Unit HP    = " + actualScoutingUnit.getUnit().getHitPoints());
        game.drawTextScreen(20, 80, "Unit path  = " + actualScoutingUnit.getMicroPathChooser());
    }

    public void cameraLockOnActualUnit() {
        if (actualScoutingUnit != null) {
            game.setScreenPosition(actualScoutingUnit.getUnit().getX() - 200, actualScoutingUnit.getUnit().getY() - 200);
        }
    }
}

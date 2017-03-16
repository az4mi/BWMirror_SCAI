package MODaStar;

import MapManager.*;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;

import java.util.ArrayList;

/**
 * Path calculator runs as separate thread. Variable "finished" indicates if the path is ready
 */
public class AStarPathCalculator implements Runnable {

    public static boolean DEBUG=false;

    private Thread t;

    private Position startPosition;

    private Position destinationPosition;

    private boolean airPath;

    private GridMap gridMap;

    private ArrayList<Block> blockPathArray=null;

    public boolean finished;

    public int startingHealth;

    public int levelOfSafety;

    public int constant_K=0;

    public Color pathColor;

    public AStarPathCalculator(Position pStartPosition, Position pDestinationPosition, int pStartingHealth, int pLevelOfSafety, boolean pAirPath, GridMap pGridMap, Game pGame, Color pColor) {
        this.startPosition=pStartPosition;
        this.destinationPosition=pDestinationPosition;
        this.startingHealth=pStartingHealth;
        this.levelOfSafety=pLevelOfSafety;
        this.airPath=pAirPath;
        this.gridMap=new GridMap(pGridMap,pGame);
        this.finished=false;
        this.pathColor=pColor;
    }

    public void start() {
        if(t==null) {
            t=new Thread(this);
            t.start();
        }
    }

    /**
     * Starts pathfinding with modified A* algorithm
     */
    public void run() {

        if(AStarPathCalculator.DEBUG) {
            System.out.println("--:: Beginning pathfinding method ::--");
        }

        ArrayList<Block> blockPath=new ArrayList<>();
        Tree bst=new Tree();

        Block actualBlock=gridMap.getBlockByPosition_blockMap(startPosition);
        actualBlock.setHealth(startingHealth);
        actualBlock.setStartingBlock(true);
        actualBlock.setDistance_value(0);
        actualBlock.setDestination_distance(0);
        actualBlock.setFValue(0);

        if(AStarPathCalculator.DEBUG) {
            System.out.println("::: A* run :::");
            System.out.println("--- STARTING BLOCK ---");
            System.out.println(""+actualBlock.toString());
        }


        switch (levelOfSafety) {
            case 0: constant_K=0;
                break;
            case 1: constant_K=15; //5
                break;
            case 2: constant_K=6; //10
                break;
            case 3: constant_K=2;
                break;
            case 4: constant_K=20;
                break;
            case 5: constant_K=30; //30
        }

        if(AStarPathCalculator.DEBUG) {
            System.out.println(" Safety level = "+levelOfSafety);
            System.out.println(" Constant K   = "+constant_K);
        }

        Block destinationBlock=gridMap.getBlockByPosition_blockMap(destinationPosition);

        if(AStarPathCalculator.DEBUG) {
            System.out.println("--- DESTINATION BLOCK ---");
            System.out.println(""+destinationBlock.toString());
        }

        boolean found=false;
        Block blk;

        if(AStarPathCalculator.DEBUG) {
            System.out.println("     - Starting A*");
        }

        long startTime=System.currentTimeMillis();
        while(!found) {
            for(Block b:gridMap.getNeighbourBlocks(actualBlock)) {
                if(b.getRow()==destinationBlock.getRow()&&b.getColumn()==destinationBlock.getColumn()) {
                    long endTime=System.currentTimeMillis();

                    if(AStarPathCalculator.DEBUG) {
                        System.out.println("     - Path found !");
                        System.out.println("     - A* duration [ms] = " + (endTime - startTime));
                    }

                    destinationBlock.setParent(actualBlock);
                    found=true;
                }
                if(!found) {
                    blockInitializer(actualBlock,b,destinationBlock,bst,airPath);
                }
            }

            if(!found) {
                blk=bst.getSmallestBlock();
                if(blk==null) {
                    if(AStarPathCalculator.DEBUG) {
                        System.out.println("     - Can not find path !");
                    }
                    //ToDo: Osetrenie - zmierneneie podmienok
                    finished=true;
                    break;
                } else {
                    actualBlock=blk;
                    bst.delete(blk);
                }
            }
        }


        if(found) {
            if(AStarPathCalculator.DEBUG) {
                System.out.println("     - Assembling path to array");
            }

            Block workingBlock;
            workingBlock=destinationBlock;

            boolean pathCompleted=false;

            while(!pathCompleted) {
                workingBlock.setColor(pathColor);
                blockPath.add(workingBlock);
                workingBlock=workingBlock.getParent();
                if(workingBlock.isStartingBlock()) {
                    pathCompleted=true;
                }
            }

            if(AStarPathCalculator.DEBUG) {
                System.out.println("     - Path size [blocks] = " + blockPath.size());
                System.out.println("     - Path distance = " + blockPath.get(1).getDistance_value());
                System.out.println("--:: Pathfinding method ended ::--");
            }

            blockPathArray=blockPath;
            finished=true;
        }
    }

    /**
     * Initialization method for neighbour block
     *
     * @param pActualBlock
     * @param pNeighbourBlock
     * @param pDestinationBlock
     * @param pBinaryTree
     * @param pAirPath
     * @return
     */
    public int blockInitializer(Block pActualBlock, Block pNeighbourBlock, Block pDestinationBlock, Tree pBinaryTree, boolean pAirPath) {
        double newDistance=(pActualBlock.getDistance_value()+(pNeighbourBlock.getPosition().getDistance(pActualBlock.getPosition())));
        if(!pNeighbourBlock.isStartingBlock()) {
            if(pAirPath) {
                if(!pNeighbourBlock.hasParent()) {
                    if(pNeighbourBlock.isAirDamage()) {
                        if(pActualBlock.getHealth()-pNeighbourBlock.getDamage()>0) {
                            pNeighbourBlock.setHealth((int) (pActualBlock.getHealth() - pNeighbourBlock.getDamage()));
                            pNeighbourBlock.setParent(pActualBlock);
                            pNeighbourBlock.setDistance_value(newDistance);
                            pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                            pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));

                            pBinaryTree.insert(new Block(pNeighbourBlock));

                            return 1;
                        }
                    } else {
                        pNeighbourBlock.setHealth((pActualBlock.getHealth()));
                        pNeighbourBlock.setParent(pActualBlock);
                        pNeighbourBlock.setDistance_value(newDistance);
                        pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                        pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));

                        pBinaryTree.insert(new Block(pNeighbourBlock));

                        return 1;
                    }

                } else {
                    if(pNeighbourBlock.isAirDamage()) {
                        if((newDistance<pNeighbourBlock.getDistance_value()&&pActualBlock.getHealth()-pNeighbourBlock.getDamage()>0)||(pActualBlock.getHealth()-pNeighbourBlock.getDamage()>pNeighbourBlock.getHealth()&&newDistance<=pNeighbourBlock.getDistance_value())) {

                            pBinaryTree.delete(pNeighbourBlock);

                            pNeighbourBlock.setHealth((int) (pActualBlock.getHealth() - pNeighbourBlock.getDamage()));
                            pNeighbourBlock.setDistance_value(newDistance);
                            pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                            pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));
                            pNeighbourBlock.setParent(pActualBlock);
                            pNeighbourBlock.setLeftChild(null);
                            pNeighbourBlock.setRightChild(null);

                            pBinaryTree.insert(new Block(pNeighbourBlock));

                            return 2;
                        }
                    } else {
                        if((newDistance<pNeighbourBlock.getDistance_value())) {

                            pBinaryTree.delete(pNeighbourBlock);

                            pNeighbourBlock.setHealth((pActualBlock.getHealth()));
                            pNeighbourBlock.setDistance_value(newDistance);
                            pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                            pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));
                            pNeighbourBlock.setParent(pActualBlock);
                            pNeighbourBlock.setLeftChild(null);
                            pNeighbourBlock.setRightChild(null);

                            pBinaryTree.insert(new Block(pNeighbourBlock));

                            return 2;
                        }
                    }
                }
            } else {
                if(pNeighbourBlock.isAccessibleByGround()) {
                    if(!pNeighbourBlock.hasParent()) {
                        if(pNeighbourBlock.isGroundDamage()) {
                            if(pActualBlock.getHealth()-pNeighbourBlock.getDamage()>0) {
                                pNeighbourBlock.setHealth((int) (pActualBlock.getHealth() - pNeighbourBlock.getDamage()));
                                pNeighbourBlock.setParent(pActualBlock);
                                pNeighbourBlock.setDistance_value(newDistance);
                                pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                                pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));

                                pBinaryTree.insert(new Block(pNeighbourBlock));

                                return 1;
                            }
                        } else {
                            pNeighbourBlock.setHealth((pActualBlock.getHealth()));
                            pNeighbourBlock.setParent(pActualBlock);
                            pNeighbourBlock.setDistance_value(newDistance);
                            pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                            pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));

                            pBinaryTree.insert(new Block(pNeighbourBlock));

                            return 1;
                        }

                    } else {
                        if(pNeighbourBlock.isGroundDamage()) {
                            if((newDistance<pNeighbourBlock.getDistance_value()&&pActualBlock.getHealth()-pNeighbourBlock.getDamage()>0)||(pActualBlock.getHealth()-pNeighbourBlock.getDamage()>pNeighbourBlock.getHealth()&&newDistance<=pNeighbourBlock.getDistance_value())) {

                                pBinaryTree.delete(pNeighbourBlock);

                                pNeighbourBlock.setHealth((int) (pActualBlock.getHealth() - pNeighbourBlock.getDamage()));
                                pNeighbourBlock.setDistance_value(newDistance);
                                pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                                pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));
                                pNeighbourBlock.setParent(pActualBlock);
                                pNeighbourBlock.setLeftChild(null);
                                pNeighbourBlock.setRightChild(null);

                                pBinaryTree.insert(new Block(pNeighbourBlock));

                                return 2;
                            }
                        } else {
                            if((newDistance<pNeighbourBlock.getDistance_value())) {

                                pBinaryTree.delete(pNeighbourBlock);

                                pNeighbourBlock.setHealth((pActualBlock.getHealth()));
                                pNeighbourBlock.setDistance_value(newDistance);
                                pNeighbourBlock.setDestination_distance(getManhattanDistance(pNeighbourBlock, pDestinationBlock));
                                pNeighbourBlock.setFValue(pNeighbourBlock.getDistance_value() + pNeighbourBlock.getDestination_distance() - (pNeighbourBlock.getHealth() * constant_K));
                                pNeighbourBlock.setParent(pActualBlock);
                                pNeighbourBlock.setLeftChild(null);
                                pNeighbourBlock.setRightChild(null);

                                pBinaryTree.insert(new Block(pNeighbourBlock));

                                return 2;
                            }
                        }

                    }
                }
            }
        }
        return 0;
    }

    public int getManhattanDistance(Block pActualBlock, Block pDestinationBlock) {
        int actualRow=pActualBlock.getRow();
        int actualColumn=pActualBlock.getColumn();
        int destinationRow=pDestinationBlock.getRow();
        int destinationColumn=pDestinationBlock.getColumn();

        int manhattanDistance=Math.abs(actualRow - destinationRow)+Math.abs(actualColumn - destinationColumn);

        return manhattanDistance*MapManager.GRIDEDGESIZE;
    }

    public ArrayList<Block> getBlockPathArray() {
        return blockPathArray;
    }

    public Position getDestinationPosition() {
        return destinationPosition;
    }

    public void setDestinationPosition(Position destinationPosition) {
        this.destinationPosition = destinationPosition;
    }
}

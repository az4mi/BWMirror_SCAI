package MapManager.MapLayers;

import MapManager.Block;
import MapManager.GridBasedMap;
import MapManager.PotentialField;
import bwapi.*;

import java.util.*;

/**
 * Created by Chudjak Kristián on 27.02.2017.
 */
public class WalkableLayer extends Layer {
    private static final int TILE_SIZE = 32;
    private static final int STARTING_VALUE = 5;
    private HashMap<Integer,PotentialField> blockingUnits;

    public WalkableLayer(Game game) {
        this.game = game;
        this.blockingUnits = new HashMap<>(200);
        System.out.println("Map size: " + game.mapWidth() + "," + game.mapHeight());
        this.gridMap = new GridBasedMap(game.mapWidth(), game.mapHeight());
        for (int i = 0; i < game.mapWidth(); i++) {
            for (int j = 0; j < game.mapHeight(); j++) {
                Position position = new Position(j * TILE_SIZE + TILE_SIZE / 2, i * TILE_SIZE + TILE_SIZE / 2);
                Block b = new Block(position, TILE_SIZE, i, j);
                b.setValue(0);
                gridMap.getBlockMap()[i][j] = b;
            }
        }

        setBlocks();


    }


    private void setBlocks() {
        for (Unit unit :
                game.getAllUnits()) {
            setResourcesFields(unit);
            setCommandCentres(unit);
        }
    }





    private int frameCounter = 1;
    @Override
    public void manage() {
        if(frameCounter % 100 == 0){
            long milis = System.currentTimeMillis();
            frameCounter = 1;
            managePotFields();
            for (PotentialField pf :
                    blockingUnits.values()) {
                refreshGridMapNonRecursive(pf);
            }

            System.out.println("manage walckable time: "+(System.currentTimeMillis() - milis));
        }
        frameCounter ++;
    }

    private void managePotFields() {
        for(Unit unit:game.getAllUnits()) {
            if(unitContained(unit)){
                PotentialField pf = blockingUnits.get(unit.getID());
                pf.refreshPosition(unit);
            }else{
                setResourcesFields(unit);
                setCommandCentres(unit);
            }
        }

    }

    private boolean unitContained(Unit unit) {
        return blockingUnits.containsKey(unit.getID());
    }


    public void refreshGridMapNonRecursive(PotentialField pf) {
        gridMap.zmenene.clear();
        Queue<Block> blocks = gridMap.blocks;
        blocks.clear();
        Block centerBlock = gridMap.getBlockByPosition_blockMap(pf.getPosition());
        UnitType unit = pf.getUnitType();
        blokSetValue(centerBlock, STARTING_VALUE,pf);
        ArrayList<Block> neighbour = gridMap.getNeighbourBlocks(centerBlock);
        for (Block b :
                neighbour) {
            blocks.add(b);
            blokSetValue(b, gridMap.getValueForBlockDec(b),pf);
        }

        while (!blocks.isEmpty()) {
            Block block = blocks.poll();
            neighbour = gridMap.getNeighbourBlocks(block);

            for (Block b :
                    neighbour) {
                if (pf.isPositionInRange(b.getPosition())) {
                    if (!b.isSet()) {
                        blokSetValue(b, gridMap.getValueForBlockDec(b),pf);
                        blocks.add(b);
                    }
                }
            }

        }

        for (Block block :
                gridMap.zmenene) {
            block.setSet(false);
        }
    }

    private void blokSetValue(Block blok, double value,PotentialField pf) {
        if (blok.getValue() < value) {
            blok.setValue(value);
        }
        blok.setValue(value);

        blok.setSet(true);
        gridMap.zmenene.add(blok);
    }


    private void setBlockValue(Block b, Block centerBlock) {
        gridMap.blokSetValue(b, ((centerBlock.getValue() <= 0) ? 0 : centerBlock.getValue() - 1));
    }


    private void setCommandCentres(Unit unit) {
        if (unit.getType() == UnitType.Terran_Command_Center ||
                unit.getType() == UnitType.Zerg_Infested_Command_Center ||
                unit.getType() == UnitType.Protoss_Nexus){
            Position position = unit.getPosition();
            PotentialField pf = new PotentialField(game,position,70);
            blockingUnits.put(unit.getID(),pf);

            /*
            Block block = gridMap.getBlock(position);
            block.setValue(STARTING_VALUE);

            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn() -2].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn() -1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn()].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn() + 1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow()][block.getColumn() - 2].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow()][block.getColumn() - 1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow()][block.getColumn() + 1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() + 1][block.getColumn() - 2].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() + 1][block.getColumn() - 1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() + 1][block.getColumn()].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() + 1][block.getColumn() + 1].setValue(STARTING_VALUE);

            */
        }
    }


    public boolean isMineralField(Unit unit) {
        if (unit.getType() == UnitType.Resource_Mineral_Field ||
                unit.getType() == UnitType.Resource_Mineral_Field_Type_2 ||
                unit.getType() == UnitType.Resource_Mineral_Field_Type_3) {
            return true;
        }
        return false;
    }

    private void setResourcesFields(Unit unit) {
        // mineral field
        if (isMineralField(unit)) {
            Position position = unit.getPosition();
            PotentialField pf = new PotentialField(game,position,50);
            blockingUnits.put(unit.getID(),pf);
            /*
            Block block = gridMap.getBlock(position);
            block.setValue(STARTING_VALUE);

            if (block.getRow() != 0) {
                gridMap.getBlockMap()[block.getRow() - 1][block.getColumn()].setValue(STARTING_VALUE);
            }
            if (block.getColumn() != 0) {
                gridMap.getBlockMap()[block.getRow()][block.getColumn() - 1].setValue(STARTING_VALUE);
            }

            if (block.getColumn() != 0 && block.getRow() != 0) {
                gridMap.getBlockMap()[block.getRow() - 1][block.getColumn() - 1].setValue(STARTING_VALUE);
            }
           */

        } else if (unit.getType() == UnitType.Resource_Vespene_Geyser) {
            /*Position position = unit.getPosition();
            Block block = gridMap.getBlock(position);
            block.setValue(STARTING_VALUE);
            System.out.println("size: " + unit.getType().size());
            System.out.println(block.getRow() + "," + block.getColumn());
            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn()].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn() - 1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow() - 1][block.getColumn() - 2].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow()][block.getColumn() - 1].setValue(STARTING_VALUE);
            gridMap.getBlockMap()[block.getRow()][block.getColumn() - 2].setValue(STARTING_VALUE);
*/

        }


    }

    @Override
    public void draw() {
        for (PotentialField pf :
                blockingUnits.values()) {
            pf.showGraphicsCircular(Color.Blue);
        }

        List<Unit> units = game.getSelectedUnits();

        for (Unit unit :
                units) {
            int row = unit.getPosition().getY() / TILE_SIZE;
            int col = unit.getPosition().getX() / TILE_SIZE;
            for (int i = row - 5; i < row + 5; i++) {
                for (int j = col - 5; j < col + 5; j++) {
                    if (i >= 0 && i < gridMap.getBlockMap().length) {
                        if (j >= 0 && j < gridMap.getBlockMap()[0].length)
                            if (gridMap.getBlockMap()[i][j].getValue() == STARTING_VALUE) {
                                gridMap.getBlockMap()[i][j].drawBlock(Color.Red, game);
                            } else {
                                gridMap.getBlockMap()[i][j].drawBlock(Color.Green, game);
                            }
                    }
                }
            }


        }

    }
}

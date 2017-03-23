package MapManager.MapLayers;

import MapManager.Block;
import MapManager.GridBasedMap;
import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.Unit;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Chudjak Kristián on 02.03.2017.
 */
public class StaticWalkableLayer extends Layer {
    private static final int TILE_SIZE = 32;
    private static final int STARTING_VALUE = 5;

    public StaticWalkableLayer(Game game) {
        this.game = game;
        System.out.println("Map size: " + game.mapWidth() + "," + game.mapHeight());
        this.gridMap = new GridBasedMap(game.mapWidth(), game.mapHeight());
        for (int i = 0; i < game.mapWidth(); i++) {
            for (int j = 0; j < game.mapHeight(); j++) {
                Position position = new Position(j * TILE_SIZE + TILE_SIZE / 2, i * TILE_SIZE + TILE_SIZE / 2);
                Block b = new Block(position, TILE_SIZE, i, j);
                b.setValue(isTileWalkable(position) ? 0 : STARTING_VALUE);
                gridMap.getBlockMap()[i][j] = b;
            }
        }

        for (int i = 0; i < game.mapWidth(); i++) {
            for (int j = 0; j < game.mapHeight(); j++) {
                Block b = gridMap.getBlockMap()[i][j];
                if (b.getValue() == STARTING_VALUE) {
                    setWalkableGridMap(b);
                }
            }
        }

    }


    public void setWalkableGridMap(Block block) {
        gridMap.zmenene.clear();


        Queue<Block> blocks = gridMap.blocks;
        blocks.clear();
        Block centerBlock = block;
        gridMap.blokSetValue(centerBlock, STARTING_VALUE);
        ArrayList<Block> neighbour = gridMap.getNeighbourBlocks(centerBlock);
        int pom = 0;
        for (Block b :
                neighbour) {
            blocks.add(b);
            setBlockValue(b, centerBlock);
        }
        while (!blocks.isEmpty()) {
            Block curBlock = blocks.poll();
            neighbour = gridMap.getNeighbourBlocks(curBlock);

            for (Block b :
                    neighbour) {
                if (!b.isSet()) {
                    if (b.getValue() >= curBlock.getValue()) {
                        gridMap.blokSetValue(b,b.getValue());
                    } else {

                        setBlockValue(b, curBlock);
                        if (b.getValue() > 0 && curBlock.getValue() > b.getValue()) {
                            blocks.add(b);
                        }
                    }
                }
                pom++;
            }

        }

        for (Block b :
                gridMap.zmenene) {
            b.setSet(false);
        }



    }

    private void setBlockValue(Block b, Block centerBlock) {
        gridMap.blokSetValue(b, ((centerBlock.getValue() <= 0) ? 0 : centerBlock.getValue() - 1));
    }

    /*private boolean isTileWalkable(Position position) {
        return game.isWalkable(position.getX() / 8, position.getY() / 8);
    }*/
    private boolean isTileWalkable(Position position) {
        return game.isWalkable((position.getX() / 8) - 2, (position.getY() / 8) - 1) &&
                game.isWalkable((position.getX() / 8) - 2, (position.getY() / 8) + 1) &&
                game.isWalkable((position.getX() / 8) + 1, (position.getY() / 8) - 1) &&
                game.isWalkable((position.getX() / 8) + 1, (position.getY() / 8) + 1);
    }


    @Override
    public void draw() {
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

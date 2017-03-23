package MapManager;

import bwapi.*;

import java.util.*;

/**
 * Grid map consists of array of blocks. Provides GEO information for other classes.
 */
public class GridBasedMap {

    public static boolean DEBUG = true;

    private Block[][] blockMap;


    private int rows;

    private int columns;

    private List<Position> unwalkablePositions;

    public static final double DAMAGE_MODIFIER = 0.3;

    public static final boolean SHOW_GRIDINPOTENTIALFIELD = false;


    private Game game;
    /* ------------------- Constructors ------------------- */


    public GridBasedMap(int rectangleSidePX, Game pGame) {
        game = pGame;
        rows = (pGame.mapHeight() * TilePosition.SIZE_IN_PIXELS) / rectangleSidePX;
        columns = (pGame.mapWidth() * TilePosition.SIZE_IN_PIXELS) / rectangleSidePX;

        blockMap = new Block[rows][columns];
        unwalkablePositions = new LinkedList<>();


        if (DEBUG) {
            System.out.println("--:: GridMap initialization ::--");
            System.out.println("     - Rectangle size = " + rectangleSidePX);
            System.out.println("     - Map X = " + pGame.mapWidth() * TilePosition.SIZE_IN_PIXELS + " ,Grid rows = " + rows);
            System.out.println("     - Map Y = " + pGame.mapHeight() * TilePosition.SIZE_IN_PIXELS + " ,Grid cols = " + columns);
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {

                Block b = new Block(new Position((rectangleSidePX / 2) + rectangleSidePX * j, (rectangleSidePX / 2) + rectangleSidePX * i), rectangleSidePX, i, j, pGame);
                b.setAccessibleByGround(pGame.isWalkable(b.getPosition().getX() / 8, b.getPosition().getY() / 8));
                blockMap[i][j] = b;
            }
        }

        if (DEBUG) {
            System.out.println("BlockMap size = " + getBlockMapSize());
        }
    }

    public GridBasedMap(GridBasedMap pGridMap, Game game) {
        rows = pGridMap.getRows();
        columns = pGridMap.getColumns();
        blockMap = new Block[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Block b = new Block(pGridMap.getBlockMap()[i][j].getPosition(), pGridMap.getBlockMap()[i][j].getRadius(), i, j, game);
                b.setValue(pGridMap.getBlockMap()[i][j].getValue());
                b.setAirDamage(pGridMap.getBlockMap()[i][j].isAirDamage());
                b.setAirDamageValue(pGridMap.getBlockMap()[i][j].getAirDamageValue());
                b.setGroundDamage(pGridMap.getBlockMap()[i][j].isGroundDamage());
                b.setGroundDamageValue(pGridMap.getBlockMap()[i][j].getGroundDamageValue());
                b.setInPotentialField(pGridMap.getBlockMap()[i][j].isInPotentialField());
                b.setAccessibleByGround(pGridMap.getBlockMap()[i][j].isAccessibleByGround());
                blockMap[i][j] = b;
            }
        }
    }

    public GridBasedMap(int pRows, int pColumns) {
        rows = pRows;
        columns = pColumns;
        blockMap = new Block[rows][columns];
    }


    /* ------------------- Initialization methods ------------------- */

    public void initializeBlockMap(Game pGame) {
        double mapWidth = pGame.mapWidth();
        double mapHeight = pGame.mapHeight();
        int radius = (int) (mapWidth / rows);

        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < columns; i++) {
                Block block = new Block(new Position((radius / 2) + (radius * i), (radius / 2) + (radius * j)), radius, j, i, pGame);
                blockMap[j][i] = block;
            }
        }

        if (DEBUG) {
            System.out.println("BlockMap size = " + getBlockMapSize());
        }
    }

    /* ------------------- Main functionality methods ------------------- */


    /**
     * sets all blocks values to 0
     */
    public void refreshStart() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                blockMap[i][j].setValue(0);
                blockMap[i][j].setGroundDamageValue(0);
                blockMap[i][j].setAirDamageValue(0);
                blockMap[i][j].setSet(false);
            }
        }
    }

    // blocks that were changed
    public LinkedList<Block> zmenene = new LinkedList<>();
    public Queue<Block> blocks = new LinkedList<>();
    public void refreshGridMapNonRecursive(PotentialField pf) {
        zmenene.clear();
        blocks.clear();
        Block centerBlock = getBlockByPosition_blockMap(pf.getPosition());
        UnitType unit = pf.getUnitType();
        blokSetValue(centerBlock, getBlokValue(unit),pf);
        ArrayList<Block> neighbour = getNeighbourBlocks(centerBlock);
        for (Block b :
                neighbour) {
            blocks.add(b);
            blokSetValue(b, getValueForBlock(b,0.8),pf);
        }

        while (!blocks.isEmpty()) {
            Block block = blocks.poll();
            neighbour = getNeighbourBlocks(block);

            for (Block b :
                    neighbour) {
                if (pf.isPositionInRange(b.getPosition())) {
                    if (!b.isSet()) {
                        blokSetValue(b, getValueForBlock(b,0.8),pf);
                        blocks.add(b);
                    }
                }
            }

        }

        for (Block block :
                zmenene) {
            block.setSet(false);
        }
    }




    public void refreshGridMap(PotentialField pPotentialField) {
        zmenene.clear();
        Block centerBlock = getBlockByPosition_blockMap(pPotentialField.getPosition());
        UnitType unit = pPotentialField.getUnitType();
        blokSetValue(centerBlock, getBlokValue(unit),pPotentialField);
//        System.out.println("start");
        setValueForBlockNeighbour(centerBlock, pPotentialField);
//        System.out.println("end");
        for (Block block :
                zmenene) {
            block.setSet(false);
        }
    }


    private double getBlokValue(UnitType unit) {
        double value = unit.airWeapon().damageAmount() / (1 + unit.airWeapon().damageCooldown()) * 10;
        value += unit.groundWeapon().damageAmount() / (1 + unit.airWeapon().damageCooldown()) * 10;
        return value;
    }

    private void setValueForBlockNeighbour(Block block, PotentialField pf) {
        ArrayList<Block> neighbour = getNeighbourBlocks(block);
        for (Block b :
                neighbour) {
            if (pf.isPositionInRange(b.getPosition())) {
                if (!b.isSet()) {
                    blokSetValue(b, getValueForBlock(b,0.8),pf);
                    setValueForBlockNeighbour(b, pf);
                }
            }
        }
    }

    private void blokSetValue(Block blok, double value,PotentialField pf) {
        if (blok.getValue() < value) {
            blok.setValue(value);
        }
        setValuesForBlock(blok,pf);

        blok.setSet(true);
        zmenene.add(blok);
    }
    public void blokSetValue(Block blok, double value) {
        if (blok.getValue() < value) {
            blok.setValue(value);
        }

        blok.setSet(true);
        zmenene.add(blok);
    }


    public void setValuesForBlock(Block blok,PotentialField pf) {
        blok.setInPotentialField(true);
        boolean airToAir = pf.getUnitType().airWeapon().targetsAir();
        boolean airToGround = pf.getUnitType().airWeapon().targetsGround();
        boolean groundToGround = pf.getUnitType().groundWeapon().targetsGround();
        boolean groundToAir = pf.getUnitType().groundWeapon().targetsAir();

        if (airToAir || groundToAir) {
            blok.setAirDamage(true);
        }
        if (airToGround || groundToGround) {
            blok.setGroundDamage(true);
        }

        //System.out.println("Air DMG = "+blockMap[row][col].isAirDamage());
        //System.out.println("Ground DMG = "+blockMap[row][col].isGroundDamage());

        if (pf.getUnitType().groundWeapon().maxRange() > 30 || pf.getUnitType().airWeapon().maxRange() > 30) {
            blok.setValue(0.5*(blok.getValue() + DAMAGE_MODIFIER * pf.getUnitType().groundWeapon().damageAmount()));
        } else {
            blok.setValue(0.5*(blok.getValue() + DAMAGE_MODIFIER * (pf.getRangeLengthInPercent(blok.getPosition()) * (pf.getUnitType().groundWeapon().damageAmount()))));
        }
    }



    public double getValueForBlockDec(Block block){
        ArrayList<Block> neighbour = getNeighbourBlocks(block);
        double value = block.getValue();

        for (Block b :
                neighbour) {
            if (value < b.getValue()) {
                value = b.getValue();
            }
        }
        value--;
        return value < 0 ? 0: value;
    }

    private double getValueForBlock(Block block,double multipe) {


        ArrayList<Block> neighbour = getNeighbourBlocks(block);
        double value = block.getValue();

        for (Block b :
                neighbour) {
            if (value < b.getValue()) {
                value = b.getValue();
            }
        }
        return value * multipe;
/*
//        double value = block.getValue()* 4/16;
        Block[] neighbour = getNeighbourBlocksArray(block);
        double value = block.getValue()* 8/16;


        value += neighbour[1].getValue() * 2/16;
        value += neighbour[3].getValue() * 2/16;
        value += neighbour[5].getValue() * 2/16;
        value += neighbour[7].getValue() * 2/16;
        value += neighbour[0].getValue() * 1/16;
        value += neighbour[2].getValue() * 1/16;
        value += neighbour[6].getValue() * 1/16;
        value += neighbour[8].getValue() * 1/16;

        return value;

        */
    }

    public void updateGridMap(PotentialField pPotentialField) {
        Block centerBlock = getBlockByPosition_blockMap(pPotentialField.getPosition());
        int columnCounter = centerBlock.getColumn();

        double blockSideX = centerBlock.getRadius();
        int radiusBlockCount = 0;
        radiusBlockCount = (int) (pPotentialField.getRadius() / blockSideX);

        while (pPotentialField.isPositionInRange(blockMap[centerBlock.getRow()][columnCounter].getPosition())) {
            radiusBlockCount++;
            columnCounter++;
        }


        int row = centerBlock.getRow() - radiusBlockCount;
        int col = centerBlock.getColumn() - radiusBlockCount;
        int maxRowRange = centerBlock.getRow() + radiusBlockCount;
        int maxColRange = centerBlock.getColumn() + radiusBlockCount;

        while (row <= maxRowRange) {
            if (row < rows) {
                while (col <= maxColRange) {
                    if (col < columns) {
                        if (pPotentialField.isPositionInRange(blockMap[row][col].getPosition())) {
                            if (SHOW_GRIDINPOTENTIALFIELD) {
                                blockMap[row][col].setShowInGame(true);
                            }
                            blockMap[row][col].setInPotentialField(true);

                            boolean airToAir = pPotentialField.getUnitType().airWeapon().targetsAir();
                            boolean airToGround = pPotentialField.getUnitType().airWeapon().targetsGround();
                            boolean groundToGround = pPotentialField.getUnitType().groundWeapon().targetsGround();
                            boolean groundToAir = pPotentialField.getUnitType().groundWeapon().targetsAir();

                            if (airToAir || groundToAir) {
                                blockMap[row][col].setAirDamage(true);
                            }
                            if (airToGround || groundToGround) {
                                blockMap[row][col].setGroundDamage(true);
                            }

                            //System.out.println("Air DMG = "+blockMap[row][col].isAirDamage());
                            //System.out.println("Ground DMG = "+blockMap[row][col].isGroundDamage());

                            if (pPotentialField.getUnitType().groundWeapon().maxRange() > 30 || pPotentialField.getUnitType().airWeapon().maxRange() > 30) {
                                blockMap[row][col].setValue(blockMap[row][col].getValue() + DAMAGE_MODIFIER * pPotentialField.getUnitType().groundWeapon().damageAmount());
                            } else {
                                blockMap[row][col].setValue(blockMap[row][col].getValue() + DAMAGE_MODIFIER * (pPotentialField.getRangeLengthInPercent(blockMap[row][col].getPosition()) * (pPotentialField.getUnitType().groundWeapon().damageAmount())));
                            }
                        }
                    }
                    col++;
                }
            }
            row++;
            col = centerBlock.getColumn() - radiusBlockCount;
        }

        /*if(pPotentialField.isCombined()){
            updateGridMap(pPotentialField.getNeedleTip());
        }
*/
    }


    public Block getBlock(Position position){
        if(blockMap[0][0] == null) return null;
        double blockSize = blockMap[0][0].getRadius();
        int row = (int) (position.getY() / blockSize);
        int column = (int) (position.getX() / blockSize);
        return blockMap[row][column];
    }

    ArrayList<Block> bloky = new ArrayList<Block>(10);
    public List<Block> getBlocksWithinBlock(Block block) {
        bloky.clear();
        Vector2D topLeft = block.getLeftUpperCornerBoxVector();
        Vector2D bottomRight = block.getRightLowerCornerBoxVector();
        Block startingBlock = getBlock(block.getPosition());

        cycle:
        for (int i = startingBlock.getRow(); i < rows; i++) {
            for (int j = startingBlock.getColumn(); j < columns; j++) {
                if(block.isInPosition(blockMap[i][j].getPosition())){
                    bloky.add(blockMap[i][j]);
                }

                if(blockMap[i][j].getPosition().getY() > bottomRight.getY()){
                    break;
                }else if(blockMap[i][j].getPosition().getX() > bottomRight.getX()){
                    break cycle;
                }
            }


        }


        return bloky;
    }

    public Block getBlockByPosition_blockMap(Position position) {
        int posX = position.getX();
        int posY = position.getY();

        int blockLeftX = 0;
        int blockRightX = 0;
        int blockTopY = 0;
        int blockBottomY = 0;


        for (int j = 0; j < columns; j++) {
            blockLeftX = (int) (blockMap[0][j].getPosition().getX() - (blockMap[0][j].getRadius() / 2));
            blockRightX = (int) (blockMap[0][j].getPosition().getX() + (blockMap[0][j].getRadius() / 2));

            if (posX >= blockLeftX && posX <= blockRightX) {
                for (int i = 0; i < rows; i++) {
                    blockTopY = (int) (blockMap[i][j].getPosition().getY() - (blockMap[i][j].getRadius() / 2));
                    blockBottomY = (int) (blockMap[i][j].getPosition().getY() + (blockMap[i][j].getRadius() / 2));

                    if (posY >= blockTopY && posY <= blockBottomY) {
                        return blockMap[i][j];
                    }
                }
            }
        }
        return null;
    }

    public Block getBlockByRowAndColumn(int pRow, int pColumn) {
        if (pRow > -1 && pRow <= rows) {
            if (pColumn > -1 && pColumn <= columns) {
                return blockMap[pRow][pColumn];
            }
        }
        return null;
    }



    public int getBlockMapSize() {
        int size = 0;
        for (int i = 0; i < rows; i++) {
            size += blockMap[i].length;
        }
        return size;
    }


    public GridBasedMap add(GridBasedMap gridMap, GridBasedMap mergedMap){
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                Block b = mergedMap.getBlockMap()[i][j];
                if(b == null) {
                    b = new Block(blockMap[i][j].getPosition(), blockMap[i][j].getRadius(), i, j, game);
                }
                b.setValue(blockMap[i][j].getValue() + gridMap.blockMap[i][j].getValue() + b.getValue());
                b.setAirDamage(blockMap[i][j].isAirDamage() || gridMap.blockMap[i][j].isAirDamage());
                b.setAirDamageValue(blockMap[i][j].getAirDamageValue() + gridMap.blockMap[i][j].getAirDamageValue());
                b.setGroundDamage(blockMap[i][j].isGroundDamage() || gridMap.blockMap[i][j].isGroundDamage());
                b.setGroundDamageValue(blockMap[i][j].getGroundDamageValue() + gridMap.blockMap[i][j].getGroundDamageValue());
                b.setInPotentialField(blockMap[i][j].isInPotentialField() || gridMap.blockMap[i][j].isInPotentialField());
                b.setAccessibleByGround(blockMap[i][j].isAccessibleByGround() || gridMap.blockMap[i][j].isAccessibleByGround());
                mergedMap.blockMap[i][j] = b;
            }
        }
        return mergedMap;
    }



    /**
     * Returns array of neighbour blocks to given block
     *
     * @param pActualBlock
     * @return
     */
    public ArrayList<Block> getNeighbourBlocks(Block pActualBlock) {
        int actualPositionType = getBlockPositionType(pActualBlock);
        switch (actualPositionType) {
            case 1:
                return getTopLeftNeighbourCoordinates(pActualBlock);
            case 2:
                return getTopRightNeighbourCoordinates(pActualBlock);
            case 3:
                return getTopMiddleNeighbourCoordinates(pActualBlock);
            case 4:
                return getBottomLeftNeighbourCoordinates(pActualBlock);
            case 5:
                return getBottomRightNeighbourCoordinates(pActualBlock);
            case 6:
                return getBottomMiddleNeighbourCoordinates(pActualBlock);
            case 7:
                return getMiddleLeftNeighbourCoordinates(pActualBlock);
            case 8:
                return getMiddleRightNeighbourCoordinates(pActualBlock);
            case 9:
                return getMiddleMiddleNeighbourCoordinates(pActualBlock);
        }
        return null;
    }

    /*
    012
    345
    678
    4 je actual blok
     */
    public Block[] getNeighbourBlocksArray(Block pActualBlock) {
        int actualPositionType = getBlockPositionType(pActualBlock);
        switch (actualPositionType) {
            case 1:
                return getTopLeftNeighbour(pActualBlock);
            case 2:
                return getTopRightNeighbour(pActualBlock);
            case 3:
                return getTopMiddleNeighbour(pActualBlock);
            case 4:
                return getBottomLeftNeighbour(pActualBlock);
            case 5:
                return getBottomRightNeighbour(pActualBlock);
            case 6:
                return getBottomMiddleNeighbour(pActualBlock);
            case 7:
                return getMiddleLeftNeighbour(pActualBlock);
            case 8:
                return getMiddleRightNeighbour(pActualBlock);
            case 9:
                return getMiddleMiddleNeighbour(pActualBlock);
        }
        return null;
    }


    public ArrayList<Block> getTopLeftNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow; row <= actualRow + 1; row++) {
            for (int column = actualColumn; column <= actualColumn + 1; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getTopLeftNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockMiddleRight(neighbourBlocks, pActualBlock);
        setBlockBottomRight(neighbourBlocks, pActualBlock);
        setBlockBottomMiddle(neighbourBlocks, pActualBlock);

        return neighbourBlocks;
    }


    public ArrayList<Block> getTopRightNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow; row <= actualRow + 1; row++) {
            for (int column = actualColumn - 1; column <= actualColumn; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getTopRightNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockMiddleLeft(neighbourBlocks, pActualBlock);
        setBlockBottomLeft(neighbourBlocks, pActualBlock);
        setBlockBottomMiddle(neighbourBlocks, pActualBlock);

        return neighbourBlocks;
    }

    public ArrayList<Block> getTopMiddleNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow; row <= actualRow + 1; row++) {
            for (int column = actualColumn - 1; column <= actualColumn + 1; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getTopMiddleNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockMiddleLeft(neighbourBlocks, pActualBlock);
        setBlockMiddleRight(neighbourBlocks, pActualBlock);
        setBlockBottomLeft(neighbourBlocks, pActualBlock);
        setBlockBottomMiddle(neighbourBlocks, pActualBlock);
        setBlockBottomRight(neighbourBlocks, pActualBlock);


        return neighbourBlocks;
    }


    public ArrayList<Block> getBottomLeftNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow - 1; row <= actualRow; row++) {
            for (int column = actualColumn; column <= actualColumn + 1; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getBottomLeftNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockMiddleRight(neighbourBlocks, pActualBlock);
        setBlockTopMiddle(neighbourBlocks, pActualBlock);
        setBlockTopRight(neighbourBlocks, pActualBlock);

        return neighbourBlocks;
    }


    public ArrayList<Block> getBottomRightNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow - 1; row <= actualRow; row++) {
            for (int column = actualColumn - 1; column <= actualColumn; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getBottomRightNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockMiddleRight(neighbourBlocks, pActualBlock);
        setBlockTopMiddle(neighbourBlocks, pActualBlock);
        setBlockTopRight(neighbourBlocks, pActualBlock);


        return neighbourBlocks;
    }


    public ArrayList<Block> getBottomMiddleNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow - 1; row <= actualRow; row++) {
            for (int column = actualColumn - 1; column <= actualColumn + 1; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }


    public Block[] getBottomMiddleNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockTopLeft(neighbourBlocks, pActualBlock);
        setBlockTopMiddle(neighbourBlocks, pActualBlock);
        setBlockTopRight(neighbourBlocks, pActualBlock);
        setBlockMiddleLeft(neighbourBlocks, pActualBlock);
        setBlockMiddleRight(neighbourBlocks, pActualBlock);


        return neighbourBlocks;
    }

    public ArrayList<Block> getMiddleLeftNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow - 1; row <= actualRow + 1; row++) {
            for (int column = actualColumn; column <= actualColumn + 1; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getMiddleLeftNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockTopMiddle(neighbourBlocks, pActualBlock);
        setBlockTopRight(neighbourBlocks, pActualBlock);
        setBlockMiddleRight(neighbourBlocks, pActualBlock);
        setBlockBottomMiddle(neighbourBlocks, pActualBlock);
        setBlockBottomRight(neighbourBlocks, pActualBlock);

        return neighbourBlocks;
    }

    public ArrayList<Block> getMiddleRightNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow - 1; row <= actualRow + 1; row++) {
            for (int column = actualColumn - 1; column <= actualColumn; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getMiddleRightNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockTopMiddle(neighbourBlocks, pActualBlock);
        setBlockTopLeft(neighbourBlocks, pActualBlock);
        setBlockMiddleLeft(neighbourBlocks, pActualBlock);
        setBlockBottomMiddle(neighbourBlocks, pActualBlock);
        setBlockBottomLeft(neighbourBlocks, pActualBlock);

        return neighbourBlocks;
    }


    public ArrayList<Block> getMiddleMiddleNeighbourCoordinates(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        ArrayList<Block> neighbourBlocks = new ArrayList<>();

        for (int row = actualRow - 1; row <= actualRow + 1; row++) {
            for (int column = actualColumn - 1; column <= actualColumn + 1; column++) {

                if (!(row == actualRow && column == actualColumn)) {
                    neighbourBlocks.add(blockMap[row][column]);
                }
            }
        }
        return neighbourBlocks;
    }

    public Block[] getMiddleMiddleNeighbour(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        Block[] neighbourBlocks = new Block[9];

        setBlockTopMiddle(neighbourBlocks, pActualBlock);
        setBlockTopLeft(neighbourBlocks, pActualBlock);
        setBlockTopRight(neighbourBlocks, pActualBlock);

        setBlockMiddleLeft(neighbourBlocks, pActualBlock);
        setBlockMiddleRight(neighbourBlocks, pActualBlock);

        setBlockBottomMiddle(neighbourBlocks, pActualBlock);
        setBlockBottomLeft(neighbourBlocks, pActualBlock);
        setBlockBottomRight(neighbourBlocks, pActualBlock);


        return neighbourBlocks;
    }


    public void setBlockTopLeft(Block[] neighbour, Block block) {
        neighbour[0] = blockMap[block.getRow() - 1][block.getColumn() - 1];
    }

    public void setBlockTopMiddle(Block[] neighbour, Block block) {
        neighbour[1] = blockMap[block.getRow() - 1][block.getColumn()];
    }

    public void setBlockTopRight(Block[] neighbour, Block block) {
        neighbour[2] = blockMap[block.getRow() - 1][block.getColumn() + 1];
    }

    public void setBlockMiddleLeft(Block[] neighbour, Block block) {
        neighbour[3] = blockMap[block.getRow()][block.getColumn() - 1];
    }

    public void setBlockMiddleMiddle(Block[] neighbour, Block block) {
        neighbour[4] = blockMap[block.getRow()][block.getColumn()];
    }

    public void setBlockMiddleRight(Block[] neighbour, Block block) {
        neighbour[5] = blockMap[block.getRow()][block.getColumn() + 1];
    }

    public void setBlockBottomLeft(Block[] neighbour, Block block) {
        neighbour[6] = blockMap[block.getRow() + 1][block.getColumn() - 1];
    }

    public void setBlockBottomMiddle(Block[] neighbour, Block block) {
        neighbour[7] = blockMap[block.getRow() + 1][block.getColumn()];
    }

    public void setBlockBottomRight(Block[] neighbour, Block block) {
        neighbour[8] = blockMap[block.getRow() + 1][block.getColumn() + 1];
    }


    public int getBlockPositionType(Block pActualBlock) {
        int actualRow = pActualBlock.getRow();
        int actualColumn = pActualBlock.getColumn();
        int maxRows = rows;
        int maxColumns = columns;

        int actualPositionType = -1;

        if (actualRow - 1 < 0) {                                     //je v nultom riadku
            if (actualColumn - 1 < 0) {                              //je v L.H rohu
                actualPositionType = 1;                           //je v L.H. rohu - situacia 1
            } else if (actualColumn + 1 == maxColumns) {              //je v P.H. rohu
                actualPositionType = 2;                           //je v P.H. rohu - situacia 2
            } else {                                            //je na nultom riadku ale nie v rohoch
                actualPositionType = 3;                           //je na nultom riadku ale nie v rohoch - situacia 3
            }
        } else if (actualRow + 1 == maxRows) {                        //je v poslednom riadku
            if (actualColumn - 1 < 0) {                              //je v L.D rohu
                actualPositionType = 4;                           //je v L.D. rohu - situacia 4
            } else if (actualColumn + 1 == maxColumns) {              //je v P.D. rohu
                actualPositionType = 5;                           //je v P.D. rohu - situacia 5
            } else {                                            //je na maxumalnom riadku ale nie v rohoch
                actualPositionType = 6;                           //je na maximalnomm riadku ale nie v rohoch - situacia 6
            }
        } else {                                                //nie je na nultom ani na poslednom riadku
            if (actualColumn - 1 < 0) {                              //je na lavom kraji
                actualPositionType = 7;                           //je na lavom kraji - situacia 7
            } else if (actualColumn + 1 == maxColumns) {              //je na pravom kraji
                actualPositionType = 8;                           //je na pravom kraji - situacia 8
            } else {                                            //nie je na krajoch
                actualPositionType = 9;                           //nie je na krajoch - situacia 9
            }
        }

        //System.out.println("Block row="+actualRow+",column="+actualColumn+",position type="+actualPositionType);

        return actualPositionType;
    }

    public boolean isSameBlock(Block pBlock1, Block pBlock2) {
        if (pBlock1.getRow() == pBlock2.getRow() && pBlock1.getColumn() == pBlock2.getColumn()) {
            return true;
        }
        return false;
    }


    /* ------------------- Drawing methods ------------------- */

    public void drawDangerGrid(Color color, Game pGame) {
        if(GridBasedMap.SHOW_GRIDINPOTENTIALFIELD) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    if (blockMap[i][j].isShowInGame()) {
                        blockMap[i][j].drawBlock(color, pGame);
                    }
                }
            }
        }
    }

    public void drawGridMap(Color color, Game pGame) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (blockMap[i][j].getValue() > 0.1)
                    blockMap[i][j].drawBlock(color, pGame);
            }
        }
    }

    /* ------------------- Getters and Setters ------------------- */

    public List<Position> getUnwalkablePositions() {
        return unwalkablePositions;
    }

    public void setUnwalkablePositions(List<Position> unwalkablePositions) {
        this.unwalkablePositions = unwalkablePositions;
    }

    public Block[][] getBlockMap() {
        return blockMap;
    }

    public void setBlockMap(Block[][] blockMap) {
        this.blockMap = blockMap;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

}

package MapManager;

import bwapi.Color;
import bwapi.Game;
import bwapi.Position;
import bwapi.TilePosition;

/**
 * Created by Chudjak Kristián on 05.01.2017.
 */
public class HeatMap {
    private int SIZE = TilePosition.SIZE_IN_PIXELS*GRIDTILESIZE;

    public static final int GRIDTILESIZE = 8;

    public static boolean DEBUG=false;

    /**
     * Two dimensional array of Potential fields in heat map
     */
    private PotentialField[][] fieldMap;

    private int rows;

    private int columns;


    /* ------------------- Constructors ------------------- */

    /**
     * Creates the instance of HeatMp with given Game instance
     *
     * @param game
     */
    public HeatMap(Game game) {

    }


    /* ------------------- Initialization methods ------------------- */

    public void initializeHeatMap(int pRectangleSidePX, Game pGame) {
        /*rows=pGame.mapHeight()/pRectangleSidePX;

        columns=pGame.mapWidth()/pRectangleSidePX;*/

        rows = pGame.mapWidth()/GRIDTILESIZE;
        columns = pGame.mapHeight()/GRIDTILESIZE;

        fieldMap=new PotentialField[rows][columns];

        if(HeatMap.DEBUG) {
            System.out.println("--:: HeatMap initialization ::--");
            System.out.println("     - Rectangle size = "+pRectangleSidePX);
            System.out.println("     - Map PX = "+pGame.mapHeight()+" ,Grid rows = "+rows);
            System.out.println("     - Map PY = "+pGame.mapWidth()/pRectangleSidePX+" ,Grid cols = "+columns);
        }

        for(int i=0;i<rows;i++) {
            for(int j=0;j<columns;j++) {
                //i a j su prehodene preto, lebo Block(x,y) - pre x zodpoveda hodnota column

                fieldMap[i][j]=new PotentialField(pGame,new Position(i*SIZE,
                        j*SIZE),SIZE,SIZE,i,j);
            }
        }
//
//        if(GridMap.DEBUG) {
//            System.out.println("BlockMap size = "+getBlockMapSize());
//        }
    }


    /* ------------------- Main functonality methods ------------------- */

    public PotentialField getHeatBlock(int pRow, int pColumn) {
        return fieldMap[pRow][pColumn];
    }

    /**
     * Retruns the exact potential field block, where the given position is.
     *
     * @param position
     * @return PotentialField
     */
    public PotentialField getHeatBlockContainingPosition(Position position) {
        int posX=position.getX();
        int posY=position.getY();

        float upperBoxX;
        float lowerBoxX;
        float upperBoxY;
        float lowerBoxY;

        for(int j=0;j<columns;j++) {
            upperBoxX=fieldMap[0][j].getLeftUpperCornerBoxVector().toPosition().getX();
            lowerBoxX=fieldMap[0][j].getRightLowerCornerBoxVector().toPosition().getX();
            if(position.getX()>=upperBoxX&&position.getX()<=lowerBoxX) {
                for(int i=0;i<rows;i++) {
                    upperBoxY=fieldMap[i][j].getLeftUpperCornerBoxVector().toPosition().getY();
                    lowerBoxY=fieldMap[i][j].getRightLowerCornerBoxVector().toPosition().getY();
                    if(position.getY()>=upperBoxY&&position.getY()<=lowerBoxY) {
                        /*CONSOLE LOG */
                        System.out.println("Position found in potential field with center coordinates ["+i+","+j+"] :"+fieldMap[i][j].getCenterVector().toPosition().toString());
                        /*END MESSAGE*/
                        return fieldMap[i][j];
                    }
                }
            }
        }
        return null;
    }


    /* ------------------- Real-Time management methods ------------------- */

    public void heatManagement(Game pGame) {
        for(int i=0;i<rows;i++) {
            for(int j=0;j<columns;j++) {
                if(fieldMap[i][j].isVisible(pGame)) {

                    fieldMap[i][j].setHeat(0);
                } else {
                    fieldMap[i][j].increaseHeat();
                }
            }
        }
    }


    public void drawHeatMap(Game pGame) {
        for(int i=0;i<rows;i++) {
            for(int j=0;j<columns;j++) {

                if(fieldMap[i][j].getHeat() < 5){
                    fieldMap[i][j].showGraphicsRectangular(pGame, Color.Blue);
                }else if (fieldMap[i][j].getHeat() > 10){
                    fieldMap[i][j].showGraphicsRectangular(pGame, Color.Red);
                }else {
                    fieldMap[i][j].showGraphicsRectangular(pGame, Color.Orange);
                }
            }
        }
    }



    /* ------------------- Getters and setters ------------------- */

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }
}

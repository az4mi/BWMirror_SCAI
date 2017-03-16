package MapManager;

import bwapi.*;

import java.util.List;

import static MapManager.HeatMap.GRIDTILESIZE;

/**
 * Represents certain area on the map and provides information about this area
 */
public class PotentialField {

    private int id;

    private UnitType unitType;

    private double priority;

    private int row;
    private int column;

    /**
     * Center coordinate X
     */
    private int X;

    /**
     * Center coordinate Y
     */
    private int Y;

    /**
     * Radius of the potential field
     */
    private double radius;

    /**
     * Heat value
     */
    private double heat;

    private Game game;


    /* ------------------- Constructors ------------------- */

    /**
     * Initialization of PF with given game instance, graphics instance, center X and Y coordiantes and given radius
     *
     * @param pGame
     * @param X
     * @param Y
     * @param radius
     */
    public PotentialField(Game pGame, int X, int Y, int radius) {
        this.priority=0;
        this.X=X;
        this.Y=Y;
        this.radius=radius;
        this.heat=0;
        this.id=-1;
        this.row=-1;
        this.column=-1;
        game=pGame;
    }

    public PotentialField(Game pGame, Position position, int radius) {
        this.priority=0;
        this.X=position.getX();
        this.Y=position.getY();
        this.radius=radius;
        this.heat=0;
        this.id=-1;
        this.row=-1;
        this.column=-1;
        this.game=pGame;
    }

    public PotentialField(Game pGame, Unit pUnit) {
        this.priority=0;
        this.X=pUnit.getPosition().getX();
        this.Y=pUnit.getPosition().getY();
        this.radius=pUnit.getType().sightRange();
        this.heat=0;
        this.id=pUnit.getID();
        this.unitType=pUnit.getType();
        this.row=-1;
        this.column=-1;
        game=pGame;
    }

    public PotentialField(Game pGame, int X, int Y, int radius, double priority) {
        this.priority=priority;
        this.X=X;
        this.Y=Y;
        this.radius=radius;
        this.heat=0;
        this.id=-1;
        this.row=-1;
        this.column=-1;
        game=pGame;
    }

    public PotentialField(Game pGame,Position position,int radius, int priority) {
        this.priority=priority;
        this.X=position.getX();
        this.Y=position.getY();
        this.radius=radius;
        this.heat=0;
        this.id=-1;
        this.row=-1;
        this.column=-1;
        game=pGame;
    }

    public PotentialField(Game pGame,Position position,int radius, int priority, int pRow, int pColumn) {
        this.priority=priority;
        this.X=position.getX();
        this.Y=position.getY();
        this.radius=radius;
        this.heat=0;
        this.id=-1;
        this.row=pRow;
        this.column=pColumn;
        game=pGame;
    }


    /* ------------------- Main functonality methods ------------------- */

    /**
     * Returns upper left corner vector with coordinates
     *
     * @return Vector2D
     */
    public Vector2D getLeftUpperCornerBoxVector() {
        return new Vector2D((float)(X-(radius/2)),(float)(Y-(radius/2)));
    }

    /**
     * Returns lower right corner vector with coordinates
     *
     * @return Vector2D
     */
    public Vector2D getRightLowerCornerBoxVector() {
        return new Vector2D((float)(X+(radius/2)),(float)(Y+(radius/2)));
    }

    /**
     * Returns center vector with coordinates
     *
     * @return Vector2D
     */
    public Vector2D getCenterVector() {
        return new Vector2D(X,Y);
    }

    /**
     * If field is visible to allied units
     *
     * @return
     */
    public boolean isVisible(List<Unit> units) {
        return game.isVisible(this.getPosition().toTilePosition());
    }

    public boolean isVisible(Unit unit) {
        return game.isVisible(this.getPosition().toTilePosition());
    }

    /**
     * checks if this potential field is visible
     * @param game game objects
     * @return true if atleast one of its corner is visible
     */
    public boolean isVisible(Game game){
        /*TilePosition upLeft = new TilePosition((int)(row*GRIDTILESIZE),(int)(column*GRIDTILESIZE));
        TilePosition upRight = new TilePosition((int)(row*GRIDTILESIZE),
                (int)(column*GRIDTILESIZE)+GRIDTILESIZE-1);
        TilePosition downLeft = new TilePosition((int)(row*GRIDTILESIZE)+GRIDTILESIZE-1,
                (int)(column*GRIDTILESIZE));
        TilePosition downRight = new TilePosition((int)(row*GRIDTILESIZE)+GRIDTILESIZE-1,
                (int)(column*GRIDTILESIZE)+GRIDTILESIZE-1);*/

        TilePosition middle = new TilePosition((int)(row*GRIDTILESIZE)+GRIDTILESIZE/2,
                (int)(column*GRIDTILESIZE)+GRIDTILESIZE/2);

        return game.isVisible(middle); // checks tile in the middle, the other ones ale more precise

        /*return game.isVisible(upLeft) || game.isVisible(upRight) ||
                game.isVisible(downLeft) || game.isVisible(downRight) || game.isVisible(middle);*/

    }
    public boolean isUnitInRange(Unit pUnit) {
        Position pos=new Position(X,Y);
        if(pos.getDistance(pUnit.getPosition())<=radius&&pUnit.getID()==id) {
            return true;
        }
        return false;
    }

    public boolean isPositionInRange(Position pPosition) {
        Position pos=new Position(X,Y);
        if(pos.getDistance(pPosition)<=radius) {
            return true;
        }
        return false;
    }

    public double getRangeLengthInPercent(Position pPosition) {
        double distance=pPosition.getDistance(new Position(X,Y));
        double percent=100-(100/radius)*distance;
        return percent/100;
    }


    /* ------------------- Real-time management methods ------------------- */

    /**
     * Increases heat by +0,01
     */
    public void increaseHeat() {
        this.heat+=1/(double)100;
    }


    /* ------------------- Drawing functions ------------------- */

    /**
     * Visually draws circular field on screen
     */
    public void showGraphicsCircular(Color pColor) {
        game.drawDotMap(this.getPosition(),pColor);
        game.drawCircleMap(this.getPosition(),(int)radius,pColor);
        game.drawTextMap(X-10,Y-20,String.format("%.3g%n", heat));
        game.drawTextMap(X-10,Y-40,Boolean.toString(isVisible(game.getAllUnits())));
    }

    /**
     * Visually draws rectangular potential field on screen
     */
//    public void showGraphicsRectangular(Game game,Color color) {
//        graphics.setColor(color);
//        graphics.drawDotALTERNATIVE(new Vector2D(X,Y));
//        graphics.drawBoxALTERNATIVE(new Vector2D((float) (X - (radius / 2)), (float) (Y - (radius / 2))), new Vector2D((float) (X + (radius / 2)), (float) (Y + (radius / 2))));
//        graphics.drawTextALTERNATIVE(new Vector2D(X - 10, Y - 20), String.format("%.3g%n", heat));
//        graphics.drawTextALTERNATIVE(new Vector2D(X - 10, Y - 40), isVisible(game.getMyUnits()));
//
//        /*TESTING ADDED INFO
//        graphics.drawTextALTERNATIVE(new Vector2D(X - 10, Y - 40), new Vector2D(X,Y).toPosition().getPX()+";"+new Vector2D(X,Y).toPosition().getPY());
//        /*END TESTING*/
//    }

    /* ------------------- Getters and Setters ------------------- */

    public Position getPosition() {
        return new Position(X,Y);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public void setHeat(int heat) {
        this.heat=heat/(double)100;
    }

    public double getHeat() {
        return heat;
    }

    public int getX() {
        return X;
    }

    public void setX(int x) {
        X = x;
    }

    public int getY() {
        return Y;
    }

    public void setY(int y) {
        Y = y;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void showGraphicsRectangular(Game pGame, Color color) {
        pGame.drawBoxMap((int) (X+2),
                (int) (Y + 2),
                (int) (X -2 + (radius )),
                (int) (Y-2 + (radius )), color);
    }


}

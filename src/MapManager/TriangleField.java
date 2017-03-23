package MapManager;

import bwapi.*;
import bwapi.Color;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Created by Chudjak Kristián on 16.02.2017.
 */
public class TriangleField {



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
    private PotentialField needleTip = null;

    private Game game;
    private double aX;
    private double aY;
    private double bX;
    private double bY;
    private double cX;
    private double cY;
    double angle = 0f;

    public TriangleField(Game pGame, Unit pUnit,double radius) {

        this.X=pUnit.getPosition().getX();
        this.Y=pUnit.getPosition().getY();
        this.radius=radius;
        this.id=pUnit.getID();
        this.unitType=pUnit.getType();
        game=pGame;
        aX = X - radius;
        aY = Y;
        bX = X + radius;
        bY = Y;
        cX = X;
        cY = Y + radius*2;

    }


    protected Dimension getTriangleSize() {

        double maxX = 0;
        double maxY = 0;

        maxX =  Math.max(maxX, aX);
        maxX =  Math.max(maxX, bX);
        maxX =  Math.max(maxX, cX);

        maxY =  Math.max(maxY, aY);
        maxY =  Math.max(maxY, bY);
        maxY =  Math.max(maxY, cY);


        return new Dimension((int)maxX,(int) maxY);
    }


    public boolean isPositionInRange(Position pPosition) {


        return false;
    }

    public void draw(Color color){
        game.drawTriangleMap((int)aX,(int)aY,(int)bX,(int)bY,(int)cX,(int)cY,color);
    }

    public void setPosition(int x, int y, double angle) {
        X = x;
        Y = y;

        AffineTransform at = new AffineTransform();
        at.translate(X, Y);
        at.rotate(Math.toRadians(angle), X, Y);


    }
}

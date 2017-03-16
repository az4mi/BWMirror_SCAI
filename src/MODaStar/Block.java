package MODaStar;

import bwapi.*;

/**
 * Class Block represents each block in grid map. Blocks are also used in Tree data structure
 */
public class Block {

    /* ------------------- Other attributes ------------------- */

    private Color color;

    private boolean inPotentialField;

    private boolean startingBlock;

    private boolean accessibleByAir;

    private boolean accessibleByGround;

    private int row;

    private int column;

    private Position position;

    private double radius;

    private boolean showInGame;


    /* ------------------- A* attributes ------------------- */

    private double distance_value;

    private double destination_distance;

    private double f_value;

    private Block parent;

    private double damage;

    private int health;

    private boolean airDamage;

    private boolean groundDamage;

    /* ------------------- Tree data structure attributes ------------------- */

    private Block leftChild;

    private Block rightChild;

    private Block sibiling;


    /* ------------------- Constructors ------------------- */

    public Block(Position position, double pRadius, int pRow, int pColumn, Game game) {
        this.position=position;
        distance_value=Double.MAX_VALUE;
        destination_distance=-1;
        f_value=-1;
        parent=null;
        radius=pRadius;
        row=pRow;
        column=pColumn;
        accessibleByAir=true;
        startingBlock=false;
        damage=0;
        airDamage=false;
        groundDamage=false;
        health=0;
        showInGame=false;
        leftChild=null;
        rightChild=null;
        inPotentialField=false;
        color=Color.White;
    }

    public Block(Block pBlock) {
        this.position = pBlock.getPosition();
        this.distance_value = pBlock.getDistance_value();
        this.destination_distance = pBlock.getDestination_distance();
        this.f_value = pBlock.getFValue();
        this.parent = pBlock.getParent();
        this.radius = pBlock.getRadius();
        this.row = pBlock.getRow();
        this.column = pBlock.getColumn();
        this.accessibleByAir = pBlock.isAccessibleByAir();
        this.accessibleByGround = pBlock.isAccessibleByGround();
        this.startingBlock = pBlock.isStartingBlock();
        this.damage = pBlock.getDamage();
        this.airDamage = pBlock.isAirDamage();
        this.groundDamage = pBlock.isGroundDamage();
        this.health = pBlock.getHealth();
        this.showInGame = pBlock.isShowInGame();
        this.leftChild = pBlock.getLeftChild();
        this.rightChild = pBlock.getRightChild();
        this.inPotentialField = pBlock.isInPotentialField();
        this.color = pBlock.getColor();
    }

    /* ------------------- Drawing methods ------------------- */
    public void drawBlock(Color pColor, Game pGame) {

        pGame.drawDotMap(position,pColor);
        pGame.drawBoxMap((int)(position.getX() - (radius / 2)),(int)(position.getY() - (radius / 2)),(int)(position.getX() + (radius / 2)),(int)(position.getY() + (radius / 2)),pColor);
        pGame.drawTextMap(position.getX()-2,position.getY()-8,String.format("%.0f", damage));

//        graphicsEx.setColor(color);
//        graphicsEx.drawDotALTERNATIVE(new Vector2D(position.getPX(), position.getPY()));
//        graphicsEx.drawBoxALTERNATIVE(new Vector2D((float) (position.getPX() - (radius / 2)), (float) (position.getPY() - (radius / 2))), new Vector2D((float) (position.getPX() + (radius / 2)), (float) (position.getPY() + (radius / 2))));
//
//        graphicsEx.drawTextALTERNATIVE(new Vector2D(position.getPX() - 2, position.getPY() - 8 ), String.format("%.0f", damage));
//        //graphicsEx.drawTextALTERNATIVE(new Vector2D(position.getPX() - 2, position.getPY() - 8 ), isGroundDamage());
    }


    /* -------------------Other methods ------------------- */

    public boolean isAccessibleByGround(Game game) {
//        ToDo: nefunguje!!
//        if(game!=null) {
//            return game.isWalkable(position.getX(),position.getY());
//        }
//        else return true;
        return true;
    }

    public boolean isAccessibleByAir() {
        return accessibleByAir;
    }

    public boolean hasParent() {
        if(parent==null) {
            return false;
        }
        return true;
    }


    /* ------------------- Getters and Setters ------------------- */

    public boolean isAirDamage() {
        return airDamage;
    }

    public void setAirDamage(boolean airDamage) {
        this.airDamage = airDamage;
    }

    public boolean isGroundDamage() {
        return groundDamage;
    }

    public void setGroundDamage(boolean groundDamage) {
        this.groundDamage = groundDamage;
    }

    public Block getSibiling() {
        return sibiling;
    }

    public void setSibiling(Block sibiling) {
        this.sibiling = sibiling;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public boolean isInPotentialField() {
        return inPotentialField;
    }

    public void setInPotentialField(boolean inPotentialField) {
        this.inPotentialField = inPotentialField;
    }

    public boolean hasLeftChild() {
        if(leftChild!=null) {
            return true;
        }
        return false;
    }

    public boolean hasRightChild() {
        if(rightChild!=null) {
            return true;
        }
        return false;
    }

    public Block getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Block leftChild) {
        if(leftChild!=this) {
            this.leftChild = leftChild;
        } else {
            leftChild=null;
        }
    }

    public Block getRightChild() {
        return rightChild;
    }

    public void setRightChild(Block rightChild) {
        if(rightChild!=this) {
            this.rightChild = rightChild;
        } else {
            rightChild=null;
        }
    }

    @Override
    public String toString() {
        return "Block{" +
                "color=" + color +
                ", inPotentialField=" + inPotentialField +
                ", startingBlock=" + startingBlock +
                ", accessibleByAir=" + accessibleByAir +
                ", accessibleByGround=" + accessibleByGround +
                ", row=" + row +
                ", column=" + column +
                ", position=" + position +
                ", radius=" + radius +
                ", showInGame=" + showInGame +
                ", distance_value=" + distance_value +
                ", destination_distance=" + destination_distance +
                ", f_value=" + f_value +
                ", parent=" + parent +
                ", damage=" + damage +
                ", health=" + health +
                ", airDamage=" + airDamage +
                ", groundDamage=" + groundDamage +
                ", leftChild=" + leftChild +
                ", rightChild=" + rightChild +
                ", sibiling=" + sibiling +
                '}';
    }

    public boolean isShowInGame() {
        return showInGame;
    }

    public void setShowInGame(boolean showInGame) {
        this.showInGame = showInGame;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isStartingBlock() {
        return startingBlock;
    }

    public void setStartingBlock(boolean startingBlock) {
        this.startingBlock = startingBlock;
    }

    public boolean isAccessibleByGround() {
        return accessibleByGround;
    }

    public void setAccessibleByGround(boolean accessibleByGround) {
        this.accessibleByGround = accessibleByGround;
    }

    public void setAccessibleByAir(boolean accessibleByAir) {
        this.accessibleByAir = accessibleByAir;
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

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getDistance_value() {
        return distance_value;
    }

    public void setDistance_value(double distance_value) {
        this.distance_value = distance_value;
    }

    public double getDestination_distance() {
        return destination_distance;
    }

    public void setDestination_distance(double destination_distance) {
        this.destination_distance = destination_distance;
    }

    public double getFValue() {
        return f_value;
    }

    public void setFValue(double f_value) {
        this.f_value = f_value;
    }

    public Block getParent() {
        return parent;
    }

    public void setParent(Block parent) {
        this.parent = parent;
    }
}

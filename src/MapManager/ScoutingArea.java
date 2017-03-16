package MapManager;

import UnitManagement.ScoutingUnit;
import bwapi.Game;
import java.util.ArrayList;

/**
 * Created by Chudjak Kristián on 05.01.2017.
 */
public class ScoutingArea {
    public static int BASEAREA=1;
    public static int EXPANDAREA=2;
    public static int ARMYAREA=3;
    public static int PATHAREA=4;

    private int ID;

    private ArrayList<PotentialField> fieldArray;


    /* ------------------- Constructors ------------------- */

    public ScoutingArea() {
        fieldArray=new ArrayList<>();
    }

    public ScoutingArea(int pID) {
        fieldArray=new ArrayList<>();
        ID=pID;
    }

    public ScoutingArea(ScoutingArea pScoutingArea) {
        this.ID=pScoutingArea.getID();
        this.fieldArray=(ArrayList)pScoutingArea.getFieldArray().clone();
    }


    /* ------------------- Data structure operation methods ------------------- */

    public void insert(PotentialField pField) {
        fieldArray.add(pField);
    }

    public void remove(PotentialField pField) {
        fieldArray.remove(pField);
    }


    /* ------------------- Main functionality methods ------------------- */

    public PotentialField getNearestField(ScoutingUnit pScoutingUnit, Game pGame) {
        int difX=0;
        int difY=0;
        int value=0;
        int fieldIndex=0;

        if(!fieldArray.isEmpty()) {
            difX= Math.abs(pScoutingUnit.getUnit().getPosition().getX()-fieldArray.get(0).getPosition().getX());
            difY= Math.abs(pScoutingUnit.getUnit().getPosition().getY()-fieldArray.get(0).getPosition().getY());
            value=difX+difY;

            if(pGame.getGroundHeight(pScoutingUnit.getUnit().getPosition().getX(),pScoutingUnit.getUnit().getPosition().getY())!=
                    pGame.getGroundHeight(fieldArray.get(0).getPosition().getX(),fieldArray.get(0).getPosition().getX())) {
                value+=500;
            }
            fieldIndex=0;
            for(int i=1;i<fieldArray.size();i++) {
                difX= Math.abs(pScoutingUnit.getUnit().getPosition().getX()-fieldArray.get(i).getPosition().getX());
                difY= Math.abs(pScoutingUnit.getUnit().getPosition().getY()-fieldArray.get(i).getPosition().getY());
                if(pGame.getGroundHeight(pScoutingUnit.getUnit().getPosition().getX(),pScoutingUnit.getUnit().getPosition().getY())!=
                        pGame.getGroundHeight(fieldArray.get(i).getPosition().getX(),fieldArray.get(i).getPosition().getY())) {
                    difX+=500;
                }
                if(difX+difY<=value) {
                    fieldIndex=i;
                    value=difX+difY;
                }
            }
            //System.out.println("Unit level = "+pGame.getMap().getGroundHeight(pScoutingUnit.getUnit().getPosition()));
            //System.out.println("Field level = "+pGame.getMap().getGroundHeight(fieldArray.get(fieldIndex).getPosition()));
            return fieldArray.get(fieldIndex);
        }
        return null;
    }



    /* ------------------- Getters and setters ------------------- */

    public int size() {
        return fieldArray.size();
    }

    public ArrayList<PotentialField> getFieldArray() {
        return fieldArray;
    }

    public void setFieldArray(ArrayList<PotentialField> fieldArray) {
        this.fieldArray = fieldArray;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }
}

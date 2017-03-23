package MapManager.MapLayers;

import MapManager.GridBasedMap;
import bwapi.Game;

/**
 * Created by Chudjak Kristián on 27.02.2017.
 */
public class Layer {
    protected Game game;
    protected GridBasedMap gridMap;

    public Layer(Game game,int rows,int columns) {
        this.game = game;
        this.gridMap = new GridBasedMap(rows,columns);
    }

    public Layer() {
    }

    public void manage() {

    }

    public void draw(){

    }

    public Layer add(Layer layer){
        Layer l = new Layer(game,gridMap.getRows(),gridMap.getColumns());
        this.gridMap.add(layer.gridMap,l.gridMap);
        return l;
    }

    public void sub(Layer layer, GridBasedMap gridMap){

    }


}

package CombatLearning;

import ScoutModule.Scout_module;
import bwapi.Game;

/**
 * Created by Misho on 2.5.2017.
 */
public class CombatManager {

    Scout_module scoutModule;
    Game game;

    public CombatManager(Scout_module pScoutModule) {
        scoutModule = pScoutModule;
        game = scoutModule.getGame();
    }

}

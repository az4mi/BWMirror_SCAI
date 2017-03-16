package MODQlearning;

import UnitManagement.ScoutingUnit;

/**
 * Created by Silent1 on 02.02.2017.
 */
public class NormalAction extends Action {

    @Override
    public void executeAction(ScoutingUnit pScoutingUnit) {
        pScoutingUnit.chooseNormalPath();
    }
}

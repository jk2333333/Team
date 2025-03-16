package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Rush ability implementation.
 * Units with Rush can move and attack on the turn they are summoned.
 */
public class Rush implements Ability {
    
    @Override
    public String getName() {
        return "Rush";
    }
    
    @Override
    public boolean canActivate(GameState gameState, Unit unit) {
        // Rush is applied when a unit is first summoned
        return true;
    }
    
    @Override
    public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // When a unit with Rush is summoned, it can act immediately
        unit.resetTurnStatus();
        return true;
    }
    
    /**
     * Checks if a unit has the Rush ability
     * 
     * @param unit The unit to check
     * @return true if the unit has Rush
     */
    public static boolean hasRushAbility(Unit unit) {
        // This will be expanded when we implement unit abilities fully
        String unitConfig = unit.getClass().getName();
        return unitConfig.contains("SaberspineTiger");
    }
}
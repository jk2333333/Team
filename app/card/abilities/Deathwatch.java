package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Deathwatch ability interface.
 * When a unit dies, units with Deathwatch trigger their effect.
 */
public interface Deathwatch extends Ability {
    
    /**
     * Executes the Deathwatch effect when a unit dies
     * 
     * @param out The ActorRef for UI updates
     * @param gameState The current game state
     * @param unit The unit with Deathwatch ability
     * @param deadUnit The unit that died
     * @return true if the Deathwatch effect was successfully executed
     */
    boolean onUnitDeath(ActorRef out, GameState gameState, Unit unit, Unit deadUnit);
    
    @Override
    default String getName() {
        return "Deathwatch";
    }
    
    @Override
    default boolean canActivate(GameState gameState, Unit unit) {
        // Deathwatch activates when any unit dies
        return true;
    }
    
    @Override
    default boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // This is triggered by onUnitDeath
        return true;
    }
}
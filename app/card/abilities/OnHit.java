package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * OnHit ability interface.
 * When the unit deals damage to an enemy unit, units with OnHit trigger their
 * effect.
 */
public interface OnHit extends Ability {

    /**
     * Executes the OnHit effect when a unit deals damage to an enemy unit
     * 
     * @param out       The ActorRef for UI updates
     * @param gameState The current game state
     * @param unit      The unit with OnHit ability
     * @return true if the OnHit effect was successfully executed
     */
    boolean onHit(ActorRef out, GameState gameState);

    @Override
    default String getName() {
        return "OnHit";
    }

    @Override
    default boolean canActivate(GameState gameState, Unit unit) {
        // OnHit activates when the unit getting hurt
        return true;
    }

    @Override
    default boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // This is triggered by onHurt
        return true;
    }
}
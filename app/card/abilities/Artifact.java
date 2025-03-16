package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Artifact ability interface.
 * When the unit getting hurt, units with Artifact trigger their effect.
 */
public interface Artifact extends Ability {

    /**
     * Executes the Artifact effect when a unit getting hurt
     * 
     * @param out       The ActorRef for UI updates
     * @param gameState The current game state
     * @param unit      The unit with Artifact ability
     * @return true if the Artifact effect was successfully executed
     */
    boolean onHurt(ActorRef out, GameState gameState);

    @Override
    default String getName() {
        return "Artifact";
    }

    @Override
    default boolean canActivate(GameState gameState, Unit unit) {
        // Artifact activates when the unit getting hurt
        return true;
    }

    @Override
    default boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // This is triggered by onHurt
        return true;
    }
}
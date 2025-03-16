package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Interface for unit abilities.
 * Abilities are persistent effects that units have, such as Provoke, Flying, etc.
 */
public interface Ability {
    
    /**
     * The name of the ability
     * @return The ability name
     */
    String getName();
    
    /**
     * Checks if the ability can be triggered in the current game state
     * 
     * @param gameState The current game state
     * @param unit The unit with this ability
     * @return true if the ability can be triggered
     */
    boolean canActivate(GameState gameState, Unit unit);
    
    /**
     * Executes the ability effect
     * 
     * @param out The ActorRef for UI updates
     * @param gameState The current game state
     * @param unit The unit with this ability
     * @param targetTile Optional target tile for the ability
     * @return true if the ability was successfully executed
     */
    boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile);
}
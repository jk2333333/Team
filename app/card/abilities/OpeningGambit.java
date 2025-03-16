package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Opening Gambit ability interface.
 * When a unit is summoned, it triggers its Opening Gambit effect.
 */
public interface OpeningGambit extends Ability {
    
    /**
     * Executes the Opening Gambit effect when the unit is summoned
     * 
     * @param out The ActorRef for UI updates
     * @param gameState The current game state
     * @param unit The unit with Opening Gambit ability
     * @param summonTile The tile where the unit was summoned
     * @return true if the Opening Gambit effect was successfully executed
     */
    boolean onSummon(ActorRef out, GameState gameState, Unit unit, Tile summonTile);
    
    @Override
    default String getName() {
        return "Opening Gambit";
    }
    
    @Override
    default boolean canActivate(GameState gameState, Unit unit) {
        // Opening Gambit activates once when the unit is summoned
        return true;
    }
    
    @Override
    default boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // This is triggered by onSummon
        return true;
    }
}
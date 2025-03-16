package card;

import akka.actor.ActorRef;
import managers.GeneralManager;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;

/**
 * Base class for all spell card effects.
 */
public abstract class SpellCardEffect extends BaseCardEffect {
    
    /**
     * The effect animation to play when the spell is cast
     * @return The effect file path
     */
    protected abstract String getEffectAnimation();
    
    @Override
    public boolean executeEffect(ActorRef out, GameState gameState, Card card, Tile targetTile) {

        // Execute the spell-specific effect
        boolean success = executeSpellEffect(out, gameState, card, targetTile);
        GeneralManager.sleep(40);

        // Play the spell cast animation
        String effectFile = getEffectAnimation();
        if (success && effectFile != null) {
            playEffectAnimation(out, gameState, effectFile, targetTile);
        }
        
        return success;
    }
    
    /**
     * Executes the specific spell effect
     * 
     * @param out The ActorRef for UI updates
     * @param gameState The current game state
     * @param card The spell card
     * @param targetTile The target tile
     * @return true if the spell effect was successfully executed
     */
    protected abstract boolean executeSpellEffect(ActorRef out, GameState gameState, Card card, Tile targetTile);
}
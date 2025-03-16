package card;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;

/**
 * Interface for all card effects.
 * All cards in the game implement this interface to define their specific effect
 * when played or triggered under certain conditions.
 */
public interface CardEffect {
    
    /**
     * Executes the card's effect when played.
     * 
     * @param out The ActorRef for UI updates
     * @param gameState The current game state
     * @param card The card being played
     * @param tile The target tile if applicable
     * @return true if the effect was successfully applied
     */
    boolean executeEffect(ActorRef out, GameState gameState, Card card, Tile tile);
}
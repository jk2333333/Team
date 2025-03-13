package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;

/**
 * Handles the beginning of a new turn.
 * - Can be used for turn start animations, effects, or notifications.
 * - Does NOT draw cards (this is handled in `EndTurnClicked`).
 */
public class StartTurn implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
        // Optional: Implement animations or UI notifications for turn start.
        // No card draw or hand refresh here to avoid conflicts with EndTurnClicked.
    }
}
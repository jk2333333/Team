package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import managers.BoardManager;
import managers.HandManager;
import structures.GameState;
import structures.basic.Card;

/**
 * CardClicked: Handles player card selection.
 * - Highlights summonable tiles based on the player's active units.
 * - Ensures valid summon positions include both adjacent and diagonal tiles.
 */
public class CardClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Check if some unit moving
		if (gameState.unitActing) {
			return;
		}

		// Clear previously highlighted tiles
		HandManager.dehighlightCard(out, gameState);
		BoardManager.clearTiles(out, gameState);

		// 1️ Retrieve clicked card position from event message
		int handPosition = message.get("position").asInt();
		if (handPosition < 1 || handPosition > 6) {
			return; // Ensure the position is valid (1 to 6)
		}

		// 2️ Allow only Player 1 to select a card
		if (gameState.currentPlayer == 1 && handPosition <= gameState.player1Hand.size()) {
			HandManager.highlightCard(out, gameState, handPosition);
			Card clickedCard = gameState.player1Hand.get(handPosition - 1);
			if (clickedCard.isCreature()) {
				BoardManager.highlightSummonableTile(out, gameState);
			}
		}
	}
}

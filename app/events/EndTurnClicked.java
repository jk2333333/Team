package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import managers.TurnManager;
import managers.GeneralManager;
import managers.HandManager;
import structures.GameState;

/**
 * Handles the logic when the "End Turn" button is clicked.
 * - Clears selected cards.
 * - Switches turn to the other player.
 * - Grants additional mana and draws a new card for the next player.
 */
public class EndTurnClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {


		// Switch turns using TurnManager
		TurnManager.switchTurn(out, gameState);


	}
}

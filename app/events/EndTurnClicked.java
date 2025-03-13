package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import managers.TurnManager;
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
		// 1️ Clear any selected cards
		gameState.selectedCard = null;
		gameState.selectedHandPosition = -1;

		BasicCommands.addPlayer1Notification(out, "End Turn", 2);
		doSleep(50);

		// 2️ Switch turns using TurnManager
		TurnManager.switchTurn(out, gameState);
		doSleep(50);

		// 3️ Increase mana and draw a new card for the next player
		if (gameState.currentPlayer == 1) {
			gameState.player1.setMana(gameState.currentTurn + 1);
			BasicCommands.setPlayer1Mana(out, gameState.player1);
			doSleep(50);

			HandManager.drawCard(out, gameState, 1);
		} else {
			gameState.player2.setMana(gameState.currentTurn + 1);
			BasicCommands.setPlayer2Mana(out, gameState.player2);
			doSleep(50);

			HandManager.drawCard(out, gameState, 2);
		}

		// 4️ Trigger any turn-start logic (e.g., animations)
		StartTurn startTurnHandler = new StartTurn();
		startTurnHandler.processEvent(out, gameState, null);
	}

	private void doSleep(int ms) {
		try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
	}
}

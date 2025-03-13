package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * CardClicked: Handles player card selection.
 * - Highlights summonable tiles based on the player's active units.
 * - Ensures valid summon positions include both adjacent and diagonal tiles.
 */
public class CardClicked implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		// 1️ Retrieve clicked card position from event message
		int handPosition = message.get("position").asInt();
		if (handPosition < 1 || handPosition > 6) {
			return; // Ensure the position is valid (1 to 6)
		}

		// 2️ Allow only Player 1 to select a card
		if (gameState.currentPlayer == 1 && handPosition <= gameState.player1Hand.size()) {
			Card clickedCard = gameState.player1Hand.get(handPosition - 1); // Retrieve the selected card
			gameState.selectedCard = clickedCard; // Store selection in GameState
			gameState.selectedHandPosition = handPosition; // Store selected hand position
			BasicCommands.drawCard(out, clickedCard, handPosition, 1); // Highlight the selected card in UI

			// 3️ Clear previously highlighted summonable tiles
			gameState.summonableTiles.clear();

			// 4️ Define summonable directions (adjacent & diagonal)
			int[][] directions = new int[][] {
					{ 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent tiles (right, left, down, up)
					{ 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal tiles (top-right, bottom-right, top-left,
																// bottom-left)
			};

			// 5️ Get all active units, including the player's Avatar
			List<Unit> activeUnits = new ArrayList<>();
			for (Unit unit : gameState.playerUnits) {
				if (unit.getOwner() != gameState.currentPlayer) {
					continue;
				}
				activeUnits.add(unit);
			}
			if (!activeUnits.contains(gameState.player1Avatar)) {
				activeUnits.add(gameState.player1Avatar);
			}

			// 6️ Loop through active units to find valid summonable tiles
			for (Unit unit : activeUnits) {
				if (unit == null || unit.getPosition() == null)
					continue; // Skip invalid units
				int cx = unit.getPosition().getTilex();
				int cy = unit.getPosition().getTiley();

				// 7 Check all adjacent and diagonal tiles
				for (int[] dir : directions) {
					int nx = cx + dir[0];
					int ny = cy + dir[1];

					if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) { // Ensure within board limits
						Tile t = gameState.board[nx][ny];
						if (t.getUnit() == null) { // Only allow empty tiles
							gameState.summonableTiles.add(t);
							t.setHighlightStatus(out, 1); // Highlight tile in UI
						}
					}
				}
			}
		}
	}
}

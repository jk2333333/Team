package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import managers.BoardManager;
import managers.HandManager;
import managers.TurnManager;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;

/**
 * Handles logic when a tile is clicked.
 * - If a card is selected, checks if the tile is valid for summoning.
 * - If valid, the unit is summoned, mana is deducted, and the card is removed.
 * - If no card is selected, the event is ignored.
 */
public class TileClicked implements EventProcessor {

	/*
	 * 1. Check if the clicked tile is within bounds
	 * 
	 * 2. Declare the clicked tile
	 * 
	 * 3. If there is a card selected, summon unit or play spell
	 * 
	 * 4. If there is a unit selected, process the act
	 * 
	 * 5. If the tile is a movable or attackable tile, highlight the movable and
	 * attackable tiles
	 */
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		// Check if some unit moving
		if (gameState.unitMoving) {
			return;
		}
		
		int tilex = message.get("tilex").asInt();
		int tiley = message.get("tiley").asInt();

		// 1. Check if the clicked tile is within bounds
		if (tilex < 0 || tilex >= 9 || tiley < 0 || tiley >= 5) {
			return;
		}

		// 2. Declare the clicked tile
		Tile clickedTile = gameState.board[tilex][tiley];

		// 3. If there is a card selected, summon unit or play spell
		if (gameState.selectedCard != null && gameState.selectedCard.isCreature()) {
			if (gameState.selectedCard.isCreature()) {
				HandManager.playUnit(out, gameState, clickedTile);
			} else {
				HandManager.playSpell(out, gameState, clickedTile);
			}
			return;
		}

		// 4. If there is a unit selected, process the act
		if (gameState.selectedUnit != null) {

			// If the clicked tile is not highlighted, exit
			if (clickedTile.getHighlightStatus() == 0) {
				// Clear all highlights and highlight candidate tiles
				BoardManager.highlightCandidateTile(out, gameState);
			}

			// If the clicked tile is movable, move the unit
			else if (clickedTile.getHighlightStatus() == 1 && gameState.movableTiles.contains(clickedTile)) {
				UnitManager.moveUnit(out, gameState, clickedTile);
			}

			// If the clicked tile is attackable, attack
			else if (clickedTile.getHighlightStatus() == 2 && gameState.attackableTiles.contains(clickedTile)) {
				UnitManager.attackUnit(out, gameState, clickedTile);

				// Highlight candidate unit tiles
				BoardManager.highlightCandidateTile(out, gameState);
			}

			// Set selectedUnit as null again
			gameState.selectedUnit = null;

			return;
		}

		// 5. If the tile has an own unit, highlight the movable and attackable tiles
		if (clickedTile.getUnit() != null && clickedTile.getUnit().getOwner() == gameState.currentPlayer) {

			// Store selected unit
			gameState.selectedUnit = clickedTile.getUnit();

			// Dehighlight candidate tiles
			BoardManager.dehighlightUnitTile(out, gameState);

			// Highlight movable unit
			if (clickedTile.getUnit().canMove()) {
				BoardManager.highlightMovableTile(out, gameState, clickedTile);
			}

			// Highlight attackable unit
			if (clickedTile.getUnit().canAttack(gameState)) {
				BoardManager.highlightAttackableTile(out, gameState, clickedTile);
			}

			return;
		}
	}
}

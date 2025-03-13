package events;

import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import commands.BasicCommands;
import demo.CommandDemo;
import managers.BoardManager;
import managers.HandManager;
import managers.UnitManager;
import structures.GameState;
import structures.basic.*;
import utils.BasicObjectBuilders;
import utils.OrderedCardLoader;
import utils.StaticConfFiles;

/**
 * Handles the initialization of the game:
 * 1. Draws the game board (9×5 grid)
 * 2. Loads player avatars and sets their attack/health attributes
 * 3. Loads the card decks for both players
 * 4. Initializes player states (health, mana, starting cards)
 * 5. Triggers `StartTurn` to refresh unit states and begin gameplay
 */
public class Initialize implements EventProcessor {

	/**
	 * Processes the game initialization event and sets up the initial game state.
	 * @param out       Communication channel to the front-end (for sending UI updates)
	 * @param gameState The game state object, which stores all game-related data
	 * @param message   The incoming event message (not used in this function)
	 * @throws InterruptedException Handles possible thread sleep interruptions
	 */
	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) throws InterruptedException {
		// 1 Mark the game as initialized
		gameState.gameInitalised = true;

		// 2️ Draw the game board
		BoardManager.drawBoard(out, gameState);

		// 3️ Load decks for both players
		gameState.loadDecks();

		// 4 Set initial health values for both players
		gameState.player1.setHealth(20);
		BasicCommands.setPlayer1Health(out, gameState.player1);

		gameState.player2.setHealth(20);
		BasicCommands.setPlayer2Health(out, gameState.player2);

		// 5️ Place player avatars (Hero Units) onto the board
		// Player 1's avatar is positioned at (1,2), Player 2's avatar at (7,2)
		Tile p1Tile = gameState.board[1][2];
		Tile p2Tile = gameState.board[7][2];
		UnitManager.loadAndPlaceAvatars(out, gameState, p1Tile, p2Tile);

		// 6️ Draw the first 3 cards for Player1
		for (int i = 0; i < 3; i++) {
			if (!gameState.player1Deck.isEmpty()) {
				// Uses HandManager to draw cards
				HandManager.drawCard(out, gameState, 1);
			}
		}

		// 7️ Initialize mana for Player1
		gameState.player1.setMana(2);
		BasicCommands.setPlayer1Mana(out, gameState.player1);

		// 8️ Trigger `StartTurn` to initialize game mechanics
		StartTurn startTurnHandler = new StartTurn();
		startTurnHandler.processEvent(out, gameState, null);
	}
}
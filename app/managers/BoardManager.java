package managers;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import utils.BasicObjectBuilders;

/**
 * BoardManager: Responsible for drawing the 9×5 game board
 * and storing it in `GameState.board`.
 */
public class BoardManager {

    /**
     * Draws the 9×5 board tiles and stores them in `gameState.board`.
     * Each tile is loaded and rendered on the UI.
     *
     * @param out       The front-end communication channel
     * @param gameState The current game state storing all board data
     */
    public static void drawBoard(ActorRef out, GameState gameState) {
        for (int x = 0; x < 9; x++) { // Iterate through the 9 columns
            for (int y = 0; y < 5; y++) { // Iterate through the 5 rows
                Tile tile = BasicObjectBuilders.loadTile(x, y); // Load the tile
                gameState.board[x][y] = tile; // Store the tile in the game state
                tile.setHighlightStatus(out, 0); // Render the tile in UI
            }
        }
    }

    public static void clearMovableTiles(ActorRef out, GameState gameState) {
        for (Tile tile : gameState.movableTiles) {
            tile.setHighlightStatus(out, 0);
        }
        gameState.movableTiles.clear();
    }

    public static void clearAttackableTiles(ActorRef out, GameState gameState) {
        for (Tile tile : gameState.attackableTiles) {
            tile.setHighlightStatus(out, 0);
        }
        gameState.attackableTiles.clear();
    }
}

package managers;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
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

    public static void dehighlightUnitTile(ActorRef out, GameState gameState) {
        for (Unit unit : gameState.playerUnits) {
            unit.getTile().setHighlightStatus(out, 0);
        }
    }

    public static void highlightCandidateTile(ActorRef out, GameState gameState) {
        clearMovableTiles(out, gameState);
        clearAttackableTiles(out, gameState);
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() != 1) {
                continue;
            }
            if (unit.canMove() || unit.canAttack(gameState)) {
                unit.getTile().setHighlightStatus(out, 1);
            }
        }
    }

    public static void clearSummonableTiles(ActorRef out, GameState gameState) {
        for (Tile tile : gameState.summonableTiles) {
            tile.setHighlightStatus(out, 0);
        }
        gameState.summonableTiles.clear();
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

    public static void clearTiles(ActorRef out, GameState gameState) {
        // Clear all UI highlights
        dehighlightUnitTile(out, gameState);
        clearSummonableTiles(out, gameState);
        clearMovableTiles(out, gameState);
        clearAttackableTiles(out, gameState);
        gameState.selectedCard = null;
        gameState.selectedUnit = null;
    }

    public static void highlightMovableTile(ActorRef out, GameState gameState, Tile clickedTile) {

        // Allow only Player 1 to move
        if (gameState.currentPlayer == 1) {
            gameState.movableTiles.clear();

            // Define movable tiles (adjacent & diagonal)
            int[][] directions = new int[][] {
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent tiles (right, left, down, up)
                    { 2, 0 }, { -2, 0 }, { 0, 2 }, { 0, -2 }, // Interval tiles (right, left, down, up)
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal tiles (top-right, bottom-right, top-left,
                                                               // bottom-left)
            };

            // 6️ Loop through active units to find valid summonable tiles
            Unit unit = clickedTile.getUnit();
            if (unit == null || unit.getPosition() == null) {
                return;
            }
            int cx = unit.getPosition().getTilex();
            int cy = unit.getPosition().getTiley();

            // 7 Check all adjacent and diagonal tiles
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) { // Ensure within board limits
                    Tile tile = gameState.board[nx][ny];
                    if (tile.getUnit() == null) { // Only allow empty tiles
                        gameState.movableTiles.add(tile);
                        tile.setHighlightStatus(out, 1); // Highlight tile in UI
                    }
                }
            }
        }
    }

    public static void highlightAttackableTile(ActorRef out, GameState gameState, Tile clickedTile) {
        // Allow only Player 1 to attack
        if (gameState.currentPlayer == 1) {
            gameState.attackableTiles.clear();

            // Define attackable tiles (adjacent & diagonal)
            int[][] directions = new int[][] {
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent tiles (right, left, down, up)
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal tiles (top-right, bottom-right, top-left,
                                                               // bottom-left)
            };

            // Loop through active units to find valid attackable tiles
            Unit unit = clickedTile.getUnit();
            if (unit == null || unit.getPosition() == null) {
                return;
            }
            int cx = unit.getPosition().getTilex();
            int cy = unit.getPosition().getTiley();

            // Check all adjacent and diagonal tiles
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];

                if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) { // Ensure within board limits
                    Tile tile = gameState.board[nx][ny];

                    // If the tile is empty, continue to next loop
                    if (tile.getUnit() == null) {
                        continue;
                    }

                    // Only allow enemy tiles
                    if (tile.getUnit().getOwner() != gameState.currentPlayer) {
                        gameState.attackableTiles.add(tile);
                        tile.setHighlightStatus(out, 2); // Highlight tile in UI
                    }
                }
            }

        }
    }
}

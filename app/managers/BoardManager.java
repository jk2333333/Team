package managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    /**
     * Remove the highlight from the tiles of all player's units.
     * This method:
     * - Iterates over each player's unit in the game state.
     * - Resets the highlight status of the tile occupied by each unit to 0 (no
     * highlight).
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing the player's units, tiles,
     *                  etc.
     */
    public static void dehighlightUnitTile(ActorRef out, GameState gameState) {
        for (Unit unit : gameState.playerUnits) {
            unit.getTile().setHighlightStatus(out, 0);
        }
    }

    /**
     * Highlight the tiles of player 1's units that can either move or attack.
     * This method:
     * - Clears any existing movable or attackable tile highlights.
     * - Iterates through all player units in the game state.
     * - If a unit belongs to player 1 and can move or attack, highlights its tile
     * (mode:1).
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing units, tiles, etc.
     */
    public static void highlightCandidateTile(ActorRef out, GameState gameState) {
        clearTiles(out, gameState);
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() != 1) {
                continue;
            }
            if (unit.canMove() || unit.canAttack(gameState)) {
                unit.getTile().setHighlightStatus(out, 1);
            }
        }
    }

    /**
     * Clears the highlight from all summonable tiles, then clears the summonable
     * list.
     * This method:
     * - Iterates over each tile in the summonableTiles collection and sets its
     * highlight status to 0 (no highlight).
     * - Empties the summonableTiles list in the game state.
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing summonable tiles, units,
     *                  etc.
     */
    public static void clearSummonableTiles(ActorRef out, GameState gameState) {
        for (Tile tile : gameState.summonableTiles) {
            tile.setHighlightStatus(out, 0);
        }
        gameState.summonableTiles.clear();
    }

    /**
     * Clears the highlight from all movable tiles, then clears the movableTiles
     * list.
     * This method:
     * - Iterates over each tile in the movableTiles collection and sets its
     * highlight status to 0 (no highlight).
     * - Empties the movableTiles list in the game state.
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing movable tiles, units, etc.
     */
    public static void clearMovableTiles(ActorRef out, GameState gameState) {
        for (Tile tile : gameState.movableTiles) {
            tile.setHighlightStatus(out, 0);
        }
        gameState.movableTiles.clear();
    }

    /**
     * Clears the highlight from all attackable tiles, then clears the
     * attackableTiles list.
     * This method:
     * - Iterates over each tile in the attackableTiles collection and sets its
     * highlight status to 0 (no highlight).
     * - Empties the attackableTiles list in the game state.
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing attackable tiles, units,
     *                  etc.
     */
    public static void clearAttackableTiles(ActorRef out, GameState gameState) {
        for (Tile tile : gameState.attackableTiles) {
            tile.setHighlightStatus(out, 0);
        }
        gameState.attackableTiles.clear();
    }

    /**
     * Clears all UI highlights on the board and resets any selected card or unit.
     * This method:
     * - De-highlights tiles occupied by units.
     * - Clears the highlight from summonable, movable, and attackable tiles.
     * - Sets the selected card and selected unit in the game state to null.
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing tiles, selected card/unit,
     *                  etc.
     */
    public static void clearTiles(ActorRef out, GameState gameState) {
        // Clear all UI highlights
        dehighlightUnitTile(out, gameState);
        clearSummonableTiles(out, gameState);
        clearMovableTiles(out, gameState);
        clearAttackableTiles(out, gameState);
        HandManager.dehighlightCard(out, gameState);
        gameState.selectedCard = null;
        gameState.selectedHandPosition = -1;
        gameState.selectedUnit = null;
    }

    public static void highlightSummonableTile(ActorRef out, GameState gameState) {

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

    /**
     * Highlights the tiles to which the unit on the clicked tile can move (only if
     * it's Player 1's turn).
     * This method:
     * - Checks if the current player is Player 1; if not, does nothing.
     * - Clears any previously stored movable tiles.
     * - Defines a set of directions (adjacent, diagonal, and two-step intervals).
     * - Retrieves the unit on the clicked tile, ensuring it has a valid position.
     * - For each direction, checks if the resulting tile is within board limits and
     * unoccupied.
     * - Adds valid tiles to the movableTiles list and highlights them in the UI.
     *
     * @param out         WebSocket communication channel for UI updates.
     * @param gameState   The current game state containing the board, units, etc.
     * @param clickedTile The tile that was clicked, potentially containing a unit
     *                    to move.
     */
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

    /**
     * Highlights the tiles that the unit on the clicked tile can attack (only if
     * it's Player 1's turn).
     * This method:
     * - Checks if the current player is Player 1; if not, does nothing.
     * - Clears any previously stored attackable tiles.
     * - Defines a set of directions (adjacent and diagonal).
     * - Retrieves the unit on the clicked tile, ensuring it has a valid position.
     * - For each direction, checks if the resulting tile is within board limits and
     * occupied by an enemy unit.
     * - Adds valid tiles to the attackableTiles list and highlights them in the UI.
     *
     * @param out         WebSocket communication channel for UI updates.
     * @param gameState   The current game state containing the board, units, etc.
     * @param clickedTile The tile that was clicked, potentially containing a unit
     *                    ready to attack.
     */
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

    /**
     * 获取AI单位的可移动范围（不涉及UI高亮）
     * @param unit      需要移动的AI单位
     * @param gameState 当前游戏状态
     * @return 可移动的Tile列表
     */
    public static List<Tile> getAIMovableTiles(Unit unit, GameState gameState) {
        List<Tile> movableTiles = new ArrayList<>();
        if (unit.getOwner() != 2) return movableTiles;

        int[][] directions = {
                {1,0}, {-1,0}, {0,1}, {0,-1},
                {2,0}, {-2,0}, {0,2}, {0,-2},
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
        };

        int cx = unit.getTilex();
        int cy = unit.getTiley();
        for (int[] dir : directions) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];
            if (nx >=0 && nx <9 && ny >=0 && ny <5) {
                Tile tile = gameState.board[nx][ny];
                if (tile.getUnit() == null) {
                    movableTiles.add(tile);
                }
            }
        }
        return movableTiles;
    }

    /**
     * 获取AI单位可攻击的敌方单位
     * @param attacker  AI攻击单位
     * @param gameState 当前游戏状态
     * @return 可攻击的敌方单位列表
     */
    public static List<Unit> getAIAttackTargets(Unit attacker, GameState gameState) {
        List<Unit> targets = new ArrayList<>();
        if (attacker.getOwner() != 2) return targets;

        int[][] directions = {
                {1,0}, {-1,0}, {0,1}, {0,-1},
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
        };

        int cx = attacker.getTilex();
        int cy = attacker.getTiley();
        for (int[] dir : directions) {
            int nx = cx + dir[0];
            int ny = cy + dir[1];
            if (nx >=0 && nx <9 && ny >=0 && ny <5) {
                Tile tile = gameState.board[nx][ny];
                Unit enemy = tile.getUnit();
                if (enemy != null && enemy.getOwner() == 1) {
                    targets.add(enemy);
                }
            }
        }
        return targets;
    }

    /**
     * 为AI寻找召唤单位的最佳位置（优先靠近己方单位）
     * @param gameState 当前游戏状态
     * @return 可用的召唤位置Tile
     */
    public static Tile findAISummonTile(GameState gameState) {
        List<Tile> candidateTiles = new ArrayList<>();
        int[][] directions = {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 },
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
        };

        // 优先在己方单位周围寻找
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() != 2)
                continue;
            int cx = unit.getTilex();
            int cy = unit.getTiley();
            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) {
                    Tile tile = gameState.board[nx][ny];
                    if (tile.getUnit() == null) {
                        candidateTiles.add(tile);
                    }
                }
            }
        }

        // 如果周围无空位，则全地图搜索
        if (candidateTiles.isEmpty()) {
            for (Tile[] row : gameState.board) {
                for (Tile tile : row) {
                    if (tile.getUnit() == null) {
                        candidateTiles.add(tile);
                    }
                }
            }
        }
        return candidateTiles.isEmpty() ? null : candidateTiles.get(new Random().nextInt(candidateTiles.size()));
    }
    
    public static List<Tile> getAdjacentTiles(GameState gameState, Unit unit, boolean requireEmpty) {
        List<Tile> adjacentTiles = new ArrayList<>();

        if (unit == null || unit.getPosition() == null) {
            return adjacentTiles;
        }

        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();

        int[][] directions = {
                { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent
                { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal
        };

        for (int[] dir : directions) {
            int x = unitX + dir[0];
            int y = unitY + dir[1];

            if (x >= 0 && x < 9 && y >= 0 && y < 5) {
                Tile adjacentTile = gameState.board[x][y];

                if (!requireEmpty || adjacentTile.getUnit() == null) {
                    adjacentTiles.add(adjacentTile);
                }
            }
        }

        return adjacentTiles;
    }


}

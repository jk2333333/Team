package card;

import java.util.List;

import java.util.ArrayList;
import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.GeneralManager;
import structures.GameState;
import structures.basic.EffectAnimation;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Base implementation of CardEffect with common utility methods.
 */
public abstract class BaseCardEffect implements CardEffect {
    
    /**
     * Plays an effect animation on a tile
     * 
     * @param out The ActorRef for UI updates
     * @param effectFile The effect configuration file
     * @param tile The tile to play the effect on
     */
    protected void playEffectAnimation(ActorRef out, GameState gameState, String effectFile, Tile tile) {
        gameState.unitActing = true;
        EffectAnimation effect = utils.BasicObjectBuilders.loadEffect(effectFile);
        BasicCommands.playEffectAnimation(out, effect, tile);
        GeneralManager.sleep(1000);
        gameState.unitActing = false;
    }
    
    /**
     * Adds a notification message to the UI
     * 
     * @param out The ActorRef for UI updates
     * @param message The message to display
     * @param durationSeconds How long to display the message
     */
    protected void showNotification(ActorRef out, String message, int durationSeconds) {
        BasicCommands.addPlayer1Notification(out, message, durationSeconds);
    }
    
    /**
     * Checks if a tile is valid for targeting
     * 
     * @param gameState The current game state
     * @param tile The tile to check
     * @param requireUnit Whether the tile must contain a unit
     * @param friendlyUnit Whether the unit must be friendly
     * @return true if the tile is valid
     */
    protected boolean isValidTargetTile(GameState gameState, Tile tile, boolean requireUnit, boolean friendlyUnit) {
        if (tile == null) return false;
        
        Unit unit = tile.getUnit();
        
        if (requireUnit && unit == null) {
            return false;
        }
        
        if (requireUnit && unit != null) {
            // Check if the unit belongs to the current player
            boolean isUnitFriendly = (unit.getOwner() == gameState.currentPlayer);
            return friendlyUnit == isUnitFriendly;
        }
        
        return true;
    }
    
    /**
     * Gets all tiles adjacent to a unit
     * 
     * @param gameState The current game state
     * @param unit The unit 
     * @param requireEmpty Whether the adjacent tiles must be empty
     * @return A list of adjacent tiles
     */
    protected List<Tile> getAdjacentTiles(GameState gameState, Unit unit, boolean requireEmpty) {
        List<Tile> adjacentTiles = new ArrayList<>();
        
        if (unit == null || unit.getPosition() == null) {
            return adjacentTiles;
        }
        
        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();
        
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},  // Adjacent
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // Diagonal
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
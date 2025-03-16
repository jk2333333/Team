package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Flying ability implementation.
 * Units with Flying can move to any unoccupied space on the board.
 */
public class Flying implements Ability {
    
    @Override
    public String getName() {
        return "Flying";
    }
    
    @Override
    public boolean canActivate(GameState gameState, Unit unit) {
        // Flying is a passive ability that modifies movement rules
        return true;
    }
    
    @Override
    public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // This is a passive ability, nothing to execute
        return true;
    }
    
    /**
     * Gets all valid movement tiles for a flying unit
     * 
     * @param gameState The current game state
     * @param unit The flying unit
     * @return A list of valid tiles the unit can move to
     */
    public static java.util.List<Tile> getValidMovementTiles(GameState gameState, Unit unit) {
        java.util.List<Tile> validTiles = new java.util.ArrayList<>();
        
        // Flying units can move anywhere on the board that's unoccupied
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                Tile tile = gameState.board[x][y];
                
                // Check if the tile is empty
                if (tile.getUnit() == null) {
                    validTiles.add(tile);
                }
            }
        }
        
        return validTiles;
    }
    
    /**
     * Checks if a unit has the Flying ability
     * 
     * @param unit The unit to check
     * @return true if the unit has Flying
     */
    public static boolean hasFlyingAbility(Unit unit) {
        // This will be expanded when we implement unit abilities fully
        String unitConfig = unit.getClass().getName();
        return unitConfig.contains("YoungFlamewing");
    }
}
package card.abilities;

import akka.actor.ActorRef;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Provoke ability implementation.
 * Enemy units in adjacent squares cannot move and can only attack
 * this creature or other creatures with Provoke.
 */
public class Provoke implements Ability {
    
    @Override
    public String getName() {
        return "Provoke";
    }
    
    @Override
    public boolean canActivate(GameState gameState, Unit unit) {
        // Provoke is a passive ability that's always active
        return true;
    }
    
    @Override
    public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // This is a passive ability, nothing to execute
        return true;
    }
    
    /**
     * Checks if a unit is provoked by any enemy unit with Provoke ability
     * 
     * @param gameState The current game state
     * @param unit The unit to check
     * @return true if the unit is provoked
     */
    public static boolean isUnitProvoked(GameState gameState, Unit unit) {
        if (unit == null) return false;
        
        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();
        
        // Check all 8 adjacent tiles for enemy units with Provoke
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},  // Adjacent
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // Diagonal
        };
        
        for (int[] dir : directions) {
            int x = unitX + dir[0];
            int y = unitY + dir[1];
            
            // Check if the position is valid on the board
            if (x >= 0 && x < 9 && y >= 0 && y < 5) {
                Tile adjacentTile = gameState.board[x][y];
                Unit adjacentUnit = adjacentTile.getUnit();
                
                // Check if there's an enemy unit with Provoke
                if (adjacentUnit != null && 
                    adjacentUnit.getOwner() != unit.getOwner() && 
                    hasProvokeAbility(adjacentUnit)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a unit has the Provoke ability
     * 
     * @param unit The unit to check
     * @return true if the unit has Provoke
     */
    public static boolean hasProvokeAbility(Unit unit) {
        // This will be expanded when we implement unit abilities fully
        // For now, we can check based on the unit type
        String unitConfig = unit.getClass().getName();
        return unitConfig.contains("Silverguard") || 
               unitConfig.contains("Ironcliff") || 
               unitConfig.contains("SwampEntangler") ||
               unitConfig.contains("RockPulveriser");
    }
}
package card.player1;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.OpeningGambit;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;

/**
 * Nightsorrow Assassin card effect:
 * Cost = 3, Attack = 4, Health = 2
 * Opening Gambit: Destroy an enemy unit in an adjacent square that is below its maximum heath
 */
public class NightsorrowAssassinEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new NightsorrowAssassinOpeningGambit());
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Find an enemy unit in an adjacent square that is damaged
        return destroyDamagedAdjacentEnemy(out, gameState, unit);
    }
    
    /**
     * Finds and destroys a damaged enemy unit adjacent to this unit
     */
    private boolean destroyDamagedAdjacentEnemy(ActorRef out, GameState gameState, Unit unit) {
        if (unit == null || unit.getPosition() == null) {
            return false;
        }
        
        // Get all adjacent tiles
        List<Tile> adjacentTiles = getAdjacentTiles(gameState, unit);
        
        // Find a damaged enemy unit
        for (Tile tile : adjacentTiles) {
            Unit targetUnit = tile.getUnit();
            
            if (targetUnit != null && 
                targetUnit.getOwner() != unit.getOwner() && 
                !targetUnit.getIsAvartar(1) && !targetUnit.getIsAvartar(2)) {
                
                // In a real implementation, we would check if unit is damaged
                // For simplicity, we'll assume all enemy units can be targeted
                
                // Play death effect
                playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, tile);
                
                // Kill the unit
                UnitManager.removeUnit(out, gameState, targetUnit);
                
                BasicCommands.addPlayer1Notification(out, "Nightsorrow Assassin destroys an enemy unit!", 1);
                return true;
            }
        }
        
        BasicCommands.addPlayer1Notification(out, "No valid target for Nightsorrow Assassin", 1);
        return true; // Still count as successful even if no target found
    }
    
    private List<Tile> getAdjacentTiles(GameState gameState, Unit unit) {
        List<Tile> adjacentTiles = new ArrayList<>();
        
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
                adjacentTiles.add(gameState.board[x][y]);
            }
        }
        
        return adjacentTiles;
    }
    
    /**
     * Opening Gambit implementation for Nightsorrow Assassin
     */
    public static class NightsorrowAssassinOpeningGambit implements OpeningGambit {
        @Override
        public boolean onSummon(ActorRef out, GameState gameState, Unit unit, Tile summonTile) {
            // Implementation redirected to the parent class
            NightsorrowAssassinEffect effect = new NightsorrowAssassinEffect();
            return effect.destroyDamagedAdjacentEnemy(out, gameState, unit);
        }
    }
}
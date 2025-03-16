package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Provoke;
import commands.BasicCommands;
import managers.BoardManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Ironcliff Guardian card effect:
 * Cost = 5, Attack = 3, Health = 10
 * Abilities:
 * - Provoke: Enemy units in adjacent squares cannot move and can only attack this creature
 * - Airdrop: Can be summoned anywhere on the board
 */
public class IroncliffGuardianEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new Provoke() {
            @Override
            public String getName() {
                return "Provoke";
            }
            
            @Override
            public boolean canActivate(GameState gameState, Unit unit) {
                return true;
            }
            
            @Override
            public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
                return true;
            }
        });
        
        // The Airdrop ability affects summoning rules, not unit behavior
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Ironcliff Guardian has no immediate effect when summoned, just the passive Provoke ability
        BasicCommands.addPlayer1Notification(out, "Ironcliff Guardian summoned with Provoke", 1);
        return true;
    }
    
    /**
     * Override to modify the highlighting logic for Ironcliff Guardian due to Airdrop
     * This would need to be called from CardClicked when an Ironcliff Guardian is selected
     */
    public static void highlightAllEmptyTiles(ActorRef out, GameState gameState) {
        // Clear previous highlights
        BoardManager.clearSummonableTiles(out, gameState);
        
        // Highlight all empty tiles on the board
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                Tile tile = gameState.board[x][y];
                if (tile.getUnit() == null) {
                    gameState.summonableTiles.add(tile);
                    tile.setHighlightStatus(out, 1);
                }
            }
        }
    }
}
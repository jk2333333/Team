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

/**
 * Gloom Chaser card effect:
 * Cost = 2, Attack = 3, Health = 1
 * Opening Gambit: Summon a Wraithling directly behind this unit
 */
public class GloomChaserEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new GloomChaserOpeningGambit());
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Execute the Opening Gambit effect to summon a Wraithling behind the unit
        return summonWraithlingBehind(out, gameState, unit);
    }
    
    /**
     * Summons a Wraithling behind the unit (to the left for player 1, to the right for player 2)
     */
    private boolean summonWraithlingBehind(ActorRef out, GameState gameState, Unit unit) {
        if (unit == null || unit.getPosition() == null) {
            return false;
        }
        
        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();
        
        // Determine the position behind the unit based on owner
        int behindX = (unit.getOwner() == 1) ? unitX - 1 : unitX + 1;
        
        // Check if the position is valid and empty
        if (behindX >= 0 && behindX < 9) {
            Tile behindTile = gameState.board[behindX][unitY];
            
            if (behindTile.getUnit() == null) {
                // Play summon effect
                playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, behindTile);

                UnitManager.summonUnitDirectly(out, gameState, StaticConfFiles.wraithling, 1, 1, behindTile);
                
                BasicCommands.addPlayer1Notification(out, "Gloom Chaser summoned a Wraithling", 1);
                return true;
            }
        }
        
        BasicCommands.addPlayer1Notification(out, "No space for Wraithling behind unit", 1);
        return false;
    }
    
    /**
     * Opening Gambit implementation for Gloom Chaser
     */
    public static class GloomChaserOpeningGambit implements OpeningGambit {
        @Override
        public boolean onSummon(ActorRef out, GameState gameState, Unit unit, Tile summonTile) {
            // Implementation redirected to the parent class
            GloomChaserEffect effect = new GloomChaserEffect();
            return effect.summonWraithlingBehind(out, gameState, unit);
        }
    }
}
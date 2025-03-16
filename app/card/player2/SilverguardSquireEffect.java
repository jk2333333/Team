package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.OpeningGambit;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Silverguard Squire card effect:
 * Cost = 1, Attack = 1, Health = 1
 * Opening Gambit: Allied units in-front and behind you get +1/+1
 */
public class SilverguardSquireEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new SilverguardSquireOpeningGambit());
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Execute the Opening Gambit effect
        return buffAdjacentAlliedUnits(out, gameState, unit);
    }
    
    /**
     * Buffs allied units in-front and behind the summoned unit
     */
    private boolean buffAdjacentAlliedUnits(ActorRef out, GameState gameState, Unit unit) {
        if (unit == null || unit.getPosition() == null) {
            return false;
        }
        
        int unitX = unit.getPosition().getTilex();
        int unitY = unit.getPosition().getTiley();
        int playerAvatar = unit.getOwner();
        
        // Check which direction is in front and behind based on owner
        // For player 1, infront is left (-1,0), behind is right (+1,0)
        // For player 2, infront is right (+1,0), behind is left (-1,0)
        int inFrontX = (playerAvatar == 1) ? unitX - 1 : unitX + 1;
        int behindX = (playerAvatar == 1) ? unitX + 1 : unitX - 1;
        
        // Check and buff unit in front
        if (inFrontX >= 0 && inFrontX < 9) {
            Tile inFrontTile = gameState.board[inFrontX][unitY];
            Unit inFrontUnit = inFrontTile.getUnit();
            
            if (inFrontUnit != null && inFrontUnit.getOwner() == playerAvatar) {
                buffUnit(out, gameState, inFrontUnit);
            }
        }
        
        // Check and buff unit behind
        if (behindX >= 0 && behindX < 9) {
            Tile behindTile = gameState.board[behindX][unitY];
            Unit behindUnit = behindTile.getUnit();
            
            if (behindUnit != null && behindUnit.getOwner() == playerAvatar) {
                buffUnit(out, gameState, behindUnit);
            }
        }
        
        BasicCommands.addPlayer1Notification(out, "Silverguard Squire buffs allies in front and behind", 1);
        return true;
    }
    
    /**
     * Applies +1/+1 buff to a unit
     */
    private void buffUnit(ActorRef out, GameState gameState, Unit unit) {
        // Increase attack and health by 1
        int newAttack = unit.getAttack() + 1;
        int newHealth = unit.getHealth() + 1;
        
        unit.setAttack(out, newAttack);
        unit.setHealth(out, newHealth);
        
        // Update the UI
        BasicCommands.setUnitAttack(out, unit, newAttack);
        BasicCommands.setUnitHealth(out, unit, newHealth);
        
        // Play a buff effect
        playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, unit.getTile());
    }
    
    /**
     * Opening Gambit implementation for Silverguard Squire
     */
    public static class SilverguardSquireOpeningGambit implements OpeningGambit {
        @Override
        public boolean onSummon(ActorRef out, GameState gameState, Unit unit, Tile summonTile) {
            // Implementation redirected to the parent class
            SilverguardSquireEffect effect = new SilverguardSquireEffect();
            return effect.buffAdjacentAlliedUnits(out, gameState, unit);
        }
    }
}
package card.player2;

import akka.actor.ActorRef;
import card.SpellCardEffect;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Truestrike card effect:
 * Cost = 1
 * Deal 2 damage to an enemy unit
 */
public class TruestrikeEffect extends SpellCardEffect {
    
    @Override
    protected String getEffectAnimation() {
        return StaticConfFiles.f1_inmolation;
    }
    
    @Override
    protected boolean executeSpellEffect(ActorRef out, GameState gameState, Card card, Tile targetTile) {
        Unit targetUnit = targetTile.getUnit();
        
        // Check if the target is valid (an enemy unit)
        if (targetUnit == null || targetUnit.getOwner() == gameState.currentPlayer) {
            showNotification(out, "Invalid target for Truestrike", 2);
            return false;
        }
        
        // Deal 2 damage to the target
        int newHealth = targetUnit.getHealth() - 2;
        targetUnit.setHealth(out, Math.max(0, newHealth));
        
        // Update the UI
        BasicCommands.setUnitHealth(out, targetUnit, targetUnit.getHealth());
        
        showNotification(out, "Truestrike deals 2 damage to the target", 2);
        
        // If the unit is dead, remove it
        if (targetUnit.getHealth() <= 0) {
            managers.UnitManager.removeUnit(out, gameState, targetUnit);
        }
        
        return true;
    }
}
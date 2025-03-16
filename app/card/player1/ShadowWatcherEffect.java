package card.player1;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Deathwatch;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Shadow Watcher card effect:
 * Cost = 3, Attack = 3, Health = 2
 * Deathwatch: This unit gains +1 attack and +1 health permanently
 */
public class ShadowWatcherEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new ShadowWatcherDeathwatch());
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Shadow Watcher has no immediate effect when summoned
        return true;
    }
    
    /**
     * Deathwatch implementation for Shadow Watcher
     */
    public class ShadowWatcherDeathwatch implements Deathwatch {
        @Override
        public boolean onUnitDeath(ActorRef out, GameState gameState, Unit unit, Unit deadUnit) {
            // Increase the unit's attack and health by 1
            int newAttack = unit.getAttack() + 1;
            int newHealth = unit.getHealth() + 1;
            
            unit.setAttack(out, newAttack);
            unit.setHealth(out, newHealth);
            
            // Update the UI
            BasicCommands.setUnitAttack(out, unit, newAttack);
            BasicCommands.setUnitHealth(out, unit, newHealth);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Play a buff effect
            playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, unit.getTile());
            
            BasicCommands.addPlayer1Notification(out, "Shadow Watcher gains +1/+1 from Deathwatch", 1);
            
            return true;
        }
    }
}
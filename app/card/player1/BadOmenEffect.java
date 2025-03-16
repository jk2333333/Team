package card.player1;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Deathwatch;
import commands.BasicCommands;
import managers.GeneralManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Bad Omen card effect:
 * Cost = 0, Attack = 0, Health = 1
 * Deathwatch: This unit gains +1 attack permanently
 */
public class BadOmenEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        // 实际添加 Deathwatch 能力到单位
        unit.addAbility(new BadOmenDeathwatch());
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        return true;
    }
    
    /**
     * Deathwatch implementation for Bad Omen
     */
    public class BadOmenDeathwatch implements Deathwatch {
        @Override
        public boolean onUnitDeath(ActorRef out, GameState gameState, Unit unit, Unit deadUnit) {
            // Increase the unit's attack by 1
            int newAttack = unit.getAttack() + 1;
            unit.setAttack(out, newAttack);
            GeneralManager.sleep(100);
            
            // 添加视觉特效 - 使用buff效果
            playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, unit.getTile());
            
            // Show an effect
            showNotification(out, "Bad Omen gains +1 attack from Deathwatch", 1);
            
            return true;
        }
        
        private void showNotification(ActorRef out, String message, int seconds) {
            BasicCommands.addPlayer1Notification(out, message, seconds);
        }
    }
}
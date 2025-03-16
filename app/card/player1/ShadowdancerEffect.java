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
 * Shadowdancer card effect:
 * Cost = 5, Attack = 5, Health = 4
 * Deathwatch: Deal 1 damage to the enemy avatar and heal yourself for 1
 */
public class ShadowdancerEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new ShadowdancerDeathwatch());
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Shadowdancer has no immediate effect when summoned
        return true;
    }
    
    /**
     * Deathwatch implementation for Shadowdancer
     */
    public class ShadowdancerDeathwatch implements Deathwatch {
        @Override
        public boolean onUnitDeath(ActorRef out, GameState gameState, Unit unit, Unit deadUnit) {
            // Deal 1 damage to enemy avatar
            Unit enemyAvatar = (unit.getOwner() == 1) ? gameState.player2Avatar : gameState.player1Avatar;
            
            if (enemyAvatar != null) {
                // Deal damage to enemy avatar
                int newHealth = enemyAvatar.getHealth() - 1;
                enemyAvatar.setHealth(out, Math.max(0, newHealth));
                
                // Update enemy player health
                if (unit.getOwner() == 1) {
                    gameState.player2.setHealth(newHealth);
                    BasicCommands.setPlayer2Health(out, gameState.player2);
                } else {
                    gameState.player1.setHealth(newHealth);
                    BasicCommands.setPlayer1Health(out, gameState.player1);
                }
                
                // Update enemy avatar health display
                BasicCommands.setUnitHealth(out, enemyAvatar, newHealth);
                
                // Play damage effect on enemy avatar
                playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, enemyAvatar.getTile());
            }
            
            // Heal player avatar
            Unit playerAvatar = (unit.getOwner() == 1) ? gameState.player1Avatar : gameState.player2Avatar;
            
            if (playerAvatar != null) {
                // Set maximum health to 20
                int maxHealth = 20;
                int newHealth = Math.min(playerAvatar.getHealth() + 1, maxHealth);
                playerAvatar.setHealth(out, newHealth);
                
                // Update player health
                if (unit.getOwner() == 1) {
                    gameState.player1.setHealth(newHealth);
                    BasicCommands.setPlayer1Health(out, gameState.player1);
                } else {
                    gameState.player2.setHealth(newHealth);
                    BasicCommands.setPlayer2Health(out, gameState.player2);
                }
                
                // Update player avatar health display
                BasicCommands.setUnitHealth(out, playerAvatar, newHealth);
                
                // Play heal effect on player avatar
                playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, playerAvatar.getTile());
            }
            
            BasicCommands.addPlayer1Notification(out, "Shadowdancer deals 1 damage to enemy avatar and heals yours for 1", 2);
            return true;
        }
    }
}
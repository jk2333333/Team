package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Skyrock Golem card effect:
 * Cost = 2, Attack = 4, Health = 2
 * No special abilities
 */
public class SkyrockGolemEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        // No abilities to add
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Skyrock Golem has no effect when summoned
        BasicCommands.addPlayer1Notification(out, "Skyrock Golem summoned", 1);
        return true;
    }
}
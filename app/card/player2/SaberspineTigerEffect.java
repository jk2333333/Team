package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Rush;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Saberspine Tiger card effect:
 * Cost = 3, Attack = 3, Health = 2
 * Rush: Can move and attack on the turn it is summoned
 */
public class SaberspineTigerEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new Rush() {
            @Override
            public String getName() {
                return "Rush";
            }
            
            @Override
            public boolean canActivate(GameState gameState, Unit unit) {
                return true;
            }
            
            @Override
            public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
                // Unit can move and attack immediately
                unit.resetTurnStatus(); 
                return true;
            }
        });
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Allow the unit to act immediately
        unit.resetTurnStatus();
        
        BasicCommands.addPlayer1Notification(out, "Saberspine Tiger summoned with Rush", 1);
        return true;
    }
}
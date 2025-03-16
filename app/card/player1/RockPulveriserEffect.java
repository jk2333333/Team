package card.player1;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Provoke;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Rock Pulveriser card effect:
 * Cost = 2, Attack = 1, Health = 4
 * Provoke: Enemy units in adjacent squares cannot move and can only attack this creature
 */
public class RockPulveriserEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new Provoke() {
            @Override
            public String getName() {
                return "Provoke";
            }
            
            @Override
            public boolean canActivate(GameState gameState, Unit unit) {
                // Provoke is a passive ability that's always active
                return true;
            }
            
            @Override
            public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
                // This is a passive ability, nothing to execute
                return true;
            }
        });
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Rock Pulveriser has no immediate effect when summoned, just the passive Provoke ability
        BasicCommands.addPlayer1Notification(out, "Rock Pulveriser summoned with Provoke", 1);
        return true;
    }
}
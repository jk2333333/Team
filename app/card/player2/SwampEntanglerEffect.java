package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Provoke;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Swamp Entangler card effect:
 * Cost = 1, Attack = 0, Health = 3
 * Provoke: Enemy units in adjacent squares cannot move and can only attack this creature
 */
public class SwampEntanglerEffect extends CreatureCardEffect {
    
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
        // Swamp Entangler has no immediate effect when summoned, just the passive Provoke ability
        BasicCommands.addPlayer1Notification(out, "Swamp Entangler summoned with Provoke", 1);
        return true;
    }
}
package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Flying;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Young Flamewing card effect:
 * Cost = 4, Attack = 5, Health = 4
 * Flying: Can move anywhere on the board
 */
public class YoungFlamewingEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new Flying() {
            @Override
            public String getName() {
                return "Flying";
            }
            
            @Override
            public boolean canActivate(GameState gameState, Unit unit) {
                return true;
            }
            
            @Override
            public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
                return true;
            }
        });
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Young Flamewing has no immediate effect when summoned, just the passive Flying ability
        BasicCommands.addPlayer1Notification(out, "Young Flamewing summoned with Flying", 1);
        return true;
    }
}
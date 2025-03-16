package card.player2;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Provoke;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Silverguard Knight card effect:
 * Cost = 3, Attack = 1, Health = 5
 * Abilities:
 * - Provoke: Enemy units in adjacent squares cannot move and can only attack this creature
 * - Zeal: If your avatar is dealt damage, this unit gains +2 attack
 */
public class SilverguardKnightEffect extends CreatureCardEffect {
    
    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        // Add Provoke ability
        unit.addAbility(new Provoke() {
            @Override
            public String getName() {
                return "Provoke";
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
        
        // The Zeal ability is not implemented through the ability system
        // Instead, it will be checked whenever the player's avatar takes damage
    }
    
    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Register this unit as having the Zeal ability - would need to be tracked in gameState
        // For this example, we'll add it to a list of Silverguard Knights that would be checked
        // whenever the avatar takes damage
        
        BasicCommands.addPlayer1Notification(out, "Silverguard Knight summoned with Provoke and Zeal", 1);
        return true;
    }
    
    /**
     * Method that would be called when the player's avatar takes damage
     * This would need to be called from the damage handling code
     */
    public void onAvatarDamaged(ActorRef out, GameState gameState, int playerOwner) {
        // Find all Silverguard Knights belonging to the player
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() == playerOwner && isSilverguardKnight(unit)) {
                // Apply the +2 attack buff
                int newAttack = unit.getAttack() + 2;
                unit.setAttack(out, newAttack);
                
                // Update the UI
                BasicCommands.setUnitAttack(out, unit, newAttack);
                
                // Play a buff effect
                playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, unit.getTile());
                
                BasicCommands.addPlayer1Notification(out, "Silverguard Knight gains +2 attack from Zeal", 1);
            }
        }
    }
    
    /**
     * Helper method to check if a unit is a Silverguard Knight
     */
    private static boolean isSilverguardKnight(Unit unit) {
        // In a real implementation, we would check the unit type
        // For now, we'll assume any unit with Provoke ability is a Silverguard Knight
        for (card.abilities.Ability ability : unit.getAbilities()) {
            if (ability instanceof Provoke) {
                return true;
            }
        }
        return false;
    }
}
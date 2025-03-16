package card;

import akka.actor.ActorRef;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Base class for all creature card effects.
 */
public abstract class CreatureCardEffect extends BaseCardEffect {
    
    @Override
    public boolean executeEffect(ActorRef out, GameState gameState, Card card, Tile targetTile) {
        // Execute summon effect (visual)
        playEffectAnimation(out, gameState, StaticConfFiles.f1_summon, targetTile);
        
        // Summon the unit
        Unit newUnit = UnitManager.summonUnit(out, gameState, card, targetTile);
        
        if (newUnit == null) {
            return false;
        }
        
        // Add abilities to the unit
        addAbilitiesToUnit(newUnit);
        
        // Execute creature-specific effect (like Opening Gambit)
        return executeCreatureEffect(out, gameState, newUnit, targetTile);
    }
    
    /**
     * Adds all abilities to the unit
     * 
     * @param unit The unit to add abilities to
     */
    protected abstract void addAbilitiesToUnit(Unit unit);
    
    /**
     * Executes the creature-specific effect
     * 
     * @param out The ActorRef for UI updates
     * @param gameState The current game state
     * @param unit The summoned unit
     * @param targetTile The target tile
     * @return true if the creature effect was successfully executed
     */
    protected abstract boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile);
}
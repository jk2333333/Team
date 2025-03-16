package card.player1;

import akka.actor.ActorRef;
import card.SpellCardEffect;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import utils.StaticConfFiles;

/**
 * Dark Terminus card effect:
 * Cost = 4
 * Destroy an enemy creature
 * Summon a Wraithling on the tile of the destroyed creature
 */
public class DarkTerminusEffect extends SpellCardEffect {

    @Override
    protected String getEffectAnimation() {
        return StaticConfFiles.f1_wraithsummon;
    }

    @Override
    protected boolean executeSpellEffect(ActorRef out, GameState gameState, Card card, Tile targetTile) {

        if (targetTile.getUnit() == null) {
            BasicCommands.addPlayer1Notification(out, "Can't play this spell to an empty tile", 2);
            return false;
        }

        if (targetTile.getUnit().getOwner() == gameState.currentPlayer) {
            BasicCommands.addPlayer1Notification(out, "Can't play this spell to a friend unit", 2);
            return false;
        }

        if (targetTile.getUnit().getIsAvartar(1) || targetTile.getUnit().getIsAvartar(2)) {
            BasicCommands.addPlayer1Notification(out, "Can't play this spell to an avatar", 2);
            return false;
        }

        UnitManager.removeUnit(out, gameState, targetTile.getUnit());

        // Play summon effect
        playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, targetTile);
        UnitManager.summonUnitDirectly(out, gameState, StaticConfFiles.wraithling, 1, 1, targetTile);
        return true;
    }
}

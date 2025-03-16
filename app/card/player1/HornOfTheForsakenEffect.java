package card.player1;

import akka.actor.ActorRef;
import card.SpellCardEffect;
import card.abilities.Artifact;
import card.abilities.OnHit;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

/**
 * Horn of the Forsaken card effect:
 * Cost = 1
 * Artifact 3: When cast the artifact is equipped to the player’s avatar with 3
 * robustness. Whenever the player’s avatar takes damage from any source,
 * decrease this artifact’s robustness by 1 (regardless of the amount of damage
 * taken). When this artifact’s robustness reaches 0, the artifact is destroyed
 * and its effects no longer trigger.
 * 
 * On Hit (whenever this unit deals damage to an enemy unit): Summon a
 * Wraithling on a randomly selected unoccupied adjacent tile. If there are no
 * unoccupied tiles, then this ability has no effect.
 */
public class HornOfTheForsakenEffect extends SpellCardEffect {

    int robustness = 3;

    @Override
    protected String getEffectAnimation() {
        return null;
    }

    @Override
    protected boolean executeSpellEffect(ActorRef out, GameState gameState, Card card, Tile targetTile) {

        Unit unit = (gameState.currentPlayer == 1) ? gameState.player1Avatar : gameState.player2Avatar;

        // 将 HornOfTheForsakenArtifact 能力加入到对应的单位
        unit.addAbility(new HornOfTheForsakenArtifact());
        BasicCommands.addPlayer1Notification(out, "Player " + gameState.currentPlayer + " obtains Artifact 3", 2);

        return true;
    }

    /**
     * Artifact implementation for Horn of the Forsaken.
     */
    public class HornOfTheForsakenArtifact implements Artifact, OnHit {

        @Override
        public String getName() {
            return "Horn of the Forsaken";
        }

        @Override
        public boolean canActivate(GameState gameState, Unit unit) {
            return Artifact.super.canActivate(gameState, unit) && OnHit.super.canActivate(gameState, unit);
        }

        @Override
        public boolean executeAbility(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
            return Artifact.super.executeAbility(out, gameState, unit, targetTile)
                    && OnHit.super.executeAbility(out, gameState, unit, targetTile);
        }

        @Override
        public boolean onHurt(ActorRef out, GameState gameState) {

            showNotification(out, "Horn of the Forsaken protects", 1);

            Unit unit = (gameState.currentPlayer == 1) ? gameState.player1Avatar : gameState.player2Avatar;

            robustness--;
            if (robustness >= 0) {
                playEffectAnimation(out, gameState, StaticConfFiles.f1_martyrdom, unit.getTile());
                return true;
            }
            return false;
        }

        @Override
        public boolean onHit(ActorRef out, GameState gameState) {

            showNotification(out, "Horn of the Forsaken triggers on hit", 1);

            Unit unit = (gameState.currentPlayer == 1) ? gameState.player1Avatar : gameState.player2Avatar;
            playEffectAnimation(out, gameState, StaticConfFiles.f1_inmolation, unit.getTile());
            return true;
        }

        private void showNotification(ActorRef out, String message, int seconds) {
            BasicCommands.addPlayer1Notification(out, message, seconds);
        }
    }
}

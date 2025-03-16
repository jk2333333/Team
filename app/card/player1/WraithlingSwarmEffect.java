package card.player1;

import akka.actor.ActorRef;
import card.SpellCardEffect;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Wraithling Swarm card effect:
 * Cost = 3
 * Summon 3 Wraithlings in sequence
 */
public class WraithlingSwarmEffect extends SpellCardEffect {

    @Override
    protected String getEffectAnimation() {
        return null;
    }

    @Override
    protected boolean executeSpellEffect(ActorRef out, GameState gameState, Card card, Tile targetTile) {
        // Get empty tiles adjacent to friendly units
        List<Tile> validTiles = getValidSummonTiles(gameState);

        if (validTiles.isEmpty()) {
            showNotification(out, "No valid tiles to summon Wraithlings", 2);
            return false;
        }

        // Summon up to 3 Wraithlings
        Random random = new Random();
        int summonCount = 0;

        for (int i = 0; i < 3 && !validTiles.isEmpty(); i++) {
            // Select a random valid tile
            int index = random.nextInt(validTiles.size());
            Tile summonTile = validTiles.get(index);
            validTiles.remove(index);

            // Play summon effect
            playEffectAnimation(out, gameState, StaticConfFiles.f1_buff, summonTile);

            // Summon a Wraithling
            Unit wraithling = UnitManager.summonUnitDirectly(out, gameState, StaticConfFiles.wraithling, 1, 1,
                    summonTile);
            wraithling.setOwner(gameState.currentPlayer);

            // Place the Wraithling on the board
            wraithling.setTile(summonTile);
            summonTile.setUnit(wraithling);

            // Add to the game state
            gameState.playerUnits.add(wraithling);

            summonCount++;
        }

        showNotification(out, "Summoned " + summonCount + " Wraithlings", 2);
        return true;
    }

    /**
     * Gets all valid tiles for summoning Wraithlings
     */
    private List<Tile> getValidSummonTiles(GameState gameState) {
        List<Tile> validTiles = new ArrayList<>();

        // Get all tiles adjacent to friendly units
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() == gameState.currentPlayer) {
                validTiles.addAll(getAdjacentTiles(gameState, unit, true));
            }
        }

        return validTiles;
    }
}
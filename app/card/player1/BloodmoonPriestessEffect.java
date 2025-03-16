package card.player1;

import akka.actor.ActorRef;
import card.CreatureCardEffect;
import card.abilities.Deathwatch;
import commands.BasicCommands;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.StaticConfFiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Bloodmoon Priestess card effect:
 * Cost = 4, Attack = 3, Health = 3
 * Deathwatch: Summon a Wraithling on a randomly selected unoccupied adjacent
 * tile
 */
public class BloodmoonPriestessEffect extends CreatureCardEffect {

    @Override
    protected void addAbilitiesToUnit(Unit unit) {
        unit.addAbility(new BloodmoonPriestessDeathwatch());
    }

    @Override
    protected boolean executeCreatureEffect(ActorRef out, GameState gameState, Unit unit, Tile targetTile) {
        // Bloodmoon Priestess has no immediate effect when summoned
        return true;
    }

    /**
     * Deathwatch implementation for Bloodmoon Priestess
     */
    public static class BloodmoonPriestessDeathwatch implements Deathwatch {
        @Override
        public boolean onUnitDeath(ActorRef out, GameState gameState, Unit unit, Unit deadUnit) {
            // Get all empty adjacent tiles
            List<Tile> emptyAdjacentTiles = getEmptyAdjacentTiles(gameState, unit);

            if (emptyAdjacentTiles.isEmpty()) {
                BasicCommands.addPlayer1Notification(out, "No space for Bloodmoon Priestess to summon a Wraithling", 1);
                return false;
            }

            // Select a random empty tile
            Random random = new Random();
            Tile summonTile = emptyAdjacentTiles.get(random.nextInt(emptyAdjacentTiles.size()));

            UnitManager.summonUnitDirectly(out, gameState, StaticConfFiles.wraithling, 1, 1, summonTile);

            BasicCommands.addPlayer1Notification(out, "Bloodmoon Priestess summons a Wraithling", 1);
            return true;
        }

        private List<Tile> getEmptyAdjacentTiles(GameState gameState, Unit unit) {
            List<Tile> emptyAdjacentTiles = new ArrayList<>();

            if (unit == null || unit.getPosition() == null) {
                return emptyAdjacentTiles;
            }

            int unitX = unit.getPosition().getTilex();
            int unitY = unit.getPosition().getTiley();

            int[][] directions = {
                    { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent
                    { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal
            };

            for (int[] dir : directions) {
                int x = unitX + dir[0];
                int y = unitY + dir[1];

                if (x >= 0 && x < 9 && y >= 0 && y < 5) {
                    Tile adjacentTile = gameState.board[x][y];
                    if (adjacentTile.getUnit() == null) {
                        emptyAdjacentTiles.add(adjacentTile);
                    }
                }
            }

            return emptyAdjacentTiles;
        }
    }
}
package managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.SystemUtils;

import akka.actor.ActorRef;
import card.abilities.Ability;
import card.abilities.Artifact;
import card.abilities.OnHit;
import card.abilities.Deathwatch;
import card.abilities.Rush;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;
import structures.basic.Unit;
import structures.basic.UnitAnimationType;
import utils.BasicObjectBuilders;
import utils.StaticConfFiles;

/**
 * UnitManager: Manages all unit-related operations, including summoning and
 * avatar initialization.
 * - This replaces the original `UnitSummoner` and is now structured inside the
 * `managers` package.
 * - In the future, this class can be expanded to handle unit movement, attacks,
 * and special abilities.
 */
public class UnitManager {

    /**
     * Initializes and places the avatars (Hero Units) for both players onto their
     * starting positions.
     * This method:
     * - Loads the avatar unit data.
     * - Sets the correct attack and health attributes.
     * - Positions the avatars at predefined tiles on the board.
     * - Notifies the UI to render the avatars and update their stats.
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state where unit data is stored.
     * @param p1Tile    Tile where Player 1's avatar will be placed.
     * @param p2Tile    Tile where Player 2's avatar will be placed.
     * @throws InterruptedException Ensures smooth UI updates with slight delays.
     */
    public static void loadAndPlaceAvatars(ActorRef out, GameState gameState, Tile p1Tile, Tile p2Tile)
            throws InterruptedException {
        // Load Player 1 and Player 2's avatars
        Unit p1Avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 0, Unit.class);
        Unit p2Avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 1, Unit.class);

        // Set avatar units is avartar
        p1Avatar.setIsAvartar(1);
        p2Avatar.setIsAvartar(2);

        // Set the position of avatars on the board
        p1Avatar.setPositionByTile(p1Tile);
        p2Avatar.setPositionByTile(p2Tile);

        // Store avatars in the game state
        gameState.setAvatars(p1Avatar, p2Avatar);

        p1Avatar.setOwner(1);
        p2Avatar.setOwner(2);

        // Set attack and health attributes
        p1Avatar.setAttack(out, 2);
        p1Avatar.setHealth(out, 20);
        p2Avatar.setAttack(out, 2);
        p2Avatar.setHealth(out, 20);

        // Render avatars on the board
        BasicCommands.drawUnit(out, p1Avatar, p1Tile);
        Thread.sleep(50);
        BasicCommands.drawUnit(out, p2Avatar, p2Tile);
        Thread.sleep(50);

        // Update UI to reflect unit stats
        BasicCommands.setUnitAttack(out, p1Avatar, 2);
        BasicCommands.setUnitHealth(out, p1Avatar, 20);
        BasicCommands.setUnitAttack(out, p2Avatar, 2);
        BasicCommands.setUnitHealth(out, p2Avatar, 20);

        // Play idle animations for both avatars
        BasicCommands.playUnitAnimation(out, p1Avatar, UnitAnimationType.idle);
        BasicCommands.playUnitAnimation(out, p2Avatar, UnitAnimationType.idle);
    }

    /**
     * Summons a unit onto a specific tile on the board.
     * This method:
     * - Loads the unit data from the card configuration.
     * - Assigns the correct attack and health values from the card.
     * - Places the unit on the board and marks the tile as occupied.
     * - Notifies the UI to render the unit and update its stats.
     * - Adds the summoned unit to the respective player's active unit list.
     *
     * @param out         WebSocket communication channel for UI updates.
     * @param gameState   The current game state tracking units and board state.
     * @param card        The card used for summoning the unit.
     * @param clickedTile The tile where the unit will be placed.
     */
    public static Unit summonUnitDirectly(ActorRef out, GameState gameState, String config, int attack, int health,
            Tile targetTile) {
        // Load the unit based on the card's configuration
        Unit newUnit = BasicObjectBuilders.loadUnit(config, -1, Unit.class);
        newUnit.setOwner(gameState.currentPlayer);
        newUnit.setId(gameState.getCurrentUnitId());

        // Ensure the unit has at least 1 health (prevents immediate death)
        if (health <= 0) {
            health = 1;
        }

        // Assign the attack and health values
        newUnit.setAttack(out, attack);
        newUnit.setHealth(out, health);

        // Set unit position and mark the tile as occupied
        newUnit.setTile(targetTile);
        targetTile.setUnit(newUnit);

        // Notify the UI to render the unit
        BasicCommands.drawUnit(out, newUnit, targetTile);
        BasicCommands.playUnitAnimation(out, newUnit, UnitAnimationType.idle);

        // Ensure UI updates attack & health after unit rendering
        GeneralManager.sleep(100);

        // Update UI with correct attack and health values
        BasicCommands.setUnitAttack(out, newUnit, attack);
        BasicCommands.setUnitHealth(out, newUnit, health);

        // Check if the unit has Rush ability and apply it
        if (Rush.hasRushAbility(newUnit)) {
            newUnit.resetTurnStatus();
        }

        // Add the unit to the unit list
        gameState.playerUnits.add(newUnit);

        return newUnit;
    }

    public static Unit summonUnit(ActorRef out, GameState gameState, Card card, Tile targetTile) {
        return summonUnitDirectly(out, gameState, card.getUnitConfig(), card.getAttack(), card.getHealth(), targetTile);
    }

    /**
     * Unit move to another tiles.
     * This method will:
     * Clear
     * 
     * @param out        WebSocket communication channel for UI updates.
     * @param gameState  The current game state tracking units and board state.
     * @param targetTile The tile where the unit will move to.
     */
    public static void moveUnit(ActorRef out, GameState gameState, Tile targetTile) {
        // Clear all highlights
        BoardManager.clearMovableTiles(out, gameState);
        BoardManager.clearAttackableTiles(out, gameState);

        Unit unit = gameState.selectedUnit;
        if (unit.getTile() == null) {
            return;
        }
        unit.getTile().setUnit(null);
        unit.setTile(targetTile);
        targetTile.setUnit(unit);
        BasicCommands.moveUnitToTile(out, unit, targetTile);
        unit.addMoves();
    }

    /**
     * Performs an attack action from the currently selected unit to a unit on the
     * specified tile.
     * <p>
     * This method will:
     * - Clears all highlighted movable/attackable tiles.
     * - Retrieves the attacker (the currently selected unit in gameState).
     * - Retrieves the target unit from the specified tile.
     * - Deals damage to the target and marks the attacker as having attacked and
     * unable to move.
     * - Causes a counter-attack from the target (if still alive) back to the
     * attacker.
     *
     * @param out        WebSocket communication channel for UI updates.
     * @param gameState  The current game state tracking units and board state.
     * @param targetTile The tile that contains the unit to be attacked.
     */
    public static void attackUnit(ActorRef out, GameState gameState, Tile targetTile) {
        // Clear all highlights
        BoardManager.clearMovableTiles(out, gameState);
        BoardManager.clearAttackableTiles(out, gameState);

        Unit attacker = gameState.selectedUnit;
        Unit target = targetTile.getUnit();

        // Set unitActing as true while play attacking animation
        gameState.unitActing = true;

        // Attack and attack back
        UnitManager.causeDamage(out, gameState, attacker, target);
        attacker.addAttacks();
        attacker.cantMove();

        target = targetTile.getUnit();
        UnitManager.causeDamage(out, gameState, target, attacker);

        // Set unitActing as false after play attacking animation
        gameState.unitActing = false;
    }

    /**
     * Cause the attacker to deal damage to the target.
     * This method:
     * - Checks if the attacker or target is null; if so, returns immediately.
     * - Subtracts the attacker's attack value from the target's health.
     * - Ensures the target's health doesn't drop below zero.
     * - If the target is an avatar, updates the corresponding player's health.
     * - Plays the attack, hit, and idle animations for both units.
     * - If the target's health reaches zero, removes the target from the game.
     *
     * @param out       WebSocket communication channel for UI updates.
     * @param gameState The current game state containing units, players, etc.
     * @param attacker  The unit performing the attack.
     * @param target    The unit receiving the attack.
     */
    public static void causeDamage(ActorRef out, GameState gameState, Unit attacker, Unit target) {
        if (attacker == null || target == null) {
            return; // Ensure the attacker and target are exist
        }

        // Play attack animation
        UnitManager.playAnimation(out, attacker, UnitAnimationType.attack, 0);
        UnitManager.playAnimation(out, target, UnitAnimationType.hit, 1000);
        UnitManager.playAnimation(out, attacker, UnitAnimationType.idle, 0);
        UnitManager.playAnimation(out, target, UnitAnimationType.idle, 0);

        // Trigger Artifact effects for all units
        boolean hasArtifact = false;
        if (target.getIsAvartar(1) || target.getIsAvartar(2)) {
            hasArtifact = triggerArtifactEffects(out, gameState, target);
        }
        if (hasArtifact) {
            return;
        }

        // Calculate attack demage
        target.setHealth(out, target.getHealth() - attacker.getAttack());

        // Ensure the health >= 0
        if (target.getHealth() <= 0) {
            target.setHealth(out, 0);
        }

        // If the unit is avartar, update the player health
        if (target.getIsAvartar(1)) {
            gameState.player1.setHealth(target.getHealth());
            BasicCommands.setPlayer1Health(out, gameState.player1);
        }
        if (target.getIsAvartar(2)) {
            gameState.player2.setHealth(target.getHealth());
            BasicCommands.setPlayer2Health(out, gameState.player2);
        }

        BasicCommands.setUnitHealth(out, target, target.getHealth());
        BasicCommands.setUnitHealth(out, attacker, attacker.getHealth());

        boolean hasOnHit = triggerOnHitEffects(out, gameState, attacker);
        if (hasOnHit) {
            System.out.println("on hit");
            Random random = new Random();
            List<Tile> emptyAdjacentTiles = BoardManager.getAdjacentTiles(gameState, attacker, true);
            if (!emptyAdjacentTiles.isEmpty()) {
                Tile summonTile = emptyAdjacentTiles.get(random.nextInt(emptyAdjacentTiles.size()));
                UnitManager.summonUnitDirectly(out, gameState, StaticConfFiles.wraithling, 1, 1, summonTile);
            }
        }

        // If the unit is dead, remove the unit
        if (target.getHealth() <= 0) {
            removeUnit(out, gameState, target);
        }
    }

    public static void removeUnit(ActorRef out, GameState gameState, Unit unit) {
        // Store the unit's tile before removing it
        if (unit == null) {
            return;
        }

        // Trigger Deathwatch effects for all units
        triggerDeathwatchEffects(out, gameState, unit);

        // Remove the unit from its tile
        unit.getTile().setUnit(null);

        // Play death animation
        UnitManager.playAnimation(out, unit, UnitAnimationType.death, 2000);

        // Remove from the game state
        gameState.playerUnits.remove(unit);

        // Delete the unit from the UI
        BasicCommands.deleteUnit(out, unit);
    }

    /**
     * Triggers Deathwatch effects for all units when a unit dies
     * 
     * @param out       The ActorRef for UI updates
     * @param gameState The current game state
     * @param deadUnit  The unit that died
     */
    private static void triggerDeathwatchEffects(ActorRef out, GameState gameState, Unit deadUnit) {
        // Loop through all units and trigger Deathwatch effects
        List<Unit> copy = new ArrayList<>(gameState.playerUnits);
        for (Unit unit : copy) {
            for (Ability ability : unit.getAbilities()) {
                if (ability instanceof Deathwatch) {
                    ((Deathwatch) ability).onUnitDeath(out, gameState, unit, deadUnit);
                }
            }
        }
    }

    private static boolean triggerArtifactEffects(ActorRef out, GameState gameState, Unit unit) {
        for (Ability ability : unit.getAbilities()) {
            if (ability instanceof Artifact) {
                System.out.println(1);
                boolean applied = ((Artifact) ability).onHurt(out, gameState);
                if (applied) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean triggerOnHitEffects(ActorRef out, GameState gameState, Unit unit) {
        for (Ability ability : unit.getAbilities()) {
            if (ability instanceof OnHit) {
                System.out.println(1);
                boolean applied = ((OnHit) ability).onHit(out, gameState);
                if (applied) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void playAnimation(ActorRef out, Unit unit, UnitAnimationType type, int time) {
        BasicCommands.playUnitAnimation(out, unit, type);
        GeneralManager.sleep(time);
    }
}
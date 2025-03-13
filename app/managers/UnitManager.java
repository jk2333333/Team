package managers;

import akka.actor.ActorRef;
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
        Unit p1Avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.humanAvatar, 100, Unit.class);
        Unit p2Avatar = BasicObjectBuilders.loadUnit(StaticConfFiles.aiAvatar, 101, Unit.class);

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
        p1Avatar.setAttack(2);
        p1Avatar.setHealth(20);
        p2Avatar.setAttack(2);
        p2Avatar.setHealth(20);

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
    public static void summonUnit(ActorRef out, GameState gameState, Card card, Tile clickedTile) {
        // Load the unit based on the card's configuration
        Unit newUnit = BasicObjectBuilders.loadUnit(card.getUnitConfig(), -1, Unit.class);
        newUnit.setOwner(1);
        newUnit.setId(gameState.getCurrentUnitId());

        // Retrieve attack and health values from the card
        int attackValue = card.getAttack();
        int healthValue = card.getHealth();

        // Ensure the unit has at least 1 health (prevents immediate death)
        if (healthValue <= 0) {
            healthValue = 1;
        }

        // Assign the attack and health values
        newUnit.setAttack(attackValue);
        newUnit.setHealth(healthValue);

        // Set unit position and mark the tile as occupied
        newUnit.setTile(clickedTile);
        clickedTile.setUnit(newUnit);

        // Notify the UI to render the unit
        BasicCommands.drawUnit(out, newUnit, clickedTile);
        BasicCommands.playUnitAnimation(out, newUnit, UnitAnimationType.idle);

        // Ensure UI updates attack & health after unit rendering
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Update UI with correct attack and health values
        BasicCommands.setUnitAttack(out, newUnit, attackValue);
        BasicCommands.setUnitHealth(out, newUnit, healthValue);

        // Add the unit to the unit list
        gameState.playerUnits.add(newUnit);
    }

    /**
     * Unit move to another tiles.
     * This method:
     * - {{ Do something }}
     * 
     * @param out         WebSocket communication channel for UI updates.
     * @param gameState   The current game state tracking units and board state.
     * @param unit        The unit ready to move.
     * @param clickedTile The tile where the unit will move to.
     */
    public static void moveUnit(ActorRef out, GameState gameState, Tile clickedTile) {
        // Clear all highlights
        BoardManager.clearMovableTiles(out, gameState);
        BoardManager.clearAttackableTiles(out, gameState);
        
        Unit unit = gameState.selectedUnit;
        if (unit.getTile() == null) {
            return;
        }
        unit.getTile().setUnit(null);
        unit.setTile(clickedTile);
        clickedTile.setUnit(unit);
        BasicCommands.moveUnitToTile(out, unit, clickedTile);
        unit.addMoves();
    }

    public static void attackUnit(ActorRef out, GameState gameState, Tile clickedTile) {
        // Clear all highlights
        BoardManager.clearMovableTiles(out, gameState);
        BoardManager.clearAttackableTiles(out, gameState);

        Unit attacker = gameState.selectedUnit;
        Unit target = clickedTile.getUnit();

        // Attack and attack back
        UnitManager.causeDamage(out, gameState, attacker, target);
        attacker.addAttacks();
        attacker.cantMove();

        target = clickedTile.getUnit();
        UnitManager.causeDamage(out, gameState, target, attacker);
    }

    public static void causeDamage(ActorRef out, GameState gameState, Unit attacker, Unit target) {
        if (attacker == null || target == null) {
            return; // Ensure the attacker and target are exist
        }

        // Calculate attack demage
        target.setHealth(target.getHealth() - attacker.getAttack());

        // Ensure the health >= 0
        if (target.getHealth() <= 0) {
            target.setHealth(0);
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

        // Play attack animation
        BasicCommands.setUnitHealth(out, target, target.getHealth());
        BasicCommands.setUnitHealth(out, attacker, attacker.getHealth());
        UnitManager.playAnimation(out, attacker, UnitAnimationType.attack, 0);
        UnitManager.playAnimation(out, target, UnitAnimationType.hit, 1000);
        UnitManager.playAnimation(out, attacker, UnitAnimationType.idle, 0);
        UnitManager.playAnimation(out, target, UnitAnimationType.idle, 0);

        // If the unit is dead, remove the unit
        if (target.getHealth() <= 0) {
            removeUnit(out, gameState, target);
        }
    }

    public static void removeUnit(ActorRef out, GameState gameState, Unit unit) {
        unit.getTile().setUnit(null);
        UnitManager.playAnimation(out, unit, UnitAnimationType.death, 2000);
        BasicCommands.deleteUnit(out, unit);
        gameState.playerUnits.remove(unit);
        unit = null;
    }

    public static void playAnimation(ActorRef out, Unit unit, UnitAnimationType type, int time) {
        BasicCommands.playUnitAnimation(out, unit, type);
        GeneralManager.sleep(time);
    }
}
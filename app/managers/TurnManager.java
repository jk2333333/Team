package managers;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

/**
 * TurnManager: Handles turn switching, resets unit statuses, and clears
 * selections.
 * - It does NOT handle card drawing.
 */
public class TurnManager {

    /**
     * Switches the turn to the next player.
     * - Updates `gameState.currentPlayer` and increments the turn counter.
     * - Resets unit movement and attack status.
     * - Clears selected cards, units, and UI highlights.
     *
     * @param out       The front-end communication channel
     * @param gameState The current game state tracking player turns and selections
     */
    public static void switchTurn(ActorRef out, GameState gameState) {
        // Switch to the next player
        gameState.currentPlayer = (gameState.currentPlayer == 1) ? 2 : 1;
        gameState.currentTurn++;

        // Reset all units' movement and attack status for the new turn
        for (Unit u : gameState.playerUnits) {
            u.resetTurnStatus();
        }

        // Clear all selected objects and UI highlights
        gameState.selectedCard = null;
        gameState.selectedHandPosition = -1;
        gameState.selectedUnit = null;
        gameState.summonableTiles.clear();
        gameState.movableTiles.clear();

        if (gameState.currentPlayer == 1) {
            highlightPlayer1ReadyUnits(out, gameState);
        }
        // Notify the front-end about the turn switch
        BasicCommands.addPlayer1Notification(out, "Turn switched to Player " + gameState.currentPlayer
                + ", Turn = " + gameState.currentTurn, 2);
    }

    public static void highlightPlayer1ReadyUnits(ActorRef out, GameState gameState) {
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() != gameState.currentPlayer) {
                continue;
            }
            if (unit.canMove() || (unit.canAttack(gameState))) {
                System.out.println("Ready");
                unit.getTile().setHighlightStatus(out, 1);
            }
        }
    }
}

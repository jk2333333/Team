package events;

import akka.actor.ActorRef;
import managers.BoardManager;
import managers.TurnManager;
import managers.UnitManager;

import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

public class UnitAttacking implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

        // Clear all highlights
        BoardManager.clearMovableTiles(out, gameState);
        BoardManager.clearAttackableTiles(out, gameState);

        int targetX = message.get("tilex").asInt();
        int targetY = message.get("tiley").asInt();

        Tile targetTile = gameState.board[targetX][targetY];

        Unit attacker = gameState.selectedUnit;
        Unit target = targetTile.getUnit();

        // Attack and attack back
        UnitManager.attackUnit(out, gameState, attacker, target);
        attacker.addAttacks();
        attacker.cantMove();

        target = targetTile.getUnit();
        UnitManager.attackUnit(out, gameState, target, attacker);
        TurnManager.highlightPlayer1ReadyUnits(out, gameState);
    }
}

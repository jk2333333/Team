package events;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import managers.BoardManager;
import managers.TurnManager;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;

/**
 * Indicates that a unit instance has started a move.
 * The event reports the unique id of the unit.
 * 
 * {
 * messageType = “unitMoving”
 * id = <unit id>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class UnitMoving implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {
		gameState.unitMoving = true;
	}

}

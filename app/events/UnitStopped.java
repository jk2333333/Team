package events;

import org.apache.commons.lang3.SystemUtils;

import com.fasterxml.jackson.databind.JsonNode;

import akka.actor.ActorRef;
import managers.BoardManager;
import managers.TurnManager;
import managers.UnitManager;
import structures.GameState;
import structures.basic.Tile;
import structures.basic.Unit;

/**
 * Indicates that a unit instance has stopped moving.
 * The event reports the unique id of the unit.
 * 
 * {
 * messageType = “unitStopped”
 * id = <unit id>
 * }
 * 
 * @author Dr. Richard McCreadie
 *
 */
public class UnitStopped implements EventProcessor {

	@Override
	public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

		BoardManager.highlightCandidateTile(out, gameState);
		gameState.unitActing = false;
	}

}

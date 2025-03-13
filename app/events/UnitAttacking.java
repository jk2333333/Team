package events;

import akka.actor.ActorRef;

import com.fasterxml.jackson.databind.JsonNode;
import structures.GameState;

public class UnitAttacking implements EventProcessor {

    @Override
    public void processEvent(ActorRef out, GameState gameState, JsonNode message) {

    }
}

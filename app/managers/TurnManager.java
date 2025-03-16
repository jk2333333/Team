// TurnManager.java 修改后的代码
package managers;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Unit;

public class TurnManager {

    public static void switchTurn(ActorRef out, GameState gameState) {

        // 1️ Clear any selected cards
        HandManager.dehighlightCard(out, gameState);
        BasicCommands.addPlayer1Notification(out, "End Turn", 2);
        GeneralManager.sleep(50);

        int previousPlayer = gameState.currentPlayer;
        gameState.currentPlayer = (previousPlayer == 1) ? 2 : 1;

        // 仅当切换到玩家1回合时增加回合数
        if (gameState.currentPlayer == 1) {
            gameState.currentTurn++;
            BoardManager.highlightCandidateTile(out, gameState);
        } else {
            BoardManager.clearTiles(out, gameState);
        }

        // 重置单位状态
        for (Unit unit : gameState.playerUnits) {
            unit.resetTurnStatus();
        }

        // 清除选择状态
        gameState.selectedCard = null;
        gameState.selectedHandPosition = -1;
        gameState.selectedUnit = null;

        if (gameState.currentPlayer == 1) {
            BoardManager.highlightCandidateTile(out, gameState);
        } else {
            BoardManager.clearTiles(out, gameState);
        }

        // 通知回合切换
        BasicCommands.addPlayer1Notification(out, "Turn switched to Player " + gameState.currentPlayer
                + ", Turn: " + gameState.currentTurn, 2);

        // 3️ Increase mana and draw a new card for the next player
        if (gameState.currentPlayer == 1) {
            gameState.player1.setMana(gameState.currentTurn + 1);
            BasicCommands.setPlayer1Mana(out, gameState.player1);
            GeneralManager.sleep(50);
            HandManager.drawCard(out, gameState);
        } else {
            gameState.player2.setMana(gameState.currentTurn + 1);
            BasicCommands.setPlayer2Mana(out, gameState.player2);
            GeneralManager.sleep(50);
            HandManager.drawCard(out, gameState);
        }

        // 触发AI回合
        if (gameState.currentPlayer == 2) {
            try {
                Thread.sleep(300);
            } catch (Exception e) {
            }
            AIManager.executeAITurn(out, gameState);
        }

        GeneralManager.sleep(50);
    }
}
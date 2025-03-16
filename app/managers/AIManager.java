package managers;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.*;
import utils.BasicObjectBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIManager {

    private static final Random random = new Random();

    public static void executeAITurn(ActorRef out, GameState gameState) {
        if (gameState.currentPlayer != 2) {
            return;
        }

        gameState.AIActing = true;

        BasicCommands.addPlayer1Notification(out, "AI正在行动...", 2);
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // 重置AI单位状态
        resetAIUnits(gameState);

        // 执行AI行动
        // **优先执行 AI 攻击**
        boolean actionTaken = aiAttack(out, gameState);

        // **如果不能攻击，再考虑移动**
        if (!actionTaken) {
            actionTaken |= aiMove(out, gameState);
        }

        

        BasicCommands.addPlayer1Notification(out, "AI回合结束", 2);
        gameState.AIActing = false;
        TurnManager.switchTurn(out, gameState);
    }

    private static void resetAIUnits(GameState gameState) {
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() == 2) {
                unit.resetTurnStatus();
            }
        }
    }

    private static boolean aiAttack(ActorRef out, GameState gameState) {
        boolean attacked = false;
        List<Unit> aiUnits = getAIUnits(gameState);

        for (Unit unit : aiUnits) {
            if (unit.hasAttacked() || unit.isDead())
                continue;

            // **获取单位攻击范围内的敌人**
            List<Unit> enemies = getEnemiesInAttackRange(unit, gameState);

            if (!enemies.isEmpty()) {
                // **选择生命值最少的敌人作为优先攻击目标**
                Unit target = selectWeakestEnemy(enemies);
                attackEnemy(out, unit, target);
                attacked = true;
            }
        }
        return attacked;
    }

    private static boolean aiMove(ActorRef out, GameState gameState) {
        boolean moved = false;
        List<Unit> aiUnits = getAIUnits(gameState);

        for (Unit unit : aiUnits) {
            if (unit.hasMoved() || unit.isDead())
                continue;

            Tile targetTile = findMoveTowardsEnemyAvatar(unit, gameState);
            if (targetTile != null) {
                moveUnit(out, unit, targetTile);
                moved = true;
            }
        }
        return moved;
    }

    private static boolean aiSummonUnits(ActorRef out, GameState gameState) {
    	if (gameState.player2Hand.isEmpty()) {
    	    BasicCommands.addPlayer1Notification(out, "AI手牌为空，无法召唤单位", 2);
    	    return false;
    	}
    	if (gameState.player2.getMana() < 1) {
    	    BasicCommands.addPlayer1Notification(out, "AI魔法值不足，无法召唤单位", 2);
    	    return false;
    	}
            

        for (Card card : gameState.player2Hand) {
            if (card.getManacost() <= gameState.player2.getMana()) {
                Tile summonTile = findAnyEmptyTile(gameState);
                if (summonTile != null) {
                    summonUnit(out, gameState, card, summonTile);
                    return true;
                }
            }
        }
        return false;
    }

    // 辅助方法
    private static List<Unit> getAIUnits(GameState gameState) {
        List<Unit> aiUnits = new ArrayList<>();
        for (Unit unit : gameState.playerUnits) {
            if (unit.getOwner() == 2 && !unit.isDead()) {
                aiUnits.add(unit);
            }
        }
        return aiUnits;
    }

    private static List<Unit> getAdjacentEnemies(Unit attacker, GameState gameState) {
        List<Unit> enemies = new ArrayList<>();
        Tile attackerTile = attacker.getTile();

        int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } };
        for (int[] dir : directions) {
            int x = attackerTile.getTilex() + dir[0];
            int y = attackerTile.getTiley() + dir[1];
            if (x >= 0 && x < 9 && y >= 0 && y < 5) {
                Tile tile = gameState.board[x][y];
                Unit enemy = tile.getUnit();
                if (enemy != null && enemy.getOwner() == 1) {
                    enemies.add(enemy);
                }
            }
        }
        return enemies;
    }

    private static Tile findMoveTowardsEnemyAvatar(Unit unit, GameState gameState) {
        Unit enemyAvatar = gameState.player1Avatar;
        List<Tile> movableTiles = BoardManager.getAIMovableTiles(unit, gameState);
        Tile bestTile = null;
        int minDistance = Integer.MAX_VALUE;

        for (Tile tile : movableTiles) {
            int distance = Math.abs(tile.getTilex() - enemyAvatar.getTilex())
                    + Math.abs(tile.getTiley() - enemyAvatar.getTiley());
            if (distance < minDistance) {
                minDistance = distance;
                bestTile = tile;
            }
        }
        return bestTile;
    }

    private static Tile findAnyEmptyTile(GameState gameState) {
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 5; y++) {
                Tile tile = gameState.board[x][y];
                if (tile.getUnit() == null)
                    return tile;
            }
        }
        return null;
    }

    private static void attackEnemy(ActorRef out, Unit attacker, Unit target) {
        BasicCommands.playUnitAnimation(out, attacker, UnitAnimationType.attack);



        // **更新血量**
        target.setHealth(out, target.getHealth() - attacker.getAttack());
        attacker.setHealth(out, attacker.getHealth() - target.getAttack()); // 反击

        // **更新 UI**
        BasicCommands.setUnitHealth(out, target, target.getHealth());
        BasicCommands.setUnitHealth(out, attacker, attacker.getHealth());

        // **如果目标死亡，移除**
       

        // **标记单位已攻击**
        attacker.setHasAttacked(true);
    }

    private static void moveUnit(ActorRef out, Unit unit, Tile targetTile) {
        BasicCommands.moveUnitToTile(out, unit, targetTile);
        unit.setPositionByTile(targetTile);
        unit.setHasMoved(true);
    }

    private static void summonUnit(ActorRef out, GameState gameState, Card card, Tile tile) {
        Unit unit = BasicObjectBuilders.loadUnit(card.getUnitConfig(), gameState.getCurrentUnitId(), Unit.class);
        unit.setOwner(2);
        unit.setPositionByTile(tile);
        gameState.playerUnits.add(unit);
        gameState.player2.setMana(gameState.player2.getMana() - card.getManacost());
        BasicCommands.drawUnit(out, unit, tile);
    }
    private static List<Unit> getEnemiesInAttackRange(Unit attacker, GameState gameState) {
        List<Unit> enemies = new ArrayList<>();
        Tile attackerTile = attacker.getTile();

        int[][] attackRange = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}, 
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1} 
        };

        for (int[] dir : attackRange) {
            int x = attackerTile.getTilex() + dir[0];
            int y = attackerTile.getTiley() + dir[1];

            if (x >= 0 && x < 9 && y >= 0 && y < 5) {
                Tile tile = gameState.board[x][y];
                Unit enemy = tile.getUnit();

                if (enemy != null && enemy.getOwner() == 1) {
                    enemies.add(enemy);
                }
            }
        }
        return enemies;
    }
    private static Unit selectWeakestEnemy(List<Unit> enemies) {
        Unit weakest = enemies.get(0);

        for (Unit enemy : enemies) {
            if (enemy.getHealth() < weakest.getHealth()) {
                weakest = enemy;
            }
        }
        return weakest;
    }
}
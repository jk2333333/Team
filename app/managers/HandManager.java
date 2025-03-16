package managers;

import java.util.List;
import akka.actor.ActorRef;
import card.CardEffect;
import card.CardFactory;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;
import structures.basic.Tile;

public class HandManager {

    /**
     * Handles drawing a new card.
     * - Adds a new card to the last available hand slot.
     * - Does not redraw existing cards.
     *
     * @param out       The front-end communication channel
     * @param gameState The current game state storing card data
     * @param player    The player drawing the card (1 for Player 1, 2 for Player 2)
     */
    // HandManager.java 修改后的drawCard方法
    public static void drawCard(ActorRef out, GameState gameState) {
        List<Card> deck;
        List<Card> hand;

        // 根据当前回合的玩家来决定抽卡目标
        if (gameState.currentPlayer == 1) {
            deck = gameState.player1Deck;
            hand = gameState.player1Hand;
        } else {
            deck = gameState.player2Deck;
            hand = gameState.player2Hand;
        }

        // 手牌已满检查
        if (hand.size() >= 6) {
            BasicCommands.addPlayer1Notification(out, "Hand is full (6 cards)", 2);
            GeneralManager.sleep(100);
            return;
        }

        // 牌库为空检查
        if (deck.isEmpty()) {
            BasicCommands.addPlayer1Notification(out, "Deck is empty, cannot draw more cards!", 2);
            GeneralManager.sleep(100);
            return;
        }

        // 抽卡并更新UI
        Card newCard = deck.remove(0);
        hand.add(newCard);
        if (gameState.currentPlayer == 1) {
            BasicCommands.drawCard(out, newCard, hand.size(), 0);
        }

    }

    public static void highlightCard(ActorRef out, GameState gameState, int handPosition) {
        if (gameState.currentPlayer != 1) {
            return;
        }
        Card clickedCard = gameState.player1Hand.get(handPosition - 1); // Retrieve the selected card
        gameState.selectedCard = clickedCard; // Store selection in GameState
        gameState.selectedHandPosition = handPosition; // Store selected hand position
        clickedCard.setHighlightStatus(out, gameState, 1); // Highlight the selected card in UI
    }

    public static void dehighlightCard(ActorRef out, GameState gameState) {
        if (gameState.selectedCard == null) {
            return;
        }
        gameState.selectedCard.setHighlightStatus(out, gameState, 0);
        gameState.selectedHandPosition = -1;
        gameState.selectedCard = null;
        GeneralManager.sleep(40);
    }

    /**
     * Handles playing a card:
     * - Deducts mana if the player has enough.
     * - Removes the card from the player's hand.
     * - Updates the UI by shifting the remaining cards forward.
     *
     * @param out       The front-end communication channel
     * @param gameState The current game state storing player mana and hand
     * @param card      The card being played
     * @param handPos   The card's position in the player's hand (1-6)
     * @return Returns `true` if the card was successfully played
     */
    public static boolean deductManaAndRemoveCard(ActorRef out, GameState gameState, Card card, int handPos) {
        int index = handPos - 1; // Convert hand position to index
        if (index < 0 || index > 5) {
            return false; // Ensure index is valid
        }

        // Handle mana deduction and hand management for Player 1
        if (gameState.currentPlayer == 1) {
            if (gameState.player1.getMana() < card.getManacost()) {
                BasicCommands.addPlayer1Notification(out, "No enough mana!", 2);
                GeneralManager.sleep(40);
                return false;
            }
            gameState.player1.setMana(gameState.player1.getMana() - card.getManacost());
            BasicCommands.setPlayer1Mana(out, gameState.player1);
            GeneralManager.sleep(40);

            if (index < gameState.player1Hand.size()) {
                gameState.player1Hand.remove(index);
                BasicCommands.deleteCard(out, handPos);
            }
            GeneralManager.sleep(40);

            shiftCardsUI(out, gameState.player1Hand, index, 1);

        } else { // Handle Player 2
            if (gameState.player2.getMana() < card.getManacost()) {
                BasicCommands.addPlayer1Notification(out, "Opponent does not have enough mana!", 2);
                GeneralManager.sleep(40);
                return false;
            }
            gameState.player2.setMana(gameState.player2.getMana() - card.getManacost());
            BasicCommands.setPlayer2Mana(out, gameState.player2);
            GeneralManager.sleep(40);

            if (index < gameState.player2Hand.size()) {
                gameState.player2Hand.remove(index);
                BasicCommands.deleteCard(out, handPos);
            }
            GeneralManager.sleep(40);

            shiftCardsUI(out, gameState.player2Hand, index, 2);
        }
        return true;
    }

    public static void playUnit(ActorRef out, GameState gameState, Tile targetTile) {

        Card cardToPlay = gameState.selectedCard;
        int handPos = gameState.selectedHandPosition;

        // Ensure the selected card is a spell
        if (cardToPlay == null || !cardToPlay.isCreature()) {
            return;
        }

        // Ensure the clicked tile is a valid summonable location
        if (!gameState.summonableTiles.contains(targetTile)) {
            return;
        }

        // Clear highlight status
        dehighlightCard(out, gameState);
        BoardManager.clearTiles(out, gameState);

        // Get card effect creater, process card effect and summon it
        card.CardEffect cardEffect = card.CardFactory.createCardEffect(cardToPlay);
        if (cardEffect == null) {
            BasicCommands.addPlayer1Notification(out, "Card effect not implemented!", 2);
            GeneralManager.sleep(40);
            return;
        }
        cardEffect.executeEffect(out, gameState, cardToPlay, targetTile);

        // Deduct mana and remove the card from the player's hand
        if (!HandManager.deductManaAndRemoveCard(out, gameState, cardToPlay, handPos)) {
            return; // Mana insufficient or other restrictions
        }

        // Clear selection and reset tile highlights
        gameState.selectedCard = null;
        gameState.selectedHandPosition = -1;

        // Restore highlight status
        BoardManager.highlightCandidateTile(out, gameState);
    }

    public static void playSpell(ActorRef out, GameState gameState, Tile targetTile) {

        Card cardToPlay = gameState.selectedCard;
        int handPos = gameState.selectedHandPosition;

        // Ensure the selected card is a spell
        if (cardToPlay == null || cardToPlay.isCreature()) {
            return;
        }

        // Get the appropriate card effect handler
        CardEffect cardEffect = CardFactory.createCardEffect(cardToPlay);
        if (cardEffect == null) {
            BasicCommands.addPlayer1Notification(out, "Card effect not implemented!", 2);
            GeneralManager.sleep(40);
            return;
        }

        // Execute the spell effect
        boolean success = cardEffect.executeEffect(out, gameState, cardToPlay, targetTile);
        GeneralManager.sleep(40);

        // Deduct mana and remove the card from the player's hand
        if (!success) {
            return;
        }

        // Deduct mana and remove the card from the player's hand
        if (!HandManager.deductManaAndRemoveCard(out, gameState, cardToPlay, handPos)) {
            return; // Mana insufficient or other restrictions
        }

        // Clear selection and reset tile highlights
        gameState.selectedCard = null;
        gameState.selectedHandPosition = -1;

        // Restore highlight status
        BoardManager.highlightCandidateTile(out, gameState);

        // Clear selection and reset tile highlights
        dehighlightCard(out, gameState);
        BoardManager.clearTiles(out, gameState);
        BoardManager.highlightCandidateTile(out, gameState);
    }

    private static void shiftCardsUI(ActorRef out, List<Card> hand, int removedIndex, int player) {
        for (int i = removedIndex; i < 6; i++) {
            BasicCommands.deleteCard(out, i + 1);
            GeneralManager.sleep(40);

            if (i < hand.size()) {
                Card c = hand.get(i);
                BasicCommands.drawCard(out, c, i + 1, 0);
                GeneralManager.sleep(40);
            }
        }
    }
}
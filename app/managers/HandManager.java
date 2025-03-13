package managers;

import akka.actor.ActorRef;
import commands.BasicCommands;
import structures.GameState;
import structures.basic.Card;

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
    public static void drawCard(ActorRef out, GameState gameState, int player){
        java.util.List<Card> deck = (player == 1) ? gameState.player1Deck : gameState.player2Deck;
        java.util.List<Card> hand = (player == 1) ? gameState.player1Hand : gameState.player2Hand;

        // Check if hand is full (limit: 6 cards)
        if (hand.size() >= 6) {
            BasicCommands.addPlayer1Notification(out, "Hand is full (6 cards)", 2);
            doSleep(40);
            return;
        }

        // Check if deck is empty
        if (deck.isEmpty()) {
            BasicCommands.addPlayer1Notification(out, "Deck is empty, cannot draw more cards!", 2);
            doSleep(40);
            return;
        }

        // Draw a card from the deck
        Card newCard = deck.remove(0);
        int oldSize = hand.size();
        hand.add(newCard);

        // Draw the new card in the UI (in the next available slot)
        BasicCommands.drawCard(out, newCard, oldSize + 1, 0);
        doSleep(100);
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
     * @return          Returns `true` if the card was successfully played
     */
    public static boolean deductManaAndRemoveCard(ActorRef out, GameState gameState, Card card, int handPos){
        int index = handPos - 1; // Convert hand position to index
        if (index < 0) return false; // Ensure index is valid

        // Handle mana deduction and hand management for Player 1
        if (gameState.currentPlayer == 1) {
            if (gameState.player1.getMana() < card.getManacost()) {
                BasicCommands.addPlayer1Notification(out, "Not enough mana!", 2);
                doSleep(40);
                return false;
            }
            gameState.player1.setMana(gameState.player1.getMana() - card.getManacost());
            BasicCommands.setPlayer1Mana(out, gameState.player1);
            doSleep(40);

            if (index < gameState.player1Hand.size()) {
                gameState.player1Hand.remove(index);
            }
            BasicCommands.deleteCard(out, handPos);
            doSleep(40);

            shiftCardsUI(out, gameState.player1Hand, index, 1);

        } else { // Handle Player 2
            if (gameState.player2.getMana() < card.getManacost()) {
                BasicCommands.addPlayer1Notification(out, "Opponent does not have enough mana!", 2);
                doSleep(40);
                return false;
            }
            gameState.player2.setMana(gameState.player2.getMana() - card.getManacost());
            BasicCommands.setPlayer2Mana(out, gameState.player2);
            doSleep(40);

            if (index < gameState.player2Hand.size()) {
                gameState.player2Hand.remove(index);
            }
            BasicCommands.deleteCard(out, handPos);
            doSleep(40);

            shiftCardsUI(out, gameState.player2Hand, index, 2);
        }
        return true;
    }

    /**
     * Shifts the remaining cards in the player's hand forward to fill the empty slot
     * after playing a card.
     * - Deletes the UI representation of the removed card.
     * - Moves all subsequent cards forward.
     *
     * @param out          The front-end communication channel
     * @param hand         The player's hand (list of cards)
     * @param removedIndex The index of the removed card
     * @param player       The player whose hand is being updated
     */
    private static void shiftCardsUI(ActorRef out, java.util.List<Card> hand, int removedIndex, int player){
        for (int i = removedIndex; i < 6; i++) {
            BasicCommands.deleteCard(out, i + 1);
            doSleep(40);

            if (i < hand.size()) {
                Card c = hand.get(i);
                BasicCommands.drawCard(out, c, i + 1, 0);
                doSleep(40);
            }
        }
    }

    /**
     * Introduces a delay to allow UI commands to execute in the correct order.
     * Prevents UI desynchronization.
     *
     * @param ms The sleep duration in milliseconds
     */
    private static void doSleep(int ms){
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
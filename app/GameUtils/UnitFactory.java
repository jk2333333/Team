package GameUtils;

import structures.basic.Card;
import structures.basic.Unit;
import utils.BasicObjectBuilders;

/**
 * UnitFactory: Responsible for creating Unit instances from card JSON configurations.
 * - This class works with UnitManager to handle unit creation.
 * - Ensures units are correctly initialized with attack and health values.
 */
public class UnitFactory {

    /**
     * Creates a Unit instance based on the provided card configuration.
     * - Loads the unit based on the card's predefined unit configuration.
     * - Assigns attack and health values, ensuring they are at least 1.
     *
     * @param card The card containing unit configuration and stats.
     * @return The generated Unit instance.
     */
    public static Unit createUnitFromCard(Card card) {
        Unit newUnit = BasicObjectBuilders.loadUnit(card.getUnitConfig(), -1, Unit.class);

        // Ensure attack and health values are properly assigned (minimum value of 1)
        int attackValue = (card.getAttack() > 0) ? card.getAttack() : 1;
        int healthValue = (card.getHealth() > 0) ? card.getHealth() : 1;

        newUnit.setAttack(attackValue);
        newUnit.setHealth(healthValue);

        return newUnit;
    }
}

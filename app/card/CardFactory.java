package card;

import structures.basic.Card;
import card.player1.*;
import card.player2.*;

/**
 * Factory class for creating card effects based on the card configuration.
 */
public class CardFactory {
    
    /**
     * Creates a card effect for the given card
     * 
     * @param card The card to create an effect for
     * @return The CardEffect for the card, or null if not found
     */
    public static CardEffect createCardEffect(Card card) {
        if (card == null) return null;
        
        String cardName = card.getCardname();
        
        // Player 1 cards
        if (cardName.equals("Bad Omen")) return new BadOmenEffect();
        if (cardName.equals("Gloom Chaser")) return new GloomChaserEffect();
        if (cardName.equals("Rock Pulveriser")) return new RockPulveriserEffect();
        if (cardName.equals("Shadow Watcher")) return new ShadowWatcherEffect();
        if (cardName.equals("Nightsorrow Assassin")) return new NightsorrowAssassinEffect();
        if (cardName.equals("Bloodmoon Priestess")) return new BloodmoonPriestessEffect();
        if (cardName.equals("Shadowdancer")) return new ShadowdancerEffect();
        if (cardName.equals("Horn of the Forsaken")) return new HornOfTheForsakenEffect();
        if (cardName.equals("Wraithling Swarm")) return new WraithlingSwarmEffect();
        if (cardName.equals("Dark Terminus")) return new DarkTerminusEffect();
        
        // Player 2 cards
        if (cardName.equals("Swamp Entangler")) return new SwampEntanglerEffect();
        if (cardName.equals("Silverguard Squire")) return new SilverguardSquireEffect();
        if (cardName.equals("Skyrock Golem")) return new SkyrockGolemEffect();
        if (cardName.equals("Saberspine Tiger")) return new SaberspineTigerEffect();
        if (cardName.equals("Silverguard Knight")) return new SilverguardKnightEffect();
        if (cardName.equals("Young Flamewing")) return new YoungFlamewingEffect();
        if (cardName.equals("Ironcliff Guardian")) return new IroncliffGuardianEffect();
        //if (cardName.equals("Sundrop Elixir")) return new SundropElixirEffect();
        if (cardName.equals("Truestrike")) return new TruestrikeEffect();
        //if (cardName.equals("Beamshock")) return new BeamshockEffect();
        
        return null;
    }
}
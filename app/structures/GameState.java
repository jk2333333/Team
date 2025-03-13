package structures;
import structures.basic.Card;
import structures.basic.Player;
import structures.basic.Tile;
import structures.basic.Unit;
import utils.OrderedCardLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the game state, including all game-related data structures.
 * This class only maintains data storage and provides methods such as `loadDecks()`.
 * It does not contain logic for drawing cards or switching turns.
 */
public class GameState {

    public boolean gameInitalised = false; // Indicates if the game has been initialized

    // The game board (9x5 tiles)
    public Tile[][] board = new Tile[9][5];

    // Card decks and hands for both players (stored as Lists)
    public List<Card> player1Deck = new ArrayList<>();
    public List<Card> player2Deck = new ArrayList<>();
    public List<Card> player1Hand = new ArrayList<>();
    public List<Card> player2Hand = new ArrayList<>();

    // Current game turn information
    public int currentTurn = 1;
    public int currentPlayer = 1; // 1 for player 1, 2 for player 2

    // Player and their avatars (Hero Units)
    public Player player1;
    public Player player2;
    public Unit player1Avatar;
    public Unit player2Avatar;

    // List of active units for each player
    public List<Unit> playerUnits;

    // Tiles and cards related to user interactions (highlighted/movable/selected)
    public List<Tile> movableTiles;
    public List<Tile> attackableTiles;
    public List<Tile> summonableTiles;
    public Card selectedCard;
    public int selectedHandPosition = -1; // Default value when no card is selected
    public Unit selectedUnit;
    private int currentUnitId;

    public Tile selectedTile;  // 记录当前选中的棋子 Tile


    /**
     * Initializes the game state, including player objects and empty lists for cards and units.
     */
    public GameState() {
        player1 = new Player(20, 0);
        player2 = new Player(20, 0);

        playerUnits = new ArrayList<>();

        movableTiles = new ArrayList<>();
        attackableTiles = new ArrayList<>();
        summonableTiles = new ArrayList<>();
        currentUnitId = 0;
    }

    /**
     * Loads the decks for both players.
     * Each player is assigned a deck of 20 cards.
     */
    public void loadDecks() {
        this.player1Deck = OrderedCardLoader.getPlayer1Cards(20);
        this.player2Deck = OrderedCardLoader.getPlayer2Cards(20);
    }

    /**
     * Sets the avatars (hero units) for both players.
     * @param p1 Avatar for player 1
     * @param p2 Avatar for player 2
     */
    public void setAvatars(Unit p1, Unit p2) {
        this.player1Avatar = p1;
        this.player2Avatar = p2;
        playerUnits.add(player1Avatar);
        playerUnits.add(player2Avatar);
    }

    /**
     * Retrieves a unit by its unique ID.
     * @param id The unit's unique identifier
     * @return The unit with the given ID, or `null` if not found
     */
    public Unit getUnitById(int id) {
        for (Unit u : playerUnits) {
            if (u.getId() == id)
                return u;
        }
        return null;
    }
    
    public int getCurrentUnitId() {
        int id = this.currentUnitId;
        ++this.currentUnitId;
        return id;
    }
}
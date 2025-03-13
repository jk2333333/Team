package structures.basic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

import actors.GameActor;
import structures.GameState;

/**
 * Represents a unit on the game board.
 * Each unit has a unique ID, animation state, position, animation data, and
 * image correction information.
 * The newly added `hasMoved` and `hasAttacked` flags track whether the unit has
 * moved or attacked
 * during the current turn. These flags are reset at the beginning of each turn
 * and used in movement
 * and attack logic.
 *
 * @author Dr. Richard McCreadie
 */
public class Unit {

	@JsonIgnore
	protected static ObjectMapper mapper = new ObjectMapper(); // Jackson serialization tool

	int id;
	private int health;
	private int attack;
	@JsonIgnore
	private Tile tile;
	private int owner;

	private UnitAnimationType animation;
	private Position position;
	private UnitAnimationSet animations;
	private ImageCorrection correction;

	// Newly added fields: Track whether the unit has moved or attacked
	private int moves;
	private int maxMoves;
	private int attacks;
	private int maxAttacks;
	private boolean sleeping;
	private boolean isAvartar1;
	private boolean isAvartar2;

	public Unit() {
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;

		this.isAvartar1 = false;
		this.isAvartar2 = false;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		position = new Position(0, 0, 0, 0);
		this.correction = correction;
		this.animations = animations;
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
	}

	public Unit(int id, UnitAnimationSet animations, ImageCorrection correction, Tile currentTile) {
		super();
		this.id = id;
		this.animation = UnitAnimationType.idle;
		position = new Position(currentTile.getXpos(), currentTile.getYpos(), currentTile.getTilex(),
				currentTile.getTiley());
		this.correction = correction;
		this.animations = animations;
		this.tile = currentTile;
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
	}

	public Unit(int id, UnitAnimationType animation, Position position, UnitAnimationSet animations,
			ImageCorrection correction) {
		super();
		this.id = id;
		this.animation = animation;
		this.position = position;
		this.animations = animations;
		this.correction = correction;
		this.moves = 0;
		this.maxMoves = 1;
		this.attacks = 0;
		this.maxAttacks = 1;
		this.sleeping = true;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getAttack() {
		return attack;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	// Getter and Setter methods
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public UnitAnimationType getAnimation() {
		return animation;
	}

	public void setAnimation(UnitAnimationType animation) {
		this.animation = animation;
	}

	public ImageCorrection getCorrection() {
		return correction;
	}

	public void setCorrection(ImageCorrection correction) {
		this.correction = correction;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public UnitAnimationSet getAnimations() {
		return animations;
	}

	public void setAnimations(UnitAnimationSet animations) {
		this.animations = animations;
	}

	/**
	 * Sets the unit's owner (1 = Player, 2 = AI)
	 */
	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	/**
	 * Sets the unit's position based on the given tile.
	 * 
	 * @param tile The tile where the unit is placed.
	 */
	@JsonIgnore
	public void setPositionByTile(Tile tile) {
		position = new Position(tile.getXpos(), tile.getYpos(), tile.getTilex(), tile.getTiley());
		if (this.tile != null) {
			this.tile.setUnit(null);
		}
		this.tile = tile;
		tile.setUnit(this);
	}

	// The following methods are newly added for tracking unit actions.
	public void setMaxMoves(int num) {
		this.maxMoves = num;
	}

	public void setMaxAttacks(int num) {
		this.maxAttacks = num;
	}

	/**
	 * Checks if the unit is allowed to move.
	 */
	public boolean canMove() {
		return (!sleeping && maxMoves - moves > 0);
	}

	/**
	 * Checks if the unit is allowed to attack.
	 */
	public boolean canAttack(GameState gameState) {

		// Check if there is an adjacent enemy
		// Define attackable tiles (adjacent & diagonal)
		int[][] directions = new int[][] {
				{ 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 }, // Adjacent tiles (right, left, down, up)
				{ 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 } // Diagonal tiles (top-right, bottom-right, top-left,
															// bottom-left)
		};

		int cx = getPosition().getTilex();
		int cy = getPosition().getTiley();

		// Check all adjacent and diagonal tiles
		boolean hasAdjacentEnemy = false;
		for (int[] dir : directions) {
			int nx = cx + dir[0];
			int ny = cy + dir[1];

			if (nx >= 0 && nx < 9 && ny >= 0 && ny < 5) { // Ensure within board limits
				Tile tile = gameState.board[nx][ny];

				// If the tile has an enemy, set hasAdjacentEnemy to true
				if (tile.getUnit() != null && tile.getUnit().getOwner() != gameState.currentPlayer) {
					hasAdjacentEnemy = true;
				}
			}
		}
		return hasAdjacentEnemy && (!sleeping && maxAttacks - attacks > 0);
	}

	public void addMoves() {
		++moves;
	}

	public void addAttacks() {
		++attacks;
	}

	public void cantMove() {
		moves = maxMoves;
	}

	/**
	 * Resets the unit's status at the beginning of each turn.
	 */
	public void resetTurnStatus() {
		this.moves = 0;
		this.attacks = 0;
		sleeping = false;
	}

	/**
	 * Retrieves the tile where the unit is currently located.
	 * 
	 * @return The tile occupied by the unit.
	 */
	public Tile getTile() {
		return tile;
	}

	/**
	 * Sets the tile where the unit is currently located.
	 * 
	 * @param tile The tile occupied by the unit.
	 */
	public void setTile(Tile tile) {
		setPositionByTile(tile);
	}

	public void setIsAvartar(int n) {
		if (n == 1) {
			this.isAvartar1 = true;
		} else if (n == 2) {
			this.isAvartar2 = true;
		} else {
			System.out.println("Player not exist. ");
		}
	}

	public boolean getIsAvartar(int n) {
		if (n == 1) {
			return this.isAvartar1;
		} else if (n == 2) {
			return this.isAvartar2;
		}
		System.out.println("Player not exist. ");
		return false;
	}
}

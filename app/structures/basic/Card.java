package structures.basic;

import actors.GameActor;
import akka.actor.ActorRef;
import commands.BasicCommands;
import managers.GeneralManager;
import structures.GameState;

/**
 * 这是卡牌的基本表示，用于玩家手牌的渲染。
 * 卡牌具有 id、名称 (cardname) 和法力消耗 (manacost)。
 * 迷你版本 (miniCard) 用于手牌显示，大版本 (bigCard) 用于点击放大查看。
 *
 * **扩展功能:**
 * - **增加 `attack` 和 `health`** 以支持单位的攻击力和生命值
 * - **确保 `getUnitConfig()` 仍然能正确获取单位配置**
 *
 * @author Dr. Richard McCreadie
 */
public class Card {

	int id;
	String cardname;
	int manacost;

	MiniCard miniCard;
	BigCard bigCard;

	boolean isCreature;
	String unitConfig;

	// **新增字段: 攻击力和生命值**
	int attack;
	int health;

	public Card() {
	}

	public Card(int id, String cardname, int manacost, MiniCard miniCard, BigCard bigCard, boolean isCreature,
			String unitConfig, int attack, int health) {
		this.id = id;
		this.cardname = cardname;
		this.manacost = manacost;
		this.miniCard = miniCard;
		this.bigCard = bigCard;
		this.isCreature = isCreature;
		this.unitConfig = unitConfig;
		this.attack = attack;
		this.health = health;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCardname() {
		return cardname;
	}

	public void setCardname(String cardname) {
		this.cardname = cardname;
	}

	public int getManacost() {
		return manacost;
	}

	public void setManacost(int manacost) {
		this.manacost = manacost;
	}

	public MiniCard getMiniCard() {
		return miniCard;
	}

	public void setMiniCard(MiniCard miniCard) {
		this.miniCard = miniCard;
	}

	public BigCard getBigCard() {
		return bigCard;
	}

	public void setBigCard(BigCard bigCard) {
		this.bigCard = bigCard;
	}

	public boolean getIsCreature() {
		return isCreature;
	}

	public void setIsCreature(boolean isCreature) {
		this.isCreature = isCreature;
	}

	public boolean isCreature() {
		return isCreature;
	}

	public String getUnitConfig() {
		return unitConfig;
	}

	public void setUnitConfig(String unitConfig) {
		this.unitConfig = unitConfig;
	}

	// **新增 getter 和 setter**
	/**
	 * 获取卡牌攻击力，从 bigCard 中获取
	 * 
	 * @return 卡牌攻击力
	 */
	public int getAttack() {
		// 优先从 bigCard 中获取攻击力
		if (this.bigCard != null && this.isCreature()) {
			return this.bigCard.getAttack();
		}
		return attack;
	}

	/**
	 * 获取卡牌生命值，从 bigCard 中获取
	 * 
	 * @return 卡牌生命值
	 */
	public int getHealth() {
		// 优先从 bigCard 中获取生命值
		if (this.bigCard != null && this.isCreature()) {
			return this.bigCard.getHealth();
		}
		return health;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setHighlightStatus(ActorRef out, GameState gameState, int mode) {
		GeneralManager.sleep(10);
		BasicCommands.drawCard(out, this, gameState.selectedHandPosition, mode);
	}

}

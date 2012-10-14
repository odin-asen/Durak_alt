package game;

import utilities.constants.GameConfigurationConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:33
 *
 * This class represents the gaming process. The game process can be directed by
 * this class.
 */
public class GameProcess {
  private static Logger LOGGER = Logger.getLogger(GameProcess.class.getName());

  private static GameProcess gameProcess;
  public static final int INDEX_FIRST_ATTACKER = 0;
  public static final int INDEX_SECOND_ATTACKER = 1;
  public static final int ATTACKERS_MAXIMUM = 2;

  private List<Player> playerList;
  private List<Player> attackers;
  private Player defender;
  private GameCardStack stack;
  private boolean gameInProcess;

  /* Constructors */
  private GameProcess() {
    this.playerList = new ArrayList<Player>();
    this.attackers = new ArrayList<Player>(ATTACKERS_MAXIMUM);
    defender = null;
    gameInProcess = false;
  }

  public static GameProcess getInstance() {
    if(gameProcess == null) {
      gameProcess = new GameProcess();
    }
    return gameProcess;
  }

  /* Methods */
  /**
   * This method initialises a game. It sets up a new shuffled stack of {@code cardsPerColour}
   * where the maximum of the cards are bordered by the stack. It also distributes 6 cards
   * to each player in the game and determines the first and second attacker and the defender
   * of the game.
   */
  public void initialiseNewGame(Integer cardsPerColour) {
    distributeCards(cardsPerColour);
    determineInitialPlayers();
    setGameInProcess(true);
  }

  /**
   * Determines the first and second attackers and the defender.
   */
  private void determineInitialPlayers() {
    final Short trumpColour = this.stack.getTrumpCard().getCardColour();
    Player smallestColourPlayer = whoHasSmallestColour(trumpColour);
    if(smallestColourPlayer == null)
      LOGGER.log(Level.INFO, "No player has a trump!");
    else {
      setFirstAttacker(smallestColourPlayer);
    }
  }

  /**
   * Sets the first attacker in the round and therefore the defender and
   * the second attacker.
   * @param firstAttacker The player who is the first attacker
   */
  private void setFirstAttacker(Player firstAttacker) {
    Player secondAttacker = firstAttacker.getLeftPlayer().getLeftPlayer();

    firstAttacker.setAttacking(true);
    secondAttacker.setAttacking(true);

    attackers.set(INDEX_FIRST_ATTACKER, firstAttacker);
    attackers.set(INDEX_SECOND_ATTACKER, secondAttacker);
    this.setDefender(firstAttacker.getLeftPlayer());
  }

  private Player whoHasSmallestColour(Short cardColour) {
    GameCard currentSmallestCard = null;
    Player smallestColourPlayer = null;

    for (Player player : playerList) {
      final GameCard smallestColour = player.getSmallestValue(cardColour);
      if(smallestColour != null)
        if(smallestColour.hasLowerValue(currentSmallestCard)) {
          currentSmallestCard = smallestColour;
          smallestColourPlayer = player;
        }
    }

    return smallestColourPlayer;
  }

  /**
   * This method initialises a new game. It sets up a new shuffled stack of 36 cards
   * and distributes 6 cards to each player in the game.
   */
  public void distributeCards(Integer cardsPerColour) {
    this.stack = GameCardStack.getInstance();
    this.stack.initialiseStack(cardsPerColour);
    System.out.println(this.stack);
    for(int i = 0; i< GameConfigurationConstants.INITIAL_CARD_COUNT; i++)
      for (Player player : playerList)
        player.pickUpCard(this.stack.drawCard());
  }

  /**
   * Switches the players states, e.g isDefender, and determines, who is the next player
   * to defend and to attack. One round starts when a new player has to defend and ends
   * when the attackers and the defender pick up new cards.
   */
  public void nextRound() {

  }

  public void addPlayer(Player player) {
    if(!gameInProcess) {
      playerList.add(player);
    }
  }

  /* Getter and Setter */
  public List<Player> getPlayerList() {
    return playerList;
  }

  public void setPlayerList(List<Player> playerList) {
    if(!gameInProcess)
      this.playerList = playerList;
  }

  public List<Player> getAttackers() {
    return attackers;
  }

  public void setAttackers(List<Player> attackers) {
    this.attackers = attackers;
    for (Player attacker : attackers) {
      if(attacker != null)
        attacker.setAttacking(true);
    }

    if(attackers.contains(defender))
      defender = null;
  }

  public Player getDefender() {
    return defender;
  }

  public void setDefender(Player defender) {
    this.defender = defender;
    if (defender != null) {
      this.defender.setDefending(true);
    }

    if(attackers.contains(defender))
      attackers.remove(defender);
  }

  public GameCardStack getStack() {
    return stack;
  }

  public void setStack(GameCardStack stack) {
    if(!gameInProcess)
      this.stack = stack;
  }

  public boolean isGameInProcess() {
    return gameInProcess;
  }

  private void setGameInProcess(boolean gameInProcess) {
    this.gameInProcess = gameInProcess;
  }
}

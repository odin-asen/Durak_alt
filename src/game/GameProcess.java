package game;

import dto.ClientInfo;
import utilities.constants.GameConfigurationConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utilities.constants.GameCardConstants.CardColour;

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
  private Boolean gameInProcess;

  /* Constructors */
  private GameProcess() {
    this.playerList = new ArrayList<Player>();
    initAttackers();
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
  private void initAttackers() {
    attackers = new ArrayList<Player>(ATTACKERS_MAXIMUM);
    for (int i = 0; i < ATTACKERS_MAXIMUM; i++) {
      attackers.add(new Player(new ClientInfo("")));
    }
  }

  /**
   * This method initialises a game. It sets up a new shuffled stack of {@code cardsPerColour}
   * cards per colour, where the maximum of the cards per colour are bordered by the stack.
   * It also distributes {@link GameConfigurationConstants#INITIAL_CARD_COUNT} cards to each
   * player in the game and determines the first and second attacker and the defender of the game.
   * The game will only be initialised if more than one player is in the list. Otherwise it does
   * nothing.
   * @param cardsPerColour Number of cards per colour for this game.
   * @return True, if the game has been initialised, otherwise false.
   */
  public Boolean initialiseNewGame(Integer cardsPerColour) {
    if(playerList.size() > 1) {
      distributeCards(cardsPerColour);
      determineInitialPlayers();
      setGameInProcess(true);
      return true;
    } else return false;
  }

  /**
   * Determines the first and second attackers and the defender.
   */
  private void determineInitialPlayers() {
    final CardColour trumpColour = this.stack.getTrumpCard().getCardColour();
    Player smallestColourPlayer = whoHasSmallestColour(trumpColour);
    //TODO einen zufälligen starter auswählen (vielleicht anhand der ID)
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

  private Player whoHasSmallestColour(CardColour cardColour) {
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
   * and distributes {@link GameConfigurationConstants#INITIAL_CARD_COUNT} cards
   * to each player in the game and sets the players neighbours.
   */
  public void distributeCards(Integer cardsPerColour) {
    initPlayerNeighbours();
    stack = GameCardStack.getInstance();
    stack.initialiseStack(cardsPerColour);
    for(int i = 0; i< GameConfigurationConstants.INITIAL_CARD_COUNT; i++)
      for (Player player : playerList)
        player.pickUpCard(this.stack.drawCard());
  }

  private void initPlayerNeighbours() {
    for (int index = 0; index < playerList.size(); index++) {
      final Player player = playerList.get(index);
      setPlayerNeighbours(index, player);
    }
  }

  private void setPlayerNeighbours(int index, Player player) {
    if(index == 0) {
      player.setLeftPlayer(playerList.get(playerList.size()-1));
      player.setRightPlayer(playerList.get(index+1));
    } else if(index+1 == playerList.size()) {
      player.setLeftPlayer(playerList.get(index-1));
      player.setRightPlayer(playerList.get(0));
    } else {
      player.setLeftPlayer(playerList.get(index-1));
      player.setRightPlayer(playerList.get(index+1));
    }
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

  public void removePlayer(Player player) {
    if(playerList.contains(player))
      removePlayer(player);
  }

  public void removePlayer(ClientInfo info) {
    int playerIndex = 0;
    for (int index = 0; index < playerList.size(); index++) {
      if(playerList.get(index).contains(info)) {
        playerIndex = index;
      }
    }
    playerList.remove(playerIndex);
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

  public boolean isGameInProcess() {
    return gameInProcess;
  }

  private void setGameInProcess(boolean gameInProcess) {
    this.gameInProcess = gameInProcess;
  }
}

package game;

import dto.DTOCard;
import game.rules.RuleChecker;
import game.rules.RuleException;
import game.rules.RuleFactory;
import rmi.GameAction;
import server.business.rmiImpl.AttackAction;
import server.business.rmiImpl.DefenseAction;
import utilities.Converter;
import utilities.constants.GameConfigurationConstants;
import utilities.constants.PlayerConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:33
 *
 * This class represents the gaming process. The game process can be directed by
 * this class.
 */
public class GameProcess {
  private static GameProcess gameProcess;

  private List<Player> playerList;
  private List<GameCard> attackCards;
  private List<GameCard> defenseCards;
  private GameCardStack stack;
  private Boolean gameInProcess;

  private RuleChecker ruleChecker;

  /* Constructors */
  private GameProcess() {
    this.playerList = new ArrayList<Player>();
    this.attackCards = new ArrayList<GameCard>();
    this.defenseCards = new ArrayList<GameCard>();
    ruleChecker = RuleFactory.getStandardRules();
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
    ruleChecker.setTrumpColour(stack.getTrumpCard().getCardColour());
    ruleChecker.initStartPlayer(playerList);
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
   * Validates a surpassed action with the current settings of the RuleChecker
   * object.
   * @param action Action to validate.
   * @throws RuleException If a rule has been broken, a
   * {@link game.rules.RuleException} will be thrown with a message for the client.
   * @throws IllegalArgumentException If {@code action} is not instanceof
   * {@link server.business.rmiImpl.AttackAction} or {@link server.business.rmiImpl.DefenseAction}
   */
  public void validateAction(GameAction action) throws RuleException {
    if(action instanceof AttackAction) {
      final AttackAction attack = (AttackAction) action;
      validateAttack(attack);
    } else if(action instanceof DefenseAction) {
      DefenseAction defense = (DefenseAction) action;
      validateDefend(defense);
    } else throw new IllegalArgumentException("The parameter is neither instanceof\n"+
      AttackAction.class +" nor\n"+DefenseAction.class);
  }

  private void validateDefend(DefenseAction defense) throws RuleException {
    ruleChecker.canDoDefendMove(
        playerList.get(defense.getExecutor().getLoginNumber()), attackCards.isEmpty(),
        Converter.fromDTO(defense.getDefendCard()),
        Converter.fromDTO(defense.getAttackCard()));

    Collections.addAll(defenseCards, Converter.fromDTO(defense.getDefendCard()));
  }

  private void validateAttack(AttackAction attack) throws RuleException {
    final List<GameCard> allCards = new ArrayList<GameCard>();
    final List<GameCard> cards = Converter.fromDTO(attack.getCards());
    Collections.addAll(allCards, (GameCard[]) attackCards.toArray());
    Collections.addAll(allCards, (GameCard[]) defenseCards.toArray());

    ruleChecker.canDoAttackMove(playerList.get(attack.getExecutor().getLoginNumber()),
        cards, allCards);

    Collections.addAll(attackCards, (GameCard[]) cards.toArray());
  }

  /**
   * Switches the players states, e.g isDefender, and determines, who is the next player
   * to defend and to attack. One round starts when a new player has to defend and ends
   * when the attackers and the defender pick up new cards.
   */
  public void nextRound() {

  }

  public void addPlayer() {
    if(!gameInProcess) {
      playerList.add(new Player());
    }
  }

  public void removePlayer(int clientIndex) {
    playerList.remove(clientIndex);
  }

  public List<List<DTOCard>> getPlayerCards() {
    return Converter.playersCardsToDTO(playerList);
  }

  /* Getter and Setter */
  public boolean isGameInProcess() {
    return gameInProcess;
  }

  private void setGameInProcess(boolean gameInProcess) {
    this.gameInProcess = gameInProcess;
  }

  public List<PlayerConstants.PlayerType> getPlayerTypes() {
    List<PlayerConstants.PlayerType> types = new ArrayList<PlayerConstants.PlayerType>();
    for (Player player : playerList) {
      types.add(player.getType());
    }

    return types;
  }
}

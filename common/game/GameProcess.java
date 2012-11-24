package common.game;

import common.dto.DTOCard;
import common.game.rules.RuleChecker;
import common.game.rules.RuleException;
import common.game.rules.RuleFactory;
import common.rmi.GameAction;
import common.utilities.Converter;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
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

  private ElementPairHolder<GameCard> inGameCardHolder;

  private List<Player> playerList;
  private GameCardStack stack;
  private Boolean gameInProcess;
  private Boolean initialiseNew;

  private RuleChecker ruleChecker;

  /* Constructors */
  private GameProcess() {
    initialiseInstance();
  }

  public static GameProcess getInstance() {
    if(gameProcess == null) {
      gameProcess = new GameProcess();
    }
    return gameProcess;
  }

  /* Methods */
  private void initialiseInstance() {
    playerList = new ArrayList<Player>();
    inGameCardHolder = new ElementPairHolder<GameCard>();
    gameInProcess = false;
    initialiseNew = true;
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
      ruleChecker = RuleFactory.getStandardRules();
      distributeCards(cardsPerColour);
      determineInitialPlayers();
      gameInProcess = true;
      return true;
    } else return false;
  }

  /**
   * Determines the first and second attackers and the defender.
   */
  private void determineInitialPlayers() {
    ruleChecker.setTrumpColour(stack.getTrumpCard().getCardColour());
    if(initialiseNew)
      ruleChecker.initStartPlayer(playerList);
    else ruleChecker.setActivePlayers(determineLoser().getRightPlayer());
  }

  /**
   * This method initialises a new game. It sets up a new shuffled stack of 36 cards
   * and distributes {@link GameConfigurationConstants#INITIAL_CARD_COUNT} cards
   * to each player in the game and sets the players neighbours.
   */
  private void distributeCards(Integer cardsPerColour) {
    initPlayerNeighbours();
    stack = GameCardStack.getInstance();
    stack.initialiseStack(cardsPerColour);
    for(int i = 0; i< GameConfigurationConstants.INITIAL_CARD_COUNT; i++)
      for (Player player : playerList)
        player.pickUpCard(stack.drawCard());
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
  public void validateAction(ValidationAction validation, GameAction action)
      throws RuleException, RemoteException {
    if(validation.equals(ValidationAction.ATTACK)) {
      validateAttack(action);
    } else if(validation.equals(ValidationAction.DEFENSE)) {
      validateDefense(action);
    } else throw new IllegalArgumentException("n" +
      ValidationAction.ATTACK +" nor\n"+ValidationAction.DEFENSE);
  }

  private void validateDefense(GameAction defense) throws RuleException, RemoteException {
    ruleChecker.doDefenseMove(
        playerList.get(defense.getExecutor().loginNumber),
        inGameCardHolder.getFirstElements().isEmpty(),
        Converter.fromDTO(defense.getCardLists().get(1).get(0)),
        Converter.fromDTO(defense.getCardLists().get(0).get(0)));
    inGameCardHolder.joinElements(
        Converter.fromDTO(defense.getCardLists().get(0).get(0)),
        Converter.fromDTO(defense.getCardLists().get(1).get(0)));
  }

  private void validateAttack(GameAction attack) throws RuleException, RemoteException {
    List<GameCard> cards = Converter.fromDTO(attack.getCardLists().get(0));
    ruleChecker.doAttackMove(playerList.get(attack.getExecutor().loginNumber),
        cards, inGameCardHolder.getFirstElements(), inGameCardHolder.getSecondElements());

    for (GameCard card : cards)
      inGameCardHolder.addPair(card, null);
  }

  /**
   * Switches the players states, e.g isDefender, and determines, who is the next player
   * to defend and to attack. One round starts when a new player has to defend and ends
   * when the attackers and the defender pick up new cards.
   * @param takeCards If true, the defender gets all current cards on the game field
   *                  and is not the first attacker of the next round.
   * @param type Player type that shows who send the next round request.
   * @return Returns true, if the next round was initialised, false, if not.
   */
  public Boolean nextRound(PlayerConstants.PlayerType type, Boolean takeCards) {
    Boolean result = false;
    if(PlayerConstants.PlayerType.DEFENDER.equals(type)) {
      result = defenderRequestNextRound(takeCards, ruleChecker.getDefender());
    } else if(PlayerConstants.PlayerType.FIRST_ATTACKER.equals(type) ||
        PlayerConstants.PlayerType.SECOND_ATTACKER.equals(type)) {
      ruleChecker.setAttackerReadyNextRound(type);
    }

    if(result)
      inGameCardHolder.clearPairs();

    return result;
  }

  private Boolean defenderRequestNextRound(Boolean takeCards, Player defender) {
    Boolean result = true;

    if(takeCards) {
      final List<List<GameCard>> pairs = inGameCardHolder.getElementPairs();
      for (List<GameCard> pair : pairs) {
        for (GameCard card : pair) {
          defender.pickUpCard(card);
        }
      }
      prepareForNextRound();
      nextRoundOrFinish(defender.getLeftPlayer());
    } else {
      if(ruleChecker.readyForNextRound() && inGameCardHolder.hasNoNullPairs()) {
        prepareForNextRound();
        nextRoundOrFinish(defender);
      } else result = false;
    }

    return result;
  }

  private void nextRoundOrFinish(Player firstAttacker) {
    final Player loser = determineLoser();
    if(loser != null) {
      loser.setType(PlayerConstants.PlayerType.LOSER);
      loser.emptyHand();
      gameInProcess = false;
    } else ruleChecker.setActivePlayers(firstAttacker);
  }

  private void prepareForNextRound() {
    fillPlayerHand(ruleChecker.getFirstAttacker());
    fillPlayerHand(ruleChecker.getSecondAttacker());
    fillPlayerHand(ruleChecker.getDefender());
    updateFinishedPlayer(ruleChecker.getFirstAttacker());
    updateFinishedPlayer(ruleChecker.getSecondAttacker());
    updateFinishedPlayer(ruleChecker.getDefender());
  }

  private Player determineLoser() {
    for (Player player : playerList) {
      if(player.isAlone())
        return player;
    }

    return null;
  }

  private void updateFinishedPlayer(Player player) {
    if(player == null)
      return;

    if(player.getCards().size() == 0) {
      player.getLeftPlayer().setRightPlayer(player.getRightPlayer());
      player.getRightPlayer().setLeftPlayer(player.getLeftPlayer());
      player.setType(PlayerConstants.PlayerType.NOT_LOSER);
    }
  }

  private void fillPlayerHand(Player player) {
    if(player == null)
      return;

    while ((stack.getStackSize() > 0) && (player.getCards().size() < 6)) {
      player.pickUpCard(stack.drawCard());
    }
  }

  public void addPlayer() {
    if(!gameInProcess) {
      playerList.add(new Player());
    }
  }

  public void removePlayer(Short playerIndex) {
    final Player player = getPlayer(playerIndex.intValue());
    playerList.remove(player);
  }

  public List<List<DTOCard>> getPlayerCards() {
    return Converter.playersCardsToDTO(playerList);
  }

  public void abortGame() {
    initialiseInstance();
  }

  public Boolean nextRoundAvailable() {
    return ruleChecker.readyForNextRound();
  }


  public Boolean gameHasFinished() {
    return determineLoser() != null;
  }

  /* Getter and Setter */
  private Player getPlayer(int playerIndex) {
    if(playerIndex >= 0 && playerIndex < playerList.size())
      return playerList.get(playerIndex);
    else return null;
  }
  public Boolean isGameInProcess() {
    return gameInProcess;
  }

  public List<PlayerConstants.PlayerType> getPlayerTypes() {
    final List<PlayerConstants.PlayerType> types = new ArrayList<PlayerConstants.PlayerType>();
    for (Player player : playerList) {
      types.add(player.getType());
    }

    return types;
  }

  public List<GameCard> getAttackCards() {
    return inGameCardHolder.getFirstElements();
  }

  public List<GameCard> getDefenseCards() {
    return inGameCardHolder.getSecondElements();
  }

  /* Inner Classes */
  public static enum ValidationAction {
    ATTACK, DEFENSE
  }
}

class ElementPairHolder<T> {
  private List<List<T>> elementPairs;

  ElementPairHolder() {
    elementPairs = new ArrayList<List<T>>();
  }

  void setPairs(List<T> firstElement, List<T> secondElement) {
    final int size;
    if(firstElement.size() > secondElement.size())
      size = firstElement.size();
    else size = secondElement.size();

    elementPairs = new ArrayList<List<T>>(size);
    for (int index = 0; index < size; index++)
      addPair(getListElement(firstElement, index), getListElement(secondElement, index));
  }

  private T getListElement(List<T> elements, int index) {
    final T element;
    if(index >= elements.size())
      element = null;
    else element = elements.get(index);
    return element;
  }

  void addPair(T firstElement, T secondElement) {
    final List<T> pair = getPair(firstElement, secondElement);
    elementPairs.add(pair);
  }

  private List<T> getPair(T firstElement, T secondElement) {
    final List<T> pair = new ArrayList<T>(2);
    pair.add(firstElement);
    pair.add(secondElement);
    return pair;
  }

  List<T> getFirstElements() {
    final List<T> firstElement = new ArrayList<T>();
    for (List<T> pair : elementPairs) {
      firstElement.add(pair.get(0));
    }
    return firstElement;
  }

  List<T> getSecondElements() {
    final List<T> secondCards = new ArrayList<T>();
    for (List<T> pair : elementPairs) {
      secondCards.add(pair.get(1));
    }
    return secondCards;
  }

  List<List<T>> getElementPairs() {
    return elementPairs;
  }

  void setElementPairs(List<List<T>> elementPairs) {
    this.elementPairs = elementPairs;
  }

  /**
   * Sets the {@code secondElement} as second pair element for {@code firstElement}
   * if this element in the pair holder exists as first element of a pair. Vice versa, the
   * method sets the {@code firstElement} as first pair element for {@code secondElement}
   * if this element in the pair holder exists as second element of a pair.
   * If none of the elements exist, a new pair will be added to the holder where the
   * {@code firstElement} is the first element of the pair and the {@code secondElement}
   * is the second element of the pair.
   * @param firstElement First element of the join action.
   * @param secondElement Second element of the join action.
   */
  public void joinElements(T firstElement, T secondElement) {
    Integer firstFoundIndex = findElementIndex(0, firstElement);
    Integer secondFoundIndex = findElementIndex(1, secondElement);

    setPairElement(firstFoundIndex, 1, secondElement);
    setPairElement(secondFoundIndex, 0, firstElement);

    if((secondFoundIndex == null) && (firstFoundIndex == null))
      addPair(firstElement, secondElement);
  }

  private Integer findElementIndex(int pairIndex, T element) {
    Integer foundIndex = null;
    for (int index = 0; index < elementPairs.size(); index++) {
      final List<T> pair = elementPairs.get(index);
      final T pairElement = pair.get(pairIndex);
      if(pairElement != null && pairElement.equals(element)) {
        foundIndex = index;
        index = elementPairs.size();
      }
    }

    return foundIndex;
  }

  private void setPairElement(Integer pairIndex, Integer pairElement, T element) {
    if(pairIndex != null) {
      final List<T> pair = elementPairs.get(pairIndex);
      pair.set(pairElement, element);
    }
  }

  public void clearPairs() {
    elementPairs = new ArrayList<List<T>>();
  }

  public boolean hasNoNullPairs() {
    for (List<T> elementPair : elementPairs) {
      for (T element : elementPair) {
        if(element == null)
          return false;
      }
    }

    return true;
  }
}
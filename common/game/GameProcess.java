package common.game;

import common.dto.DTOCard;
import common.simon.action.CardAction;
import common.simon.action.FinishAction;
import common.simon.action.GameAction;
import common.game.rules.RuleChecker;
import common.game.rules.RuleException;
import common.game.rules.RuleFactory;
import common.utilities.Converter;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 19:33
 *
 * This class represents the gaming process. The game can be directed by
 * this class. The generic type identifies the class for the player identifier.
 */
public class GameProcess<ID> {
  /**
   * This object lists pairs of cards where the first card of a pair is
   * the attacker card and the second card is the defender card.
   */
  private final ElementPairHolder<GameCard> pairCardHolder;

  private final ListMap<ID,Player> playerHolder;

  private GameCardStack stack;
  private Boolean gameInProcess;
  private Boolean initialiseNew;

  private RuleChecker ruleChecker;

  /* Constructors */
  public GameProcess() {
    pairCardHolder = new ElementPairHolder<GameCard>();
    playerHolder = new ListMap<ID, Player>();
    reInitialiseGame();
  }

  /* Methods */

  public void reInitialiseGame() {
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
    if(playerHolder.size() > 1) {
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
      ruleChecker.initStartPlayer(playerHolder.getList());
    else ruleChecker.setActivePlayers(determineLoser().getRightPlayer());
  }

  /**
   * This method initialises a new game. It sets up a new shuffled stack of 36 cards
   * and distributes {@link GameConfigurationConstants#INITIAL_CARD_COUNT} cards
   * to each player in the game and sets the players neighbours.
   */
  private void distributeCards(Integer cardsPerColour) {
    initPlayerNeighbours();
    stack = new GameCardStack();
    stack.initialiseStack(cardsPerColour);
    for(int i = 0; i< GameConfigurationConstants.INITIAL_CARD_COUNT; i++)
      for (Player player : playerHolder.getList())
        player.pickUpCard(stack.drawCard());
  }

  private void initPlayerNeighbours() {
    for (int index = 0; index < playerHolder.size(); index++) {
      final Player player = playerHolder.getList().get(index);
      setPlayerNeighbours(index, player);
    }
  }

  private void setPlayerNeighbours(int index, Player player) {
    final List<Player> playerList = playerHolder.getList();
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
   * @param playerID Identifier for the player that does the action.
   * @return Returns true, if the next round has started, else false.
   * @throws RuleException If a rule has been broken, a
   * {@link game.rules.RuleException} will be thrown with a message for the client.
   * @throws IllegalArgumentException If {@code action} is not instanceof
   * {@link common.simon.action.CardAction} or {@link common.simon.action.FinishAction}
   */
  public boolean validateAction(GameAction action, ID playerID)
      throws RuleException, IllegalArgumentException {
    boolean nextRound = false;
    if(action instanceof CardAction) {
      final CardAction cardAction = (CardAction) action;
      if(cardAction.getCardActionType().equals(CardAction.CardActionType.ATTACK))
        validateAttack(playerID, (CardAction) action);
      else if(cardAction.getCardActionType().equals(CardAction.CardActionType.DEFENSE))
        validateDefense(playerID, (CardAction) action);
    } else if(action instanceof FinishAction) {
      nextRound = validateFinish(playerID, action);
    } else throw new IllegalArgumentException("GameAction must be either " +
        "instance of "+CardAction.class.getName()+" or "+FinishAction.class.getName());

    return nextRound;
  }

  private boolean validateFinish(ID playerID, GameAction action) {
    final FinishAction finishAction;
    Boolean goToNextRound = false;

    if(action instanceof FinishAction) {
      finishAction = (FinishAction) action;
      if(FinishAction.FinishType.GO_TO_NEXT_ROUND.equals(finishAction.getFinishType())) {
        goToNextRound = nextRound(action.getExecutor().playerType, false);
      } else if(FinishAction.FinishType.TAKE_CARDS.equals(finishAction.getFinishType())) {
        goToNextRound = nextRound(action.getExecutor().playerType, true);
      } else goToNextRound = false;
    }

    return goToNextRound;
  }

  private void validateDefense(ID playerID, CardAction action) throws RuleException {
    final GameCard defenderCard = Converter.fromDTO(action.getDefenderCards().get(0));
    final GameCard attackerCard = Converter.fromDTO(action.getAttackCards().get(0));
    ruleChecker.doDefenseMove(playerHolder.get(playerID),
        pairCardHolder.getFirstElements().isEmpty(),
        defenderCard, attackerCard);
    pairCardHolder.joinElements(attackerCard, defenderCard);
  }

  private void validateAttack(ID playerID, CardAction action)
      throws RuleException {
    List<GameCard> cards = Converter.fromDTO(action.getAttackCards());
    ruleChecker.doAttackMove(playerHolder.get(playerID), cards,
        pairCardHolder.getFirstElements(), pairCardHolder.getSecondElements());

    for (GameCard card : cards)
      pairCardHolder.addPair(card, null);
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
      pairCardHolder.clearPairs();

    return result;
  }

  private Boolean defenderRequestNextRound(Boolean takeCards, Player defender) {
    Boolean result = true;

    if(takeCards) {
      final List<List<GameCard>> pairs = pairCardHolder.getElementPairs();
      for (List<GameCard> pair : pairs) {
        for (GameCard card : pair) {
          defender.pickUpCard(card);
        }
      }
      prepareForNextRound();
      nextRoundOrFinish(defender.getLeftPlayer());
    } else {
      if(ruleChecker.readyForNextRound() && pairCardHolder.hasNoNullPairs()) {
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
    for (Player player : playerHolder.getList()) {
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

  public void setPlayer(ID playerID) {
    if(!gameInProcess) {
      playerHolder.add(playerID, new Player());
    }
  }

  /**
   * Returns whether a player with this id could be removed or not.
   * @param playerID The player having this id should be removed.
   * @return True, the player existed and is now removed, else false.
   */
  public boolean removePlayer(ID playerID) {
    return playerHolder.remove(playerID);
  }

  public List<List<DTOCard>> getPlayersCards() {
    return Converter.playersCardsToDTO(playerHolder.getList());
  }

  public List<DTOCard> getPlayerCards(ID playerID) {
    return Converter.playerCardsToDTO(playerHolder.get(playerID));
  }

  public Boolean nextRoundAvailable() {
    return ruleChecker.readyForNextRound();
  }


  public Boolean gameHasFinished() {
    return determineLoser() != null;
  }

  /* Getter and Setter */

  public Boolean isGameInProcess() {
    return gameInProcess;
  }

  public List<PlayerConstants.PlayerType> getPlayersTypes() {
    final List<PlayerConstants.PlayerType> types = new ArrayList<PlayerConstants.PlayerType>();
    for (Player player : playerHolder.getList()) {
      types.add(player.getType());
    }

    return types;
  }

  public PlayerConstants.PlayerType getPlayerType(ID playerID) {
    return playerHolder.get(playerID).getType();
  }

  public List<GameCard> getAttackCards() {
    return pairCardHolder.getFirstElements();
  }

  public List<GameCard> getDefenseCards() {
    return pairCardHolder.getSecondElements();
  }

  public GameCardStack getStack() {
    return stack;
  }

  /* Inner Classes */
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

class ListMap<K,V> {
  private Map<K,V> map;
  private List<V> list;

  ListMap() {
    map = new Hashtable<K,V>();
    list = new  ArrayList<V>();
  }

  void add(K key, V value) {
    if(map.containsKey(key)) {
      final V oldValue = map.get(key);
      final int index = Miscellaneous.findIndex(list,oldValue);
      list.set(index,value);
    } else {
      list.add(value);
    }
    map.put(key, value);
  }

  V get(K key) {
    return map.get(key);
  }

  boolean remove(K key) {
    boolean removed = map.containsKey(key);
    list.remove(map.get(key));
    map.remove(key);
    return removed;
  }

  public int size() {
    return list.size();
  }

  public List<V> getList() {
    return list;
  }
}
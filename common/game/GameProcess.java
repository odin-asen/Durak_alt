package common.game;

import common.dto.DTOCard;
import common.game.rules.RuleChecker;
import common.game.rules.RuleException;
import common.game.rules.RuleFactory;
import common.i18n.BundleStrings;
import common.i18n.I18nSupport;
import common.simon.action.CardAction;
import common.simon.action.FinishAction;
import common.simon.action.GameAction;
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
  private Player currentLoser;

  private GameCardStack stack;
  private boolean gameInProcess;
  /** Indicates whether the game will be initialised for a new constellation of players or not. */
  private boolean initialiseNew;

  private RuleChecker ruleChecker;
  private RoundStateHandler roundState;
  private boolean cardsChanged;

  /* Constructors */

  public GameProcess() {
    pairCardHolder = new ElementPairHolder<GameCard>();
    playerHolder = new ListMap<ID, Player>();
    reInitialise();
  }

  /* Methods */

  /**
   * Stops the process without deleting the player list.
   */
  public void stopProcess() {
    pairCardHolder.clear();
    gameInProcess = false;
    cardsChanged = false;
    roundState = new RoundStateHandler();
  }

  /**
   * Sets the game to an initialised state. All variables will be reset and all lists
   * will be emptied.
   */
  public void reInitialise() {
    stopProcess();
    currentLoser = null;
    playerHolder.clear();
    initialiseNew = true;
  }

  /**
   * This method initialises a game. It sets up a new shuffled stack of {@code cardsPerColour}
   * cards per colour, where the maximum of the cards per colour are bordered by the stack.
   * It also distributes {@link GameConfigurationConstants#INITIAL_CARD_COUNT} cards to each
   * player in the game and determines the first and second attacker and the defender of the game.
   * The game will only be initialised if more than one player is in the list. Otherwise it does
   * nothing.
   * <p/>
   * Note: The number of added players has to be greater than 1 otherwise the method will assert.
   * @param cardsPerColour Number of cards per colour for this game.
   */
  public void initialiseNewGame(Integer cardsPerColour) {
    assert playerHolder.size() > 1;

    ruleChecker = RuleFactory.getStandardRules();
    initPlayers();
    distributeCards(cardsPerColour);
    determineInitialPlayers();
    gameInProcess = true;
    initialiseNew = false;
    currentLoser = null;
    roundState.setJustTwoPlayer(playerHolder.size() == 2);
  }

  /**
   * Determines the first and second attackers and the defender.
   */
  private void determineInitialPlayers() {
    ruleChecker.setTrumpColour(stack.getTrumpCard().getCardColour());

    final Player starter;
    if(!initialiseNew && (currentLoser != null))
      starter = currentLoser.getRightPlayer();
    else starter = ruleChecker.determineStartPlayer(playerHolder.getList());

    ruleChecker.setActivePlayers(starter);
  }

  /**
   * This method initialises a new game. It sets up a new shuffled stack of the surpassed
   * number of cards per colour and distributes
   * {@link GameConfigurationConstants#INITIAL_CARD_COUNT} cards
   * to each player in the game.
   */
  private void distributeCards(Integer cardsPerColour) {
    stack = new GameCardStack();
    stack.initialiseStack(cardsPerColour);
    for(int i = 0; i< GameConfigurationConstants.INITIAL_CARD_COUNT; i++)
      for (Player player : playerHolder.getList())
        player.pickUpCard(stack.drawCard());
  }

  /** Initialises the players and sets their neighbours. */
  private void initPlayers() {
    for (int index = 0; index < playerHolder.size(); index++) {
      final Player player = playerHolder.getList().get(index);
      player.initPlayer();
      /* set the neighbours */
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
   * Goes to the next round. This means that the player types of the players may be switched,
   * depending on the fact if the defender took cards or not. The players get also new cards
   * and it will determined if the game is over. The method will assert if the game is not in process.
   * @return Returns true if next round has started, otherwise false. A round may not be started
   *         if not all players confirmed to go to the next round.
   */
  public boolean goToNextRound() {
    assert gameInProcess;

    if(!roundState.readyForNextRound())
      return false;

    final Player defender = ruleChecker.getDefender();
    final Player nextFirstAttacker;
    if(roundState.defenderTookCards()) {
      final List<List<GameCard>> pairs = pairCardHolder.getElementPairs();
      for (List<GameCard> pair : pairs) {
        for (GameCard card : pair) {
          defender.pickUpCard(card);
        }
      }
      nextFirstAttacker = defender.getLeftPlayer();
    } else nextFirstAttacker = defender;

    prepareForNextRound();
    nextRoundOrFinish(nextFirstAttacker);

    return true;
  }

  private void nextRoundOrFinish(Player firstAttacker) {
    if(gameHasFinished()) {
      currentLoser.setType(PlayerConstants.PlayerType.LOSER);
      currentLoser.emptyHand();
      gameInProcess = false;
    } else ruleChecker.setActivePlayers(firstAttacker);
  }

  /**
   * Validates a surpassed action with the current settings of the RuleChecker
   * object. The method will assert if the game is not in process.
   * @param action Action to validate.
   * @param playerID Identifier for the player that does the action.
   * @return Returns true, if the next round can be started, else false.
   * @throws RuleException If a rule has been broken, an exception will be thrown
   *         with a message for the client.
   * @throws IllegalArgumentException If {@code action} is not instanceof
   * {@link common.simon.action.CardAction} or {@link common.simon.action.FinishAction}
   */
  public boolean validateAction(GameAction action, ID playerID)
      throws RuleException, IllegalArgumentException {
    assert gameInProcess;

    cardsChanged = false;
    if(action instanceof CardAction) {
      final CardAction cardAction = (CardAction) action;
      if(cardAction.getCardActionType().equals(CardAction.CardActionType.ATTACK)) {
        validateAttack(playerID, (CardAction) action);
        cardsChanged = true;
      } else if(cardAction.getCardActionType().equals(CardAction.CardActionType.DEFENSE)) {
        validateDefense(playerID, (CardAction) action);
        cardsChanged = true;
      }
    } else if(action instanceof FinishAction) {
      validateFinish(action);
    } else throw new IllegalArgumentException("GameAction must be either " +
        "instance of "+CardAction.class.getName()+" or "+FinishAction.class.getName());

    return roundState.readyForNextRound();
  }

  private void validateFinish(GameAction action) throws RuleException {
    final FinishAction finishAction;

    if(action instanceof FinishAction) {
      finishAction = (FinishAction) action;
      if(FinishAction.FinishType.GO_TO_NEXT_ROUND.equals(finishAction.getFinishType())) {
        setPlayerNextRound(action.getExecutor().playerType, false);
      } else if(FinishAction.FinishType.TAKE_CARDS.equals(finishAction.getFinishType())) {
        setPlayerNextRound(action.getExecutor().playerType, true);
      }
    }
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

  private void setPlayerNextRound(PlayerConstants.PlayerType type, boolean takeCards)
      throws RuleException {
    if(PlayerConstants.PlayerType.DEFENDER.equals(type)) {
      if (!takeCards && !pairCardHolder.hasNoNullPairs())
        throw new RuleException(I18nSupport.getValue(BundleStrings.USER_MESSAGES, "cards.to.beat"));
      roundState.setDefenderNextRound(true, takeCards);
    } else if(PlayerConstants.PlayerType.FIRST_ATTACKER.equals(type) ||
        PlayerConstants.PlayerType.SECOND_ATTACKER.equals(type)) {
      roundState.setAttackerNextRound(type);
    }
  }

  private void prepareForNextRound() {
    /* Set player's cards */
    fillPlayerHand(ruleChecker.getFirstAttacker());
    fillPlayerHand(ruleChecker.getSecondAttacker());
    fillPlayerHand(ruleChecker.getDefender());
    updateFinishedPlayer(ruleChecker.getFirstAttacker());
    updateFinishedPlayer(ruleChecker.getSecondAttacker());
    updateFinishedPlayer(ruleChecker.getDefender());

    /* The rest */
    pairCardHolder.clear();
    roundState.newRound();
  }

  /**
   * Checks if a player finished and refreshes the process in case of finishing.
   * @param player Player to check.
   */
  private void updateFinishedPlayer(Player player) {
    if(player == null)
      return;

    if(player.getCards().size() == 0) {
      player.getLeftPlayer().setRightPlayer(player.getRightPlayer());
      player.getRightPlayer().setLeftPlayer(player.getLeftPlayer());
      player.setType(PlayerConstants.PlayerType.NOT_LOSER);
    }
  }

  /**
   * Fills a players hand cards.
   * @param player Player to give the hand cards to.
   */
  private void fillPlayerHand(Player player) {
    if(player == null)
      return;

    while ((stack.getStackSize() > 0) && (player.getCards().size() < 6)) {
      player.pickUpCard(stack.drawCard());
    }
  }

  /**
   * Sets a new player with the specified player id. Game must not be in process.
   * @param playerID Specified id.
   */
  public void setPlayer(ID playerID) {
    assert !gameInProcess;
    if(playerHolder.get(playerID) == null)
      initialiseNew = true;

    playerHolder.add(playerID, new Player());
  }

  /**
   * Returns whether a player with this id could be removed or not.
   * Game must not be in process.
   * @param playerID The player having this id should be removed.
   * @return True, the player existed and is now removed, else false.
   */
  public boolean removePlayer(ID playerID) {
    assert !gameInProcess;

    boolean result = playerHolder.remove(playerID);
    if(result)
      initialiseNew = true;

    return result;
  }

  public List<DTOCard> getPlayerCards(ID playerID) {
    return Converter.playerCardsToDTO(playerHolder.get(playerID));
  }

  /**
   * This method returns a boolean value that indicates if the process is ready to go to the next
   * round.
   * @return True, next round can be started, else false.
   */
  public boolean readyForNextRound() {
    return roundState.readyForNextRound();
  }

  /**
   * This method returns a boolean value that indicates if the attackers in this process are ready
   * to go to the next round.
   * @return True, all attackers are ready, else false.
   */
  public boolean attackersReady() {
    return roundState.attackersReadyForNextRound();
  }

  public boolean defenderTookCards() {
    return roundState.defenderTookCards();
  }

  public boolean gameHasFinished() {
    return currentLoser != null || determineLoser();
  }

  private boolean determineLoser() {
    for (Player player : playerHolder.getList()) {
      if(player.isAlone()) {
        currentLoser = player;
        return true;
      }
    }
    return false;
  }

  /**
   * Returns a value to indicate if the cards on the field have changed after the last action.
   * @return True, cards have changed, else false.
   */
  public boolean cardsHaveChanged() {
    return cardsChanged;
  }

  /* Getter and Setter */

  public boolean isGameInProcess() {
    return gameInProcess;
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

  public int getPlayerCount() {
    return playerHolder.size();
  }

  /* Inner Classes */

  private class RoundStateHandler {
    private boolean firstAttackerNextRound;
    private boolean secondAttackerNextRound;
    private boolean defenderNextRound;
    private boolean defenderTookCards;
    private boolean justTwo;

    private RoundStateHandler() {
      newRound();
      defenderTookCards = false;
      justTwo = false;
    }

    public void newRound() {
      firstAttackerNextRound = false;
      secondAttackerNextRound = justTwo;
      defenderNextRound = false;
    }

    private void setJustTwoPlayer(boolean justTwo) {
      this.justTwo = justTwo;
      secondAttackerNextRound = justTwo;
    }

    public boolean readyForNextRound() {
      return firstAttackerNextRound && secondAttackerNextRound && defenderNextRound;
    }

    public boolean attackersReadyForNextRound() {
      return firstAttackerNextRound && secondAttackerNextRound;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setFirstAttackerNextRound(boolean readyForNextRound) {
      firstAttackerNextRound = readyForNextRound;
    }

    public void setSecondAttackerNextRound(boolean readyForNextRound) {
      if(!justTwo)
        secondAttackerNextRound = readyForNextRound;
    }

    /**
     * If takesCards is true, all players are marked as ready for the next round.
     * @param readyForNextRound Marks the defender to be ready for the next round or not.
     * @param takesCards Marks that the defender takes the cards in this round and marks also
     *                   the defender for the next round.
     */
    public void setDefenderNextRound(boolean readyForNextRound, boolean takesCards) {
      defenderNextRound = readyForNextRound;
      if(takesCards) {
        firstAttackerNextRound = true;
        secondAttackerNextRound = true;
        defenderNextRound = true;
      }
      defenderTookCards = takesCards;
    }

    /**
     * Returns a boolean value that indicates if the last defender took the cards or not.
     * @return True, the last defender took the cards, else false.
     */
    public boolean defenderTookCards() {
      return defenderTookCards;
    }

    public void setAttackerNextRound(PlayerConstants.PlayerType type) {
      if(type.equals(PlayerConstants.PlayerType.FIRST_ATTACKER))
        firstAttackerNextRound = true;
      else if(type.equals(PlayerConstants.PlayerType.SECOND_ATTACKER))
        setSecondAttackerNextRound(true);
    }
  }
}

class ElementPairHolder<T> {
  private List<List<T>> elementPairs;

  ElementPairHolder() {
    elementPairs = new ArrayList<List<T>>();
  }

  @SuppressWarnings("UnusedDeclaration")
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

  @SuppressWarnings("UnusedDeclaration")
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

  public void clear() {
    elementPairs.clear();
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

  public void clear() {
    list.clear();
    map.clear();
  }
}
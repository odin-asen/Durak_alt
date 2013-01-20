package common.game.rules;

import common.game.GameCard;
import common.game.Player;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static common.utilities.constants.GameCardConstants.CardColour;
import static common.utilities.constants.GameCardConstants.CardValue;
import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 17:20
 *
 * This class implies the rules of the game and provides methods that clarify,
 * if a move can be done or not.
 */
public abstract class RuleChecker { //TODO RuleChecker ableiten für nur 2 Spieler, mehr als 2 Spieler, 2 gegen 2 Spieler
  private static final String BUNDLE_NAME = "user.messages"; //NON-NLS
  private static final Logger LOGGER = LoggingUtility.getLogger(RuleChecker.class.getName());

  private CardColour trumpColour;
  private Player firstAttacker;
  private Player secondAttacker;
  private Player defender;

  private Boolean initAttack;

  /* Constructors */

  public RuleChecker() {
    firstAttacker = new Player();
    firstAttacker.setType(PlayerType.FIRST_ATTACKER);
    secondAttacker = new Player();
    secondAttacker.setType(PlayerType.SECOND_ATTACKER);
    defender = new Player();
    defender.setType(PlayerType.DEFENDER);
    initAttack = true;
  }

  /* Methods */

  /**
   * Returns true or false whether the move can be done or not.
   * @param attacker Player, who wants to make the move.
   * @param attackerCard Card to attack with.
   * @param currentCards Cards already on the table.
   * @throws RuleException Throws this exception with the specified message, if the
   * move can't be done.
   */
  public void doAttackMove(Player attacker, List<GameCard> attackerCards,
                           List<GameCard> currentAttackCards, List<GameCard> currentDefenderCards)
      throws RuleException {
    final StringBuilder nonExistingCards = new StringBuilder();

    checkAuthentication(attacker);
    checkAttack(attacker, attackerCards, currentAttackCards);

    final List<GameCard> allCards = new ArrayList<GameCard>();
    Miscellaneous.addAllToCollection(allCards, currentAttackCards);
    Miscellaneous.addAllToCollection(allCards, currentDefenderCards);

    if(!allCardsExist(attackerCards, allCards, nonExistingCards))
      throw new RuleException(getCardsNotOnGamePanelMessage(nonExistingCards.toString()));

    for (GameCard card : attackerCards) {
      attacker.useCard(card);
    }
    initAttack = false;
  }

  /**
   * Returns true or false whether the defense move can be done or not.
   * @param defenderCard Card to defend with.
   * @param attackerCard Card that will be defended.
   * @throws RuleException Throws this exception with the specified message, if the
   * move can't be done.
   */
  public void doDefenseMove(Player defender, Boolean attackerCardsEmpty,
                            GameCard defenderCard, GameCard attackerCard)
      throws RuleException {
    final String notHigherText = I18nSupport.getValue(BUNDLE_NAME, "value.0.lower.than.1",
        defenderCard.getCardValue().getValueName(), attackerCard.getCardValue().getValueName());
    final String noTrumpText = I18nSupport.getValue(BUNDLE_NAME, "card.0.is.not.trump",
        defenderCard.getColourAndValue());
    checkAuthentication(defender);
    if(attackerCardsEmpty)
      throw new RuleException(I18nSupport.getValue(BUNDLE_NAME, "no.cards.to.defend"));

    checkDefense(defenderCard, attackerCard, notHigherText, noTrumpText);

    defender.useCard(defenderCard);
  }

  private String getCardsNotOnGamePanelMessage(String nonExistingCards) {
    return I18nSupport.getValue(BUNDLE_NAME, "move.not.valid.cards.0.not.on.field",
        nonExistingCards);
  }

  private Boolean allCardsExist(List<GameCard> attackerCards, List<GameCard> currentCards,
                                StringBuilder nonExistingCards) {
    Boolean allCardsExist = true;

    if(currentCards.isEmpty())
      return true;

    for (GameCard attackerCard : attackerCards) {
      if(!cardValueExists(attackerCard.getCardValue(), currentCards)) {
        nonExistingCards.append("<p/>").append(attackerCard.getColourAndValue()).append(',');
        allCardsExist = false;
      }
    }

    if(nonExistingCards.length() != 0)
      nonExistingCards.deleteCharAt(nonExistingCards.length() - 1);

    return allCardsExist;
  }

  private Boolean cardValueExists(CardValue value, List<GameCard> cards) {
    if(value == null)
      return false;

    boolean exists = false;
    for (GameCard card : cards) {
      if(card != null)
        exists = exists || value.equals(card.getCardValue());
    }

    return exists;
  }

  private void checkAttack(Player wantsToAttack, List<GameCard> attackCards,
                           List<GameCard> currentAttackCards)
    throws RuleException {
    final int newAttackCardsCount = currentAttackCards.size() + attackCards.size();
    if(wantsToAttack.equals(secondAttacker) && currentAttackCards.isEmpty())
      throw new RuleException(I18nSupport.getValue(BUNDLE_NAME,"second.attacker.not.allowed"));
    else if(initAttack && (newAttackCardsCount > 5))
      throw new RuleException(I18nSupport.getValue(BUNDLE_NAME,"first.attack.five.card.restriction"));
    else if(newAttackCardsCount > 6)
      throw new RuleException(I18nSupport.getValue(BUNDLE_NAME,"not.more.than.six.cards"));
    else if(attackCards.size() > defender.getCards().size())
      throw new RuleException(I18nSupport.getValue(BUNDLE_NAME, "defender.too.few.cards"));
    else if(currentAttackCards.isEmpty()) {
      final CardValue currentValue = attackCards.get(0).getCardValue();
      for (GameCard attackCard : attackCards) {
        if (!currentValue.equals(attackCard.getCardValue()))
          throw new RuleException(I18nSupport.getValue(BUNDLE_NAME, "cards.not.same.value"));
      }
    }
  }

  private void checkAuthentication(Player player) throws RuleException {
    if(!player.equals(firstAttacker) && !player.equals(secondAttacker) && !player.equals(defender))
      throw new RuleException(I18nSupport.getValue(BUNDLE_NAME,"only.attacker.and.defender.allowed"));
  }

  private void checkDefense(GameCard defenderCard, GameCard attackerCard,
                            String notHigherText, String noTrumpText) throws RuleException {
    if(defenderCard.getCardColour().equals(trumpColour)) {
      if (attackerCard.getCardColour().equals(trumpColour) && defenderCard.getCardValue().compareTo(attackerCard.getCardValue()) <= 0)
        throw new RuleException(notHigherText);
    } else {
      if(attackerCard.getCardColour().equals(trumpColour))
        throw new RuleException(noTrumpText);
      else {
        if(defenderCard.getCardColour().equals(attackerCard.getCardColour())) {
          if(defenderCard.getCardValue().compareTo(attackerCard.getCardValue()) <= 0)
            throw new RuleException(notHigherText);
        } else {
          throw new RuleException(I18nSupport.getValue(BUNDLE_NAME,
              "card.0.neither.trump.nor.colour.1",defenderCard.getColourAndValue(),
              attackerCard.getCardColour().getName()));
        }
      }
    }
  }

  /**
   * Returns the player with the smallest trump on the hand.
   *
   * @param players List of the players.
   * @return Player that begins.
   */
  public Player initStartPlayer(List<Player> players) {
    GameCard currentSmallestCard = null;
    Player starter = null;
    initAttack = true;

    for (Player player : players) {
      final GameCard smallestColour = player.getSmallestValue(trumpColour);
      if(smallestColour != null)
        if(smallestColour.hasLowerValue(currentSmallestCard)) {
          currentSmallestCard = smallestColour;
          starter = player;
        }
    }

    if(starter == null) {
      LOGGER.info("No player has a trump!");
      starter = players.get(0);
    }

    setActivePlayers(starter);

    return starter;
  }

  /**
   * Sets the first attacker in the round and therefore the defender and
   * the second attacker. If {@code nextPlayer}s player type is
   * {@link utilities.constants.PlayerConstants.PlayerType#NOT_LOSER}
   * the next player who has not this type will be chosen as first attacker.
   * The method returns the player who became the first attacker.
   * @param nextPlayer The player who should be the first attacker
   * @return Returns the player who became the first attacker. If no
   * attacker could be found, null will be returned.
   */
  public Player setActivePlayers(Player nextPlayer) {
    final Player firstAttacker = determineFirstAttacker(nextPlayer);

    if(firstAttacker != null) {
      final Player defender = firstAttacker.getLeftPlayer();
      final Player secondAttacker = defender.getLeftPlayer();

      setFirstAttacker(firstAttacker);
      if(!secondAttacker.equals(firstAttacker))
        setSecondAttacker(secondAttacker);
      else setSecondAttacker(null);

      setDefender(defender);
    }

    return firstAttacker;
  }

  private Player determineFirstAttacker(Player player) {
    Player firstAttacker = player;
    if(player.isAlone())
      return null;

    while (firstAttacker.getType().equals(PlayerType.NOT_LOSER)) {
      firstAttacker = firstAttacker.getLeftPlayer();
      if(player.equals(firstAttacker))
        return null;
    }
    return firstAttacker;
  }

  /* Getter and Setter */

  public Player getFirstAttacker() {
    return firstAttacker;
  }

  private void setFirstAttacker(Player attacker) {
    firstAttacker = attacker;
    if(firstAttacker != null) {
      firstAttacker.setType(PlayerType.FIRST_ATTACKER);
      if(firstAttacker.equals(defender))
        defender = null;
    }
  }

  public Player getSecondAttacker() {
    return secondAttacker;
  }

  private void setSecondAttacker(Player attacker) {
    secondAttacker = attacker;
    if(secondAttacker != null) {
      secondAttacker.setType(PlayerType.SECOND_ATTACKER);
      if(secondAttacker.equals(defender))
        defender = null;
    }
  }

  public Player getDefender() {
    return defender;
  }

  private void setDefender(Player defender) {
    this.defender = defender;
    if (defender != null) {
      this.defender.setType(PlayerType.DEFENDER);
      if(defender.equals(firstAttacker))
        firstAttacker = null;
      if(defender.equals(secondAttacker))
        secondAttacker = null;
    }
  }

  public CardColour getTrumpColour() {
    return trumpColour;
  }

  public void setTrumpColour(CardColour trumpColour) {
    this.trumpColour = trumpColour;
  }

  /* Inner Classes */
}

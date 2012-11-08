package game.rules;

import game.GameCard;
import game.Player;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static utilities.constants.GameCardConstants.CardColour;
import static utilities.constants.GameCardConstants.CardValue;
import static utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 17:20
 *
 * This class implies the rules of the game and provides methods that clarify,
 * if a move can be done or not.
 */
public abstract class RuleChecker {
  private static Logger LOGGER = Logger.getLogger(RuleChecker.class.getName());
  public static final String RULE_MESSAGE_START_ATTACK_DIFFERENT_VALUES =
      "Da nicht alle Karten den gleichen Wert haben," +
      "\nkönnen diese Karten auch nicht für den ersten Angriff gelegt werden!";
  public static final String RULE_MESSAGE_ALREADY_6_CARDS =
      "Es liegen schon 6 Angriffskarten auf dem Spielfeld!";
  public static final String RULE_MESSSAGE_FIRST_ATTACK_ONLY_5_CARDS =
      "Im ersten Angriff darf nur mit maximal 5 Karten angegriffen werden!";
  public static final String RULE_MESSAGE_NO_DEFAULT_ALLOWED =
      "Nur Angreifer und Verteidiger dürfen was legen!";
  public static final String RULE_MESSAGE_START_ATTACK_SECOND_PLAYER =
      "Der zweite Angreifer darf nicht zuerst eine Angriffskarte spielen!";

  private CardColour trumpColour;
  private Player firstAttacker;
  private Player secondAttacker;
  private Player defender;

  private Boolean initAttack;

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
  public void canDoAttackMove(Player attacker, List<GameCard> attackerCards, List<GameCard> currentCards)
      throws RuleException {
    final StringBuilder nonExistingCards = new StringBuilder();

    checkAuthentication(attacker);
    checkAttack(attacker, attackerCards, currentCards);

    if(!allCardsExist(attackerCards, currentCards, nonExistingCards))
      throw new RuleException(getCardsNotOnGamePanelMessage(nonExistingCards.toString()));
  }

  private String getCardsNotOnGamePanelMessage(String nonExistingCards) {
    return "Der Zug kann nicht gemacht werden, " +
        "weil die Werte der Karten "+nonExistingCards+
        "\nnicht auf dem Spielfeld liegen!";
  }

  private Boolean allCardsExist(List<GameCard> attackerCards, List<GameCard> currentCards,
                                StringBuilder nonExistingCards) {
    Boolean allCardsExist = true;

    if(currentCards.isEmpty())
      return true;

    for (GameCard attackerCard : attackerCards) {
      if(!cardValueExists(attackerCard.getCardValue(), currentCards)) {
        nonExistingCards.append('\n').append(attackerCard.getColourAndValue()).append(',');
        allCardsExist = false;
      }
    }

    if(nonExistingCards.length() != 0)
      nonExistingCards.deleteCharAt(nonExistingCards.length() - 1);

    return allCardsExist;
  }

  private Boolean cardValueExists(CardValue value, List<GameCard> cards) {
    boolean exists = false;
    for (GameCard card : cards) {
      exists = exists || value.equals(card.getCardValue());
    }

    return exists;
  }

  private void checkAttack(Player wantsToAttack, List<GameCard> attackCards,
                           List<GameCard> currentCards)
    throws RuleException {
    if(wantsToAttack.equals(secondAttacker) && currentCards.isEmpty())
      throw new RuleException(RULE_MESSAGE_START_ATTACK_SECOND_PLAYER);
    else if(initAttack && (currentCards.size() == 5))
      throw new RuleException(RULE_MESSSAGE_FIRST_ATTACK_ONLY_5_CARDS);
    else if(currentCards.size() == 6)
      throw new RuleException(RULE_MESSAGE_ALREADY_6_CARDS);
    else if(currentCards.isEmpty()) {
      final CardValue currentValue = attackCards.get(0).getCardValue();
      for (GameCard attackCard : attackCards) {
        if (!currentValue.equals(attackCard.getCardValue()))
          throw new RuleException(RULE_MESSAGE_START_ATTACK_DIFFERENT_VALUES);
      }
    }
  }

  private void checkAuthentication(Player player) throws RuleException {
    if(!player.equals(firstAttacker) && !player.equals(secondAttacker) && !player.equals(defender))
      throw new RuleException(RULE_MESSAGE_NO_DEFAULT_ALLOWED);
  }

  /**
   * Returns true or false whether the defense move can be done or not.
   * @param defenderCard Card to defend with.
   * @param attackerCard Card that will be defended.
   * @throws RuleException Throws this exception with the specified message, if the
   * move can't be done.
   */
  public void canDoDefendMove(Player defender, Boolean attackerCardsEmpty,
                              GameCard defenderCard, GameCard attackerCard)
    throws RuleException {
    final String notHigherText = "Der Kartenwert "+defenderCard.getCardValue().getValueName()+
        " ist vielleicht in einem anderen Universum höher als "+
        attackerCard.getCardValue().getValue();
    final String noTrumpText = "Die Verteidigerkarte "+defenderCard.getColourAndValue()+
        " ist kein Trumpf!";

    checkAuthentication(defender);
    if(attackerCardsEmpty)
      throw new RuleException("Es liegen noch keine Karten auf dem Feld zum Verteidigen!");

    checkDefense(defenderCard, attackerCard, notHigherText, noTrumpText);
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
          throw new RuleException("Die Karte "+defenderCard.getColourAndValue()+
              " ist weder Trumpf noch hat sie die Farbe "+attackerCard.getCardColour().getName());
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

    for (Player player : players) {
      final GameCard smallestColour = player.getSmallestValue(trumpColour);
      if(smallestColour != null)
        if(smallestColour.hasLowerValue(currentSmallestCard)) {
          currentSmallestCard = smallestColour;
          starter = player;
        }
    }

    if(starter == null) {
      LOGGER.log(Level.INFO, "No player has a trump!");
      starter = players.get(0);
    }

    setActivePlayers(starter);

    return starter;
  }

  /**
   * Sets the first attacker in the round and therefore the defender and
   * the second attacker.
   * @param firstAttacker The player who is the first attacker
   */
  public void setActivePlayers(Player firstAttacker) {
    Player defender = firstAttacker.getLeftPlayer();
    Player secondAttacker = defender.getLeftPlayer();

    setFirstAttacker(firstAttacker);
    if(!secondAttacker.equals(firstAttacker))
      setSecondAttacker(secondAttacker);
    setDefender(defender);
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
}

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
    Boolean allCardsExist = true;
    StringBuilder nonExistingCards = new StringBuilder();

    checkAuthentication(attacker);
    checkAttack(attacker, currentCards);

    for (GameCard attackerCard : attackerCards) {
      if(!cardValueExists(attackerCard.getCardValue(), currentCards)) {
        nonExistingCards.append('\n').append(attackerCard.getColourAndValue()).append(',');
        allCardsExist = false;
      }
    }

    if(nonExistingCards.length() != 0)
      nonExistingCards.deleteCharAt(nonExistingCards.length() - 1);

    if(!allCardsExist)
      throw new RuleException("Der Zug kann nicht gemacht werden, " +
          "weil die Werte der Karten "+nonExistingCards.toString()+
          " nicht auf dem Spielfeld liegen!");
  }

  private Boolean cardValueExists(CardValue value, List<GameCard> cards) {
    return cards.isEmpty() || value.equals(cards.get(cards.size() - 1).getCardValue()) || cardValueExists(value, cards.subList(0, cards.size() - 2));
  }

  private void checkAttack(Player wantsToAttack, List<GameCard> cards)
    throws RuleException {
    if(wantsToAttack.equals(secondAttacker) && cards.isEmpty())
      throw new RuleException("Der zweite Angreifer darf nicht zuerst eine " +
          "Angriffskarte spielen!");
    else if(initAttack && (cards.size() == 5))
      throw new RuleException("Im ersten Angriff darf nur mit maximal 5 Karten " +
          "angegriffen werden!");
    else if(cards.size() == 6)
      throw new RuleException("Es liegen schon 6 Angriffskarten auf dem Spielfeld!");
  }

  private void checkAuthentication(Player wantsToAttack) throws RuleException {
    if(!wantsToAttack.equals(firstAttacker) && !wantsToAttack.equals(secondAttacker))
      throw new RuleException("Nur Angreifer und Verteidiger dürfen was legen!");
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
    checkAuthentication(defender);
    if(attackerCardsEmpty)
      throw new RuleException("Es liegen noch keine Karten auf dem Feld zum Verteidigen!");

    if(defenderCard.getCardColour().equals(attackerCard.getCardColour()) ||
       defenderCard.getCardColour().equals(trumpColour)) {
      if(defenderCard.getCardValue().compareTo(attackerCard.getCardValue()) <= 0)
        throw new RuleException("Der Kartenwert "+defenderCard.getCardValue().getValue()+
          " ist vielleicht in einem anderen Universum höher als "+
            attackerCard.getCardValue().getValue());
    } else throw new RuleException("Die Karte "+defenderCard.getColourAndValue()+
      " ist weder Trumpf noch hat er die Farbe "+attackerCard.getCardColour().getName());
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
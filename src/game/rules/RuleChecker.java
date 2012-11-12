package game.rules;

import game.GameCard;
import game.Player;
import utilities.Miscellaneous;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static game.rules.RuleMessages.*;
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
public abstract class RuleChecker { //TODO RuleChecker ableiten für nur 2 Spieler, mehr als 2 Spieler, 2 gegen 2 Spieler
  private static Logger LOGGER = Logger.getLogger(RuleChecker.class.getName());

  private CardColour trumpColour;
  private Player firstAttacker;
  private Player secondAttacker;
  private Player defender;

  private Boolean initAttack;
  private RoundStateHandler roundState;

  /* Constructors */
  public RuleChecker() {
    firstAttacker = new Player();
    firstAttacker.setType(PlayerType.FIRST_ATTACKER);
    secondAttacker = new Player();
    secondAttacker.setType(PlayerType.SECOND_ATTACKER);
    defender = new Player();
    defender.setType(PlayerType.DEFENDER);
    roundState = new RoundStateHandler();
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

    if(attacker.getType().equals(PlayerType.FIRST_ATTACKER))
      roundState.setFirstAttackerNextRound(false);
    else if(attacker.getType().equals(PlayerType.SECOND_ATTACKER))
      roundState.setSecondAttackerNextRound(false);

    for (GameCard card : attackerCards) {
      attacker.useCard(card);
    }
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
    final String notHigherText = "Der Kartenwert "+defenderCard.getCardValue().getValueName()+
        " ist vielleicht in einem anderen Universum höher als "+
        attackerCard.getCardValue().getValueName();
    final String noTrumpText = "Die Verteidigerkarte "+defenderCard.getColourAndValue()+
        " ist kein Trumpf!";
    checkAuthentication(defender);
    if(attackerCardsEmpty)
      throw new RuleException("Es liegen noch keine Karten auf dem Feld zum Verteidigen!");

    checkDefense(defenderCard, attackerCard, notHigherText, noTrumpText);
    defender.useCard(defenderCard);
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
    if(wantsToAttack.equals(secondAttacker) && currentAttackCards.isEmpty())
      throw new RuleException(RULE_MESSAGE_START_ATTACK_SECOND_PLAYER);
    else if(initAttack && (currentAttackCards.size() == 5))
      throw new RuleException(RULE_MESSSAGE_FIRST_ATTACK_ONLY_5_CARDS);
    else if(currentAttackCards.size() == 6)
      throw new RuleException(RULE_MESSAGE_ALREADY_6_CARDS);
    else if(currentAttackCards.isEmpty()) {
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
    roundState.setSecondAttackerNextRound(false);
    roundState.setFirstAttackerNextRound(false);

    setFirstAttacker(firstAttacker);
    if(!secondAttacker.equals(firstAttacker))
      setSecondAttacker(secondAttacker);
    else {
      setSecondAttacker(null);
      roundState.setSecondAttackerNextRound(true);
    }
    setDefender(defender);
  }

  public void setAttackerReadyNextRound(PlayerType type) {
    if(type.equals(PlayerType.FIRST_ATTACKER))
      roundState.setFirstAttackerNextRound(true);
    else if(type.equals(PlayerType.SECOND_ATTACKER))
      roundState.setSecondAttackerNextRound(true);
  }

  public Boolean readyForNextRound() {
    return roundState.readyForNextRound();
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
  private class RoundStateHandler {
    private Boolean firstAttackerNextRound;
    private Boolean secondAttackerNextRound;

    private RoundStateHandler() {
      initPlayerNextRound();
    }

    private void initPlayerNextRound() {
      firstAttackerNextRound = false;
      secondAttackerNextRound = false;
    }

    private Boolean readyForNextRound() {
      return firstAttackerNextRound && secondAttackerNextRound;
    }

    public void setFirstAttackerNextRound(Boolean readyForNextRound) {
      firstAttackerNextRound = readyForNextRound;
    }

    public void setSecondAttackerNextRound(Boolean readyForNextRound) {
      secondAttackerNextRound = readyForNextRound;
    }
  }
}

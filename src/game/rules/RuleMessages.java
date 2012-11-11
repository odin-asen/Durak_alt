package game.rules;

/**
 * User: Timm Herrmann
 * Date: 10.11.12
 * Time: 23:36
 */
public interface RuleMessages {
  String RULE_MESSAGE_START_ATTACK_DIFFERENT_VALUES =
      "Da nicht alle Karten den gleichen Wert haben," +
          "\nkönnen diese Karten auch nicht für den ersten Angriff gelegt werden!";
  String RULE_MESSAGE_ALREADY_6_CARDS =
      "Es liegen schon 6 Angriffskarten auf dem Spielfeld!";
  String RULE_MESSSAGE_FIRST_ATTACK_ONLY_5_CARDS =
      "Im ersten Angriff darf nur mit maximal 5 Karten angegriffen werden!";
  String RULE_MESSAGE_NO_DEFAULT_ALLOWED =
      "Nur Angreifer und Verteidiger dürfen was legen!";
  String RULE_MESSAGE_START_ATTACK_SECOND_PLAYER =
      "Der zweite Angreifer darf nicht zuerst eine Angriffskarte spielen!";
}

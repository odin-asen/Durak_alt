package game.rules;

/**
 * User: Timm Herrmann
 * Date: 10.11.12
 * Time: 23:36
 */
public interface RuleMessages {
  String RULE_MESSAGE_START_ATTACK_DIFFERENT_VALUES =
      "<html>Da nicht alle Karten den gleichen Wert haben," +
          "<p/>k\u00f6nnen diese Karten auch nicht f\u00fcr den ersten Angriff gelegt werden!</html>";
  String RULE_MESSAGE_ALREADY_6_CARDS =
      "Es d\u00fcrfen nicht mehr als 6 Angriffskarten auf dem Spieltisch liegen!";
  String RULE_MESSSAGE_FIRST_ATTACK_ONLY_5_CARDS =
      "Im ersten Angriff d\u00fcrfen nur maximal 5 Angriffskarten auf dem Spieltisch liegen!";
  String RULE_MESSAGE_NO_DEFAULT_ALLOWED =
      "Nur Angreifer und Verteidiger d\u00fcrfen was legen!";
  String RULE_MESSAGE_START_ATTACK_SECOND_PLAYER =
      "Der zweite Angreifer darf nicht zuerst eine Angriffskarte spielen!";
  String RULE_MESSAGE_DEFENDER_NOT_ENOUGH_CARDS =
      "<html>Der Verteidiger hat zu wenig Karten," +
          "<p/>um diese Anzahl an Karten zu verteidigen</html>";
}

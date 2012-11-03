package utilities.constants;

/**
 * User: Timm Herrmann
 * Date: 29.10.12
 * Time: 18:13
 */
public interface PlayerConstants {
  /* Player Type */
  public static enum PlayerType {
    DEFAULT(""),
    FIRST_ATTACKER("Erster Angreifer"),
    SECOND_ATTACKER("Zweiter Angreifer"),
    DEFENDER("Verteidiger");

    private String description;
    PlayerType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}

package common.utilities.constants;

import common.i18n.I18nSupport;

import static common.i18n.BundleStrings.GENERAL_PLAYER;

/**
 * User: Timm Herrmann
 * Date: 29.10.12
 * Time: 18:13
 */
public interface PlayerConstants {
  /* Player Type */
  public static enum PlayerType {
    DEFAULT(I18nSupport.getValue(GENERAL_PLAYER,"default")),
    FIRST_ATTACKER(I18nSupport.getValue(GENERAL_PLAYER,"first.attacker")),
    SECOND_ATTACKER(I18nSupport.getValue(GENERAL_PLAYER,"second.attacker")),
    DEFENDER(I18nSupport.getValue(GENERAL_PLAYER,"defender")),
    NOT_LOSER(I18nSupport.getValue(GENERAL_PLAYER,"not.loser")),
    LOSER(I18nSupport.getValue(GENERAL_PLAYER,"loser"));

    private final String description;
    PlayerType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}

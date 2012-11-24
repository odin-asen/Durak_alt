package common.utilities.constants;

import common.i18n.I18nSupport;

/**
 * User: Timm Herrmann
 * Date: 29.10.12
 * Time: 18:13
 */
public interface PlayerConstants {
  String BUNDLE_NAME = "general.player"; //NON-NLS
  
  /* Player Type */
  public static enum PlayerType {
    DEFAULT(""),
    FIRST_ATTACKER(I18nSupport.getValue(BUNDLE_NAME,"first.attacker")),
    SECOND_ATTACKER(I18nSupport.getValue(BUNDLE_NAME,"second.attacker")),
    DEFENDER(I18nSupport.getValue(BUNDLE_NAME,"defender")),
    NOT_LOSER(I18nSupport.getValue(BUNDLE_NAME,"not.loser")),
    LOSER(I18nSupport.getValue(BUNDLE_NAME,"loser"));

    private final String description;
    PlayerType(String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }
}

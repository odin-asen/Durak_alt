package client.data.xStreamModel;

/**
 * User: Timm Herrmann
 * Date: 21.01.13
 * Time: 20:04
 * <p/>
 * This class is for the XStream Model to write and read the settings. It contains all popup
 * concerning settings.
 */
public class PopupSettings {
  private static final double DEFAULT_DURATION = 3.0;

  private boolean enabled;

  private PopupSetting chat;
  private PopupSetting rule;
  private PopupSetting game;

  public PopupSettings() {
    enabled = true;
    chat = new PopupSetting(true, DEFAULT_DURATION);
    rule = new PopupSetting(true, DEFAULT_DURATION);
    game = new PopupSetting(true, DEFAULT_DURATION);
  }

  public PopupSetting getChat() {
    return chat;
  }

  public void setChat(PopupSetting chat) {
    this.chat = chat;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public PopupSetting getGame() {
    return game;
  }

  public void setGame(PopupSetting game) {
    this.game = game;
  }

  public PopupSetting getRule() {
    return rule;
  }

  public void setRule(PopupSetting rule) {
    this.rule = rule;
  }
}


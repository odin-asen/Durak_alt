package client.data.xStreamModel;

/**
 * User: Timm Herrmann
 * Date: 21.01.13
 * Time: 20:04
 * <p/>
 * This class is for the XStream Model to write and read the settings.
 */
public class SoundSettings {
  private boolean ruleException;

  public SoundSettings() {
    ruleException = false;
  }

  public boolean isRuleException() {
    return ruleException;
  }

  public void setRuleException(boolean ruleException) {
    this.ruleException = ruleException;
  }
}

package client.data.xStreamModel;

/**
 * User: Timm Herrmann
 * Date: 21.01.13
 * Time: 20:38
 * <p/>
 * This class is for the XStream Model to write and read the settings. It represents the settings
 * for one popup type, e.g. chat popups.
 */
public class PopupSetting {
  private boolean enabled;
  private double duration;

  public PopupSetting(boolean enabled, double duration) {
    this.enabled = enabled;
    this.duration = duration;
  }

  public double getDuration() {
    return duration;
  }

  public void setDuration(double duration) {
    this.duration = duration;
  }

  public boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}

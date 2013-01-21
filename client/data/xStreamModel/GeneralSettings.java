package client.data.xStreamModel;

import java.util.Locale;

/**
 * User: Timm Herrmann
 * Date: 21.01.13
 * Time: 20:05
 * <p/>
 * This class is for the XStream Model to write and read the settings.
 */
public class GeneralSettings {
  private Locale locale;

  public GeneralSettings() {
    locale = Locale.getDefault();
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }
}

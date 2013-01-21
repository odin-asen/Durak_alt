package client;

import client.data.GlobalSettings;
import client.gui.frame.ClientFrame;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.util.Locale;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:36
 */
public class StartClient {

  private static final String SETTINGS_FILE = "durakSettings.xml"; //NON-NLS

  public static void main(String[] args) {
    init();

    final ClientFrame mainFrame = ClientFrame.getInstance();
    mainFrame.init();
    mainFrame.setVisible(true);
  }

  private static void init() {
    /* init logging class */
    String loggingPath = System.getProperty("user.dir")
        +System.getProperty("file.separator")+"clientLog.txt"; //NON-NLS
    LoggingUtility.setFirstTimeLoggingFile(loggingPath);

    /* init look and feel */
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e){}

    /* init settings */
    final GlobalSettings settings = GlobalSettings.getInstance();
    settings.readGlobalSettings(SETTINGS_FILE);
    Locale.setDefault(settings.general.getLocale());
  }
}
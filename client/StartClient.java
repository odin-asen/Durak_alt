package client;

import client.data.GlobalSettings;
import client.gui.frame.ClientFrame;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:36
 */
public class StartClient {
  static {
    /* init logging class */
    LoggingUtility.setFirstTimeLoggingFile(System.getProperty("user.dir")
        +System.getProperty("file.separator")+"clientLog.txt"); //NON-NLS
  }

  private static final String SETTINGS_FILE = "durakSettings.xml"; //NON-NLS
  private static final Logger LOGGER = LoggingUtility.getLogger(StartClient.class.getName());

  public static void main(String[] args) {
    init();

    final ClientFrame mainFrame = ClientFrame.getInstance();
    mainFrame.init();
    mainFrame.setVisible(true);
  }

  private static void init() {
    /* init look and feel */
    try {
      LOGGER.info("Load Look And Feel...");
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      LOGGER.info("Look And Feel loaded");
    } catch(Exception e){}

    /* init settings */
    LOGGER.info("Load Settings...");
    final GlobalSettings settings = GlobalSettings.getInstance();
    try {
      settings.readGlobalSettings(SETTINGS_FILE);
      Locale.setDefault(settings.general.getLocale());
      LOGGER.info("Settings loaded");
    } catch (IOException e) {
      LOGGER.info("Couldn't load settings file: "+e.getMessage()+"\n\t-> Start with default");
    }
    try {
      settings.writeGlobalSettings(SETTINGS_FILE);
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
}
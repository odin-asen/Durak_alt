package client;

import client.gui.frame.ClientFrame;
import common.utilities.LoggingUtility;

import javax.swing.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:36
 */
public class StartClient {
  public static void main(String[] args) {
    loadLaF();
    String loggingPath = System.getProperty("user.dir")
        +System.getProperty("file.separator")+"clientLog.txt"; //NON-NLS
    LoggingUtility.setFirstTimeLoggingFile(loggingPath);
    final ClientFrame mainFrame = ClientFrame.getInstance();
    mainFrame.init();
    mainFrame.setVisible(true);
  }

  public static void loadLaF() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e){}
  }
}
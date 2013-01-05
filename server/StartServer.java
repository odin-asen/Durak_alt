package server;

import common.utilities.LoggingUtility;
import server.gui.ServerFrame;

import javax.swing.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:46
 */
public class StartServer {
  public static void main(String[] args) {
    loadLaF();
    LoggingUtility.setFirstTimeLoggingFile("serverLog.txt"); //NON-NLS
    final ServerFrame frame = new ServerFrame();
    frame.setVisible(true);
  }

  public static void loadLaF() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e){}
  }
}

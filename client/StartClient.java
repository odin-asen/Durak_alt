package client;

import client.gui.frame.ClientFrame;

import javax.swing.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:36
 */
public class StartClient {
  public static void main(String[] args) {
    loadLaF();
    final ClientFrame frame = new ClientFrame();
    frame.setVisible(true);
  }

  public static void loadLaF() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception e){}
  }
}
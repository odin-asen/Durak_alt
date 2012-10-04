package client;

import client.gui.ClientFrame;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:36
 */
public class StartClient {
  public static final String LOOK_AND_FEEL = "com.jtattoo.plaf.noire.NoireLookAndFeel";

  public static void main(String[] args) {
    loadLaF(new ClientFrame());
  }

  public static void loadLaF(Component comp)
  {
    try {
      UIManager.setLookAndFeel(LOOK_AND_FEEL);
      SwingUtilities.updateComponentTreeUI(comp);
    }
    catch(ClassNotFoundException e){e.printStackTrace();}
    catch(InstantiationException e){e.printStackTrace();}
    catch(IllegalAccessException e){e.printStackTrace();}
    catch(UnsupportedLookAndFeelException e){e.printStackTrace();}
  }
}

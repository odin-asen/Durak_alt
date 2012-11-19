package server;

import server.gui.ServerFrame;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:46
 */
public class StartServer {
  public static final String LOOK_AND_FEEL = "";

  public static void main(String[] args) {
    new ServerFrame();
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

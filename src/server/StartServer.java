package server;

import server.gui.ServerFrame;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:46
 */
public class StartServer {
//  public static final String LOOK_AND_FEEL = "com.jtattoo.plaf.noire.NoireLookAndFeel";
//
//  private static JFrame frame = new JFrame();

  public static void main(String[] args) {
//    loadLaF(frame);
//    frame.dispose();
//    loadLaF(new ServerFrame());
    new ServerFrame();
  }

//  public static void loadLaF(Component comp)
//  {
//    try {
//      UIManager.setLookAndFeel(LOOK_AND_FEEL);
//      SwingUtilities.updateComponentTreeUI(comp);
//    }
//    catch(ClassNotFoundException e){e.printStackTrace();}
//    catch(InstantiationException e){e.printStackTrace();}
//    catch(IllegalAccessException e){e.printStackTrace();}
//    catch(UnsupportedLookAndFeelException e){e.printStackTrace();}
//  }
}

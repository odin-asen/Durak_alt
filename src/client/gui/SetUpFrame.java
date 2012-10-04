package client.gui;

import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 30.09.12
 * Time: 21:13
 */
public class SetUpFrame extends JFrame {
  public static final float SCREEN_SIZE_FENSTER_HOEHE = 0.7f;
  public static final float SCREEN_SIZE_FENSTER_BREITE = 0.3f;

  private static SetUpFrame setUpFrame;

  private SetUpFrame() {
    final FensterPositionen positionen = FensterPositionen.createFensterPositionen(
        SCREEN_SIZE_FENSTER_BREITE, SCREEN_SIZE_FENSTER_HOEHE);
    this.setBounds(positionen.getRectangle());
  }

  public static synchronized SetUpFrame getInstance() {
    if(setUpFrame == null) {
      setUpFrame = new SetUpFrame();
    }

    return setUpFrame;
  }
}

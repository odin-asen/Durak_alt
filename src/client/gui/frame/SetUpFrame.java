package client.gui.frame;

import utilities.gui.FensterPositionen;

import javax.swing.*;

/**
 * User: Timm Herrmann
 * Date: 30.09.12
 * Time: 21:13
 */
public class SetUpFrame extends JFrame {

  private static SetUpFrame setUpFrame;

  private SetUpFrame() {
    final FensterPositionen positionen = FensterPositionen.createFensterPositionen(
        ClientGUIConstants.SET_UP_FRAME_SCREEN_SIZE_WIDTH, ClientGUIConstants.SET_UP_FRAME_SCREEN_SIZE_HEIGHT);
    this.setBounds(positionen.getRectangle());
  }

  public static SetUpFrame getInstance() {
    if(setUpFrame == null) {
      setUpFrame = new SetUpFrame();
    }

    return setUpFrame;
  }
}

package client.gui.frame;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 12.11.12
 * Time: 02:24
 */
public class ClientFramePopup extends JWindow {
  private JLabel message;
  private JPanel panel;

  /* Constructors */
  public ClientFramePopup() {
    panel = new JPanel();
    message = new JLabel();
    panel.setBorder(createPopupBorder());
    panel.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    panel.add(message);
    add(panel);
    setSize(panel.getPreferredSize());
  }

  private Border createPopupBorder() {
    Border line = BorderFactory.createLineBorder(Color.BLACK);
    Border compound = BorderFactory.createCompoundBorder(
        BorderFactory.createBevelBorder(BevelBorder.RAISED),
        BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    return BorderFactory.createCompoundBorder(line, compound);
  }

  /* Methods */
  /* Getter and Setter */
  public void setText(String text) {
    message.setText(text);
  }

  public Dimension getPrefferedSize() {
    return panel.getPreferredSize();
  }
}

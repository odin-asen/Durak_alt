package client.gui.widget.card;

import client.gui.frame.gamePanel.GamePanel;

import java.awt.event.MouseEvent;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 */
public class DefaultCardMoveListener extends CardMoveListener {
  /* Constructors */
  protected DefaultCardMoveListener(GamePanel parent) {
    super(parent);
  }

  /* Methods */
  public void mousePressed(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    widget.setLastLocation(widget.getLocation());
    widget.setLastZOrderIndex(parent.getComponentZOrder(widget));

    super.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    widget.setLocation(widget.getLastLocation());
    parent.setComponentZOrder(widget, widget.getLastZOrderIndex());

    super.mouseReleased(e);
  }

  /* Getter and Setter */
}

package client.gui.widget.card;

import javax.swing.*;
import java.awt.event.MouseEvent;

/**
 * User: Timm Herrmann
 * Date: 05.11.12
 * Time: 00:52
 */
public class DefaultCardMoveListener extends CardMoveListener {
  /* Constructors */
  protected DefaultCardMoveListener() {
    super();
  }

  /* Methods */
  public void mousePressed(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    final JComponent parent = (JComponent) widget.getParent();
    final int lastZOrder = (parent != null) ? parent.getComponentZOrder(widget) : 0;
    widget.setLastLocation(widget.getLocation());
    widget.setLastZOrderIndex(lastZOrder);

    super.mousePressed(e);
  }

  public void mouseReleased(MouseEvent e) {
    final GameCardWidget widget = (GameCardWidget) e.getComponent();
    widget.setLocation(widget.getLastLocation());
    if (widget.getParent() != null)
      widget.getParent().setComponentZOrder(widget, widget.getLastZOrderIndex());

    super.mouseReleased(e);
  }

  /* Getter and Setter */
}

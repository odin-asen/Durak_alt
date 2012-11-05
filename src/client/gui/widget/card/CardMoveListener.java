package client.gui.widget.card;

import client.gui.frame.gamePanel.CombatCardPanel;
import client.gui.frame.gamePanel.GamePanel;
import utilities.gui.Compute;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 06.10.12
 * Time: 02:14
 */
public abstract class CardMoveListener
    implements ComponentListener, MouseListener, MouseMotionListener {
  protected GamePanel parent;

  private Point oldPoint;
  private Point grabbingPoint;

  /* Constructor */
  public CardMoveListener(GamePanel parent) {
    this.parent = parent;
    oldPoint = new Point();
    grabbingPoint = null;
  }

  public static CardMoveListener getDefenderInstance(GamePanel parent,
                                                     List<CombatCardPanel> combatPanels) {
    return new DefenseCardMoveListener(parent, combatPanels);
  }

  public static CardMoveListener getAttackerInstance(GamePanel parent) {
    return new AttackCardMoveListener(parent);
  }

  public static CardMoveListener getDefaultInstance(GamePanel parent) {
    return new DefaultCardMoveListener(parent);
  }

  /* Methods */
  public final void mouseDragged(MouseEvent e) {
    final GameCardWidget cardWidget = (GameCardWidget) e.getSource();
    parent.setComponentZOrder(cardWidget,0);
    Point point = e.getLocationOnScreen();

    /* Draw the widget at the correct position */
    if(Compute.componentContainsPoint(point, parent)) {
      cardWidget.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      cardWidget.setLocation(((point.x - oldPoint.x)+cardWidget.getLocation().x),
          (point.y - oldPoint.y)+cardWidget.getLocation().y);
      oldPoint.setLocation(cardWidget.getLocationOnScreen().getX()+ grabbingPoint.getX(),
          cardWidget.getLocationOnScreen().getY()+ grabbingPoint.getY());
    }
  }

  public void mouseMoved(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {
    parent.setComponentZOrder(e.getComponent(),0);
  }

  public void mousePressed(MouseEvent e) {
    GameCardWidget widget = (GameCardWidget) e.getComponent();
    widget.setLastLocation(widget.getLocation());
    widget.setLastZOrderIndex(parent.getComponentZOrder(widget));

    grabbingPoint = e.getPoint();
    if(!e.getComponent().getCursor().equals(Cursor.getDefaultCursor()))
      e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
  }

  /**
   * Should be called at the end of the extending subclasses method. So it is
   * guaranteed that the cards can be properly be moved.
   * @param e The given mouse event.
   */
  public void mouseReleased(MouseEvent e) {
    grabbingPoint = null;
    if(!e.getComponent().getCursor().equals(Cursor.getDefaultCursor()))
      e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public void mouseEntered(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public void mouseExited(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getDefaultCursor());
  }

  public void componentResized(ComponentEvent e) {}

  public void componentMoved(ComponentEvent e) {}

  public void componentShown(ComponentEvent e) {}

  public void componentHidden(ComponentEvent e) {}

  /* Getter and Setter */
  protected Point getGrabbingPoint() {
    if(grabbingPoint != null)
      return new Point(grabbingPoint);
    else return null;
  }
}

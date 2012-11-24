package client.gui.widget.card;

import client.gui.frame.gamePanel.CombatCardPanel;
import client.gui.frame.gamePanel.GamePanel;
import common.utilities.gui.Compute;

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
  protected Boolean dragged;

  private Point oldPoint;
  private Point grabbingPoint;

  /* Constructor */
  public CardMoveListener(GamePanel parent) {
    this.parent = parent;
    this.dragged = false;
    oldPoint = new Point();
    grabbingPoint = null;
  }

  public static CardMoveListener getDefenderInstance(GamePanel parent,
                                                     List<CombatCardPanel> combatPanels) {
    return new DefenseCardMoveListener(parent, combatPanels);
  }

  public static CardMoveListener getAttackerInstance(GamePanel parent, Float relativePointHeight) {
    return new AttackCardMoveListener(parent, relativePointHeight);
  }

  public static CardMoveListener getDefaultInstance(GamePanel parent) {
    return new DefaultCardMoveListener(parent);
  }

  /* Methods */

  /**
   * Supports the moving of a widget. Should be called in the first line if
   * the widget movement should be enabled.
   * @param e The given mouse event.
   */
  public void mouseDragged(MouseEvent e) {
    final GameCardWidget cardWidget = (GameCardWidget) e.getSource();
    parent.setComponentZOrder(cardWidget,0);
    Point point = e.getLocationOnScreen();

    /* Draw the widget at the correct position */
    moveWidget(cardWidget, point);
  }

  /**
   * Moves the specified widget to the position of the point {@code dragPointScreen} if
   * the point is in the area of {@link CardMoveListener#parent}.
   * @param cardWidget Specified widget, should be a widget of {@link CardMoveListener#parent}.
   * @param dragPointScreen Position to move to.
   */
  protected void moveWidget(GameCardWidget cardWidget, Point dragPointScreen) {
    if(Compute.componentContainsPoint(dragPointScreen, parent)) {
      cardWidget.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
      cardWidget.setLocation(((dragPointScreen.x - oldPoint.x)+cardWidget.getLocation().x),
          (dragPointScreen.y - oldPoint.y)+cardWidget.getLocation().y);
      oldPoint.setLocation(cardWidget.getLocationOnScreen().getX()+ grabbingPoint.getX(),
          cardWidget.getLocationOnScreen().getY()+ grabbingPoint.getY());
      dragged = true;
    }
  }

  public void mouseMoved(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}

  /**
   * Should be called at the end of the extending subclasses method. So it is
   * guaranteed that the cards can properly be moved.
   * @param e The given mouse event.
   */
  public void mousePressed(MouseEvent e) {
    grabbingPoint = e.getPoint();
    if(!e.getComponent().getCursor().equals(Cursor.getDefaultCursor()))
      e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
  }

  /**
   * Should be called at the end of the extending subclasses method. So it is
   * guaranteed that the cards can properly be moved.
   * @param e The given mouse event.
   */
  public void mouseReleased(MouseEvent e) {
    dragged = false;
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

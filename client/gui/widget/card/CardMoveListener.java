package client.gui.widget.card;

import client.gui.frame.gamePanel.CardContainer;
import client.gui.frame.gamePanel.CombatCardPanel;
import client.gui.frame.gamePanel.CurtainWidget;
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
  protected boolean dragged;

  private Point oldPoint;
  private Point grabbingPoint;

  /* Constructor */

  public CardMoveListener() {
    this.dragged = false;
    oldPoint = new Point();
    grabbingPoint = null;
  }

  /**
   * Creates a CardMoveListener for defending players.
   * @param cardPanels
   * @param cardContainer
   * @return
   */
  public static CardMoveListener getDefenderInstance(List<CombatCardPanel> cardPanels,
                                                    CardContainer<GameCardWidget> cardContainer) {
    return new DefenseCardMoveListener(cardPanels, cardContainer);
  }

  public static CardMoveListener getAttackerInstance(CardContainer<GameCardWidget> container,
                                            Rectangle attackArea, CurtainWidget curtainWidget,
                                            Float relativePointHeight) {
    return new AttackCardMoveListener(container, attackArea, curtainWidget, relativePointHeight);
  }

  public static CardMoveListener getDefaultInstance() {
    return new DefaultCardMoveListener();
  }

  /* Methods */

  /**
   * Supports the moving of a widget. Should be called in the first line if
   * the widget movement should be enabled.
   * @param e The given mouse event.
   */
  public void mouseDragged(MouseEvent e) {
    final GameCardWidget cardWidget = (GameCardWidget) e.getSource();
    if(cardWidget.getParent() != null)
      cardWidget.getParent().setComponentZOrder(cardWidget,0);
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
    if(cardWidget.getParent() != null &&
        Compute.componentContainsPoint(dragPointScreen, cardWidget.getParent())) {
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
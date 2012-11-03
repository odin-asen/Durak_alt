package client.gui.widget.card;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:25
 */
public class GameCardListener implements MouseListener, MouseMotionListener {
  private Point oldPoint;
  private Point grabbingPoint;

  private Double borderLeft;       //TODO als bewegungsbegrenzung implementieren
  private Double borderRight;
  private Double borderTop;
  private Double borderBottom;

  public GameCardListener() {
    oldPoint = new Point();
    grabbingPoint = new Point();
  }

  public void mouseDragged(MouseEvent e) {
    final GameCardWidget cardWidget = (GameCardWidget) e.getSource();
    cardWidget.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    cardWidget.getParent().setComponentZOrder(cardWidget,0);
    Point point = e.getLocationOnScreen();
    grabbingPoint = getCentredGrabingPoint(cardWidget);

    if(componentContainsPoint(point, cardWidget.getParent())) {
      cardWidget.setLocation(((point.x - oldPoint.x)+cardWidget.getLocation().x),
          (point.y - oldPoint.y)+cardWidget.getLocation().y);
      oldPoint.setLocation(cardWidget.getLocationOnScreen().getX()+ grabbingPoint.getX(),
          cardWidget.getLocationOnScreen().getY()+ grabbingPoint.getY());
      cardWidget.repaint();
    }
  }

  private Point getCentredGrabingPoint(Component comp) {
    return new Point(comp.getWidth()/2, comp.getHeight()/2);
  }

  private boolean componentContainsPoint(Point point, Container parent) {
    final Point screenLocation = parent.getLocationOnScreen();
    final Dimension dimension = parent.getSize();
    return ((screenLocation.x + dimension.width) >= point.x) &&
        ((screenLocation.x) <= point.x) &&
        ((screenLocation.y + dimension.height) >= point.y) &&
        ((screenLocation.y) <= point.y);
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
    e.getComponent().getParent().setComponentZOrder(e.getComponent(),0);
  }

  public void mousePressed(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
  }

  public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getDefaultCursor());
  }

  public void mouseEntered(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public void mouseExited(MouseEvent e) {
    e.getComponent().setCursor(Cursor.getDefaultCursor());
  }

  /**
   * These four values are percentages of the width and the height of the
   * listeners holders parent. E.g. if borderLeft is 0.1 and the holders parents
   * width is 100, the left border for the holder is 10 pixels. If borderRight
   * is 0.9 the holders right border is at 90 pixels.
   * The border values should bound the moving of the holder to a specified
   * area. The range for the values is between 0.0 and 1.0.
   * @param borderLeft Percentage of the left border.
   * @param borderRight Percentage of the right border.
   * @param borderTop Percentage of the upper border.
   * @param borderBottom Percentage of the lower border.
   */
  public void setMoveArea(Double borderLeft, Double borderRight, Double borderTop, Double borderBottom) {
    this.borderLeft = getBorderValues(borderLeft);
    this.borderRight = getBorderValues(borderRight);
    this.borderTop = getBorderValues(borderTop);
    this.borderBottom = getBorderValues(borderBottom);
  }

  private Double getBorderValues(Double d) {
    if(d < 0.0)
      return 0.0;
    else if(d > 1.0)
      return 1.0;
    else return d;
  }
}

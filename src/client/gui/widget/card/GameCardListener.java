package client.gui.widget.card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.peer.ComponentPeer;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:25
 */
public class GameCardListener implements MouseListener, MouseMotionListener {
  private Point oldPoint;
  private Point grabingPoint;

  public GameCardListener() {
    oldPoint = new Point();
    grabingPoint = new Point();
  }

  public void mouseDragged(MouseEvent e) {
    Component comp = (Component) e.getSource();
    comp.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    comp.getParent().setComponentZOrder(comp,0);
    Point point = e.getLocationOnScreen();
    grabingPoint = getCentredGrabingPoint(comp);

    if(componentContainsPoint(point, comp.getParent())) {
      comp.setLocation(((point.x - oldPoint.x)+comp.getLocation().x),
          (point.y - oldPoint.y)+comp.getLocation().y);
      oldPoint.setLocation(comp.getLocationOnScreen().getX()+grabingPoint.getX(),
          comp.getLocationOnScreen().getY()+grabingPoint.getY());
      comp.repaint();
    }
  }

  private Point getCentredGrabingPoint(Component comp) {
    return new Point(comp.getWidth()/2, comp.getHeight()/2);
  }

  private boolean componentContainsPoint(Point point, Container parent) {
    final Point screenLocation = parent.getLocationOnScreen();
    final Dimension dimension = parent.getSize();
    if(((screenLocation.x+dimension.width)>=point.x) &&
       ((screenLocation.x)<=point.x) &&
       ((screenLocation.y+dimension.height)>=point.y) &&
       ((screenLocation.y)<=point.y))
      return true;
    else
      return false;
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    //To change body of implemented methods use File | Settings | File Templates.
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
}
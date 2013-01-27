package common.utilities.gui;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 03.11.12
 * Time: 16:13
 */
public class Compute {
  /**
   * Returns the component of the specified {@code componentSet} that is touching the
   * specified {@code widget} and has the biggest area covered by {@code widget}.
   *
   * @param componentSet Component set that possibly touches the widget.
   * @param widget Widget that may cover components of the component set.
   * @return Returns the component of the component set that is most touched by the widget.
   */
  public static Component getMostTouchedComponent(List<? extends Component> componentSet,
                                                  Component widget) {
    Component resultWidget = null;
    List<? extends Component> touchedWidgets = getAllTouchedComponents(componentSet, widget);
    Rectangle currentBiggest = new Rectangle();
    Rectangle newBiggest;

    final Rectangle touchier = widget.getBounds();
    for (Component touchedWidget : touchedWidgets) {
      final Rectangle touched = touchedWidget.getBounds();
      newBiggest = SwingUtilities.computeIntersection(touchier.x, touchier.y,
          touchier.width, touchier.height, touched);
      if((currentBiggest.width*currentBiggest.height)<(newBiggest.width*newBiggest.height)) {
        currentBiggest = newBiggest;
        resultWidget = touchedWidget;
      }
    }

    return resultWidget;
  }

  private static List<? extends Component> getAllTouchedComponents(
      List<? extends Component> componentSet, Component widget) {
    List<Component> touchedWidgets = new ArrayList<Component>();
    final Rectangle toucher = widget.getBounds();

    for (Component component : componentSet) {
      final Rectangle touched = component.getBounds();
      if((component != widget) && (toucher.intersects(touched)))
        touchedWidgets.add(component);
    }
    return touchedWidgets;
  }

  public static boolean componentContainsPoint(Point componentLocation, Container parent) {
    final Rectangle parentBounds = new Rectangle(
        parent.getLocationOnScreen(), parent.getSize());
    return ((parentBounds.x + parentBounds.width) >= componentLocation.x) &&
        ((parentBounds.x) <= componentLocation.x) &&
        ((parentBounds.y + parentBounds.height) >= componentLocation.y) &&
        ((parentBounds.y) <= componentLocation.y);
  }

  public static ImageIcon getScaledImage(ImageIcon srcImg, int width, int height){
    if(srcImg == null) return null;
    if(width <= 0 && height <= 0) return srcImg;

    if(width <= 0)
      width = (int) (height*(float)srcImg.getIconWidth()/srcImg.getIconHeight());
    else if(height <= 0)
      height = (int) (width*(float)srcImg.getIconHeight()/srcImg.getIconWidth());

    BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = resizedImg.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.drawImage(srcImg.getImage(), 0, 0, width, height, null);
    g2.dispose();

    return new ImageIcon(resizedImg);
  }

  public static Rectangle getFramelessBounds(JFrame frame) {
    return new Rectangle(frame.getX()+frame.getWidth()-frame.getContentPane().getWidth(),
        frame.getY()+frame.getHeight()-frame.getContentPane().getHeight(),
        frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
  }
}

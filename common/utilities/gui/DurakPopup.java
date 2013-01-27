package common.utilities.gui;

import common.utilities.LoggingUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 12.11.12
 * Time: 02:24
 */
public class DurakPopup extends JWindow {
  public static final int LOCATION_UP_LEFT = 0;
  public static final int LOCATION_UP_RIGHT = 1;
  public static final int LOCATION_DOWN_LEFT = 2;
  public static final int LOCATION_DOWN_RIGHT = 3;
  public static final int LOCATION_CENTRE = 4;

  private JPanel panel;
  private PopupWindowListener listener;
  private int popupLocation;
  private Rectangle parentBounds;

  /* Constructors */
  public DurakPopup(Color backgroundColour, JComponent message, Rectangle parentBounds,
                    int popupLocation) {
    panel = new JPanel();
    this.popupLocation = popupLocation;
    this.parentBounds = parentBounds;
    listener = new PopupWindowListener();
    listener.setOpenSeconds(0);

    panel.setBorder(WidgetCreator.createPopupBorder());
    panel.setBackground(backgroundColour);
    panel.add(message);
    add(panel);

    addWindowListener(listener);
    addMouseListener(listener);
    setSize(getPreferredSize());
    setLocation(computeLocation());
    setAlwaysOnTop(true);
  }

  /* Methods */

  private Point computeLocation() {
    final int offset = 10;
    final Point centre = new Point(parentBounds.x + (parentBounds.width-getWidth())/2,
        parentBounds.y + (parentBounds.height-getHeight())/2);
    switch (popupLocation) {
      case LOCATION_DOWN_LEFT:
        return new Point(parentBounds.x + offset,
            parentBounds.y + parentBounds.height - getHeight() - offset);
      case LOCATION_DOWN_RIGHT:
        return new Point(parentBounds.x + parentBounds.width - getWidth() - offset,
            parentBounds.y + parentBounds.height - getHeight() - offset);
      case LOCATION_UP_LEFT:
        return new Point(parentBounds.x + offset, parentBounds.y + offset);
      case LOCATION_UP_RIGHT:
        return new Point(parentBounds.x + parentBounds.width - getWidth() - offset,
            parentBounds.y + offset);
      case LOCATION_CENTRE:
        return centre;
      default: return centre;
    }
  }

  /* Getter and Setter */

  public Dimension getPreferredSize() {
    return panel.getPreferredSize();
  }

  /**
   * Sets the duration of the popup until it fades out.
   * If seconds is 0.0 the popup is permanent and will only be closed with a mouse click.
   * @param seconds Opening duration of the popup.
   */
  public void setOpenSeconds(double seconds) {
    listener.setOpenSeconds(seconds);
  }

  @SuppressWarnings("ALL")
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setBounds(new Rectangle(300, 200, 500, 500));
    final Rectangle parentBounds = frame.getBounds();
    final Object[] vector = new Object[]{"Down","Left"};
    DurakPopup popup1 = new DurakPopup(new Color(246, 136,0), new JComboBox(vector),
        parentBounds, LOCATION_DOWN_LEFT);
    DurakPopup popup2 = new DurakPopup(new Color(13, 153,0), new JLabel("Down Right"),
        parentBounds, LOCATION_DOWN_RIGHT);
    DurakPopup popup3 = new DurakPopup(new Color(13, 153,0), new JLabel("Up Left"),
        parentBounds, LOCATION_UP_LEFT);
    DurakPopup popup4 = new DurakPopup(new Color(13, 153,0), new JLabel("Up Right"),
        parentBounds, LOCATION_UP_RIGHT);
    popup1.setOpenSeconds(0.0);
    popup2.setOpenSeconds(3);
    popup3.setOpenSeconds(3);
    popup4.setOpenSeconds(3);

    frame.setVisible(true);
    popup1.setVisible(true);
    popup2.setVisible(true);
    popup3.setVisible(true);
    popup4.setVisible(true);
  }
}

class PopupWindowListener extends WindowAdapter implements MouseListener {
  private static final Logger LOGGER =
    LoggingUtility.getLogger(PopupWindowListener.class.getName());
  private boolean fade;
  private double openSeconds;
  private boolean isFading;

  PopupWindowListener() {
    openSeconds = 0.0;
    isFading = false;
    fade = true;
  }

  private void closeSlow(Window window) {
    final float step = 0.03f;
    final long breakTime = 75L;

    isFading = true;
    for (float opacity = 1.0f; fade && opacity > 0.0f; opacity=opacity-step) {
      window.setOpacity(opacity);
      pause(breakTime);
    }
    if(fade) {
      window.setOpacity(0.0f);
      window.dispose();
    } else {
      window.setOpacity(1.0f);
      startTimer(window);
    }
    isFading = false;
  }

  private void pause(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      LOGGER.info("Error during thread pause: " + e.getMessage());
    }
  }

  private void startTimer(final Window window) {
    if(isFading)
      return;

    new Thread(new Runnable() {
      public void run() {
        final long breakTime = 500L;
        final double seconds = getOpenSeconds();
        if(seconds > 0.001) {
          int loopTurns = (int) (seconds*1000L/breakTime);
          while(fade && (loopTurns > 0)) {
            pause(breakTime);
            loopTurns--;
          }
          if(fade)
            closeSlow(window);
        }
      }
    }).start();
  }

  public void windowOpened(WindowEvent e) {

    startTimer(e.getWindow());
  }

  public void mouseClicked(MouseEvent e) {
    final Window window = (Window) e.getComponent();
    window.setVisible(false);
    window.dispose();
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
    fade = false;
  }

  public void mouseExited(MouseEvent e) {
    fade = true;
    startTimer((Window) e.getComponent());
  }

  public double getOpenSeconds() {
    return openSeconds;
  }

  public void setOpenSeconds(double seconds) {
    openSeconds = seconds;
  }
}

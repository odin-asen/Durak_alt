package common.utilities.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * User: Timm Herrmann
 * Date: 12.11.12
 * Time: 02:24
 */
public class DurakPopup extends JWindow {
  private JLabel message;
  private JPanel panel;
  private PopupWindowListener listener;

  /* Constructors */
  public DurakPopup(Color backgroundColour) {
    panel = new JPanel();
    message = new JLabel();
    panel.setBorder(WidgetCreator.createPopupBorder());
    panel.setBackground(backgroundColour);
    panel.add(message);
    add(panel);
    listener = new PopupWindowListener();
    listener.setOpenSeconds(0);
    addWindowListener(listener);
    addMouseListener(listener);
  }

  /* Methods */
  /* Getter and Setter */
  public void setText(String text) {
    message.setText(text);
  }

  public Dimension getPrefferedSize() {
    return panel.getPreferredSize();
  }

  public void setOpenSeconds(double seconds) {
    listener.setOpenSeconds(seconds);
  }

  public static void main(String[] args) {
    DurakPopup popup = new DurakPopup(new Color(13, 153,0));
    popup.setText("Test Text"); //NON-NLS
    Dimension dim = popup.getPrefferedSize();
    popup.setBounds(400,300,dim.width,dim.height);
    popup.setOpenSeconds(3);
    popup.setVisible(true);
  }
}

class PopupWindowListener extends WindowAdapter implements MouseListener {
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
    } catch (InterruptedException e) {}
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

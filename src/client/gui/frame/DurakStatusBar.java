package client.gui.frame;

import resources.ResourceGetter;
import resources.ResourceGetterException;

import javax.swing.*;
import javax.swing.text.DateFormatter;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 19:37
 */
public class DurakStatusBar extends JPanel implements Runnable {
  private static final Logger LOGGER = Logger.getLogger(DurakStatusBar.class.getName());

  private static final DateFormat format = new SimpleDateFormat("EEE d. MMM yyyy HH:mm:ss  ");
  private static final Calendar calendar = GregorianCalendar.getInstance(Locale.GERMANY);

  private JLabel statusLabel;
  private JLabel clockLabel;
  private boolean connected;
  private boolean running;

  /* Constructors */
  public DurakStatusBar() {
    statusLabel = new JLabel();
    clockLabel = new JLabel();
    clockLabel.setPreferredSize(new Dimension(190, 0));
    clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    format.setCalendar(calendar);

    running = true;

    this.setLayout(new BorderLayout());
    this.add(Box.createHorizontalStrut(5), BorderLayout.LINE_START);
    this.add(statusLabel, BorderLayout.CENTER);
    this.add(clockLabel, BorderLayout.LINE_END);
    this.setConnected(false, "");

    new Thread(this).start();
  }

  /* Methods */
  /**
   * This method changes the icon of the status label defendent on the boolean.
   * @param connected Sets the corresponding icon to the value
   * @param serverAddress Will be set to the tooltip as information
   */
  public void setConnected(boolean connected, String serverAddress) {
    try {
      if(connected) {
        statusLabel.setIcon(ResourceGetter.loadImage(ResourceGetter.STRING_IMAGE_CONNECTED,"Verbunden"));
        statusLabel.setToolTipText("Verbunden mit "+serverAddress);
      } else {
        statusLabel.setIcon(ResourceGetter.loadImage(ResourceGetter.STRING_IMAGE_DISCONNECTED,"Verbunden"));
        statusLabel.setToolTipText("Momentan besteht keine Verbindung zu einem Server");
      }
    } catch (ResourceGetterException ex) {
      LOGGER.log(Level.INFO, ex.getMessage());
    }
    this.connected = connected;
  }

  public void setText(String text) {
    statusLabel.setText(text);
  }

  private void setTime(long millisSince1970) {
    calendar.setTimeInMillis(millisSince1970);
    clockLabel.setText(format.format(calendar.getTime()));
  }

  public void run() {
    long millis = System.currentTimeMillis();
    final long waitingTime = 1000L;
    while(running) {
      try {
        this.setTime(millis);
        Thread.sleep(waitingTime);
        millis = millis + waitingTime;
      } catch (InterruptedException ex) {
        LOGGER.log(Level.INFO, "Error while thread pausing!");
      }
    }
  }

  /* Getter and Setter */
}

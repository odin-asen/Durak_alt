package client.gui.frame;

import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.constants.PlayerConstants;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 19:37
 */
public class DurakStatusBar extends JPanel implements Runnable {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MESSAGE_BUNDLE = "user.messages"; //NON-NLS
  private static final Logger LOGGER = Logger.getLogger(DurakStatusBar.class.getName());

  private static final DateFormat format = new SimpleDateFormat(I18nSupport.getValue(CLIENT_BUNDLE,"format.date"), Locale.getDefault());
  private static final Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
  private static final ImageIcon connectedIcon = ResourceGetter.getImage(ResourceList.IMAGE_STATUS_CONNECTED,
      I18nSupport.getValue(CLIENT_BUNDLE,"image.description.connected"));
  private static final ImageIcon disconnectedIcon = ResourceGetter.getImage(ResourceList.IMAGE_STATUS_DISCONNECTED,
      I18nSupport.getValue(CLIENT_BUNDLE,"image.description.disconnected"));

  private JPanel besideLabelPanel;
  private JLabel mainStatusLabel;
  private JLabel gameStatusLabel;
  private JLabel connectionLabel;
  private JLabel clockLabel;
  private Boolean running;

  /* Constructors */
  public DurakStatusBar() {
    mainStatusLabel = new JLabel();
    mainStatusLabel.setBorder(WidgetCreator.createStatusBorder());
    format.setCalendar(calendar);
    running = true;

    setLayout(new BorderLayout());
    add(mainStatusLabel, BorderLayout.CENTER);
    add(getBesideLabelPanel(), BorderLayout.LINE_END);
    setConnected(false, "");

    new Thread(this).start();
  }

  /* Methods */
  private JPanel getBesideLabelPanel() {
    if(besideLabelPanel != null)
      return besideLabelPanel;

    besideLabelPanel = new JPanel();
    clockLabel = new JLabel();
    gameStatusLabel = new JLabel();
    connectionLabel = new JLabel();

    final Border border = WidgetCreator.createStatusBorder();
    clockLabel.setBorder(border);
    clockLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    gameStatusLabel.setBorder(border);
    gameStatusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    gameStatusLabel.setToolTipText(I18nSupport.getValue(CLIENT_BUNDLE,"label.tooltip.game.status"));
    connectionLabel.setBorder(border);
    connectionLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    besideLabelPanel.setLayout(new BoxLayout(besideLabelPanel, BoxLayout.LINE_AXIS));
    besideLabelPanel.add(gameStatusLabel);
    besideLabelPanel.add(connectionLabel);
    besideLabelPanel.add(clockLabel);
    return besideLabelPanel;
  }
  /**
   * This method changes the icon of the status label dependent on the boolean.
   * @param connected Sets the corresponding icon to the value
   * @param serverAddress Will be set to the tooltip as information
   */
  public void setConnected(Boolean connected, String serverAddress) {
    if(connected) {
      connectionLabel.setIcon(connectedIcon);
      connectionLabel.setToolTipText(I18nSupport.getValue(MESSAGE_BUNDLE,"status.connected.with.0", serverAddress));
    } else {
      connectionLabel.setIcon(disconnectedIcon);
      connectionLabel.setToolTipText(I18nSupport.getValue(MESSAGE_BUNDLE,"status.disconnected"));
    }
  }

  public void setConnected(Boolean connected) {
    if(connected) {
      connectionLabel.setIcon(connectedIcon);
    } else setConnected(false, "");
  }

  public void setPlayerType(PlayerConstants.PlayerType type) {
    if(!type.getDescription().equals(gameStatusLabel.getText())) {
      gameStatusLabel.setIcon(ResourceGetter.getPlayerTypeIcon(type,
          mainStatusLabel.getHeight()-mainStatusLabel.getBorder().getBorderInsets(mainStatusLabel).top*2));
      gameStatusLabel.setText(type.getDescription());
    }
  }

  public void setText(String text) {
    mainStatusLabel.setText(text);
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
        LOGGER.info("Error while thread pausing!");
      }
    }
  }

  /* Getter and Setter */
}

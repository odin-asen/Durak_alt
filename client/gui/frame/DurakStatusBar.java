package client.gui.frame;

import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.LoggingUtility;
import common.utilities.constants.GameCardConstants;
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

import static common.i18n.BundleStrings.*;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 19:37
 */
public class DurakStatusBar extends JPanel implements Runnable {
  private static final Logger LOGGER = LoggingUtility.getLogger(DurakStatusBar.class.getName());
  private static final Dimension MINIMUM_DIMENSION = new Dimension(16,16);

  private static final DateFormat format =
      new SimpleDateFormat(I18nSupport.getValue(GENERAL_FORMAT, "date"), Locale.getDefault());
  private static final Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());
  private static final ImageIcon connectedIcon = ResourceGetter.getStatusIcon("status.connected");
  private static final ImageIcon disconnectedIcon =
      ResourceGetter.getStatusIcon("status.disconnected");

  private JPanel besideLabelPanel;
  private JLabel mainStatusLabel;
  private JLabel stackStatusLabel;
  private JLabel playerTypeStatusLabel;
  private JLabel connectionLabel;
  private JLabel clockLabel;
  private boolean running;

  /* Constructors */
  public DurakStatusBar() {
    mainStatusLabel = new JLabel();
    mainStatusLabel.setBorder(WidgetCreator.createStatusBorder());
    mainStatusLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    format.setCalendar(calendar);
    running = true;

    setLayout(new BorderLayout());
    add(mainStatusLabel, BorderLayout.CENTER);
    add(getBesideLabelPanel(), BorderLayout.LINE_END);

    /* initialise fields */
    setStackStatus(null, 0);
    setText("");
    setConnected(false);
    setPlayerType(PlayerConstants.PlayerType.DEFAULT);

    /* start clock */
    new Thread(this).start();
  }

  /* Methods */
  private JPanel getBesideLabelPanel() {
    if(besideLabelPanel != null)
      return besideLabelPanel;

    final Border border = WidgetCreator.createStatusBorder();
    besideLabelPanel = new JPanel();

    clockLabel = createStatusLabel(border, null);
    playerTypeStatusLabel = createStatusLabel(border,
        I18nSupport.getValue(GUI_COMPONENT, "tooltip.player.type.status"));
    connectionLabel = createStatusLabel(border, null);
    stackStatusLabel = createStatusLabel(border, null);

    besideLabelPanel.setLayout(new BoxLayout(besideLabelPanel, BoxLayout.LINE_AXIS));
    besideLabelPanel.add(stackStatusLabel);
    besideLabelPanel.add(playerTypeStatusLabel);
    besideLabelPanel.add(connectionLabel);
    besideLabelPanel.add(clockLabel);

    return besideLabelPanel;
  }

  private JLabel createStatusLabel(Border border, String tooltip) {
    final JLabel label = new JLabel();
    label.setBorder(border);
    label.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    label.setToolTipText(tooltip);

    return label;
  }

  /**
   * This method changes the icon of the status label dependent on the boolean.
   * @param connected Sets the corresponding icon to the value
   * @param serverAddress Will be set to the tooltip as information
   */
  public void setConnected(boolean connected, String serverAddress) {
    if(connected) {
      connectionLabel.setIcon(connectedIcon);
      connectionLabel.setToolTipText(I18nSupport.getValue(USER_MESSAGES,"status.connected.with.0",
          serverAddress));
    } else {
      connectionLabel.setIcon(disconnectedIcon);
      connectionLabel.setToolTipText(I18nSupport.getValue(USER_MESSAGES,"status.disconnected"));
    }
  }

  public void setConnected(boolean connected) {
    if(connected) {
      connectionLabel.setIcon(connectedIcon);
    } else setConnected(false, "");
  }

  public void setPlayerType(PlayerConstants.PlayerType type) {
    if(type == null)
      playerTypeStatusLabel.setPreferredSize(MINIMUM_DIMENSION);
    else {
      if(!type.getDescription().equals(playerTypeStatusLabel.getText())) {
        playerTypeStatusLabel.setIcon(
            ResourceGetter.getPlayerTypeIcon(type, mainStatusLabel.getHeight()
                - mainStatusLabel.getBorder().getBorderInsets(mainStatusLabel).top*2));
        playerTypeStatusLabel.setText(type.getDescription());
        playerTypeStatusLabel.setPreferredSize(null);
      }
    }
  }

  public void setText(String text) {
    mainStatusLabel.setText(text);
  }

  public void setStackStatus(GameCardConstants.CardColour cardColour, int cardCount) {
    stackStatusLabel.setIcon(cardColour == null ?
        null : ResourceGetter.getStatusIcon("status.card.suit.0",cardColour.getValue()));
    stackStatusLabel.setText(cardColour == null ? "" : Integer.toString(cardCount));
    stackStatusLabel.setToolTipText(cardColour == null ?
        I18nSupport.getValue(GUI_COMPONENT, "tooltip.shows.stack.status") :
        I18nSupport.getValue(GUI_COMPONENT, "tooltip.trump.colour.0.stack.count.0",
            cardColour.getName(), cardCount));
    if(cardColour == null)
      stackStatusLabel.setPreferredSize(MINIMUM_DIMENSION);
    else stackStatusLabel.setPreferredSize(null);
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
      } catch (InterruptedException e) {
        LOGGER.info("Error while pausing clock thread: "+e.getMessage());
      }
    }
  }

  /* Getter and Setter */
}

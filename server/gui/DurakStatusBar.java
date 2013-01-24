package server.gui;

import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
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

import static common.i18n.BundleStrings.GENERAL_FORMAT;
import static common.i18n.BundleStrings.SERVER_GUI;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 19:37
 */
public class DurakStatusBar extends JPanel implements Runnable {
  private static final Logger LOGGER = LoggingUtility.getLogger(DurakStatusBar.class.getName());

  private static final DateFormat format =
      new SimpleDateFormat(I18nSupport.getValue(GENERAL_FORMAT,"date"), Locale.getDefault());
  private static final Calendar calendar = GregorianCalendar.getInstance(Locale.getDefault());

  private JPanel besideLabelPanel;
  private JLabel mainStatusLabel;
  private JLabel playerCountLabel;
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

    /* initialise fields */
    setText("");
    setPlayerCount(0,0);

    /* start clock */
    new Thread(this).start();
  }

  /* Methods */
  private JPanel getBesideLabelPanel() {
    if(besideLabelPanel != null)
      return besideLabelPanel;

    besideLabelPanel = new JPanel();
    playerCountLabel = new JLabel();
    clockLabel = new JLabel();

    final Border border = WidgetCreator.createStatusBorder();
    playerCountLabel.setBorder(border);
    playerCountLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    clockLabel.setBorder(border);
    clockLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    besideLabelPanel.setLayout(new BoxLayout(besideLabelPanel, BoxLayout.LINE_AXIS));
    besideLabelPanel.add(playerCountLabel);
    besideLabelPanel.add(clockLabel);
    return besideLabelPanel;
  }

  public void setText(String text) {
    mainStatusLabel.setText(text);
  }

  public void setPlayerCount(Integer takers, Integer spectators) {
    if(takers < 0) takers = 0;
    if(spectators < 0) spectators = 0;

    String text = takers+"/"+spectators;
    String tooltip = I18nSupport.getValue(SERVER_GUI, "label.tooltip.takers.spectators",
        takers, spectators);
    playerCountLabel.setText(text);
    playerCountLabel.setToolTipText(tooltip);
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

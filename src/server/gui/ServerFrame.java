package server.gui;

import dto.message.GUIObserverType;
import dto.message.MessageObject;
import resources.ResourceGetter;
import server.business.GameServer;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;

import static server.gui.ServerGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:33
 */
public class ServerFrame extends JFrame implements Observer {
  private JToolBar toolBar;
  private JButton startButton;
  private JButton stopButton;
  private JButton closeButton;
  private JPanel settingsPanel;
  private JFormattedTextField portField;
  private JPanel statusPanel;
  private JLabel statusBar;

  private GameServer gameServer;

  public ServerFrame() {
    FensterPositionen position = FensterPositionen.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);

    this.setBounds(position.getRectangle());
    initComponents();

    this.setVisible(true);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  private void initComponents() {
    initToolBar();
    initSettingsPanel();
    initStatusPanel();

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(settingsPanel, BorderLayout.CENTER);
    getContentPane().add(statusPanel, BorderLayout.PAGE_END);
  }

  private void initStatusPanel() {
    statusPanel = new JPanel();
    statusBar = new JLabel();

    statusBar.setText(SERVER_INACTIVE);
    statusPanel.setPreferredSize(new Dimension(0, 16));

    statusPanel.setLayout(new BorderLayout());
    statusPanel.add(Box.createRigidArea(new Dimension(5, 0)), BorderLayout.LINE_START);
    statusPanel.add(statusBar, BorderLayout.CENTER);
  }

  private void initToolBar() {
    toolBar = new JToolBar();
    startButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_PLAY, TOOLTIP_START,
        ACTION_COMMAND_START, ALTERNATIVE_START, KeyEvent.VK_G);
    stopButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_STOP_PLAYER, TOOLTIP_STOP,
        ACTION_COMMAND_STOP, ALTERNATIVE_STOP, KeyEvent.VK_A);
    closeButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE, TOOLTIP_CLOSE,
        ACTION_COMMAND_CLOSE, ALTERNATIVE_CLOSE, KeyEvent.VK_Q);

    toolBar.setMargin(new Insets(5, 5, 5, 5));
    toolBar.setRollover(true);
    toolBar.setFloatable(false);

    toolBar.add(startButton);
    toolBar.addSeparator();
    toolBar.add(stopButton);
    toolBar.add(Box.createHorizontalGlue());
    toolBar.addSeparator();
    toolBar.add(closeButton);
  }

  private void initSettingsPanel() {
    final int row = 3;
    final int column = 3;
    settingsPanel = new JPanel(new GridLayout(row,column));
    final int limit = (row*column)/2;

    for (int i = 0; i < limit; i++) {
      settingsPanel.add(Box.createGlue());
    }

    settingsPanel.add(getPortPanel());

    for (int i = 0; i < limit; i++) {
      settingsPanel.add(Box.createGlue());
    }
  }

  private JPanel getPortPanel() {
    final int row = 1;
    final int column = 1;
    final int limit = (row*column)/2;
    JPanel panel = new JPanel(new GridLayout(row, column));
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);
    portField = new JFormattedTextField(format);
    portField.setText("1025");

    panel.setBorder(BorderFactory.createTitledBorder(LABEL_PORT));
    for (int i = 0; i < limit; i++) {
      panel.add(Box.createGlue());
    }
    panel.add(portField);
    for (int i = 0; i < limit; i++) {
      panel.add(Box.createGlue());
    }
    return panel;
  }

  private JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, String alternativeText,
                                    int virtualKey) {
    JButton button = new JButton();
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setMnemonic(virtualKey);
    button.addActionListener(new ToolBarComponentAL());
    button.setIcon(ResourceGetter.getImage(pictureName, alternativeText));

    if (button.getIcon() == null)
      button.setText(alternativeText);

    return button;
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handleUpdate(o, object);
  }

  private void handleUpdate(Observable o, MessageObject object) {
    if (GUIObserverType.CLIENT_CONNECTED.equals(object.getType())) {

    } else if (GUIObserverType.CLIENT_DISCONNECTED.equals(object.getType())) {

    } else if (GUIObserverType.SERVER_START.equals(object.getType())) {
      statusBar.setText("Server l\u00e4uft");
    } else if (GUIObserverType.SERVER_STOP.equals(object.getType())) {
      statusBar.setText(SERVER_INACTIVE);
    } else if (GUIObserverType.SERVER_FAIL.equals(object.getType())) {
      statusBar.setText("Keine Berechtigung f\u00dcr Port "+portField.getText()+" oder schon belegt.");
    }
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        closeFrame(e);
      } else if (ACTION_COMMAND_START.equals(e.getActionCommand())) {
        startGameServer((Observer) SwingUtilities.getRoot((Component) e.getSource()));
      } else if (ACTION_COMMAND_STOP.equals(e.getActionCommand())) {
        stopGameServer();
      }
    }

    private void stopGameServer() {
      gameServer.stopRunning();
    }

    private void closeFrame(ActionEvent e) {
      JFrame frame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());
      frame.setVisible(false);
      frame.dispose();
      System.exit(0);
    }

    private void startGameServer(Observer observer) {
      gameServer = GameServer.getServerInstance();
      gameServer.addObserver(observer);
      gameServer.setPort(Integer.parseInt(portField.getText()));
      gameServer.startServer();
    }
  }
}

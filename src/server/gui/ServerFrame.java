package server.gui;

import dto.observer.GUIObserverConstants;
import dto.observer.ObserverUpdateObject;
import server.business.GameServer;
import resources.ResourceGetter;
import resources.ResourceGetterException;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:33
 */
public class ServerFrame extends JFrame implements Observer {
  public static final float SCREEN_SIZE_FENSTER = 0.3f;
  public static final String ACTION_COMMAND_START = "start";
  public static final String ACTION_COMMAND_STOP = "stop";
  public static final String ACTION_COMMAND_CLOSE = "close";
  public static final String LABEL_SERVERADRESSE = "Serveradresse:";
  public static final String LABEL_PORT = "Port:";
  public static final String TOOLTIP_START = "Starten den Server f\u00fcr ein Spiel";
  public static final String TOOLTIP_STOP = "Stopt den Server";
  public static final String TOOLTIP_SCHLIESSEN = "Schlie\u00dft die Anwendung";
  public static final String ALTERNATIVE_SCLHIESSEN = "Schlie\u00dfen";
  public static final String ALTERNATIVE_STOP = "Stop";
  public static final String ALTERNATIVE_START = "Start";
  public static final String SERVER_INAKTIV = "Server ist inaktiv";

  private JToolBar toolBar;
  private JButton startButton;
  private JButton stopButton;
  private JButton closeButton;
  private JPanel settingsPanel;
  private JComboBox<String> addressField;
  private JFormattedTextField portField;
  private JPanel statusPanel;
  private JLabel statusBar;

  private GameServer gameServer;

  public ServerFrame() {
    FensterPositionen position = FensterPositionen.createFensterPositionen(
        SCREEN_SIZE_FENSTER, SCREEN_SIZE_FENSTER);
    initComponents();

    this.setBounds(position.getRectangle());
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

    statusBar.setText(SERVER_INAKTIV);
    statusPanel.setPreferredSize(new Dimension(0, 16));

    statusPanel.setLayout(new BorderLayout());
    statusPanel.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.LINE_START);
    statusPanel.add(statusBar, BorderLayout.CENTER);
  }

  private void initToolBar() {
    toolBar = new JToolBar();
    startButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_PLAY, TOOLTIP_START,
        ACTION_COMMAND_START, ALTERNATIVE_START, KeyEvent.VK_G);
    stopButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_STOPPLAYER, TOOLTIP_STOP,
        ACTION_COMMAND_STOP, ALTERNATIVE_STOP, KeyEvent.VK_A);
    closeButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE, TOOLTIP_SCHLIESSEN,
        ACTION_COMMAND_CLOSE, ALTERNATIVE_SCLHIESSEN, KeyEvent.VK_Q);

    toolBar.setMargin(new Insets(5,5,5,5));
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
    settingsPanel = new JPanel(new BorderLayout());
    JLabel addressLabel = new JLabel(LABEL_SERVERADRESSE);
    JLabel portLabel = new JLabel(LABEL_PORT);
    JPanel gridPanel = new JPanel(new GridLayout(0,2,0,25));

    initConnectionFields();

    gridPanel.add(addressLabel);
    gridPanel.add(addressField);
    gridPanel.add(portLabel);
    gridPanel.add(portField);

    settingsPanel.add(Box.createVerticalStrut(30), BorderLayout.PAGE_START);
    settingsPanel.add(Box.createHorizontalStrut(25), BorderLayout.LINE_START);
    settingsPanel.add(Box.createHorizontalStrut(80), BorderLayout.LINE_END);
    settingsPanel.add(Box.createVerticalStrut(60), BorderLayout.PAGE_END);
    settingsPanel.add(gridPanel, BorderLayout.CENTER);
  }

  private void initConnectionFields() {
    final Vector<String> comboBoxContent = new Vector<String>();
    addressField = new JComboBox<String>(comboBoxContent);
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);
    portField = new JFormattedTextField(format);

    addressField.setEditable(true);
    try {
      addressField.addItem(InetAddress.getLocalHost().getHostName());
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    addressField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          InetAddress address = InetAddress.getByName(addressField.getSelectedItem().toString());
          final String selected = address.getHostName();
          if(!comboBoxContent.contains(selected)) {
            addressField.addItem(selected);
            addressField.setSelectedItem(selected);
          }
        } catch (UnknownHostException e1) {
          e1.printStackTrace();
        }
      }
    });
  }

  private JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, String alternativeText,
                                    int virtualKey) {
    JButton button = new JButton();
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setMnemonic(virtualKey);
    button.addActionListener(new ToolBarComponentAL());
    try {
      button.setIcon(ResourceGetter.loadImage(pictureName, alternativeText));
    } catch (ResourceGetterException e) {
      e.printStackTrace();
    }

    if(button.getIcon() == null)
      button.setText(alternativeText);

    return button;
  }

  public void update(Observable o, Object arg) {
    final ObserverUpdateObject object = (ObserverUpdateObject) arg;
    handleUpdate(o, object);
  }

  private void handleUpdate(Observable o, ObserverUpdateObject object) {
    if(GUIObserverConstants.CLIENT_CONNECTED.equals(object.getObserverConstant())) {

    } else if(GUIObserverConstants.CLIENT_DISCONNECTED.equals(object.getObserverConstant())) {

    } else if(GUIObserverConstants.SERVER_START.equals(object.getObserverConstant())) {
      statusBar.setText("Server l\u00e4uft");
    } else if(GUIObserverConstants.SERVER_STOP.equals(object.getObserverConstant())) {
      statusBar.setText(SERVER_INAKTIV);
    }
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        closeFrame(e);
      } else if(ACTION_COMMAND_START.equals(e.getActionCommand())) {
        startGameServer((Observer) SwingUtilities.getRoot((Component) e.getSource()));
      } else if(ACTION_COMMAND_STOP.equals(e.getActionCommand())) {
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
      try {
        gameServer = GameServer.getServerInstance();
        gameServer.addObserver(observer);
        gameServer.setConnection(addressField.getSelectedItem().toString(),
            Integer.parseInt(portField.getText()));
        new Thread(gameServer).start();
      } catch (IOException e) {
        statusBar.setText("Keine Berechtigung f\u00dcr Port "+portField.getText()+" oder schon belegt.");
      }
    }
  }
}

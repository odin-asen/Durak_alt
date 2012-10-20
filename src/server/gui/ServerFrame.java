package server.gui;

import dto.ClientInfo;
import dto.message.GUIObserverType;
import dto.message.MessageObject;
import resources.ResourceGetter;
import server.business.GameServer;
import utilities.gui.FensterPositionen;
import utilities.gui.WidgetCreator;

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
@SuppressWarnings("FieldCanBeLocal")
public class ServerFrame extends JFrame implements Observer {
  private JToolBar toolBar;
  private JButton startButton;
  private JButton stopButton;
  private JButton closeButton;
  private JPanel settingsPanel;
  private JFormattedTextField portField;
  private JScrollPane clientListPanel;
  private JPanel statusPanel;
  private JLabel statusBar;

  private GameServer gameServer;
  private JList<ClientInfo> clientList;
  private DefaultListModel<ClientInfo> listModel;

  /* Constructors */
  public ServerFrame() {
    FensterPositionen position = FensterPositionen.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);

    this.setBounds(position.getRectangle());
    initComponents();

    this.setVisible(true);
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  /* Methods */
  private void initComponents() {
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(getToolBar(), BorderLayout.PAGE_START);
    getContentPane().add(getClientListPanel(), BorderLayout.LINE_START);
    getContentPane().add(getSettingsPanel(), BorderLayout.CENTER);
    getContentPane().add(getStatusPanel(), BorderLayout.PAGE_END);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handleUpdate(object);
  }

  private void handleUpdate(MessageObject object) {
    if (GUIObserverType.CLIENT_CONNECTED.equals(object.getType())) {
      listModel.addElement((ClientInfo) object.getSendingObject());
    } else if (GUIObserverType.CLIENT_DISCONNECTED.equals(object.getType())) {
      listModel.removeElement(object.getSendingObject());
    } else if (GUIObserverType.SERVER_START.equals(object.getType())) {
      statusBar.setText("Server l\u00e4uft");
    } else if (GUIObserverType.SERVER_STOP.equals(object.getType())) {
      statusBar.setText(SERVER_INACTIVE);
    } else if (GUIObserverType.SERVER_FAIL.equals(object.getType())) {
      statusBar.setText("Keine Berechtigung f\u00dcr Port "+portField.getText()+" oder schon belegt.");
    }
  }

  /* Getter and Setter */
  private JPanel getStatusPanel() {
    if(statusPanel != null)
      return statusPanel;

    statusPanel = new JPanel();
    statusBar = new JLabel();

    statusBar.setText(SERVER_INACTIVE);
    statusPanel.setPreferredSize(new Dimension(0, 16));

    statusPanel.setLayout(new BorderLayout());
    statusPanel.add(Box.createRigidArea(new Dimension(5, 0)), BorderLayout.LINE_START);
    statusPanel.add(statusBar, BorderLayout.CENTER);

    return statusPanel;
  }

  private JToolBar getToolBar() {
    if(toolBar != null)
      return toolBar;

    toolBar = new JToolBar();
    ToolBarComponentAL listener = new ToolBarComponentAL();
    startButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_PLAY, TOOLTIP_START,
        ACTION_COMMAND_START, ALTERNATIVE_START, listener, KeyEvent.VK_G);
    stopButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_STOP_PLAYER, TOOLTIP_STOP,
        ACTION_COMMAND_STOP, ALTERNATIVE_STOP, listener, KeyEvent.VK_A);
    closeButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE, TOOLTIP_CLOSE,
        ACTION_COMMAND_CLOSE, ALTERNATIVE_CLOSE, listener, KeyEvent.VK_Q);

    toolBar.setMargin(new Insets(5, 5, 5, 5));
    toolBar.setRollover(true);
    toolBar.setFloatable(false);

    toolBar.add(startButton);
    toolBar.addSeparator();
    toolBar.add(stopButton);
    toolBar.add(Box.createHorizontalGlue());
    toolBar.addSeparator();
    toolBar.add(closeButton);

    return toolBar;
  }

  private JPanel getSettingsPanel() {
    if(settingsPanel != null)
      return settingsPanel;

    settingsPanel = new JPanel(new GridBagLayout());

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.ipadx = 5;
    constraints.ipady = 5;
    settingsPanel.add(getPortPanel(), constraints);

    return settingsPanel;
  }

  private JPanel getPortPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);

    portField = new JFormattedTextField(format);
    portField.setText("1025");
    portField.setPreferredSize(new Dimension(PREFERRED_FIELD_WIDTH, portField.getPreferredSize().height));
    portField.setMaximumSize(new Dimension(Integer.MAX_VALUE, portField.getPreferredSize().height));

    panel.setBorder(BorderFactory.createTitledBorder(LABEL_PORT));

    GridBagConstraints constraints = new GridBagConstraints();
    panel.add(portField, constraints);

    return panel;
  }

  private JScrollPane getClientListPanel() {
    if(clientListPanel != null)
      return clientListPanel;

    clientListPanel = new JScrollPane();
    listModel = new DefaultListModel<ClientInfo>();
    clientList = new JList<ClientInfo>(listModel);
    clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    clientListPanel.setPreferredSize(new Dimension(LIST_WIDTH, clientListPanel.getPreferredSize().height));
    clientListPanel.setViewportView(clientList);

    return clientListPanel;
  }

  /* Inner Classes */
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
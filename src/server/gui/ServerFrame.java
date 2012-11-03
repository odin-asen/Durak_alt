package server.gui;

import dto.ClientInfo;
import dto.message.GUIObserverType;
import dto.message.MessageObject;
import resources.ResourceGetter;
import resources.ResourceList;
import server.business.GameServer;
import utilities.gui.Constraints;
import utilities.gui.FensterPositionen;
import utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static server.gui.ServerGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:33
 */
public class ServerFrame extends JFrame implements Observer {
  private JToolBar toolBar;
  private JScrollPane settingsPanel;
  private JFormattedTextField portField;
  private JScrollPane clientListPanel;
  private JPanel statusPanel;
  private JLabel statusBar;

  private JList<ClientInfo> clientList;
  private DefaultListModel<ClientInfo> listModel;
  private JComboBox<Integer> stackSizeCombo;
  private JPanel gameSettingsPanel;

  /* Constructors */
  public ServerFrame() {
    FensterPositionen position = FensterPositionen.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);

    this.setBounds(position.getRectangle());
    initComponents();

    GameServer.getServerInstance().addObserver(this);
    this.setTitle(APPLICATION_NAME+TITLE_SEPARATOR+VERSION);
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

  public Integer getStackSize() {
    return (Integer) stackSizeCombo.getSelectedItem();
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handleUpdate(object);
  }

  private void handleUpdate(MessageObject object) {
    if (GUIObserverType.ADD_CLIENT.equals(object.getType())) {
      listModel.addElement((ClientInfo) object.getSendingObject());
    } else if (GUIObserverType.REMOVE_CLIENT.equals(object.getType())) {
      removeClient((ClientInfo) object.getSendingObject());
    } else if (GUIObserverType.SERVER_FAIL.equals(object.getType())) {
      setStatusBarText("Keine Berechtigung f\u00dcr Port "+portField.getText()+" oder schon belegt.");
    }
  }

  private void removeClient(ClientInfo client) {
    int clientIndex = -1;
    for (int i = 0; i < listModel.size(); i++) {
      if(listModel.get(i).isEqual(client)) {
        clientIndex = i;
        i = listModel.size();
      }
    }

    if(clientIndex != -1) {
      listModel.remove(clientIndex);
    }
  }

  public void setStatusBarText(String status) {
    statusBar.setText(status);
  }

  public Integer getPortValue() {
    return Integer.parseInt(portField.getText());
  }

  /* Getter and Setter */
  private JPanel getStatusPanel() {
    if(statusPanel != null)
      return statusPanel;

    statusPanel = new JPanel();
    statusBar = new JLabel();

    statusBar.setText(STATUS_SERVER_INACTIVE);
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
    ToolBarComponentAL listener = new ToolBarComponentAL(this);
    JButton startStopButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_PLAY, TOOLTIP_START,
        ACTION_COMMAND_START, ALTERNATIVE_START, listener, KeyEvent.VK_G);
    JButton gameStartButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_GAME_START, TOOLTIP_GAME_START,
        ACTION_COMMAND_GAME_START, ALTERNATIVE_GAME_START, listener, KeyEvent.VK_S);
    JButton closeButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CLOSE, TOOLTIP_CLOSE,
        ACTION_COMMAND_CLOSE, ALTERNATIVE_CLOSE, listener, KeyEvent.VK_Q);

    toolBar.setMargin(new Insets(5, 5, 5, 5));
    toolBar.setRollover(true);
    toolBar.setFloatable(false);

    toolBar.add(startStopButton);
    toolBar.addSeparator();
    toolBar.add(gameStartButton);
    toolBar.add(Box.createHorizontalGlue());
    toolBar.addSeparator();
    toolBar.add(closeButton);

    return toolBar;
  }

  private JScrollPane getSettingsPanel() {
    if(settingsPanel != null)
      return settingsPanel;

    settingsPanel = new JScrollPane();
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints constraints;

    constraints = Constraints.getDefaultFieldConstraintLeft(0,0,1,1);
    constraints.ipady = 5;
    panel.add(getPortPanel(), constraints);
    constraints.gridy = 1;
    panel.add(getGameSettingsPanel(), constraints);

    settingsPanel.setViewportView(panel);
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
    clientList.setCellRenderer(new DefaultListCellRenderer());
    clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    clientListPanel.setPreferredSize(new Dimension(LIST_WIDTH, clientListPanel.getPreferredSize().height));
    clientListPanel.setViewportView(clientList);

    return clientListPanel;
  }

  public JPanel getGameSettingsPanel() {
    if(gameSettingsPanel != null)
      return gameSettingsPanel;

    gameSettingsPanel = new JPanel();
    stackSizeCombo = new JComboBox<Integer>(new Integer[]{36,40,44,48,52});
    JLabel stackSizeLabel = new JLabel("Anzahl Karten:");

    stackSizeCombo.setEditable(false);
    stackSizeCombo.setToolTipText("Gesamte Anzahl der Karten im Spiel");
    stackSizeCombo.setMaximumSize(stackSizeCombo.getPreferredSize());
    gameSettingsPanel.setLayout(new GridLayout(0,2,2,0));
    gameSettingsPanel.setBorder(BorderFactory.createTitledBorder("Spieleinstellungen"));
    gameSettingsPanel.add(stackSizeLabel);
    gameSettingsPanel.add(stackSizeCombo);

    return gameSettingsPanel;
  }

  public DefaultListModel<ClientInfo> getClientList() {
    return (DefaultListModel<ClientInfo>) clientList.getModel();
  }

  /* Inner Classes */
}

class ToolBarComponentAL implements ActionListener {
  private static final Logger LOGGER = Logger.getLogger(ToolBarComponentAL.class.getName());

  private ServerFrame frame;

  ToolBarComponentAL(ServerFrame frame) {
    this.frame = frame;
  }

  public void actionPerformed(ActionEvent e) {
    if (ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
      closeFrame(e);
    } else if (ACTION_COMMAND_START.equals(e.getActionCommand())) {
      startGameServer();
      changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_STOP_PLAYER,
          ACTION_COMMAND_STOP, TOOLTIP_STOP, ALTERNATIVE_STOP);
    } else if (ACTION_COMMAND_STOP.equals(e.getActionCommand())) {
      stopGameServer();
      changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_PLAY,
          ACTION_COMMAND_START, TOOLTIP_START, ALTERNATIVE_START);
    } else if (ACTION_COMMAND_GAME_START.equals(e.getActionCommand())) {
      startGame();
    }
  }

  private void changeButton(JButton button, String pictureName, String actionCommand,
                            String toolTipText, String alternativeText) {
    button.setActionCommand(actionCommand);
    if(pictureName != null) {
      ImageIcon icon = ResourceGetter.getImage(pictureName, alternativeText);
      button.setIcon(icon);
    }
    button.setToolTipText(toolTipText);
  }

  private void startGame() {
    GameServer.getServerInstance().startGame(frame.getStackSize());
  }

  private void stopGameServer() {
    closeServer();
    frame.setStatusBarText(STATUS_SERVER_INACTIVE);
  }

  private void closeServer() {
    try {
      GameServer.getServerInstance().shutdownServer();
    } catch (NotBoundException e) {
      LOGGER.info(e.getMessage());
    } catch (RemoteException e) {
      LOGGER.info(e.getMessage());
    }
  }

  private void closeFrame(ActionEvent e) {
    JFrame frame = (JFrame) SwingUtilities.getRoot((Component) e.getSource());
    closeServer();
    frame.setVisible(false);
    frame.dispose();
    System.exit(0);
  }

  private void startGameServer() {
    GameServer gameServer = GameServer.getServerInstance(frame.getPortValue(), "");

    try {
      gameServer.startServer();
      frame.setStatusBarText(STATUS_SERVER_ACTIVE);
    } catch (IllegalAccessException e) {
      LOGGER.severe(e.getClass()+" "+e.getMessage());
    } catch (InstantiationException e) {
      LOGGER.severe(e.getClass()+" "+e.getMessage());
    } catch (RemoteException e) {
      LOGGER.severe(e.getClass()+" Could not register services");
      e.printStackTrace();
    }
  }
}
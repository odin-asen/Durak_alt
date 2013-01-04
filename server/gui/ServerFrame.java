package server.gui;

import common.dto.ClientInfo;
import common.dto.message.GUIObserverType;
import common.dto.message.MessageObject;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.Miscellaneous;
import common.utilities.gui.Constraints;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;
import server.business.GameServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
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
  private static final String SERVER_BUNDLE = "server.server"; //NON-NLS
  private static final String MESSAGE_BUNDLE = "user.messages"; //NON-NLS
  private static Logger LOGGER = Logger.getLogger(ServerFrame.class.getName());

  private static final String VERSION_NUMBER = "0.1";
  private static final String ACTION_COMMAND_START = "start"; //NON-NLS
  private static final String ACTION_COMMAND_STOP = "stop"; //NON-NLS
  private static final String ACTION_COMMAND_CLOSE = "close"; //NON-NLS
  private static final String ACTION_COMMAND_START_GAME = "gameStart"; //NON-NLS

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
    FramePosition position = FramePosition.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);

    setBounds(position.getRectangle());
    initComponents();

    GameServer.getServerInstance().addObserver(this);
    setTitle(MessageFormat.format("{0} - {1} {2}", I18nSupport.getValue(SERVER_BUNDLE,"application.title"),
        I18nSupport.getValue(SERVER_BUNDLE,"version"), VERSION_NUMBER));
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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

  public void refreshClientList(List<ClientInfo> newClients) {
    listModel.clear();
    for (ClientInfo client : newClients)
      listModel.add(listModel.size(), client);
  }

  private void handleUpdate(MessageObject object) {
    if (GUIObserverType.REFRESH_CLIENT_LIST.equals(object.getType())) {
      refreshClientList(GameServer.getServerInstance().getClients());
    } else if (GUIObserverType.SERVER_FAIL.equals(object.getType())) {
      setStatusBarText(I18nSupport.getValue(MESSAGE_BUNDLE, "server.port.0.used",
          portField.getText()));
    }
  }

  private void removeClient(ClientInfo client) {
    for (int i = 0; i < listModel.size(); i++) {
      if(listModel.get(i).equals(client)) {
        listModel.remove(i);
        i = listModel.size();
      }
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

    statusBar.setText(I18nSupport.getValue(MESSAGE_BUNDLE, "status.server.inactive"));
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
    final ToolBarComponentAL listener = new ToolBarComponentAL();
    JButton startStopButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_PLAY,
        I18nSupport.getValue(SERVER_BUNDLE,"button.tooltip.start.server"), ACTION_COMMAND_START,
        I18nSupport.getValue(SERVER_BUNDLE,"image.description.start.server"), listener, KeyEvent.VK_G);
    JButton gameStartButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_GAME_START,
        I18nSupport.getValue(SERVER_BUNDLE,"button.tooltip.start.game"), ACTION_COMMAND_START_GAME,
        I18nSupport.getValue(SERVER_BUNDLE,"image.description.start.game"), listener, KeyEvent.VK_S);
    JButton closeButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CLOSE,
        I18nSupport.getValue(SERVER_BUNDLE,"button.tooltip.close"), ACTION_COMMAND_CLOSE,
        I18nSupport.getValue(SERVER_BUNDLE,"image.description.close"), listener, KeyEvent.VK_Q);

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

    panel.setBorder(BorderFactory.createTitledBorder(I18nSupport.getValue(SERVER_BUNDLE, "border.title.server.port")));

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
    JLabel stackSizeLabel = new JLabel(I18nSupport.getValue(SERVER_BUNDLE, "label.text.card.number"));

    stackSizeCombo.setEditable(false);
    stackSizeCombo.setToolTipText(I18nSupport.getValue(SERVER_BUNDLE,"combo.box.tooltip.card.number"));
    stackSizeCombo.setMaximumSize(stackSizeCombo.getPreferredSize());
    gameSettingsPanel.setLayout(new GridLayout(0, 2, 2, 0));
    gameSettingsPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(SERVER_BUNDLE, "border.title.game.settings")));
    gameSettingsPanel.add(stackSizeLabel);
    gameSettingsPanel.add(stackSizeCombo);

    return gameSettingsPanel;
  }

  public DefaultListModel<ClientInfo> getClientList() {
    return (DefaultListModel<ClientInfo>) clientList.getModel();
  }

  /* Inner Classes */
  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        closeFrame(e);
      } else if (ACTION_COMMAND_START.equals(e.getActionCommand())) {
        startGameServer();
        changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_STOP_PLAYER,
            ACTION_COMMAND_STOP, I18nSupport.getValue(SERVER_BUNDLE,"button.tooltip.stop.server"),
            I18nSupport.getValue(SERVER_BUNDLE,"image.description.stop.server"));
      } else if (ACTION_COMMAND_STOP.equals(e.getActionCommand())) {
        stopGameServer();
        changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_PLAY,
            ACTION_COMMAND_START, I18nSupport.getValue(SERVER_BUNDLE,"button.tooltip.start.server"),
            I18nSupport.getValue(SERVER_BUNDLE,"image.description.start.server"));
      } else if (ACTION_COMMAND_START_GAME.equals(e.getActionCommand())) {
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
      GameServer.getServerInstance().startGame(getStackSize());
    }

    private void stopGameServer() {
      closeServer();
      setStatusBarText(I18nSupport.getValue(MESSAGE_BUNDLE, "status.server.inactive"));
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
      closeServer();
      setVisible(false);
      dispose();
      System.exit(0);
    }

    private void startGameServer() {
      final GameServer gameServer = GameServer.getServerInstance(getPortValue(), "");

      try {
        String ipAddress;
        try {
          ipAddress = Miscellaneous.getHostInetAddress(Inet4Address.class).getHostAddress();
        } catch (SocketException e) {
          ipAddress = InetAddress.getLoopbackAddress().getHostAddress();
        }
        gameServer.startServer(ipAddress);
        setStatusBarText(I18nSupport.getValue(MESSAGE_BUNDLE, "status.server.running"));
      } catch (IllegalAccessException e) {
        setStatusBarText(I18nSupport.getValue(MESSAGE_BUNDLE, "network.error"));
        LOGGER.severe(e.getClass()+" "+e.getMessage());
      } catch (InstantiationException e) {
        setStatusBarText(I18nSupport.getValue(MESSAGE_BUNDLE, "network.error"));
        LOGGER.severe(e.getClass()+" "+e.getMessage());
      } catch (RemoteException e) {
        setStatusBarText(I18nSupport.getValue(MESSAGE_BUNDLE, "network.error"));
        LOGGER.severe(e.getClass() + " Could not register services");
      }
    }
  }
}
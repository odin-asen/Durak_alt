package server.gui;

import dto.ClientInfo;
import dto.message.GUIObserverType;
import dto.message.MessageObject;
import game.GameCardStack;
import game.GameProcess;
import resources.ResourceGetter;
import server.business.GameServer;
import server.business.exception.GameServerException;
import utilities.Converter;
import utilities.constants.GameConfigurationConstants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static server.gui.ServerGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:33
 */
@SuppressWarnings("FieldCanBeLocal")
public class ServerFrame extends JFrame implements Observer {
  private static final Logger LOGGER = Logger.getLogger(ServerFrame.class.getName());

  private JToolBar toolBar;
  private JButton startButton;
  private JButton stopButton;
  private JButton gameStartButton;
  private JButton closeButton;
  private JScrollPane settingsPanel;
  private JFormattedTextField portField;
  private JScrollPane clientListPanel;
  private JPanel statusPanel;
  private JLabel statusBar;

  private GameServer gameServer;
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
    ToolBarComponentAL listener = new ToolBarComponentAL();
    startButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_PLAY, TOOLTIP_START,
        ACTION_COMMAND_START, ALTERNATIVE_START, listener, KeyEvent.VK_G);
    stopButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_STOP_PLAYER, TOOLTIP_STOP,
        ACTION_COMMAND_STOP, ALTERNATIVE_STOP, listener, KeyEvent.VK_A);
    gameStartButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_PLAY, TOOLTIP_GAME_START,
        ACTION_COMMAND_GAME_START, ALTERNATIVE_GAME_START, listener, KeyEvent.VK_S);
    closeButton = WidgetCreator.makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE, TOOLTIP_CLOSE,
        ACTION_COMMAND_CLOSE, ALTERNATIVE_CLOSE, listener, KeyEvent.VK_Q);

    toolBar.setMargin(new Insets(5, 5, 5, 5));
    toolBar.setRollover(true);
    toolBar.setFloatable(false);

    toolBar.add(startButton);
    toolBar.addSeparator();
    toolBar.add(stopButton);
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

  /* Inner Classes */
  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        closeFrame(e);
      } else if (ACTION_COMMAND_START.equals(e.getActionCommand())) {
        startGameServer((Observer) SwingUtilities.getRoot((Component) e.getSource()));
      } else if (ACTION_COMMAND_STOP.equals(e.getActionCommand())) {
        stopGameServer();
      } else if (ACTION_COMMAND_GAME_START.equals(e.getActionCommand())) {
        startGame();
      }
    }

    private void startGame() {
      GameProcess process = GameProcess.getInstance();
      GameServer server = GameServer.getServerInstance();
      process.initialiseNewGame((Integer) stackSizeCombo.getSelectedItem());
      try {
        server.broadcastMessage(GUIObserverType.INITIALISE_STACK, Converter.toDTO(GameCardStack.getInstance()));
        server.broadcastArray(GUIObserverType.INITIALISE_CARDS, Converter.playersCardsToDTO(process.getPlayerList()));

        List<ClientInfo> clientInfoList = new ArrayList<ClientInfo>();
        DefaultListModel<ClientInfo> listModel = (DefaultListModel<ClientInfo>) clientList.getModel();
        for (int index = 0; index < listModel.getSize(); index++) {
          final ClientInfo client = listModel.get(index);
          client.setCardCount(GameConfigurationConstants.INITIAL_CARD_COUNT);
          clientInfoList.add(client);
        }
        for (ClientInfo clientInfo : clientInfoList) {
          System.out.println(clientInfo+" "+clientInfo.getCardCount());
        }
        server.broadcastMessage(GUIObserverType.INITIALISE_OPPONENTS, clientInfoList);
      } catch (GameServerException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
      }
    }

    private void stopGameServer() {
      try {
        GameServer.getServerInstance().shutdownServer();
      } catch (NotBoundException e) {
        LOGGER.info(e.getMessage());
      } catch (RemoteException e) {
        LOGGER.info(e.getMessage());
      }
      statusBar.setText(STATUS_SERVER_INACTIVE);
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
      try {
        gameServer.startServer();
        statusBar.setText(STATUS_SERVER_ACTIVE);
      } catch (IllegalAccessException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
      } catch (InstantiationException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
      } catch (RemoteException e) {
        LOGGER.log(Level.SEVERE, "Could not register services");
        e.printStackTrace();
      }
    }
  }
}
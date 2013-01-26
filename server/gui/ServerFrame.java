package server.gui;

import common.dto.DTOClient;
import common.dto.message.MessageObject;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;
import server.business.GUIObserverType;
import server.business.GameServer;
import server.business.GameServerException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static common.i18n.BundleStrings.*;
import static server.gui.ServerGUIConstants.LIST_WIDTH;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:33
 */
public class ServerFrame extends JFrame implements Observer {
  private static final String VERSION_NUMBER = "0.5";
  private static final String ACTION_COMMAND_START = "start"; //NON-NLS
  private static final String ACTION_COMMAND_STOP = "stop"; //NON-NLS
  private static final String ACTION_COMMAND_START_GAME = "gameStart"; //NON-NLS
  private static final String ACTION_COMMAND_STOP_GAME = "gameStop"; //NON-NLS

  private JToolBar toolbar;
  private JPanel panelSettings;
  private JFormattedTextField fieldPort;
  private JPasswordField fieldPassword;
  private JScrollPane panelClientList;
  private DurakStatusBar statusBar;

  private DefaultListModel<DTOClient> listModel;
  private JComboBox<Integer> comboStackSize;
  private JButton buttonGame;

  /* Constructors */
  public ServerFrame() {
    initComponents();

    GameServer.getServerInstance().addObserver(this);
    setTitle(MessageFormat.format("{0} - {1} {2}",
        I18nSupport.getValue(GUI_TITLE, "application.server"),
        I18nSupport.getValue(GUI_MISC, "version"), VERSION_NUMBER));
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        GameServer.getServerInstance().shutdownServer();
        setVisible(false);
        dispose();
        System.exit(0);
      }
    });
    pack();

    setBounds(FramePosition.createFramePositions(getWidth(), getHeight()).getRectangle());
  }

  /* Methods */
  private void initComponents() {
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(getToolbar(), BorderLayout.PAGE_START);
    getContentPane().add(getPanelClientList(), BorderLayout.LINE_START);
    getContentPane().add(getPanelSettings(), BorderLayout.CENTER);
    getContentPane().add(getStatusBar(), BorderLayout.PAGE_END);
  }

  public Integer getStackSize() {
    return (Integer) comboStackSize.getSelectedItem();
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handleUpdate(object);
  }

  public void refreshClientList(List<DTOClient> newClients) {
    listModel.clear();
    int spectating = 0;
    int playing = 0;
    for (DTOClient client : newClients) {
      if(client.spectating)
        spectating++;
      else playing++;
      listModel.add(listModel.size(), client);
    }
    statusBar.setPlayerCount(playing, spectating);
  }

  private void handleUpdate(MessageObject object) {
    if (GUIObserverType.CLIENT_LIST.equals(object.getType())) {
      refreshClientList(GameServer.getServerInstance().getClients());
    } else if (GUIObserverType.REMOVE_CLIENTS.equals(object.getType())) {
      listModel.clear();
    } else if(GUIObserverType.GAME_FINISHED.equals(object.getType())) {
      buttonGame.setAction(new GameStartStop(true));
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  private void removeClient(DTOClient client) {
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
    return Integer.parseInt(fieldPort.getText());
  }

  /* Getter and Setter */

  private JPanel getStatusBar() {
    if(statusBar != null)
      return statusBar;

    statusBar = new DurakStatusBar();

    statusBar.setText(I18nSupport.getValue(USER_MESSAGES, "status.server.inactive"));
    statusBar.setPreferredSize(new Dimension(0, 16));

    return statusBar;
  }

  private JToolBar getToolbar() {
    if(toolbar != null)
      return toolbar;

    toolbar = new JToolBar();
    JButton buttonServer = new JButton(new ServerStartStop(true));
    buttonGame = new JButton(new GameStartStop(true));

    toolbar.setMargin(new Insets(5, 5, 5, 5));
    toolbar.setRollover(true);
    toolbar.setFloatable(false);

    toolbar.add(buttonServer);
    toolbar.addSeparator();
    toolbar.add(buttonGame);

    return toolbar;
  }

  private JPanel getPanelSettings() {
    if(panelSettings != null)
      return panelSettings;

    panelSettings = new JPanel();

    panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.PAGE_AXIS));
    panelSettings.add(getServerSettingsPanel());
    panelSettings.add(getGameSettingsPanel());
    panelSettings.add(Box.createGlue());

    return panelSettings;
  }

  private JPanel getServerSettingsPanel() {
    final JPanel panel = new JPanel();

    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);

    fieldPort = new JFormattedTextField(format);
    fieldPort.setText(GameConfigurationConstants.DEFAULT_PORT.toString());

    fieldPassword = new JPasswordField();                                     //TODO SERVER_GUI und CLIENT_GUI zu GUI zusammenführen
    fieldPassword.setToolTipText(I18nSupport.getValue(GUI_COMPONENT, "tooltip.password"));

    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(GUI_TITLE, "server")));
    panel.add(getGridLinePanel(
        new JLabel(I18nSupport.getValue(GUI_COMPONENT, "text.port")), fieldPort));
    panel.add(getGridLinePanel(
        new JLabel(I18nSupport.getValue(GUI_COMPONENT, "text.password")), fieldPassword));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getGridLinePanel(Component... components) {
    final JPanel panel = new JPanel();

    panel.setLayout(new GridLayout(1,0));
    for (Component component : components)
      panel.add(component);

    return panel;
  }

  private JScrollPane getPanelClientList() {
    if(panelClientList != null)
      return panelClientList;

    panelClientList = new JScrollPane();
    listModel = new DefaultListModel<DTOClient>();
    JList<DTOClient> clientList = new JList<DTOClient>(listModel);
    clientList.setCellRenderer(new DefaultListCellRenderer());
    clientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    panelClientList.setPreferredSize(new Dimension(LIST_WIDTH, panelClientList.getPreferredSize().height));
    panelClientList.setViewportView(clientList);

    return panelClientList;
  }

  public JPanel getGameSettingsPanel() {
    final JPanel panel = new JPanel();

    comboStackSize = new JComboBox<Integer>(new Integer[]{12,36,40,44,48,52});
    comboStackSize.setEditable(false);
    comboStackSize.setToolTipText(I18nSupport.getValue(GUI_COMPONENT, "tooltip.card.number"));

    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(GUI_TITLE, "game")));
    panel.add(getGridLinePanel(
        new JLabel(I18nSupport.getValue(GUI_COMPONENT, "text.card.number")), comboStackSize));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  /* Inner Classes */

  private class ServerStartStop extends AbstractAction {
    private ServerStartStop(boolean start) {
      setAction(start);
    }

    private void setAction(boolean start) {
      if(start) {
        WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_S, ACTION_COMMAND_START,
            "", I18nSupport.getValue(GUI_COMPONENT, "tooltip.start.server"),
            ResourceGetter.getToolbarIcon("toolbar.play"));
      } else {
        WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_S, ACTION_COMMAND_STOP,
            "", I18nSupport.getValue(GUI_COMPONENT, "tooltip.stop.server"),
            ResourceGetter.getToolbarIcon("toolbar.stop.player"));
      }
    }

    public void actionPerformed(ActionEvent e) {
      if (ACTION_COMMAND_START.equals(e.getActionCommand())) {
        startGameServer();
        setAction(false);
      } else if (ACTION_COMMAND_STOP.equals(e.getActionCommand())) {
        GameServer.getServerInstance().shutdownServer();
        setStatusBarText(I18nSupport.getValue(USER_MESSAGES, "status.server.inactive"));
        buttonGame.setAction(new GameStartStop(true));
        setAction(true);
      }
    }

    private void startGameServer() {
      final GameServer gameServer = GameServer.getServerInstance(getPortValue());

      try {
        gameServer.startServer(String.copyValueOf(fieldPassword.getPassword()));
        setStatusBarText(I18nSupport.getValue(USER_MESSAGES, "status.server.running"));
      } catch (GameServerException e) {
        setStatusBarText(e.getMessage());
      }
    }
  }

  private class GameStartStop extends AbstractAction {
    private GameStartStop(boolean start) {
      setAction(start);
    }

    public void actionPerformed(ActionEvent e) {
      if (ACTION_COMMAND_START_GAME.equals(e.getActionCommand())) {
        if(GameServer.getServerInstance().startGame(getStackSize()))
          setAction(false);
      } else if(ACTION_COMMAND_STOP_GAME.equals(e.getActionCommand())) {
        GameServer.getServerInstance().stopGame(true,
            I18nSupport.getValue(USER_MESSAGES, "game.abort.server"));
        setAction(true);
      }
    }

    private void setAction(boolean start) {
      if(start) {
        WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_G, ACTION_COMMAND_START_GAME,
            "", I18nSupport.getValue(GUI_ACTION, "tooltip.start.game"),
            ResourceGetter.getToolbarIcon("toolbar.game.start"));
      } else {
        WidgetCreator.initialiseAction(this, null, null, KeyEvent.VK_G, ACTION_COMMAND_STOP_GAME,
            "", I18nSupport.getValue(GUI_ACTION, "tooltip.stop.game"),
            ResourceGetter.getToolbarIcon("toolbar.game.stop"));
      }
    }
  }
}
package client.gui.frame;

import client.business.client.GameClient;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.gamePanel.GamePanel;
import client.gui.frame.setup.SetUpFrame;
import dto.ClientInfo;
import dto.DTOCard;
import dto.DTOCardStack;
import dto.message.*;
import resources.ResourceGetter;
import utilities.Miscellaneous;
import utilities.constants.GameConfigurationConstants;
import utilities.gui.Constraints;
import utilities.gui.FramePosition;
import utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.*;
import static utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal", "unchecked"})
public class ClientFrame extends JFrame implements Observer {
  private static final Logger LOGGER = Logger.getLogger(ClientFrame.class.getName());

  private JPanel secondPane;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel gamePanel;
  private DurakStatusBar statusBar;
  private DurakToolBar toolBar;
  private JPanel stackClientsPanel;
  private JList<ClientInfo> clientsList;

  private ClientFrameMessageHandler handler;
  private JButton roundDoneButton;
  private JButton takeCardsButton;
  private static ClientFramePopup rulePopup;

  /* Constructors */
  public ClientFrame() {
    final FramePosition position = FramePosition.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);
    GameClient.getClient().addObserver(this);

    rulePopup = new ClientFramePopup();
    handler = new ClientFrameMessageHandler(this);
    setIconImages(ResourceGetter.getApplicationIcons());
    setTitle(APPLICATION_NAME + TITLE_SEPARATOR + VERSION);
    setBounds(position.getRectangle());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initComponents();
    updateStatusBar();
  }

  /* Methods */
  public void showGameOverMessage() {
    final ClientInfo ownClient = SetUpFrame.getInstance().getClientInfo();
    final ClientFrame frame = this;
    new Thread(new Runnable() {
      public void run() {
        new UserMessageDistributor(frame).gameOverMessage(ownClient.playerType);
      }
    }).start();
  }

  public static void showRuleException(Component parent, String ruleException) {
    final ClientFrame frame = (ClientFrame) SwingUtilities.getRoot(parent);
    Rectangle bounds = frame.getBounds();
    rulePopup.setText(ruleException);
    rulePopup.setSize(rulePopup.getPrefferedSize());
    rulePopup.setLocation(bounds.x + bounds.width - rulePopup.getWidth() - 20,
        bounds.y + bounds.height - rulePopup.getHeight() - 20);
    rulePopup.setVisible(true);

    new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep(5000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        rulePopup.setVisible(false);
        rulePopup.dispose();
      }
    }).start();
  }

  private void initComponents() {
    toolBar = new DurakToolBar(this);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(getSecondPane(), BorderLayout.CENTER);
  }

  public void setStatusBarText(String mainText, Boolean connected, String serverAddress) {
    statusBar.setConnected(connected, serverAddress);
    statusBar.setText(mainText);
    statusBar.setPlayerType(SetUpFrame.getInstance().getClientInfo().playerType);
  }

  public void updateStatusBar() {
    statusBar.setConnected(GameClient.getClient().isConnected());
    statusBar.setText("");
    statusBar.setPlayerType(SetUpFrame.getInstance().getClientInfo().playerType);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handler.handleUpdate(object);
  }

  public void clearClientList() {
    ((DefaultListModel<ClientInfo>) clientsList.getModel()).removeAllElements();
  }

  public void clearGameCards() {
    gamePanel.deleteCards();
    opponentsPanel.deleteCards();
    cardStackPanel.deleteCards();
  }

  public void updateClientList(List<ClientInfo> clients) {
    final DefaultListModel<ClientInfo> listModel =
        ((DefaultListModel<ClientInfo>) clientsList.getModel());
    final ClientInfo ownInfo = SetUpFrame.getInstance().getClientInfo();

    listModel.clear();

    for (ClientInfo client : clients) {
      if(ownInfo.isEqual(client))
        ownInfo.setClientInfo(client);
      else listModel.add(listModel.size(), client);
    }
  }

  public void updateClientCards(List<DTOCard> clientCards) {
    gamePanel.placeClientCards(clientCards);
  }

  public void updateStack(DTOCardStack cardStack) {
    cardStackPanel.updateStack(cardStack);
  }

  public void updatePlayers(List<ClientInfo> clients) {
    final ClientInfo ownInfo = SetUpFrame.getInstance().getClientInfo();
    for (ClientInfo client : clients) {
      if (ownInfo.isEqual(client) && !ownInfo.playerType.equals(client.playerType)) {
        ownInfo.setClientInfo(client);
        gamePanel.setListenerType(ownInfo.playerType);
      }
    }
    opponentsPanel.updateOpponents(clients);
  }

  public void initialisePlayers(List<ClientInfo> clients) {
    opponentsPanel.removeAll();
    final ClientInfo ownInfo = SetUpFrame.getInstance().getClientInfo();
    for (ClientInfo client : clients) {
      if(!ownInfo.isEqual(client))
        opponentsPanel.addOpponent(client);
      else {
        ownInfo.setClientInfo(client);
        gamePanel.setListenerType(ownInfo.playerType);
      }
    }
    opponentsPanel.updateOpponents(clients);
  }

  public void updateInGameCards(List<DTOCard> attackerCards, List<DTOCard> defenderCards) {
    gamePanel.placeInGameCards(attackerCards, defenderCards);
  }

  public void enableButtons(Boolean roundFinished) {
    final Boolean take;
    final Boolean round;
    final PlayerType playerType = SetUpFrame.getInstance().getClientInfo().playerType;
    final Boolean cardsOnTable = gamePanel.hasInGameCards();
    if(playerType.equals(PlayerType.FIRST_ATTACKER) ||
       playerType.equals(PlayerType.SECOND_ATTACKER)) {
      take = false;
      round = !roundFinished;
    } else if (playerType.equals(PlayerType.DEFENDER)) {
      take = true;
      round = roundFinished && gamePanel.inGameCardsAreCovered();
    } else {
      take = false;
      round = false;
    }
    takeCardsButton.setEnabled(take && cardsOnTable);
    roundDoneButton.setEnabled(round && cardsOnTable);
  }

  /* Getter and Setter */
  private JPanel getSecondPane() {
    if(secondPane != null)
      return secondPane;

    secondPane = new JPanel();
    gamePanel = new GamePanel();

    secondPane.setLayout(new BorderLayout());
    secondPane.add(getOpponentButtonPanel(), BorderLayout.PAGE_START);
    secondPane.add(getStackClientsPanel(), BorderLayout.LINE_START);
    secondPane.add(gamePanel, BorderLayout.CENTER);
    secondPane.add(getStatusPanel(), BorderLayout.PAGE_END);

    return secondPane;
  }

  private JPanel getOpponentButtonPanel() {
    final JPanel panel = new JPanel();
    final JPanel buttonPanel = getButtonPanel();
    opponentsPanel = new OpponentsPanel();

    buttonPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, OPPONENT_PANEL_HEIGHT));
    buttonPanel.setMaximumSize(new Dimension(CARD_STACK_PANEL_WIDTH, Integer.MAX_VALUE));
    panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
    panel.add(buttonPanel);
    panel.add(opponentsPanel);

    return panel;
  }

  private DurakStatusBar getStatusPanel() {
    if(statusBar != null)
      return statusBar;

    statusBar = new DurakStatusBar();
    statusBar.setPreferredSize(new Dimension(0, STATUS_BAR_HEIGHT));

    return statusBar;
  }

  private JPanel getStackClientsPanel() {
    if(stackClientsPanel != null)
      return stackClientsPanel;

    stackClientsPanel = new JPanel();
    cardStackPanel = new CardStackPanel();

    clientsList = new JList<ClientInfo>(new DefaultListModel<ClientInfo>());
    JPanel listPanel = new JPanel();
    JScrollPane listScroll = new JScrollPane(clientsList);

    cardStackPanel.setLayout(new BorderLayout());
    cardStackPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, cardStackPanel.getPreferredSize().height));
    cardStackPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_START);
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_END);

    clientsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    clientsList.setCellRenderer(new ClientInfoCellRenderer());
    listPanel.setLayout(new GridLayout());
    listPanel.setBorder(BorderFactory.createTitledBorder("Mitspieler"));
    listPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, 100));
    listPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, listPanel.getPreferredSize().height));
    listPanel.add(listScroll);

    stackClientsPanel.setLayout(new BoxLayout(stackClientsPanel, BoxLayout.PAGE_AXIS));
    stackClientsPanel.add(cardStackPanel);
    stackClientsPanel.add(listPanel);

    return stackClientsPanel;
  }

  private JPanel getButtonPanel() {
    final JPanel panel = new JPanel();
    final ActionListener listener = new GameButtonListener();
    takeCardsButton = WidgetCreator.makeButton(null, BUTTON_TEXT_TAKE_CARDS,
        "Karten nehmen", ACTION_COMMAND_TAKE_CARDS, listener);
    roundDoneButton = WidgetCreator.makeButton(null, BUTTON_TEXT_ROUND_DONE,
        "Runde beenden", ACTION_COMMAND_ROUND_DONE, listener);
    takeCardsButton.setEnabled(false);
    roundDoneButton.setEnabled(false);

    panel.setBackground(GAME_TABLE_COLOUR);
    panel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = Constraints.getDefaultFieldConstraintLeft(0,0,1,1);
    constraints.weighty = 1.0;
    panel.add(takeCardsButton, constraints);
    constraints = Constraints.getDefaultFieldConstraintLeft(0,1,1,1);
    constraints.weighty = 1.0;
    panel.add(roundDoneButton, constraints);

    return panel;
  }

  /* Inner Classes */
  private class ClientInfoCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component superComponent = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

      if(value ==null)
        return this;

      final ClientInfo client = (ClientInfo) value;
      final Color foreground;
      if(client.loginNumber >= GameConfigurationConstants.SPECTATOR_START_NUMBER) {
        foreground = new Color(164, 164, 164);
        this.setToolTipText("Zuschauer");
      } else {
        foreground = superComponent.getForeground();
        this.setToolTipText(null);
      }
      this.setText(client.toString());
      this.setBackground(superComponent.getBackground());
      this.setForeground(foreground);

      return this ;
    }
  }

  private class GameButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      final ClientInfo clientInfo = SetUpFrame.getInstance().getClientInfo();
      Boolean takeCards = null;
      if(e.getActionCommand().equals(ACTION_COMMAND_TAKE_CARDS)) {
        takeCards = true;
      } else if(e.getActionCommand().equals(ACTION_COMMAND_ROUND_DONE)) {
        takeCards = false;
      }

      if(nextRoundRequest(clientInfo, takeCards)) {
        updateInGameCards(null,null);
      }
    }

    private Boolean nextRoundRequest(ClientInfo clientInfo, Boolean takeCards) {
      if(takeCards == null)
        return false;

      try {
        return GameClient.getClient().finishRound(clientInfo, takeCards);
      } catch (RemoteException ex) {
        JOptionPane.showMessageDialog(null, "Die Verbindung zum Server wurde unterbrochen!");
      }

      return false;
    }
  }
}

@SuppressWarnings("unchecked")
class ClientFrameMessageHandler {
  private static final Logger LOGGER = Logger.getLogger(ClientFrameMessageHandler.class.getName());
  private ClientFrame frame;

  ClientFrameMessageHandler(ClientFrame frame) {
    this.frame = frame;
  }

  void handleUpdate(MessageObject object) {
    if(object == null)
      return;

    final Class<? extends Enum> enumClass = object.getType().getClass();
    if(enumClass.equals(BroadcastType.class)) {
      handleBroadcastType(object);
    } else if(enumClass.equals(GameUpdateType.class)) {
      handleGameUpdateType(object);
    } else if(enumClass.equals(MessageType.class)) {
      handleMessageType(object);
    }
  }

  private void handleMessageType(MessageObject object) {
    if(MessageType.LOGIN_NUMBER.equals(object.getType())) {
      SetUpFrame.getInstance().getClientInfo().setClientInfo((ClientInfo) object.getSendingObject());
    }
  }

  private void handleBroadcastType(MessageObject object) {
    if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
      ChatFrame.getFrame().addMessage(buildChatAnswer(object));
    } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
      final List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      frame.updateClientList(clients);
    } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
      disconnectClient();
      frame.clearGameCards();
    }
  }

  private void handleGameUpdateType(MessageObject object) {
    if(GameUpdateType.INITIALISE_PLAYERS.equals(object.getType())) {
      final List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      frame.initialisePlayers(clients);
      frame.updateStatusBar();
    } else if(GameUpdateType.PLAYERS_UPDATE.equals(object.getType())) {
      final List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      frame.updatePlayers(clients);
      frame.updateStatusBar();
    } else if(GameUpdateType.STACK_UPDATE.equals(object.getType())) {
      frame.updateStack((DTOCardStack) object.getSendingObject());
    } else if(GameUpdateType.INGAME_CARDS.equals(object.getType())) {
      final List<List<DTOCard>> cardLists = (List<List<DTOCard>>) object.getSendingObject();
      guiInGameCardsUpdate(cardLists);
    } else if(GameUpdateType.NEXT_ROUND_AVAILABLE.equals(object.getType())) {
      frame.enableButtons((Boolean) object.getSendingObject());
    } else if(GameUpdateType.CLIENT_CARDS.equals(object.getType())) {
      frame.updateClientCards((List<DTOCard>) object.getSendingObject());
    } else if(GameUpdateType.GAME_FINISHED.equals(object.getType())) {
      frame.showGameOverMessage();
    }
  }

  private void guiInGameCardsUpdate(List<List<DTOCard>> cardLists) {
    List<DTOCard> attackerCards = null;
    List<DTOCard> defenderCards = null;

    if(cardLists != null) {
      if (cardLists.size() == 2) {
        attackerCards = cardLists.get(0);
        defenderCards = cardLists.get(1);
      } else {
        JOptionPane.showMessageDialog(frame, "Ein Fehler im Server ist aufgetreten",
            "Fehler", JOptionPane.ERROR_MESSAGE);
        LOGGER.severe("Server sends the wrong format for the client!");
      }
    }

    frame.updateInGameCards(attackerCards, defenderCards);
  }

  private void disconnectClient() {
    try {
      GameClient.getClient().disconnect(SetUpFrame.getInstance().getClientInfo());
    } catch (NotBoundException e) {
      LOGGER.warning(e.getMessage());
    } catch (RemoteException e) {
      LOGGER.warning(e.getMessage());
    }
    frame.setStatusBarText("", false, "");
  }

  private String buildChatAnswer(MessageObject object) {
    final ChatMessage chatMessage = (ChatMessage) object.getSendingObject();
    String message = Miscellaneous.getChatMessage(chatMessage.getSender().name,
        chatMessage.getMessage());
    if(chatMessage.getSender().isEqual(SetUpFrame.getInstance().getClientInfo()))
      message = Miscellaneous.changeChatMessageInBrackets("ich", message);
    return message;
  }
}

class UserMessageDistributor {
  private static final Logger LOGGER = Logger.getLogger(UserMessageDistributor.class.getName());
  private JFrame frame;

  UserMessageDistributor(JFrame frame) {
    this.frame = frame;
  }

  void gameOverMessage(PlayerType type) {
    if(type.equals(PlayerType.NOT_LOSER)) {
      showNotLoserOption();
    } else if(type.equals(PlayerType.LOSER)) {
      showLoserOption();
    } else {
      showNoPlayerOption();
    }
  }

  private void showNotLoserOption() {
    final String message = "<html>Sooo, das Spiel ist zu Ende! Wie siehts aus?" +
        "<p/>M\u00f6chtest du den anderen weiterhin zeigen,<p/>wie man dieses Spiel spielt?</html>";
    final Object[] strings =
        new Object[]{"<html>Ja klar,<p/>denen zeig ichs!</html>", "Nein, danke!"};
    int option = JOptionPane.showOptionDialog(frame, message, "Das Spiel ist vorbei",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      reconnect(true);
    }
  }

  private void showLoserOption() {
    final String message = "<html>Sooo, das Spiel ist zu Ende! Wie siehts aus?" +
        "<p/>M\u00f6chtest du den anderen beweisen," +
        "<p/>wer hier der wahre Durak ist?</html>";
    final Object[] strings =
        new Object[]{"<html>Wie hast du mich genannt?<p/>Revanche!</html>", "<html>Nein, ich bin ein kleines," +
        "<p/>\u00e4ngstliches Kind und will meinen<p/>Teletubbie wieder haben!</html>"};
    int option = JOptionPane.showOptionDialog(frame, message, "Das Spiel ist vorbei",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      reconnect(true);
    }
  }

  private void showNoPlayerOption() {
    final String message = "<html>Soooo, das Spiel ist nun zu Ende! Wie siehts aus?" +
        "<p/>M\u00f6chtest du diesmal nicht nur zuschauen," +
        "<p/>sondern auch mitspielen?</html>";
    final Object[] strings =
        new Object[]{"<html>Ja,<p/>ich m\u00f6chte mitspielen!</html>", "<html>Nein,<p/>ich m\u00f6chte nur beobachten!</html>"};
    int option = JOptionPane.showOptionDialog(frame, message, "Das Spiel ist vorbei",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[1]);
    if(option == 0) {
      reconnect(false);
    }
  }

  private void reconnect(boolean spectate) {
    try {
      final SetUpFrame setup = SetUpFrame.getInstance();
      final GameClient client = GameClient.getClient();
      final ClientInfo info = setup.getClientInfo();
      info.spectating = spectate;
      setup.updateClientInfo();
      client.disconnect(info);
      client.connect(info, setup.getConnectionInfo().getPassword());
    } catch (RemoteException e) {
      LOGGER.severe(e.getMessage());
    } catch (NotBoundException e) {
      LOGGER.severe(e.getMessage());
    } catch (ServerNotActiveException e) {
      LOGGER.severe(e.getMessage());
    }
  }
}
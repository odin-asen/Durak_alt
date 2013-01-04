package client.gui.frame;

import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.gamePanel.GamePanel;
import client.gui.frame.setup.SetupFrame;
import common.dto.ClientInfo;
import common.dto.DTOCard;
import common.dto.DTOCardStack;
import common.dto.message.*;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.Miscellaneous;
import common.utilities.gui.DurakPopup;
import common.utilities.gui.Constraints;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.*;
import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
public class ClientFrame extends JFrame implements Observer {
  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
  
  private static final String VERSION_NUMBER = "0.1";
  private static final String ACTION_COMMAND_TAKE_CARDS = "takeCards"; //NON-NLS
  private static final String ACTION_COMMAND_ROUND_DONE = "roundDone"; //NON-NLS

  private JPanel secondPane;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel gamePanel;
  private DurakStatusBar statusBar;
  private JPanel stackClientsPanel;
  private JList<ClientInfo> clientsList;

  private ClientFrameMessageHandler handler;
  private JButton roundDoneButton;
  private JButton takeCardsButton;
  private DurakToolBar toolBar;

  /* Constructors */

  public ClientFrame() {
    final FramePosition position = FramePosition.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);
    GameClient.getClient().addObserver(this);

    handler = new ClientFrameMessageHandler(this);
    setIconImages(ResourceGetter.getApplicationIcons());
    setTitle(MessageFormat.format("{0} - {1} {2}", I18nSupport.getValue(BUNDLE_NAME,"application.title"),
        I18nSupport.getValue(BUNDLE_NAME,"version"), VERSION_NUMBER));
    setBounds(position.getRectangle());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initComponents();
    updateStatusBar();
  }

  /* Methods */

  public void showGameOverMessage() {
    final ClientInfo ownClient = SetupFrame.getInstance().getClientInfo();
    final ClientFrame frame = this;
    new Thread(new Runnable() {
      public void run() {
        new UserMessageDistributor(frame).gameOverMessage(ownClient.playerType);
      }
    }).start();
  }

  public static void showRuleException(Component parent, String ruleException) {
    final ClientFrame frame = (ClientFrame) SwingUtilities.getRoot(parent);
    final Rectangle bounds = frame.getBounds();
    final DurakPopup rulePopup = WidgetCreator.createPopup(
        ClientGUIConstants.GAME_TABLE_COLOUR, ruleException, bounds, 3);
    rulePopup.setVisible(true);
  }

  private void initComponents() {
    toolBar = new DurakToolBar(this);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(getSecondPane(), BorderLayout.CENTER);
  }

  /**
   * Sets the status bar's main text, server address field and indicates if a connection
   * consists to the server. The boolean parameter also indicates the representation of the
   * gui, e.g. the picture of the connection toolbar button, the picture in the status bar,
   * etc...
   * @param mainText Text to set to the status bar.
   * @param connected Indicates whether the client is connected or not.
   * @param serverAddress Shows the server address as tooltip.
   */
  public void setStatus(String mainText, Boolean connected, String serverAddress) {
    statusBar.setConnected(connected, serverAddress);
    statusBar.setText(mainText);
    statusBar.setPlayerType(SetupFrame.getInstance().getClientInfo().playerType);
    toolBar.setConnection(connected);
  }

  public void updateStatusBar() {
    statusBar.setConnected(GameClient.getClient().isConnected());
    statusBar.setText("");
    statusBar.setPlayerType(SetupFrame.getInstance().getClientInfo().playerType);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handler.handleUpdate(object);
  }

  /**
   * Empties the client list, the opponent card widgets and ...?
   */
  public void clearClients() {
    ((DefaultListModel<ClientInfo>) clientsList.getModel()).removeAllElements();
    opponentsPanel.removeAllOpponents();
  }

  public void clearGameCards() {
    gamePanel.deleteCards();
    opponentsPanel.removeAllOpponents();
    cardStackPanel.deleteCards();
  }

  public void updateClientList(List<ClientInfo> clients) {
    final DefaultListModel<ClientInfo> listModel =
        ((DefaultListModel<ClientInfo>) clientsList.getModel());
    final ClientInfo ownInfo = SetupFrame.getInstance().getClientInfo();

    listModel.clear();
    for (ClientInfo client : clients) {
      listModel.add(listModel.size(), client);
    }
  }

  public void updateClientCards(List<DTOCard> clientCards) {
    gamePanel.placeClientCards(clientCards);
  }

  public void updateStack(DTOCardStack cardStack) {
    cardStackPanel.updateStack(cardStack);
  }

  public void updateOpponents(List<ClientInfo> clients) {
    opponentsPanel.updateOpponents(clients);
  }

  public void initialisePlayers(List<ClientInfo> clients) {
    opponentsPanel.removeAll();
    for (ClientInfo client : clients) {
      opponentsPanel.addOpponent(client);
    }
    opponentsPanel.updateOpponents(clients);
  }

  public void updateInGameCards(List<DTOCard> attackerCards, List<DTOCard> defenderCards) {
    gamePanel.placeInGameCards(attackerCards, defenderCards);
  }

  public void enableButtons(Boolean roundFinished) {
    final Boolean take;
    final Boolean round;
    final PlayerType playerType = SetupFrame.getInstance().getClientInfo().playerType;
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

  public void updateSubComponents() {
    gamePanel.setListenerType(SetupFrame.getInstance().getClientInfo().playerType);
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
    final JPanel listPanel = new JPanel();
    final JScrollPane listScroll = new JScrollPane(clientsList);

    cardStackPanel.setLayout(new BorderLayout());
    cardStackPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, cardStackPanel.getPreferredSize().height));
    cardStackPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_START);
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_END);

    clientsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    clientsList.setCellRenderer(new ClientInfoCellRenderer());
    listPanel.setLayout(new GridLayout());
    listPanel.setBorder(BorderFactory.createTitledBorder(I18nSupport.getValue(BUNDLE_NAME,"border.title.opponents")));
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
    takeCardsButton = WidgetCreator.makeButton(null, I18nSupport.getValue(BUNDLE_NAME,"button.text.take.cards"),
        I18nSupport.getValue(BUNDLE_NAME,"button.tooltip.take.cards"), ACTION_COMMAND_TAKE_CARDS,
        listener);
    roundDoneButton = WidgetCreator.makeButton(null, I18nSupport.getValue(BUNDLE_NAME,"button.text.finish.round"),
        I18nSupport.getValue(BUNDLE_NAME,"button.tooltip.finish.round"), ACTION_COMMAND_ROUND_DONE,
        listener);
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
      if(client.spectating) {
        foreground = new Color(164, 164, 164);
        this.setToolTipText(I18nSupport.getValue(BUNDLE_NAME,"list.tooltip.audience"));
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
      final ClientInfo clientInfo = SetupFrame.getInstance().getClientInfo();
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
        JOptionPane.showMessageDialog(null, I18nSupport.getValue(BUNDLE_NAME,"dialog.text.error.lost.connection"));
      }

      return false;
    }
  }
}

@SuppressWarnings("unchecked")
class ClientFrameMessageHandler {
  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
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
    if(MessageType.OWN_CLIENT_INFO.equals(object.getType())) {
      final ClientInfo info = SetupFrame.getInstance().getClientInfo();
      info.setClientInfo((ClientInfo) object.getSendingObject());
      frame.updateSubComponents();
    }
  }

  private void handleBroadcastType(MessageObject object) {
    if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
      ChatFrame.getFrame().addMessage(buildChatAnswer(object));
    } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
      final List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      frame.updateClientList(clients);
    } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
      frame.clearClients();
      frame.clearGameCards();
      frame.setStatus("Der Server wurde geschlossen!", false, "");
    }
  }

  private void handleGameUpdateType(MessageObject object) {
    if(GameUpdateType.INITIALISE_PLAYERS.equals(object.getType())) {
      final List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      frame.initialisePlayers(clients);
      frame.updateStatusBar();
    } else if(GameUpdateType.PLAYERS_UPDATE.equals(object.getType())) {
      final List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      frame.updateOpponents(clients);
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
        JOptionPane.showMessageDialog(frame, I18nSupport.getValue(BUNDLE_NAME,"dialog.text.error.server.error"),
            I18nSupport.getValue(BUNDLE_NAME,"dialog.title.error"), JOptionPane.ERROR_MESSAGE);
        LOGGER.severe("Server sends the wrong format for the client!");
      }
    }

    frame.updateInGameCards(attackerCards, defenderCards);
  }

  private String buildChatAnswer(MessageObject object) {
    final ChatMessage chatMessage = (ChatMessage) object.getSendingObject();
    String message = Miscellaneous.getChatMessage(chatMessage.getSender().name,
        chatMessage.getMessage());
    return message;
  }
}

class UserMessageDistributor {
  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
  private static final Logger LOGGER = Logger.getLogger(UserMessageDistributor.class.getName());

  private ClientFrame frame;

  UserMessageDistributor(ClientFrame frame) {
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
    final String message = I18nSupport.getValue(BUNDLE_NAME,"dialog.text.game.finished.not.loser");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(BUNDLE_NAME,"dialog.button.play.again"), I18nSupport.getValue(BUNDLE_NAME,"dialog.button.option.no")};
    int option = JOptionPane.showOptionDialog(frame, message, I18nSupport.getValue(BUNDLE_NAME,"dialog.title.game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      reconnect(true);
    }
  }

  private void showLoserOption() {
    final String message = I18nSupport.getValue(BUNDLE_NAME,"dialog.text.game.finished.loser");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(BUNDLE_NAME,"dialog.button.play.again.revenge"), I18nSupport.getValue(BUNDLE_NAME,"dialog.button.option.no")};
    int option = JOptionPane.showOptionDialog(frame, message, I18nSupport.getValue(BUNDLE_NAME,"dialog.title.game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      reconnect(true);
    }
  }

  private void showNoPlayerOption() {
    final String message = I18nSupport.getValue(BUNDLE_NAME,"dialog.text.game.finished.no.player");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(BUNDLE_NAME,"dialog.button.join.game"), I18nSupport.getValue(BUNDLE_NAME,"dialog.button.option.no")};
    int option = JOptionPane.showOptionDialog(frame, message, I18nSupport.getValue(BUNDLE_NAME,"dialog.title.game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[1]);
    if(option == 0) {
      reconnect(false);
    }
  }

  private void reconnect(boolean spectate) {
    try {
      final SetupFrame setup = SetupFrame.getInstance();
      final GameClient client = GameClient.getClient();
      final ClientInfo info = setup.getClientInfo();
      info.spectating = spectate;
      setup.updateClientInfo();
      client.disconnect(info);
      client.connect(info, setup.getConnectionInfo().getPassword());
    } catch (GameClientException e) {
      frame.setStatus(e.getMessage(), false, "");
      LOGGER.severe(e.getMessage());
    }
  }
}
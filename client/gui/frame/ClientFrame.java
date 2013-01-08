package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.Client;
import client.business.client.GameClient;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.gamePanel.GamePanel;
import common.dto.DTOCard;
import common.dto.DTOCardStack;
import common.dto.DTOClient;
import common.dto.message.*;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.simon.action.FinishAction;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.gui.Constraints;
import common.utilities.gui.DurakPopup;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
  private static ClientFrame frameInstance;

  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
  
  private static final String VERSION_NUMBER = "0.2";
  private static final String ACTION_COMMAND_TAKE_CARDS = "takeCards"; //NON-NLS
  private static final String ACTION_COMMAND_ROUND_DONE = "roundDone"; //NON-NLS

  private JPanel secondPane;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel gamePanel;
  private DurakStatusBar statusBar;
  private JPanel stackClientsPanel;
  private JList<DTOClient> clientsList;

  private ClientFrameMessageHandler handler;
  private JButton roundDoneButton;
  private JButton takeCardsButton;
  private DurakToolBar toolBar;

  /* Constructors */

  private ClientFrame() {
    handler = new ClientFrameMessageHandler();
    final FramePosition position = FramePosition.createFramePositions(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);

    setIconImages(ResourceGetter.getApplicationIcons());
    setTitle(MessageFormat.format("{0} - {1} {2}",
        I18nSupport.getValue(BUNDLE_NAME,"application.title"),
        I18nSupport.getValue(BUNDLE_NAME,"version"), VERSION_NUMBER));
    setBounds(position.getRectangle());
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
  }

  public static ClientFrame getInstance() {
    if(frameInstance == null) {
      frameInstance = new ClientFrame();
      GameClient.getClient().addObserver(frameInstance);
    }

    return frameInstance;
  }

  /* Methods */

  public void showGameOverMessage() {
    final PlayerType type = Client.getOwnInstance().getPlayerType();
    new Thread(new Runnable() {
      public void run() {
        new UserMessageDistributor().gameOverMessage(type);
      }
    }).start();
  }

  public static void showRuleException(Component parent, Object ruleException) {
    final Rectangle bounds = frameInstance.getBounds();
    final DurakPopup rulePopup = WidgetCreator.createPopup(
        ClientGUIConstants.GAME_TABLE_COLOUR, ruleException.toString(), bounds, 3);
    rulePopup.setVisible(true);
  }

  /**
   * Initialises the frame. Shoudl be called at least after the first cration of the frame
   * object.
   */
  public void init() {
    toolBar = new DurakToolBar();

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(getSecondPane(), BorderLayout.CENTER);
    updateStatusBar();
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
    statusBar.setPlayerType(Client.getOwnInstance().getPlayerType());
    toolBar.setConnection(connected);
  }

  public void updateStatusBar() {
    statusBar.setConnected(GameClient.getClient().isConnected());
    statusBar.setText("");
    statusBar.setPlayerType(Client.getOwnInstance().getPlayerType());
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handler.handleUpdate(object);
  }

  /**
   * Empties the client list, the opponent card widgets and ...?
   */
  public void clearClients() {
    ((DefaultListModel<DTOClient>) clientsList.getModel()).removeAllElements();
    opponentsPanel.removeAllOpponents();
  }

  public void clearGameCards() {
    gamePanel.deleteCards();
    opponentsPanel.removeAllOpponents();
    cardStackPanel.deleteCards();
  }

  public void updateClientList(List<DTOClient> clients) {
    final DefaultListModel<DTOClient> listModel =
        ((DefaultListModel<DTOClient>) clientsList.getModel());
    final DTOClient ownInfo = Client.getOwnInstance().toDTO();

    listModel.clear();
    for (DTOClient client : clients) {
      listModel.add(listModel.size(), client);
    }
  }

  public void updateClientCards(List<DTOCard> clientCards) {
    gamePanel.placeClientCards(clientCards);
  }

  public void updateStack(DTOCardStack cardStack) {
    cardStackPanel.updateStack(cardStack);
  }

  public void updateOpponents(List<DTOClient> clients) {
    opponentsPanel.updateOpponents(clients);
  }

  public void initialisePlayers(List<DTOClient> clients) {
    opponentsPanel.removeAll();
    for (DTOClient client : clients) {
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
    final PlayerType playerType = Client.getOwnInstance().getPlayerType();
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

  /* Updates the representation and behavior of subcomponents like the card moving
   * at the game panel. */
  public void updateSubComponents() {
    gamePanel.setListenerType(Client.getOwnInstance().getPlayerType());
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

    clientsList = new JList<DTOClient>(new DefaultListModel<DTOClient>());
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

      final DTOClient client = (DTOClient) value;
      final Color foreground;
      if(client.spectating) {
        foreground = new Color(164, 164, 164);
        this.setToolTipText(I18nSupport.getValue(BUNDLE_NAME,"list.tooltip.audience"));
      } else {
        foreground = superComponent.getForeground();
        this.setToolTipText(null);
      }
      this.setText(client.name);
      this.setBackground(superComponent.getBackground());
      this.setForeground(foreground);

      return this ;
    }
  }

  private class GameButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      final DTOClient clientInfo = Client.getOwnInstance().toDTO();
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

    private Boolean nextRoundRequest(DTOClient dtoClient, Boolean takeCards) {
      if(takeCards == null)
        return false;

      FinishAction.FinishType type = FinishAction.FinishType.GO_TO_NEXT_ROUND;
      if(dtoClient.playerType.equals(PlayerType.DEFENDER)) {
        if(takeCards)
          type = FinishAction.FinishType.TAKE_CARDS;
        return GameClient.getClient().finishRound(dtoClient, type);
      } else if(dtoClient.playerType.equals(PlayerType.FIRST_ATTACKER) ||
          dtoClient.playerType.equals(PlayerType.SECOND_ATTACKER))
        return GameClient.getClient().finishRound(dtoClient, type);

      return false;
    }
  }
}

@SuppressWarnings("unchecked")
class ClientFrameMessageHandler {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS

  private static final Logger LOGGER = LoggingUtility.getLogger(ClientFrameMessageHandler.class.getName());

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
      final Client client = Client.getOwnInstance();
      client.setClientInfo((DTOClient) object.getSendingObject());
      ClientFrame.getInstance().updateSubComponents();
    } else if(MessageType.RULE_MESSAGE.equals(object.getType())) {
      ClientFrame.showRuleException(ClientFrame.getInstance(), object.getSendingObject());
    } else if(MessageType.STATUS_MESSAGE.equals(object.getType())) {
      ClientFrame.getInstance().setStatus(object.getSendingObject().toString(),
          GameClient.getClient().isConnected(), ConnectionInfo.getOwnInstance().getServerAddress()); //TODO statusbar besser zugreifbar machen
    }
  }

  private void handleBroadcastType(MessageObject object) {
    if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
      ChatFrame.getFrame().addMessage(buildChatAnswer(object));
    } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
      final List<DTOClient> clients = (List<DTOClient>) object.getSendingObject();
      ClientFrame.getInstance().updateClientList(clients);
    } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
      final ClientFrame frame = ClientFrame.getInstance();
      frame.clearClients();
      frame.clearGameCards();
      frame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.closed.server"), false, "");
      GameClient.getClient().disconnect(true);
    }
  }

  private void handleGameUpdateType(MessageObject object) {
    if(GameUpdateType.INITIALISE_PLAYERS.equals(object.getType())) {
      final List<DTOClient> clients = (List<DTOClient>) object.getSendingObject();
      final ClientFrame frame = ClientFrame.getInstance();
      frame.initialisePlayers(clients);
      frame.updateStatusBar();
    } else if(GameUpdateType.PLAYERS_UPDATE.equals(object.getType())) {
      final List<DTOClient> clients = (List<DTOClient>) object.getSendingObject();
      final ClientFrame frame = ClientFrame.getInstance();
      frame.updateOpponents(clients);
      frame.updateStatusBar();
    } else if(GameUpdateType.STACK_UPDATE.equals(object.getType())) {
      ClientFrame.getInstance().updateStack((DTOCardStack) object.getSendingObject());
    } else if(GameUpdateType.INGAME_CARDS.equals(object.getType())) {
      final List<List<DTOCard>> cardLists = (List<List<DTOCard>>) object.getSendingObject();
      guiInGameCardsUpdate(cardLists);
    } else if(GameUpdateType.NEXT_ROUND_AVAILABLE.equals(object.getType())) {
      ClientFrame.getInstance().enableButtons((Boolean) object.getSendingObject());
    } else if(GameUpdateType.CLIENT_CARDS.equals(object.getType())) {
      ClientFrame.getInstance().updateClientCards((List<DTOCard>) object.getSendingObject());
    } else if(GameUpdateType.GAME_FINISHED.equals(object.getType())) {
      ClientFrame.getInstance().showGameOverMessage();
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
        JOptionPane.showMessageDialog(ClientFrame.getInstance(),
            I18nSupport.getValue(CLIENT_BUNDLE,"dialog.text.error.server.error"),
            I18nSupport.getValue(CLIENT_BUNDLE,"dialog.title.error"),
            JOptionPane.ERROR_MESSAGE);
        LOGGER.severe("Server sends the wrong format for the client!");
      }
    }

    ClientFrame.getInstance().updateInGameCards(attackerCards, defenderCards);
  }

  private String buildChatAnswer(MessageObject object) {
    final ChatMessage chatMessage = (ChatMessage) object.getSendingObject();
    String message = Miscellaneous.getChatMessage(chatMessage.getSender().name,
        chatMessage.getMessage());
    return message;
  }
}

class UserMessageDistributor {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final Logger LOGGER =
      LoggingUtility.getLogger(UserMessageDistributor.class.getName());

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
    final String message =
        I18nSupport.getValue(CLIENT_BUNDLE,"dialog.text.game.finished.not.loser");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(CLIENT_BUNDLE,"dialog.button.play.again"),
            I18nSupport.getValue(CLIENT_BUNDLE,"dialog.button.option.no")};
    int option = JOptionPane.showOptionDialog(ClientFrame.getInstance(), message,
        I18nSupport.getValue(CLIENT_BUNDLE,"dialog.title.game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      updateInformation(true);
    }
  }

  private void showLoserOption() {
    final String message =
        I18nSupport.getValue(CLIENT_BUNDLE,"dialog.text.game.finished.loser");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(CLIENT_BUNDLE,"dialog.button.play.again.revenge"),
            I18nSupport.getValue(CLIENT_BUNDLE,"dialog.button.option.no")};
    int option = JOptionPane.showOptionDialog(ClientFrame.getInstance(), message,
        I18nSupport.getValue(CLIENT_BUNDLE,"dialog.title.game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      updateInformation(true);
    }
  }

  private void showNoPlayerOption() {
    final String message =
        I18nSupport.getValue(CLIENT_BUNDLE,"dialog.text.game.finished.no.player");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(CLIENT_BUNDLE,"dialog.button.join.game"),
            I18nSupport.getValue(CLIENT_BUNDLE,"dialog.button.option.no")};
    int option = JOptionPane.showOptionDialog(ClientFrame.getInstance(), message,
        I18nSupport.getValue(CLIENT_BUNDLE,"dialog.title.game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[1]);
    if(option == 0) {
      updateInformation(false);
    }
  }

  private void updateInformation(boolean spectate) {
    final Client client = Client.getOwnInstance();
    final GameClient gameClient = GameClient.getClient();
    client.setSpectating(spectate);
    gameClient.sendClientUpdate(client.toDTO());
  }
}
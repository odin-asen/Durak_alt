package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.Client;
import client.business.client.GameClient;
import client.gui.ActionCollection;
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
import common.utilities.gui.DurakPopup;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
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
  private final Logger LOGGER = LoggingUtility.getLogger(ClientFrame.class.getName());

  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS

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

  private MessageHandler handler;
  private ComponentUpdate update;
  private JButton roundDoneButton;
  private JButton takeCardsButton;
  private DurakToolBar toolBar;

  /* Constructors */

  private ClientFrame() {
    handler = new MessageHandler();
    update = new ComponentUpdate();

    final FramePosition position = FramePosition.createFramePositions(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);

    setIconImages(ResourceGetter.getApplicationIcons());
    setTitle(MessageFormat.format("{0} - {1} {2}",
        I18nSupport.getValue(CLIENT_BUNDLE,"application.title"),
        I18nSupport.getValue(CLIENT_BUNDLE,"version"), VERSION_NUMBER));
    setBounds(position.getRectangle());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        WidgetCreator.doAction(e.getSource(), ActionCollection.DISCONNECT);
        ClientFrame.getInstance().setVisible(false);
        ClientFrame.getInstance().dispose();
        System.exit(0);
      }
    });
  }

  public static ClientFrame getInstance() {
    if(frameInstance == null) {
      frameInstance = new ClientFrame();
      GameClient.getClient().addObserver(frameInstance);
    }

    return frameInstance;
  }

  /* Methods */

  /**
   * If logEntry is false the message will not be formatted to a log entry and unchanged
   * delegated to the chat frame. If the chat frame is invisible a popup window with the message
   * and a button that opens the chat frame appears.
   */
  public void addChatMessage(String message, boolean logEntry) {
    final ChatFrame frame = ChatFrame.getFrame();
    if(logEntry) {
      message = LoggingUtility.SHORT_STARS+" "+message+" "+LoggingUtility.SHORT_STARS;
    } else {
      if(!frame.isVisible()) {
        /* Make a popup that shows a message and has a button to open the chat */
        final Action openChatAction =
            WidgetCreator.createActionCopy(ActionCollection.OPEN_CHAT_DIALOG);
        openChatAction.putValue(Action.NAME,
            I18nSupport.getValue(CLIENT_BUNDLE, "action.name.open.chat"));
        final DurakPopup popup = WidgetCreator.createPopup(USER_MESSAGE_INFO_COLOUR, message,
            openChatAction, true, getBounds(), DurakPopup.LOCATION_DOWN_LEFT, 5.0);
        popup.setVisible(true);
      }
    }
    frame.addMessage(message);
  }

  public void showGameOverMessage() {
    final PlayerType type = Client.getOwnInstance().getPlayerType();
    new Thread(new Runnable() {
      public void run() {
        new UserMessageDistributor().gameOverMessage(type);
      }
    }).start();
  }

  public void showRuleException(Object ruleException) {
    final Rectangle bounds = new Rectangle(getX(),
        getY()+getHeight()-getContentPane().getHeight(), getWidth(), getHeight());
    final DurakPopup rulePopup = WidgetCreator.createPopup(
        ClientGUIConstants.GAME_TABLE_COLOUR, ruleException.toString(), bounds,
        DurakPopup.LOCATION_DOWN_RIGHT, 3);
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

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handler.handleUpdate(object);
  }

  private Boolean nextRoundRequest(Boolean takeCards) {
    if(takeCards == null)
      return false;

    final DTOClient dtoClient = Client.getOwnInstance().toDTO();
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

  /**
   * Resets all game widgets, clears the client list and sets a text in the status bar.
   * The gui disconnects also from the server.
   * @param statusText Text to be shown in the status bar.
   * @param serverShutdown Notifies the client whether the reset is because of a
   *                 server serverShutdown or not.
   */
  public void resetAll(String statusText, boolean serverShutdown) {
    resetAll(statusText, serverShutdown, true, false);
  }

  public void resetAll(String statusText, boolean serverShutdown,
                        boolean disconnect, boolean popupMessage) {
    update.resetGameWidgets();
    addChatMessage(statusText, true);
    if(disconnect) {
      updateClientList(null);
      GameClient.getClient().disconnect(serverShutdown);
      setStatus(statusText, false, "");
    } else setStatus(statusText, true, GameClient.getClient().getSocketAddress());
    if(popupMessage)
      WidgetCreator.createPopup(USER_MESSAGE_WARNING_COLOUR, statusText, getBounds(),
          DurakPopup.LOCATION_UP_LEFT, 3).setVisible(true);
  }

  /***********************/
  /* status bar methods  */
  /***********************/
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

  /***********************/
  /* client list methods */
  /***********************/
  /**
   * Updates the client list. If clients is null, all clients will be removed from the list.
   * @param clients All clients to show on the list.
   */
  public void updateClientList(List<DTOClient> clients) {
    final DefaultListModel<DTOClient> listModel =
        ((DefaultListModel<DTOClient>) clientsList.getModel());
    listModel.clear();

    if(clients != null) {
      for (DTOClient client : clients)
        listModel.add(listModel.size(), client);
    }
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
    listPanel.setBorder(BorderFactory.createTitledBorder(I18nSupport.getValue(CLIENT_BUNDLE,"border.title.opponents")));
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
    takeCardsButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE,"button.text.take.cards"),
        I18nSupport.getValue(CLIENT_BUNDLE,"button.tooltip.take.cards"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if(nextRoundRequest(true)) {
              update.updateGamePanel(new ArrayList<DTOCard>(), new ArrayList<DTOCard>(), null);
            }
          }
        });
    roundDoneButton = WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE,"button.text.finish.round"),
        I18nSupport.getValue(CLIENT_BUNDLE,"button.tooltip.finish.round"), null,
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            if(nextRoundRequest(false)) {
              update.updateGamePanel(new ArrayList<DTOCard>(), new ArrayList<DTOCard>(), null);
            }
          }
        });
    takeCardsButton.setEnabled(false);
    roundDoneButton.setEnabled(false);

    panel.setBackground(GAME_TABLE_COLOUR);
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(takeCardsButton);
    panel.add(roundDoneButton);

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
        this.setToolTipText(I18nSupport.getValue(CLIENT_BUNDLE,"list.tooltip.audience"));
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

  private class MessageHandler {
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
        Client.getOwnInstance().setClientInfo((DTOClient) object.getSendingObject());
        update.updateSubComponents();
      } else if(MessageType.RULE_MESSAGE.equals(object.getType())) {
        ClientFrame.getInstance().showRuleException(object.getSendingObject());
      } else if(MessageType.STATUS_MESSAGE.equals(object.getType())) {
        ClientFrame.getInstance().setStatus(object.getSendingObject().toString(),
            GameClient.getClient().isConnected(),
            ConnectionInfo.getOwnInstance().getServerAddress()); //TODO statusbar besser zugreifbar machen
      }
    }

    private void handleBroadcastType(MessageObject object) {
      if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
        addChatMessage(buildChatAnswer(object), false);
      } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
        updateClientList((List<DTOClient>) object.getSendingObject());
      } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
        resetAll(I18nSupport.getValue(MSGS_BUNDLE, "status.closed.server"), true);
      }
    }

    private void handleGameUpdateType(MessageObject object) {
      if(GameUpdateType.INITIALISE_PLAYERS.equals(object.getType())) {
        update.updateOpponents((List<DTOClient>) object.getSendingObject(), true);
        updateStatusBar();
      } else if(GameUpdateType.PLAYERS_UPDATE.equals(object.getType())) {
        update.updateOpponents((List<DTOClient>) object.getSendingObject(), false);
        updateStatusBar();
      } else if(GameUpdateType.STACK_UPDATE.equals(object.getType())) {
        update.updateStack((DTOCardStack) object.getSendingObject());
      } else if(GameUpdateType.INGAME_CARDS.equals(object.getType())) {
        final List<List<DTOCard>> cards = (List<List<DTOCard>>) object.getSendingObject();
        final List<DTOCard> attackerCards = new ArrayList<DTOCard>();
        final List<DTOCard> defenderCards = new ArrayList<DTOCard>();
        prepareInGameCards(cards, attackerCards, defenderCards);
        update.updateGamePanel(attackerCards, defenderCards, null);
      } else if(GameUpdateType.NEXT_ROUND_AVAILABLE.equals(object.getType())) {
        update.enableButtons((Boolean) object.getSendingObject());
      } else if(GameUpdateType.CLIENT_CARDS.equals(object.getType())) {
        update.updateGamePanel(null,null,(List<DTOCard>) object.getSendingObject());
      } else if(GameUpdateType.GAME_ABORTED.equals(object.getType())) {
        final String message = I18nSupport.getValue(MSGS_BUNDLE, "game.aborted.0",
            object.getSendingObject());
        resetAll(message, false, false, true);
        LOGGER.info(LoggingUtility.STARS+" Game finished "+LoggingUtility.STARS);
      } else if(GameUpdateType.GAME_FINISHED.equals(object.getType())) {
        showGameOverMessage();
        final String message = I18nSupport.getValue(MSGS_BUNDLE, "game.finished");
        addChatMessage(message, true);
        setStatus(message, true, GameClient.getClient().getSocketAddress());
        LOGGER.info(LoggingUtility.STARS+" Game finished "+LoggingUtility.STARS);
      }
    }

    /* Reads the attacker cards and defender cards out of the list and writes them in */
    private void prepareInGameCards(List<List<DTOCard>> cards, List<DTOCard> attackerCards,
                                    List<DTOCard> defenderCards) {
      if(cards != null) {
        if (cards.size() == 2) {
          Miscellaneous.addAllToCollection(attackerCards, cards.get(0));
          Miscellaneous.addAllToCollection(defenderCards, cards.get(1));
        } else {
          JOptionPane.showMessageDialog(ClientFrame.getInstance(),
              I18nSupport.getValue(CLIENT_BUNDLE,"dialog.text.error.server.error"),
              I18nSupport.getValue(CLIENT_BUNDLE,"dialog.title.error"),
              JOptionPane.ERROR_MESSAGE);
          LOGGER.severe("Server sends the wrong format for the client!");
        }
      } else {
        attackerCards = null;
        defenderCards = null;
      }
    }

    private String buildChatAnswer(MessageObject object) {
      final ChatMessage chatMessage = (ChatMessage) object.getSendingObject();
      String message = Miscellaneous.getChatMessage(chatMessage.getSender().name,
          chatMessage.getMessage());
      return message;
    }
  }

  /**
   * Consits of all methods that are changing the in game relevant components, like
   * the hand cards, opponent widgets, ingame card widgets, etc...
   */
  private class ComponentUpdate {
    /* Updates the representation and behavior of subcomponents,
    like the card moving at the game panel. */
    private void updateSubComponents() {
      gamePanel.setListenerType(Client.getOwnInstance().getPlayerType());
    }

    /* If a parameter is null the specified card update will be ignored. */
    /* An empty list will remove the cards. */
    /* Note: Defender cards can never be shown without the appropriate attacker card */
    private void updateGamePanel(List<DTOCard> attackerCards, List<DTOCard> defenderCards,
                                List<DTOCard> clientCards) {
      if(clientCards != null)
        gamePanel.placeClientCards(clientCards);
      if(attackerCards != null)
        gamePanel.placeInGameCards(attackerCards, defenderCards);
    }

    private void updateStack(DTOCardStack cardStack) {
      cardStackPanel.updateStack(cardStack);
    }

    /* If clients is null, all components will be removed */
    private void updateOpponents(List<DTOClient> clients, boolean initialisation) {
      if(clients != null) {
        if(initialisation) {
          opponentsPanel.removeAllOpponents();
          for (DTOClient client : clients)
            opponentsPanel.addOpponent(client);
        }
        opponentsPanel.updateOpponents(clients);
      } else opponentsPanel.removeAllOpponents();
    }

    /* Resets all game related widgets and buttons */
    private void resetGameWidgets() { //TODO buttons noch einstellen
      gamePanel.deleteCards();
      opponentsPanel.removeAllOpponents();
      cardStackPanel.deleteCards();
    }

    /**
     * Enables the game buttons depending on the players type and the surpassed boolean.
     * @param roundFinished Surpassed boolean.
     */
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
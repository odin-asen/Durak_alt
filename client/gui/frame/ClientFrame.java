package client.gui.frame;

import client.business.Client;
import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.gui.ActionCollection;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.playerTypePanel.PlayerTypePanel;
import common.dto.DTOCard;
import common.dto.DTOCardStack;
import common.dto.DTOClient;
import common.dto.message.*;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.Converter;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.gui.Compute;
import common.utilities.gui.DurakPopup;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
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

  private MessageHandler handler;
  private UserMessageDistributor messenger;

  private PlayerTypePanel centrePanel;
  private DurakToolBar toolBar;

  /* Constructors */

  private ClientFrame() {
    handler = new MessageHandler();
    messenger = new UserMessageDistributor();

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
        setVisible(false);
        dispose();
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
        WidgetCreator.createPopup(USER_MESSAGE_INFO_COLOUR, message, openChatAction,
            true, getBounds(), DurakPopup.LOCATION_DOWN_LEFT, 5.0).setVisible(true);
      }
    }
    frame.addMessage(message);
  }

  public void showGameOverMessage() {
    final PlayerType type = Client.getOwnInstance().getPlayerType();
    new Thread(new Runnable() {
      public void run() {
        messenger.gameOverMessage(type);
      }
    }).start();
  }

  public void showRuleException(Object ruleException) {
    messenger.showRulePopup(ruleException.toString(), Compute.getFramelessBounds(this));
    ResourceGetter.playSound("computer.says.no");
  }

  public void showInformationPopup(String message) {
    messenger.showMessagePopup(USER_MESSAGE_INFO_COLOUR, message,
        Compute.getFramelessBounds(this));
  }

  public void showWarningPopup(String message) {
    messenger.showMessagePopup(USER_MESSAGE_WARNING_COLOUR, message,
        Compute.getFramelessBounds(this));
  }

  public void showErrorPopup(String message) {
    messenger.showMessagePopup(USER_MESSAGE_ERROR_COLOUR, message,
        Compute.getFramelessBounds(this));
  }

  /**
   * Initialises the frame. Should be called at least after the first cration of the frame
   * object.
   */
  public void init() {
    toolBar = new DurakToolBar();
    centrePanel = new PlayerTypePanel(PlayerType.DEFAULT);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(centrePanel, BorderLayout.CENTER);
    updateStatusBar();
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handler.handleUpdate(object);
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
    centrePanel.resetGameWidgets();
    addChatMessage(statusText, true);
    if(disconnect) {
      centrePanel.updateClients(null);
      GameClient.getClient().disconnect(serverShutdown);
      setStatus(statusText, false, "");
    } else setStatus(statusText, true, GameClient.getClient().getSocketAddress());
    if(popupMessage)
      showWarningPopup(statusText);
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
    centrePanel.setStatus(mainText);
    centrePanel.setStatus(connected, serverAddress);
    toolBar.setConnection(connected);
  }

  public void updateStatusBar() {
    centrePanel.setStatus(GameClient.getClient().isConnected());
    centrePanel.setStatus("");
  }

  /* Getter and Setter */

  /* Inner Classes */

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
        centrePanel.setPlayerType(Client.getOwnInstance().getPlayerType());
      } else if(MessageType.RULE_MESSAGE.equals(object.getType())) {
        showRuleException(object.getSendingObject());
      } else if(MessageType.STATUS_MESSAGE.equals(object.getType())) {
        final String message = (String) object.getSendingObject();
        showInformationPopup(message);
        setStatus(message, GameClient.getClient().isConnected(),
            ConnectionInfo.getOwnInstance().getServerAddress()); //TODO statusbar besser zugreifbar machen
      }
    }

    private void handleBroadcastType(MessageObject object) {
      if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
        addChatMessage(buildChatAnswer(object), false);
      } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
        centrePanel.updateClients((List<DTOClient>) object.getSendingObject());
      } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
        resetAll(I18nSupport.getValue(MSGS_BUNDLE, "status.closed.server"), true);
      }
    }

    private void handleGameUpdateType(MessageObject object) {
      if(GameUpdateType.INITIALISE_PLAYERS.equals(object.getType())) {
        centrePanel.initOpponents((List<DTOClient>) object.getSendingObject());
        updateStatusBar();
      } else if(GameUpdateType.PLAYERS_UPDATE.equals(object.getType())) {
        centrePanel.updateOpponents((List<DTOClient>) object.getSendingObject(), false);
        updateStatusBar();
      } else if(GameUpdateType.STACK_UPDATE.equals(object.getType())) {
        centrePanel.updateStack((DTOCardStack) object.getSendingObject());
      } else if(GameUpdateType.INGAME_CARDS.equals(object.getType())) {
        final List<List<DTOCard>> cards = (List<List<DTOCard>>) object.getSendingObject();
        final List<DTOCard> attackerCards = new ArrayList<DTOCard>();
        final List<DTOCard> defenderCards = new ArrayList<DTOCard>();
        prepareInGameCards(cards, attackerCards, defenderCards);
        centrePanel.setCards(Converter.fromDTO(attackerCards), Converter.fromDTO(defenderCards));
      } else if(GameUpdateType.NEXT_ROUND_INFO.equals(object.getType())) {
        final List<Boolean> info = (List<Boolean>) object.getSendingObject();
        centrePanel.enableButtons(info.get(0), info.get(1));
      } else if(GameUpdateType.CLIENT_CARDS.equals(object.getType())) {
        centrePanel.setCards(Converter.fromDTO((List<DTOCard>) object.getSendingObject()));
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
          JOptionPane.showMessageDialog(frameInstance,
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

  public void showRulePopup(String ruleMessage, Rectangle parentBounds) {
    WidgetCreator.createPopup(ClientGUIConstants.GAME_TABLE_COLOUR, ruleMessage, parentBounds,
        DurakPopup.LOCATION_DOWN_RIGHT, 3).setVisible(true);
  }

  public void showMessagePopup(Color background, String message, Rectangle parentBounds) {
    WidgetCreator.createPopup(background, message, parentBounds,
        DurakPopup.LOCATION_UP_LEFT, 3).setVisible(true);
  }
}


package client.gui.frame;

import client.business.Client;
import client.business.client.GameClient;
import client.data.GlobalSettings;
import client.data.xStreamModel.PopupSettings;
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
import static common.i18n.BundleStrings.*;
import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
public class ClientFrame extends JFrame implements Observer {
  private static ClientFrame frameInstance;
  private final Logger LOGGER = LoggingUtility.getLogger(ClientFrame.class.getName());

  private static final String VERSION_NUMBER = "0.5";

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
        I18nSupport.getValue(GUI_TITLE, "application.client"),
        I18nSupport.getValue(GUI_MISC, "version"), VERSION_NUMBER));
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
    if(GlobalSettings.getInstance().sound.getRuleException())
      ResourceGetter.playSound("computer.says.no");
  }

  public void showGamePopup(String message) {
    messenger.showGamePopup(message, Compute.getFramelessBounds(this));
  }

  public void showInformationPopup(String message) {
    messenger.showMessagePopup(USER_MESSAGE_INFO_COLOUR, message, Compute.getFramelessBounds(this),
        DEFAULT_POPUP_TIME);
  }

  public void showWarningPopup(String message) {
    messenger.showMessagePopup(USER_MESSAGE_WARNING_COLOUR, message,
        Compute.getFramelessBounds(this), DEFAULT_POPUP_TIME);
  }

  public void showErrorPopup(String message) {
    messenger.showMessagePopup(USER_MESSAGE_ERROR_COLOUR, message,
        Compute.getFramelessBounds(this), DEFAULT_POPUP_TIME);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handler.handleUpdate(object);
  }

  /************************/
  /* frame update methods */
  /************************/
  /**
   * Initialises the frame. Should be called at least after the first creation of the frame
   * object.
   */
  public void init() {
    toolBar = new DurakToolBar();
    centrePanel = new PlayerTypePanel(PlayerType.DEFAULT);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(centrePanel, BorderLayout.CENTER);
    updateGUIStatus("", false, "");
  }

  /**
   * Resets all game widgets. Depending on the parameter it also empties the client list and
   * shows a text in a popup window and the chat.
   * @param statusText Text to be shown in the status bar. If null, nothing will be shown.
   * @param clientList True, empty the client list. False, don't empty the client list.
   */
  public void resetAll(String statusText, boolean clientList) {
    if(statusText != null) {
      addChatMessage(statusText, true);
      showWarningPopup(statusText);
    }
    resetAll(clientList);
  }

  /**
   * Resets all game widgets. Depending on the parameter it also empties the client list.
   * @param clientList True, empty the client list. False, don't empty the client list.
   */
  public void resetAll(boolean clientList) {
    centrePanel.resetGameWidgets();
    centrePanel.setPlayerType(PlayerType.DEFAULT);
    if(clientList)
      centrePanel.updateClients(null);
  }

  /**
   * If {@code logEntry} is false the message will not be formatted to a log entry and unchanged
   * delegated to the chat frame. If the chat frame is invisible a popup window with the message
   * and a button that opens the chat frame appears.
   */
  public void addChatMessage(String message, boolean logEntry) {
    final ChatFrame frame = ChatFrame.getFrame();
    if(logEntry) {
      message = LoggingUtility.SHORT_STARS+" "+message+" "+LoggingUtility.SHORT_STARS;
    } else {
      final PopupSettings popupSettings = GlobalSettings.getInstance().popup;
      if(!frame.isVisible() && popupSettings.isEnabled() && popupSettings.getChat().isEnabled()) {
        /* Make a popup that shows a message and has a button to open the chat */
        final Action openChatAction =
            WidgetCreator.createActionCopy(ActionCollection.OPEN_CHAT_DIALOG);
        openChatAction.putValue(Action.NAME,
            I18nSupport.getValue(GUI_ACTION, "name.open.chat"));
        WidgetCreator.createPopup(USER_MESSAGE_INFO_COLOUR, message, openChatAction,
            true, getBounds(), DurakPopup.LOCATION_DOWN_LEFT,
            popupSettings.getChat().getDuration()).setVisible(true);
      }
    }
    frame.addMessage(message);
  }

  /**
   * Sets the status bar's main text, server address field and indicates if a connection
   * consists to the server. The boolean parameter also indicates the representation of the
   * gui, e.g. the picture of the connection toolbar button, the picture in the status bar,
   * etc...
   * @param mainText Text to set to the status bar.
   * @param connected Indicates whether the client is connected or not. If {@code connected} is
   *                  false, {@link #resetAll(boolean)} with true will be called.
   * @param serverAddress Shows the server address as tooltip.
   */
  public void updateGUIStatus(String mainText, boolean connected, String serverAddress) {
    centrePanel.setStatus(mainText);
    centrePanel.setStatus(connected, serverAddress);
    toolBar.setConnection(connected);
    if(!connected)
      resetAll(true);
  }

  /**
   * Sets the text in the status bar.
   * @param mainText Text to set to the status bar.
   */
  public void setStatus(String mainText) {
    centrePanel.setStatus(mainText);
  }

  /* Getter and Setter */
  /* Inner Classes */

  @SuppressWarnings("unchecked")
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
        setStatus(message);
      } else if(MessageType.LOST_CONNECTION.equals(object.getType())) {
        final String message = I18nSupport.getValue(USER_MESSAGES, "client.lost.connection");
        showErrorPopup(message);
        updateGUIStatus(message, false, "");
        addChatMessage(message, true);
        resetAll(true);
      }
    }

    private void handleBroadcastType(MessageObject object) {
      if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
        addChatMessage(buildChatAnswer(object), false);
      } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
        centrePanel.updateClients((List<DTOClient>) object.getSendingObject());
      } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
        GameClient.getClient().disconnect(true);
        final String message = I18nSupport.getValue(USER_MESSAGES, "status.closed.server");
        addChatMessage(message, true);
        showWarningPopup(message);
        updateGUIStatus(message, false, "");
      }
    }

    private void handleGameUpdateType(MessageObject object) {
      if(GameUpdateType.INITIALISE_PLAYERS.equals(object.getType())) {
        centrePanel.initOpponents((List<DTOClient>) object.getSendingObject());
        setStatus("");
      } else if(GameUpdateType.PLAYERS_UPDATE.equals(object.getType())) {
        centrePanel.updateOpponents((List<DTOClient>) object.getSendingObject(), false);
        setStatus("");
      } else if(GameUpdateType.STACK_UPDATE.equals(object.getType())) {
        centrePanel.updateStack((DTOCardStack) object.getSendingObject());
      } else if(GameUpdateType.IN_GAME_CARDS.equals(object.getType())) {
        final List<List<DTOCard>> cards = (List<List<DTOCard>>) object.getSendingObject();
        final List<DTOCard> attackerCards = new ArrayList<DTOCard>();
        final List<DTOCard> defenderCards = new ArrayList<DTOCard>();
        prepareInGameCards(cards, attackerCards, defenderCards);
        centrePanel.setCards(Converter.fromDTO(attackerCards), Converter.fromDTO(defenderCards));
      } else if(GameUpdateType.NEXT_ROUND_INFO.equals(object.getType())) {
        final List<Boolean> roundInfo = (List<Boolean>) object.getSendingObject();
        centrePanel.updateRoundInfo(roundInfo.get(0), roundInfo.get(1), roundInfo.get(2));
      } else if(GameUpdateType.CLIENT_CARDS.equals(object.getType())) {
        centrePanel.setCards(Converter.fromDTO((List<DTOCard>) object.getSendingObject()));
      } else if(GameUpdateType.GAME_CANCELED.equals(object.getType())) {
        final String message =
            I18nSupport.getValue(USER_MESSAGES, "game.canceled.0", object.getSendingObject());
        setStatus(message);
        resetAll(message, false);
        LOGGER.info(LoggingUtility.STARS+" Game canceled "+LoggingUtility.STARS);
      } else if(GameUpdateType.GAME_FINISHED.equals(object.getType())) {
        showGameOverMessage();
        final String message = I18nSupport.getValue(USER_MESSAGES, "game.finished");
        setStatus(message);
        addChatMessage(message, true);
        resetAll(false);
        LOGGER.info(LoggingUtility.STARS+" Game finished "+LoggingUtility.STARS);
      }
    }

    /* Reads the attacker cards and defender cards out of the list and writes them in */
    @SuppressWarnings("UnusedAssignment")
    private void prepareInGameCards(List<List<DTOCard>> cards, List<DTOCard> attackerCards,
                                    List<DTOCard> defenderCards) {
      if(cards != null) {
        if (cards.size() == 2) {
          Miscellaneous.addAllToCollection(attackerCards, cards.get(0));
          Miscellaneous.addAllToCollection(defenderCards, cards.get(1));
        } else {
          JOptionPane.showMessageDialog(frameInstance,
              I18nSupport.getValue(GUI_COMPONENT, "text.server.error.occurred"),
              I18nSupport.getValue(GUI_TITLE, "error"),
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
      return Miscellaneous.getChatMessage(chatMessage.getSender().name,
          chatMessage.getMessage());
    }
  }
}

class UserMessageDistributor {
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
        I18nSupport.getValue(GUI_COMPONENT, "text.game.finished.not.loser");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(GUI_COMPONENT, "text.play.again"),
            I18nSupport.getValue(GUI_COMPONENT, "text.no.thanks")};
    int option = JOptionPane.showOptionDialog(ClientFrame.getInstance(), message,
        I18nSupport.getValue(GUI_TITLE, "game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      updateInformation(true);
    }
  }

  private void showLoserOption() {
    final String message =
        I18nSupport.getValue(GUI_COMPONENT, "text.game.finished.loser");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(GUI_COMPONENT, "text.play.again.revenge"),
            I18nSupport.getValue(GUI_COMPONENT, "text.no.thanks")};
    int option = JOptionPane.showOptionDialog(ClientFrame.getInstance(), message,
        I18nSupport.getValue(GUI_TITLE, "game.finished"),
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, strings, strings[0]);
    if(option != 0) {
      updateInformation(true);
    }
  }

  private void showNoPlayerOption() {
    final String message =
        I18nSupport.getValue(GUI_COMPONENT, "text.game.finished.no.player");
    final Object[] strings =
        new Object[]{I18nSupport.getValue(GUI_COMPONENT, "text.join.game"),
            I18nSupport.getValue(GUI_COMPONENT, "text.no.thanks")};
    int option = JOptionPane.showOptionDialog(ClientFrame.getInstance(), message,
        I18nSupport.getValue(GUI_TITLE, "game.finished"),
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
    final GlobalSettings settings = GlobalSettings.getInstance();
    if(settings.popup.isEnabled() && settings.popup.getRule().isEnabled())
      WidgetCreator.createPopup(ClientGUIConstants.GAME_TABLE_COLOUR, ruleMessage, parentBounds,
        DurakPopup.LOCATION_DOWN_RIGHT, settings.popup.getRule().getDuration()).setVisible(true);
  }

  public void showMessagePopup(Color background, String message, Rectangle parentBounds,
                               double duration) {
    WidgetCreator.createPopup(background, message, parentBounds,
        DurakPopup.LOCATION_UP_LEFT, duration).setVisible(true);
  }

  public void showGamePopup(String message, Rectangle parentBounds) {
    final PopupSettings popupSettings = GlobalSettings.getInstance().popup;
    if(popupSettings.isEnabled() && popupSettings.getGame().isEnabled())
      showMessagePopup(ClientGUIConstants.USER_MESSAGE_INFO_COLOUR, message, parentBounds,
          popupSettings.getGame().getDuration());
  }
}


package client.gui.frame;

import client.business.client.GameClient;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.gamePanel.GamePanel;
import client.gui.frame.setup.SetUpFrame;
import dto.ClientInfo;
import dto.DTOCard;
import dto.DTOCardStack;
import dto.message.*;
import utilities.Miscellaneous;
import utilities.constants.GameCardConstants;
import utilities.gui.FramePosition;

import javax.swing.*;
import java.awt.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class ClientFrame extends JFrame implements Observer {
  private static final Logger LOGGER = Logger.getLogger(ClientFrame.class.getName());

  private JPanel secondPane;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel playerPanel;
  private DurakStatusBar statusBar;
  private DurakToolBar toolBar;
  private JPanel stackClientsPanel;
  private JList<ClientInfo> clientsList;

  /* Constructors */
  public ClientFrame() {
    final FramePosition position = FramePosition.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);
    GameClient.getClient().addObserver(this);

    this.setTitle(APPLICATION_NAME + TITLE_SEPARATOR + VERSION);
    this.setBounds(position.getRectangle());
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initComponents();
    this.setVisible(true);
  }

  /* Methods */
  private void initComponents() {
    toolBar = new DurakToolBar(this);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(getSecondPane(), BorderLayout.CENTER);
  }

  public void setStatusBarText(Boolean connected, String text, String serverAddress) {
    statusBar.setConnected(connected, serverAddress);
    statusBar.setText(text);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    ClientFrameUpdater updater = new ClientFrameUpdater();
    updater.handleUpdate(object);
  }

  public void clearClientList() {
    ((DefaultListModel<ClientInfo>) clientsList.getModel()).removeAllElements();
  }

  public void clearGameCards() {
    playerPanel.deleteCards();
    opponentsPanel.deleteCards();
    cardStackPanel.deleteCards();
  }

  public void placeCardTest(boolean init) {
    if(!init) return;

    List<DTOCard> cards = new ArrayList<DTOCard>();
    for (int i = 0; i < 15; i++) {
      final DTOCard card = new DTOCard();
      card.cardValue = GameCardConstants.CardValue.TWO;
      card.cardColour = GameCardConstants.CardColour.CLUBS;
      cards.add(card);
    }
    playerPanel.placeClientCards(cards);
  }

  public void initTest(boolean init) {
    if(!init) return;

    List<DTOCard> attackCards = new ArrayList<DTOCard>();
    for (int i = 0; i < 5; i++) {
      final DTOCard card = new DTOCard();
      card.cardValue = GameCardConstants.CardValue.ACE;
      card.cardColour = GameCardConstants.CardColour.HEARTS;
      attackCards.add(card);
    }

    List<DTOCard> defenderCards = new ArrayList<DTOCard>();
    for (int i = 0; i < 0; i++) {
      final DTOCard card = new DTOCard();
      card.cardValue = GameCardConstants.CardValue.ACE;
      card.cardColour = GameCardConstants.CardColour.SPADES;
      defenderCards.add(card);
    }

    playerPanel.placeInGameCards(attackCards, defenderCards);
  }

  /* Getter and Setter */
  private JPanel getSecondPane() {
    if(secondPane != null)
      return secondPane;

    secondPane = new JPanel();
    opponentsPanel = new OpponentsPanel();
    playerPanel = new GamePanel();

    opponentsPanel.setPreferredSize(new Dimension(0, OPPONENT_PANEL_HEIGHT));

    secondPane.setLayout(new BorderLayout());
    secondPane.add(opponentsPanel, BorderLayout.PAGE_START);
    secondPane.add(getStackClientsPanel(), BorderLayout.LINE_START);
    secondPane.add(playerPanel, BorderLayout.CENTER);
    secondPane.add(getStatusPanel(), BorderLayout.PAGE_END);

    return secondPane;
  }

  private DurakStatusBar getStatusPanel() {
    if(statusBar != null)
      return statusBar;

    statusBar = new DurakStatusBar();
    statusBar.setPreferredSize(new Dimension(0, 16));

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

    stackClientsPanel.setBackground(Color.WHITE);
    stackClientsPanel.setLayout(new BoxLayout(stackClientsPanel,BoxLayout.PAGE_AXIS));
    stackClientsPanel.add(cardStackPanel);
    stackClientsPanel.add(listPanel);

    return stackClientsPanel;
  }

  /* Inner Classes */
  private class ClientFrameUpdater {
    private void handleUpdate(MessageObject object) {
      if(object == null)
        return;

      final Class<? extends Enum> enumClass = object.getType().getClass();
      if(enumClass.equals(GUIObserverType.class)) {
        handleGUIObserverType(object);
      } else if(enumClass.equals(BroadcastType.class)) {
        handleBroadcastType(object);
      } else if(enumClass.equals(GameUpdateType.class)) {
        handleGameUpdateType(object);
      } else if(enumClass.equals(MessageType.class)) {
        handleMessageType(object);
      }
    }

    private void handleMessageType(MessageObject object) {
      if(MessageType.LOGIN_NUMBER.equals(object.getType())) {
        SetUpFrame.getInstance().updateClientInfo((ClientInfo) object.getSendingObject());
      }
    }

    private void handleBroadcastType(MessageObject object) {
      if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
        ChatFrame.getFrame().addMessage(buildChatAnswer(object));
      } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
        refreshClients(object);
      } else if(BroadcastType.SERVER_SHUTDOWN.equals(object.getType())) {
        disconnectClient();
        clearGameCards();
      }
    }

    private void disconnectClient() {
      try {
        GameClient.getClient().disconnect(SetUpFrame.getInstance().getClientInfo());
      } catch (NotBoundException e) {
        LOGGER.warning(e.getMessage());
      } catch (RemoteException e) {
        LOGGER.warning(e.getMessage());
      }
      setStatusBarText(false, "","");
    }

    private String buildChatAnswer(MessageObject object) {
      final ChatMessage chatMessage = (ChatMessage) object.getSendingObject();
      String message = Miscellaneous.getChatMessage(chatMessage.getSender().getName(),
          chatMessage.getMessage());
      if(chatMessage.getSender().isEqual(SetUpFrame.getInstance().getClientInfo()))
        message = Miscellaneous.changeChatMessageInBrackets("ich", message);
      return message;
    }

    private void handleGameUpdateType(MessageObject object) {
      if(GameUpdateType.OPPONENT_CARD_UPDATE.equals(object.getType())) {
        opponentsPanel.updateOpponents((List<ClientInfo>) object.getSendingObject());
      } else if(GameUpdateType.STACK_UPDATE.equals(object.getType())) {
        cardStackPanel.updateStack((DTOCardStack) object.getSendingObject());
      }
    }

    private void refreshClients(MessageObject object) {
      List<ClientInfo> clients = (List<ClientInfo>) object.getSendingObject();
      DefaultListModel<ClientInfo> listModel = ((DefaultListModel<ClientInfo>) clientsList.getModel());
      ClientInfo ownClientInfo = SetUpFrame.getInstance().getClientInfo();
      for (ClientInfo client : clients) {
        if(!ownClientInfo.toString().equals(client.toString()) && !listModel.contains(client)) {
          listModel.add(0, client);
        }
      }
    }

    private void handleGUIObserverType(MessageObject object) {
      if(GUIObserverType.INITIALISE_CARDS.equals(object.getType())) {
        playerPanel.placeClientCards((List<DTOCard>) object.getSendingObject());
      } else if(GUIObserverType.INITIALISE_STACK.equals(object.getType())) {
        DTOCardStack stack = (DTOCardStack) object.getSendingObject();
        cardStackPanel.initialiseStack(stack.getSize(), stack.getCardStack().getLast());
      } else if(GUIObserverType.INITIALISE_OPPONENTS.equals(object.getType())) {
        System.out.println("Initialise Opponents");
        List<ClientInfo> opponents = (List<ClientInfo>) object.getSendingObject();
        for (ClientInfo opponent : opponents) {
          if(!SetUpFrame.getInstance().getClientInfo().isEqual(opponent)) {
            opponentsPanel.addOpponent(opponent);
          }
        }
      }
    }
  }

  private class ClientInfoCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component superComponent = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

      if(value ==null)
        return this;

      final ClientInfo client = (ClientInfo) value;
      this.setText(client.getName());
      this.setBackground(superComponent.getBackground());
      this.setForeground(superComponent.getForeground());

      return this ;
    }
  }
}


package client.gui.frame;

import client.StartClient;
import client.business.client.GameClient;
import client.gui.frame.setup.SetUpFrame;
import client.gui.widget.card.GameCardWidget;
import dto.ClientInfo;
import dto.DTOCard;
import dto.DTOCardStack;
import dto.message.BroadcastType;
import dto.message.GUIObserverType;
import dto.message.GameUpdateType;
import dto.message.MessageObject;
import utilities.Miscellaneous;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
  private ChatPanel chatPanel;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  public GamePanel playerPanel;
  private DurakStatusBar statusBar;
  private DurakToolBar toolBar;
  private JPanel stackClientsPanel;
  private JList<ClientInfo> clientsList;

  /* Constructors */
  public ClientFrame() {
    final FensterPositionen positionen = FensterPositionen.createFensterPositionen(
        MAIN_FRAME_SCREEN_SIZE, MAIN_FRAME_SCREEN_SIZE);
    JFrame frame = new JFrame();
    StartClient.loadLaF(frame);
    frame.dispose();

    GameClient.getClient().addObserver(this);

    this.setTitle(APPLICATION_NAME + TITLE_SEPARATOR + VERSION);
    this.setBounds(positionen.getRectangle());
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initComponents();
    this.addComponentListener(new CardResizer());
    this.setVisible(true);
  }

  /* Methods */
  private void initComponents() {
    toolBar = new DurakToolBar(this);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(getSecondPane(), BorderLayout.CENTER);
  }

  public void setStatusBarText(String text) {
    statusBar.setText(text);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    ClientFrameUpdater updater = new ClientFrameUpdater();
    updater.handleUpdate(object);
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

    secondPane.add(getChatPanel(), BorderLayout.LINE_END);
    secondPane.add(playerPanel, BorderLayout.CENTER);
    secondPane.add(getStatusPanel(), BorderLayout.PAGE_END);

    return secondPane;
  }

  private ChatPanel getChatPanel() {
    if(chatPanel != null)
      return chatPanel;

    chatPanel = new ChatPanel();

    return chatPanel;
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
  private class CardResizer extends ComponentAdapter {
    public void componentResized(ComponentEvent e) {
      if(playerPanel != null)
        playerPanel.repaintCards();
    }
  }

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
      }
    }

    private void handleBroadcastType(MessageObject object) {
      if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
        chatPanel.addMessage(buildChatAnswer(object));
      } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
        refreshClients(object);
      }
    }

    private String buildChatAnswer(MessageObject object) {
      //TODO DTO für ChatMessage erzeugen, damit diese rumgeschickt werden kann
      final List<Object> chatMessage = (List<Object>) object.getSendingObject();
      final ClientInfo sender = (ClientInfo) chatMessage.get(1);
      String message = Miscellaneous.getChatMessage(sender.getClientName(), (String) chatMessage.get(2));
      if(sender.equalsID(SetUpFrame.getInstance().getClientInfo()))
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
      if(GUIObserverType.CONNECTED.equals(object.getType())) {
        SetUpFrame.getInstance().setConnectionEnabled(false);
        statusBar.setConnected(true, (String) object.getSendingObject());
        statusBar.setText("Verbindung zu "+object.getSendingObject()+" wurde erfolgreich aufgebaut");
      } else if(GUIObserverType.DISCONNECTED.equals(object.getType())) {
        SetUpFrame.getInstance().setConnectionEnabled(true);
        statusBar.setConnected(false, null);
        statusBar.setText("");
        ((DefaultListModel<ClientInfo>) clientsList.getModel()).removeAllElements();
      } else if(GUIObserverType.CONNECTION_FAIL.equals(object.getType())) {
        statusBar.setText("Verbindungsfehler: " + object.getSendingObject());
      } else if(GUIObserverType.INITIALISE_CARDS.equals(object.getType())) {
        final Dimension dim = GameCardWidget.computeCardDimension(playerPanel.getHeight());
        final Rectangle rect = new Rectangle(new Point(10, playerPanel.getHeight()-dim.height-10), dim);
        playerPanel.placeCards((List<DTOCard>) object.getSendingObject(), rect);
      } else if(GUIObserverType.INITIALISE_STACK.equals(object.getType())) {
        DTOCardStack stack = (DTOCardStack) object.getSendingObject();
        cardStackPanel.initialiseStack(stack.getSize(),stack.getCardStack().getLast());
      } else if(GUIObserverType.INITIALISE_OPPONENTS.equals(object.getType())) {
        List<ClientInfo> opponents = (List<ClientInfo>) object.getSendingObject();
        for (ClientInfo opponent : opponents) {
          System.out.println(opponent.getCardCount());
          if(!SetUpFrame.getInstance().getClientInfo().equalsID(opponent)) {
            opponentsPanel.addOpponent(opponent);
          }
        }
      }
    }
  }
}


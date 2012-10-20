package client.gui.frame;

import client.StartClient;
import client.business.GameClient;
import client.gui.frame.setup.SetUpFrame;
import dto.DTOCardStack;
import dto.message.BroadcastType;
import dto.message.GUIObserverType;
import dto.message.MessageObject;
import resources.ResourceGetter;
import utilities.Converter;
import utilities.constants.GameCardConstants;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
public class ClientFrame extends JFrame implements Observer {
  private static final Logger LOGGER = Logger.getLogger(ClientFrame.class.getName());

  private JPanel secondPane;
  private ChatPanel chatPanel;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel playerPanel;
  private DurakStatusBar statusBar;
  private DurakToolBar toolBar;

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
    initCards();
    this.addComponentListener(new CardResizer());
    this.setVisible(true);
  }

  /* Methods */
  private JPanel getSecondPane() {
    if(secondPane != null)
      return secondPane;

    secondPane = new JPanel();
    opponentsPanel = new OpponentsPanel();
    cardStackPanel = new CardStackPanel();
    playerPanel = new GamePanel();

    secondPane.setBackground(Color.WHITE);
    opponentsPanel.setPreferredSize(new Dimension(0, OPPONENT_PANEL_HEIGHT));
    cardStackPanel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, 0));
    cardStackPanel.setLayout(new BorderLayout());
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_START);
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_END);

    secondPane.setLayout(new BorderLayout());
    secondPane.add(opponentsPanel, BorderLayout.PAGE_START);
    secondPane.add(cardStackPanel, BorderLayout.LINE_START);

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

  private void initComponents() {
    toolBar = new DurakToolBar(this);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(getSecondPane(), BorderLayout.CENTER);
  }

  public void initCards() {
    playerPanel.placeCards();
    cardStackPanel.setStack(36, BorderLayout.CENTER);
    cardStackPanel.setCardBack(CARD_BACK);
    cardStackPanel.setTrumpCard(ResourceGetter.getCardImage(
        ResourceGetter.STRING_CARD_COLOUR_CLUBS, GameCardConstants.CardValue.ACE, "Trump"));
    opponentsPanel.addOpponent("Mark", 6);
    opponentsPanel.addOpponent("J\u00fcrgen", 6);
  }

  public void setStatusBarText(String text) {
    statusBar.setText(text);
  }

  public void update(Observable o, Object arg) {
    final MessageObject object = (MessageObject) arg;
    handleUpdate(object);
  }

  private void handleUpdate(MessageObject object) {
    if(object == null)
      return;

    final Class<? extends Enum> enumClass = object.getType().getClass();
    if(enumClass.equals(GUIObserverType.class)) {
      handleGUIObserverType(object);
    } else if(enumClass.equals(BroadcastType.class)) {
      handleBroadcastType(object);
    }
  }

  private void handleBroadcastType(MessageObject object) {
    if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
      chatPanel.addMessage((String) object.getSendingObject());
    } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
      //TODO login liste aktualisieren
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
    } else if(GUIObserverType.CONNECTION_FAIL.equals(object.getType())) {
      statusBar.setText("Verbindungsfehler: " + object.getSendingObject());
    } else if(GUIObserverType.INITIALISE_GAME.equals(object.getType())) {
      cardStackPanel.updateStack(Converter.fromDTO((DTOCardStack) object.getSendingObject()));
    } else if(GUIObserverType.LOGGED_IN.equals(object.getType())) {
      //TODO Spieler anzeigen, die eingeloggt sind
    }
  }
  /* Getter and Setter */

  private class CardResizer implements ComponentListener {
    public void componentResized(ComponentEvent e) {
      if(playerPanel != null)
        playerPanel.repaintCards();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
  }
}


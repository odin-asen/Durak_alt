package client.gui.frame;

import client.StartClient;
import client.business.GameClient;
import dto.DTOCardStack;
import dto.message.BroadcastType;
import dto.message.GUIObserverType;
import dto.message.MessageObject;
import dto.message.MessageType;
import resources.ResourceGetter;
import utilities.Converter;
import utilities.constants.GameCardConstants;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
  public static final int CHAT_WRITE_AREA_HEIGHT = 70;
  public static final String BUTTON_NAME_SEND = "Senden";

  private JPanel secondPane;
  private JPanel chatPanel;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel playerPanel;
  private DurakStatusBar statusBar;
  private DurakToolBar toolBar;
  private JButton sendButton;
  private JTextArea chatWriteArea;
  private JTextArea chatReadArea;

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
  private void initSecondPane() {
    secondPane = new JPanel();
    opponentsPanel = new OpponentsPanel();
    cardStackPanel = new CardStackPanel();
    playerPanel = new GamePanel();

    initStatusPanel();

    secondPane.setBackground(Color.WHITE);
    opponentsPanel.setPreferredSize(new Dimension(0, OPPONENT_PANEL_HEIGHT));
    cardStackPanel.setPreferredSize(new Dimension(CARDSTACK_PANEL_WIDTH, 0));
    cardStackPanel.setLayout(new BorderLayout());
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_START);
    cardStackPanel.add(Box.createGlue(), BorderLayout.PAGE_END);

    secondPane.setLayout(new BorderLayout());
    secondPane.add(opponentsPanel, BorderLayout.PAGE_START);
    secondPane.add(cardStackPanel, BorderLayout.LINE_START);

    secondPane.add(getChatPanel(), BorderLayout.LINE_END);
    secondPane.add(playerPanel, BorderLayout.CENTER);
    secondPane.add(statusBar, BorderLayout.PAGE_END);
  }

  private JPanel getChatPanel() {
    if(chatPanel == null) {
      chatPanel = new JPanel();
      sendButton = new JButton(BUTTON_NAME_SEND);
      chatWriteArea = new JTextArea();
      chatReadArea = new JTextArea();

      final JScrollPane scrollPaneRead = new JScrollPane();
      final JScrollPane scrollPaneWrite = new JScrollPane();
      final JPanel buttonPanel = new JPanel();

      sendButton.setActionCommand(BUTTON_NAME_SEND);
      sendButton.addActionListener(new ButtonListener());
      buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.add(sendButton);
      buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, sendButton.getPreferredSize().height));

      chatReadArea.setEditable(false);
      chatWriteArea.addKeyListener(new KeyboardListener());
      scrollPaneRead.setViewportView(chatReadArea);
      scrollPaneWrite.setViewportView(chatWriteArea);
      scrollPaneWrite.setPreferredSize(new Dimension(Integer.MAX_VALUE, CHAT_WRITE_AREA_HEIGHT));
      scrollPaneWrite.setMaximumSize(scrollPaneWrite.getPreferredSize());

      chatPanel.setPreferredSize(new Dimension(300, chatPanel.getPreferredSize().height));
      chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.PAGE_AXIS));
      chatPanel.add(scrollPaneRead);
      chatPanel.add(scrollPaneWrite);
      chatPanel.add(buttonPanel);
      //TODO globale components, die nicht in Listenern oder anderweiteig verwendet werden, eliminieren und init-Methoden durch getMethoden ersetzen
    }
    return chatPanel;
  }
  private void initStatusPanel() {
    statusBar = new DurakStatusBar();
    statusBar.setPreferredSize(new Dimension(0, 16));
  }

  private void initComponents() {
    toolBar = new DurakToolBar(this);
    initSecondPane();

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(secondPane, BorderLayout.CENTER);
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
    System.out.println("update");
    if(enumClass.equals(GUIObserverType.class)) {
      handleGUIObserverType(object);
    } else if(enumClass.equals(BroadcastType.class)) {
      handleBroadcastType(object);
    }
  }

  private void handleBroadcastType(MessageObject object) {
    if(BroadcastType.CHAT_MESSAGE.equals(object.getType())) {
      chatReadArea.append((String) object.getSendingObject());
    } else if(BroadcastType.LOGIN_LIST.equals(object.getType())) {
      //TODO login liste aktualisieren
    }
  }

  private void handleGUIObserverType(MessageObject object) {
    if(GUIObserverType.CONNECTED.equals(object.getType())) {
      statusBar.setConnected(true, (String) object.getSendingObject());
      statusBar.setText("Verbindung zu "+object.getSendingObject()+" wurde erfolgreich aufgebaut");
    } else if(GUIObserverType.DISCONNECTED.equals(object.getType())) {
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

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(BUTTON_NAME_SEND)) {
        new Thread(new Runnable() {
          public void run() {
            String text = chatWriteArea.getText();
            chatWriteArea.setText("");
            GameClient.getClient().send(new MessageObject(MessageType.CHAT_MESSAGE, text));
          }
        }).start();
      }
    }
  }

  private class KeyboardListener implements KeyListener {
    @Override
    public void keyTyped(KeyEvent e) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    public void keyPressed(KeyEvent e) {
      if(e.getModifiers() == KeyEvent.CTRL_MASK) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendButton.doClick();
        }
      }
    }

    @Override
    public void keyReleased(KeyEvent e) {
      //To change body of implemented methods use File | Settings | File Templates.
    }
  }

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


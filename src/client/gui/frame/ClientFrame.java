package client.gui.frame;

import client.StartClient;
import client.business.GameClient;
import dto.observer.GUIObserverConstants;
import dto.observer.ObserverUpdateObject;
import resources.ResourceGetter;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
public class ClientFrame extends JFrame implements Observer {
  private static final Logger LOGGER = Logger.getLogger(ClientFrame.class.getName());

  private static final float SCREEN_SIZE_FENSTER = 0.8f;

  public static final String APPLICATION_NAME = "Durak, das PC Spiel";
  public static final String TITLE_SEPARATOR = " - ";
  public static final String VERSION = "Version 0.0";
  public static final int OPPONENT_PANEL_HEIGHT = 70;
  public static final int CARDSTACK_PANEL_WIDTH = 250;
  public static final int CARDSTACK_PANEL_VERTICAL_INSET = 80;

  private JPanel secondPane;
  private OpponentsPanel opponentsPanel;
  private CardStackPanel cardStackPanel;
  private GamePanel playerPanel;
  private DurakStatusBar statusBar;
  private DurakToolBar toolBar;

  /* Constructors */
  public ClientFrame() {
    final FensterPositionen positionen = FensterPositionen.createFensterPositionen(
        SCREEN_SIZE_FENSTER, SCREEN_SIZE_FENSTER);
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
    secondPane.add(playerPanel, BorderLayout.CENTER);
    secondPane.add(statusBar, BorderLayout.PAGE_END);
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
    cardStackPanel.setCardBack(CardStackPanel.CARD_BACK);
    cardStackPanel.setTrumpCard(ResourceGetter.getCardImage(
        ResourceGetter.STRING_CARD_ACE,"Trump"));
    opponentsPanel.addOpponent("Mark", 6);
    opponentsPanel.addOpponent("J\u00fcrgen", 6);
  }

  public void setStatusBarText(String text) {
    statusBar.setText(text);
  }

  public void update(Observable o, Object arg) {
    final ObserverUpdateObject object = (ObserverUpdateObject) arg;
    handleUpdate(object);
  }

  private void handleUpdate(ObserverUpdateObject object) {
    if(GUIObserverConstants.CONNECTED.equals(object.getObserverConstant())) {
      statusBar.setConnected(true, (String) object.getInformation());
      statusBar.setText("Verbindung zu "+object.getInformation()+" wurde erfolgreich aufgebaut");
    } else if(GUIObserverConstants.DISCONNECTED.equals(object.getObserverConstant())) {
      statusBar.setConnected(false, null);
      statusBar.setText("");
    } else if(GUIObserverConstants.CONNECTION_FAIL.equals(object.getObserverConstant())) {
      statusBar.setText("Verbindungsfehler: "+object.getInformation());
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
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void componentShown(ComponentEvent e) {
      //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void componentHidden(ComponentEvent e) {
      //To change body of implemented methods use File | Settings | File Templates.
    }
  }
}


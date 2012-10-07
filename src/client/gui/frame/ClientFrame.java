package client.gui.frame;

import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
public class ClientFrame extends JFrame {
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
  private JPanel statusPanel;
  private JLabel statusBar;
  private DurakToolBar toolBar;

  public ClientFrame() {
    final FensterPositionen positionen = FensterPositionen.createFensterPositionen(
        SCREEN_SIZE_FENSTER, SCREEN_SIZE_FENSTER);

    this.setTitle(APPLICATION_NAME + TITLE_SEPARATOR + VERSION);
    this.setBounds(positionen.getRectangle());
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initComponents();
    initCards();
    this.addComponentListener(new CardResizer());
    this.setVisible(true);
  }

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
    secondPane.add(statusPanel, BorderLayout.PAGE_END);
  }

  private void initStatusPanel() {
    statusPanel = new JPanel();
    statusBar = new JLabel("Test für die Statusleiste");

    statusPanel.setPreferredSize(new Dimension(0, 16));

    statusPanel.setLayout(new BorderLayout());
    statusPanel.add(Box.createRigidArea(new Dimension(5,0)), BorderLayout.LINE_START);
    statusPanel.add(statusBar, BorderLayout.CENTER);
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
    cardStackPanel.addStack(36, BorderLayout.CENTER);
    opponentsPanel.addOpponent("Peter", 6);
    opponentsPanel.addOpponent("Klaus", 6);
  }

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


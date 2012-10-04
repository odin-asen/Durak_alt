package client.gui;

import client.gui.DurakToolBar;
import utilities.gui.FensterPositionen;

import javax.swing.*;
import java.awt.*;

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
  public static final int CARDSTACK_PANEL_WIDTH = 70;

  private JPanel secondPane;
  private JPanel opponentsPanel;
  private JPanel cardStackPanel;
  private JPanel playerPanel;
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

    this.setVisible(true);
  }

  private void initSecondPane() {
    secondPane = new JPanel();
    opponentsPanel = new JPanel();
    cardStackPanel = new JPanel();
    playerPanel = new JPanel();

    initStatusPanel();

    secondPane.setBackground(Color.WHITE);
    opponentsPanel.setBackground(Color.WHITE);
    opponentsPanel.setPreferredSize(new Dimension(0, OPPONENT_PANEL_HEIGHT));
    cardStackPanel.setBackground(Color.WHITE);
    cardStackPanel.setPreferredSize(new Dimension(CARDSTACK_PANEL_WIDTH, 0));
    playerPanel.setBackground(Color.WHITE);

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
}


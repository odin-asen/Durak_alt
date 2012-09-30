package client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

/**
 * User: Timm Herrmann
 * Date: 29.09.12
 * Time: 22:37
 */
public class MainFrame extends JFrame implements ActionListener {
  private static final float SCREEN_SIZE_FENSTER = 0.7f;

  public static final String APPLICATION_NAME = "Durak, das PC Spiel";
  public static final String TITLE_SEPARATOR = " - ";
  public static final String VERSION = "Version 0.0";
  public static final String ACTION_COMMAND_CONNECTION = "connection";
  public static final String ACTION_COMMAND_SETUP = "setup";
  public static final String ACTION_COMMAND_CLOSE = "close";

  private String title;

  private JPanel gamePanel;
  private JPanel opponentsPanel;
  private JPanel cardStackPanel;
  private JPanel playerPanel;
  private JPanel statusPanel;
  private JLabel statusBar;
  private JToolBar toolBar;
  private JButton connectionButton;
  private JButton setUpButton;
  private JButton closeButton;
  private boolean setUpOpen;

  public MainFrame() {
    final FensterPositionen positionen = new FensterPositionen(
        Toolkit.getDefaultToolkit().getScreenSize(), SCREEN_SIZE_FENSTER, SCREEN_SIZE_FENSTER);

    this.setTitle("");
    this.setBounds(positionen.getRectangle());
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    initComponents();

    this.setSetUpOpen(false);
    this.setVisible(true);
  }

  private void initToolBar() {
    toolBar = new JToolBar();
    connectionButton = makeToolBarButton("Network connection", "Verbindung zu Server aufbauen",
        ACTION_COMMAND_CONNECTION, "Verbindung");
    setUpButton = makeToolBarButton("Pinion32", "\u00d6ffnet Fenster f\u00fcr Einstellungen",
        ACTION_COMMAND_SETUP, "Einstellungen");
    closeButton = makeToolBarButton("Close32","Schlie\u00dft die Anwendung",
        ACTION_COMMAND_CLOSE,"Schlie\u00dfen");

    toolBar.setPreferredSize(new Dimension(0, 64));
    toolBar.setFloatable(false);
    toolBar.setRollover(true);

    toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.LINE_AXIS));
    toolBar.addSeparator();
    toolBar.add(connectionButton);
    toolBar.addSeparator();
    toolBar.add(setUpButton);
    toolBar.add(Box.createHorizontalGlue());
    toolBar.addSeparator();
    toolBar.add(closeButton);
    toolBar.addSeparator();
  }

  private JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, String alternativeText) {
    String imgLocation = "../resources/icons/"
        + pictureName
        + ".png";
    URL imageURL = MainFrame.class.getResource(imgLocation);

    JButton button = new JButton();
    button.setActionCommand(actionCommand);
    button.setToolTipText(toolTipText);
    button.addActionListener(this);

    if (imageURL != null) {
      button.setIcon(new ImageIcon(imageURL, alternativeText));
    } else {
      button.setText(alternativeText);
      System.err.println("Resource not found: " + imgLocation);
    }

    return button;
  }

  private void initGamePanel() {
    gamePanel = new JPanel();
    opponentsPanel = new JPanel();
    cardStackPanel = new JPanel();
    playerPanel = new JPanel();

    gamePanel.setBackground(Color.WHITE);
    opponentsPanel.setBackground(Color.WHITE);
    cardStackPanel.setBackground(Color.WHITE);
    playerPanel.setBackground(Color.WHITE);

    gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.PAGE_AXIS));
    gamePanel.add(opponentsPanel);
    gamePanel.add(Box.createRigidArea(new Dimension(0, 0)));
    gamePanel.add(cardStackPanel);
    gamePanel.add(Box.createRigidArea(new Dimension(0, 0)));
    gamePanel.add(playerPanel);
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
    initToolBar();
    initGamePanel();
    initStatusPanel();

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(toolBar, BorderLayout.PAGE_START);
    getContentPane().add(gamePanel, BorderLayout.CENTER);
    getContentPane().add(statusPanel, BorderLayout.PAGE_END);
  }

  public void setTitle(String title) {
    String newTitle = APPLICATION_NAME + TITLE_SEPARATOR + VERSION;
    newTitle = title.isEmpty() ? newTitle : newTitle+TITLE_SEPARATOR+title;
    this.title = title;
    super.setTitle(newTitle);
  }

  public String getTitle() {
    return this.title;
  }

  public void setSetUpOpen(boolean open) {
    setUpOpen = open;
  }

  public boolean isSetUpOpen() {
    return setUpOpen;
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(ACTION_COMMAND_CLOSE)) {
      this.setVisible(true);
      this.dispose();
      System.exit(0);
    } else if(e.getActionCommand().equals(ACTION_COMMAND_CONNECTION)) {

    } else if(e.getActionCommand().equals(ACTION_COMMAND_SETUP)) {
      if(!isSetUpOpen()) {
        openSetUpFrame();
      }
    }
  }

  private void openSetUpFrame() {
    new SetUpFrame();
  }
}

class FensterPositionen {
  private int posX;
  private int posY;
  private int breite;
  private int hoehe;
  private Rectangle rectangle;

  public FensterPositionen(Dimension screenSize, float screenSizeFensterBreite, float screenSizeFensterHoehe) {
    if(screenSize == null)
      screenSize = new Dimension(1366,768);

    breite = (int) (screenSize.width* Math.abs(screenSizeFensterBreite));
    hoehe= (int) (screenSize.height* Math.abs(screenSizeFensterHoehe));
    posX = (int) (screenSize.width*0.5f-breite*0.5f);
    posY = (int) (screenSize.height*0.5f-hoehe*0.5f);
    rectangle = new Rectangle(posX,posY,breite,hoehe);
  }

  public int getPosX() {
    return posX;
  }

  public int getPosY() {
    return posY;
  }

  public int getBreite() {
    return breite;
  }

  public int getHoehe() {
    return hoehe;
  }

  public String toString() {
    return "PosX: "+posX+"; "+
        "PosY: "+posY+"; "+
        "Breite: "+breite+"; "+
        "Hoehe: "+hoehe+"; ";
  }

  public Rectangle getRectangle() {
    return rectangle;
  }
}
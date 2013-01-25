package client.gui.frame.playerTypePanel;

import client.gui.frame.*;
import client.gui.frame.gamePanel.GamePanel;
import common.dto.DTOCardStack;
import common.dto.DTOClient;
import common.game.GameCard;
import common.i18n.BundleStrings;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.*;
import static common.utilities.constants.PlayerConstants.PlayerType;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 00:37
 */
public class PlayerTypePanel extends JPanel {
  private static final Logger LOGGER = LoggingUtility.getLogger(PlayerTypePanel.class.getName());

  private JPanel listPanel;
  private JList<DTOClient> clientList;
  private DurakStatusBar statusBar;

  private JPanel cardLayoutPanel;
  private CardLayout cardLayout;
  private Map<PlayerType, AbstractDurakGamePanel> panelMap;
  private PlayerType currentType;

  /* Game relevant attributes to update panels after a layout/PlayerType change */
  private List<GameCard> handCards;
  private List<GameCard> attackCards;
  private List<GameCard> defenseCards;
  private DTOCardStack cardStack;
  private List<DTOClient> opponents;

  /* Constructors */
  public PlayerTypePanel(PlayerType type) {
    currentType = type;
    cardLayout = new CardLayout();
    panelMap = new HashMap<PlayerType, AbstractDurakGamePanel>(PlayerType.values().length);

    handCards = new ArrayList<GameCard>();
    attackCards = new ArrayList<GameCard>(6);
    defenseCards = new ArrayList<GameCard>(6);
    cardStack = new DTOCardStack();
    opponents = new ArrayList<DTOClient>();

    initGamePanels();
    setLayout(new BorderLayout());

    final JSplitPane splitPane = initMainSplitPane();
    add(splitPane, BorderLayout.CENTER);
    add(getStatusBarContainer(), BorderLayout.PAGE_END);
  }

  private JSplitPane initMainSplitPane() {
    final int dividerSize = 10;
    final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(getClientListContainer());
    splitPane.setRightComponent(cardLayoutPanel);
    splitPane.setDividerLocation((int) (ClientFrame.getInstance().getWidth()*0.15));
    splitPane.setDividerSize(dividerSize);

    splitPane.setOneTouchExpandable(true);
    UIManager.put("SplitPane.supportsOneTouchButtons", Boolean.TRUE);

    UIManager.put("SplitPane.oneTouchButtonSize", dividerSize);
    UIManager.put("SplitPane.centerOneTouchButtons", Boolean.TRUE);
    splitPane.updateUI();

    return splitPane;
  }

  /* Methods */

  private void initGamePanels() {
    cardLayoutPanel = new JPanel(cardLayout);
    addPanel(PlayerType.DEFAULT, new DefaultPanel());
    addPanel(PlayerType.FIRST_ATTACKER, new AttackerPanel(true));
    addPanel(PlayerType.SECOND_ATTACKER, new AttackerPanel(false));
    addPanel(PlayerType.DEFENDER, new DefenderPanel());
    addPanel(PlayerType.NOT_LOSER, new DefaultPanel());
    addPanel(PlayerType.LOSER, new DefaultPanel());
    cardLayout.show(cardLayoutPanel, currentType.getDescription());
  }

  private void addPanel(PlayerType type, AbstractDurakGamePanel panel) {
    cardLayoutPanel.add(panel);
    panelMap.put(type, panel);
    cardLayout.addLayoutComponent(panel, type.getDescription());
  }

  /**********************/
  /*** Update Methods ***/
  /**********************/

  /**
   * Notifies a change to the gui layout and handling.
   *
   * @param type Specification for the layout and handling type.
   */
  public void setPlayerType(PlayerType type) {
    if (!currentType.equals(type)) {
      currentType = type;
      cardLayout.show(cardLayoutPanel, type.getDescription());
      updateCurrentGamePanel();
      statusBar.setPlayerType(currentType);
    }
  }

  private void updateCurrentGamePanel() {
    AbstractDurakGamePanel panel = panelMap.get(currentType);

    updateStack(cardStack);
    setCards(attackCards, defenseCards, handCards);
    updateOpponents(opponents, false);

    panel.getGameProcessContainer().setListenerType(currentType);
  }

  public void setStatus(String mainText) {
    statusBar.setText(mainText);
  }

  public void setStatus(boolean connected, String serverAddress) {
    statusBar.setConnected(connected, serverAddress);
    statusBar.setPlayerType(currentType);
  }

  public void setStatus(boolean connected) {
    statusBar.setConnected(connected);
  }

  /**
   * Sets the specified cards to the corresponding panel to display. If {@code handCards}
   * is null the hand cards will not be changed. If {@code attackCards} is null the ingame
   * cards will not be changed. An empty list will remove the specified cards.
   * Note: Defender cards can never be shown without the appropriate attacker card
   *
   * @param attackerCards The ingame attack cards.
   * @param defenderCards The ingame defense cards.
   * @param handCards     The clients hand cards.
   */
  public void setCards(List<GameCard> attackerCards, List<GameCard> defenderCards,
                       List<GameCard> handCards) {
    final GamePanel gamePanel = panelMap.get(currentType).getGameProcessContainer();

    if (handCards != null) {
      this.handCards = handCards;
      gamePanel.setHandCards(handCards);
    }

    if (attackerCards != null) {
      this.attackCards = attackerCards;
      this.defenseCards = defenderCards;
      gamePanel.setIngameCards(attackerCards, defenderCards);
    }

    if (handCards != null || attackerCards != null)
      gamePanel.updateCards();
  }

  /**
   * Updates the hand cards of the client with the surpassed cards.
   */
  public void setCards(List<GameCard> handCards) {
    setCards(null,null,handCards);
  }

  /**
   * Updates the attack and defense cards on the ingame field with the surpassed cards.
   */
  public void setCards(List<GameCard> attackerCards, List<GameCard> defenderCards) {
    setCards(attackerCards, defenderCards, null);
  }

  public void updateStack(DTOCardStack cardStack) {
    setStackValue(cardStack);
    panelMap.get(currentType).getCardStackContainer().updateStack(cardStack);
  }

  public void updateOpponents(List<DTOClient> clients, boolean remove) {
    setOpponentsValue(clients);
    final OpponentsPanel panel = panelMap.get(currentType).getOpponentsContainer();
    if (remove) panel.removeAllOpponents();
    else panel.updateOpponents(clients);
  }

  public void initOpponents(List<DTOClient> clients) {
    final OpponentsPanel panel = panelMap.get(currentType).getOpponentsContainer();
    panel.removeAllOpponents();
    for (DTOClient client : clients)
      panel.addOpponent(client);
    panel.updateOpponents(clients);
  }

  public void resetGameWidgets() {
    for (AbstractDurakGamePanel panel : panelMap.values()) {
      panel.enableGameButtons(false, false);
      panel.getGameProcessContainer().deleteCards();
      panel.getOpponentsContainer().removeAllOpponents();
      panel.getCardStackContainer().deleteCards();
    }
  }

  /**
   * Enables the game buttons depending on the players type and the surpassed boolean values.
   * It also notifies the client via popup if the next round is available or if the defender
   * took the cards or not. (only for non defender players).
   * @param roundFinished Specifies the enabled state of the buttons.
   * @param defenderTookCards Specifies the popup message, if the next round is available.
   * @param attackerFinished Specifies the popup message, if all attacker finished the round.
   */
  public void updateRoundInfo(boolean roundFinished, boolean defenderTookCards,
                              boolean attackerFinished) {
    panelMap.get(currentType).enableGameButtons(roundFinished, attackerFinished);
    if (roundFinished) {
      for (AbstractDurakGamePanel panel : panelMap.values())
        panel.setNewRound();

      if(defenderTookCards && !currentType.equals(PlayerType.DEFENDER)) {
        final String key = "defender.took.cards." + defenderTookCards; //NON-NLS
        ClientFrame.getInstance().showGamePopup(I18nSupport.getValue(BundleStrings.USER_MESSAGES, key));
      }
    }
  }

  /**
   * Updates the client list. If clients is null, all clients will be removed from the list.
   *
   * @param clients All clients to show on the list.
   */
  public <DTOClient> void updateClients(List<DTOClient> clients) {
    final DefaultListModel<DTOClient> listModel =
        ((DefaultListModel<DTOClient>) clientList.getModel());
    listModel.clear();

    if (clients != null) {
      for (DTOClient client : clients)
        listModel.add(listModel.size(), client);
    }
  }

  /* Getter and Setter */

  /**
   * Returns a JPanel instance that contains a JList to display the clients. The JList
   * can be scrolled.
   *
   * @return A panel displaying the client list.
   */
  public JPanel getClientListContainer() {
    if (listPanel != null)
      return listPanel;

    listPanel = new JPanel();
    clientList = new JList<DTOClient>(new DefaultListModel<DTOClient>());
    final JScrollPane listScrollPane = new JScrollPane(clientList);

    listPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(BundleStrings.CLIENT_GUI, "border.opponents")));
    listPanel.setLayout(new BorderLayout());
    listPanel.add(listScrollPane, BorderLayout.CENTER);
    listPanel.setMinimumSize(new Dimension(0,0));

    clientList.setCellRenderer(new ClientListCellRenderer());
    clientList.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    listScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    return listPanel;
  }

  /**
   * Returns a default instance of the DurakStatusBar class with the preferred width of 0 and
   * a preferred height of {@link ClientGUIConstants#STATUS_BAR_HEIGHT}.
   *
   * @return A DurakStatusBar object.
   */
  public DurakStatusBar getStatusBarContainer() {
    if (statusBar != null)
      return statusBar;

    statusBar = new DurakStatusBar();
    statusBar.setPreferredSize(new Dimension(0, STATUS_BAR_HEIGHT));

    return statusBar;
  }

  private void setStackValue(DTOCardStack stack) {
    if(stack != null) {
      if(!cardStack.equals(stack)) {
        cardStack = stack;
      }
    } else cardStack = new DTOCardStack();
  }

  private void setOpponentsValue(List<DTOClient> opponents) {
    if(opponents != null) {
      if(!this.opponents.equals(opponents)) {
        this.opponents= opponents;
      }
    } else this.opponents = new ArrayList<DTOClient>();
  }

  /* Inner Classes */

  private class ClientListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component superComponent = super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);

      if (value == null)
        return this;

      final DTOClient client = (DTOClient) value;
      final Color foreground;
      if (client.spectating) {
        foreground = new Color(164, 164, 164);
        this.setToolTipText(I18nSupport.getValue(BundleStrings.CLIENT_GUI, "list.tooltip.audience"));
      } else {
        foreground = superComponent.getForeground();
        this.setToolTipText(null);
      }
      this.setText(client.name);
      this.setBackground(superComponent.getBackground());
      this.setForeground(foreground);

      return this;
    }
  }
}

/**
 * This class is the default abstract implementation for the getter methods of the interface
 * {@link client.gui.frame.DurakGamePanel}.
 */
abstract class AbstractDurakGamePanel extends JPanel implements DurakGamePanel {
  private final Logger LOGGER = LoggingUtility.getLogger(AbstractDurakGamePanel.class.getName());

  private OpponentsPanel opponentsPanel;
  private GamePanel gamePanel;
  private CardStackPanel panel;
  private JPanel gameButtonsPanel;

  /* Methods */

  /**
   * Initialises the containers with default settings.
   */
  protected void init() {
    /* Cards Stack Panel */
    JPanel panel = getCardStackContainer();

    panel.setPreferredSize(new Dimension(CARD_STACK_PANEL_WIDTH, panel.getPreferredSize().height));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    /* Opponents Panel */
    panel = getOpponentsContainer();
    panel.setPreferredSize(new Dimension(0, OPPONENT_PANEL_HEIGHT));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
  }

  /* Getter and Setter */

  /**
   * Returns a default instance of the CardStackPanel class. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}.
   *
   * @return Return a CardStackPanel object.
   */
  public CardStackPanel getCardStackContainer() {
    if (panel != null)
      return panel;

    panel = new CardStackPanel();
    panel.setBackground(GAME_TABLE_COLOUR);

    return panel;
  }

  /**
   * Returns a JPanel that contains no buttons. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}.
   *
   * @return The game buttons container.
   */
  public JPanel getGameButtonsContainer() {
    if (gameButtonsPanel != null)
      return gameButtonsPanel;

    gameButtonsPanel = new JPanel();
    gameButtonsPanel.setBackground(GAME_TABLE_COLOUR);

    return gameButtonsPanel;
  }

  /**
   * Returns a default instance of the OpponentsPanel class. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}.
   *
   * @return The a OpponentsPanel object.
   */
  public OpponentsPanel getOpponentsContainer() {
    if (opponentsPanel != null)
      return opponentsPanel;

    opponentsPanel = new OpponentsPanel();
    opponentsPanel.setBackground(GAME_TABLE_COLOUR);

    return opponentsPanel;
  }

  /**
   * Returns an instance of the GamePanel class. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}. This panel doesn't show the clients
   * hand cards panel. The visibility of the hand card panel can be modified with the
   * method {@link GamePanel#setHandCardsVisible(boolean)}.
   *
   * @return A GamePanel object.
   */
  public GamePanel getGameProcessContainer() {
    if (gamePanel != null)
      return gamePanel;

    gamePanel = new GamePanel(false);
    gamePanel.setBackground(GAME_TABLE_COLOUR);

    return gamePanel;
  }

  /* Inner classes */
}
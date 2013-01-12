package client.gui.frame.playerTypePanel;

import client.gui.frame.*;
import client.gui.frame.gamePanel.GamePanel;
import common.dto.DTOCard;
import common.dto.DTOCardStack;
import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.GAME_TABLE_COLOUR;
import static client.gui.frame.ClientGUIConstants.STATUS_BAR_HEIGHT;
import static common.utilities.constants.PlayerConstants.*;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 00:37
 */
public class PlayerTypePanel extends JPanel {
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS

  private CardLayout cardLayout;
  private Map<PlayerType,DurakCentrePanelImpl> panelMap;
  private PlayerType currentType;

  /* Constructors */
  public PlayerTypePanel(PlayerType type) {
    cardLayout = new CardLayout();
    panelMap = new HashMap<PlayerType,DurakCentrePanelImpl>(PlayerType.values().length);
    setLayout(cardLayout);
    initPanels();
    setPlayerType(type);
  }

  /* Methods */

  private void initPanels() {
    addPanel(PlayerType.DEFAULT, new DefaultPanel());
    addPanel(PlayerType.FIRST_ATTACKER, new DefaultPanel());
    addPanel(PlayerType.SECOND_ATTACKER, new DefaultPanel());
    addPanel(PlayerType.DEFENDER, new DefaultPanel());
    addPanel(PlayerType.NOT_LOSER, new DefaultPanel());
    addPanel(PlayerType.LOSER, new DefaultPanel());
  }

  private void addPanel(PlayerType type, DurakCentrePanelImpl panel) {
    add(panel);
    panelMap.put(type, panel);
    cardLayout.addLayoutComponent(panel, type.getDescription());
  }

  public void setPlayerType(PlayerType type) {
    currentType = type;
    cardLayout.show(this, type.getDescription());
  }

  /* Getter and Setter */

  public void setStatus(String mainText) {
    panelMap.get(currentType).getStatusBarContainer().setText(mainText);
  }

  public void setStatus(Boolean connected, String serverAddress) {
    final DurakStatusBar statusBar = panelMap.get(currentType).getStatusBarContainer();
    statusBar.setConnected(connected, serverAddress);
    statusBar.setPlayerType(currentType);
  }

  public void setStatus(Boolean connected) {
    panelMap.get(currentType).getStatusBarContainer().setConnected(connected);
  }

  /**
   * Sets the specified cards to the corresponding panel to display. If {@code handCards}
   * is null the hand cards will not be changed. If {@code attackCards} is null the ingame
   * cards will not be changed. An empty list will remove the specified cards.
   * Note: Defender cards can never be shown without the appropriate attacker card
   * @param attackerCards The ingame attack cards.
   * @param defenderCards The ingame defense cards.
   * @param handCards The clients hand cards.
   */
  public void setCards(List<DTOCard> attackerCards, List<DTOCard> defenderCards,
                       List<DTOCard> handCards) {
    final GamePanel panel = panelMap.get(currentType).getGameProcessContainer();
    if(handCards != null)
      panel.placeClientCards(handCards);
    if(attackerCards != null)
      panel.placeInGameCards(attackerCards, defenderCards);
  }

  public void updateStack(DTOCardStack cardStack) {
    panelMap.get(currentType).getCardStackContainer().updateStack(cardStack);
  }

  public void updateOpponents(List<DTOClient> clients, boolean remove) {
    final OpponentsPanel panel = panelMap.get(currentType).getOpponentsContainer();
    if(remove) panel.removeAllOpponents();
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
    final DurakCentrePanelImpl panel = panelMap.get(currentType);
    //TODO buttons noch einstellen
    panel.getGameProcessContainer().deleteCards();
    panel.getOpponentsContainer().removeAllOpponents();
    panel.getCardStackContainer().deleteCards();
  }

  public void enableButtons(Boolean roundFinished, Boolean defenderTookCards) {
    panelMap.get(currentType).enableGameButtons(roundFinished);
    if(roundFinished && defenderTookCards && !currentType.equals(PlayerType.DEFENDER)) {
      ClientFrame.getInstance().showInformationPopup(
          I18nSupport.getValue(MSGS_BUNDLE, "defender.took.cards."+defenderTookCards)); //NON-NLS
    }
  }
}

/**
 * This class is the default implementation for the getter methods of the interface
 * {@link client.gui.frame.DurakCentrePanel}.
 */
abstract class DurakCentrePanelImpl extends JPanel implements DurakCentrePanel {
  private final Logger LOGGER = LoggingUtility.getLogger(DurakCentrePanelImpl.class.getName());

  protected static final String CLIENT_BUNDLE = "client.client"; //NON-NLS

  private OpponentsPanel opponentsPanel;
  private GamePanel gamePanel;

  private CardStackPanel panel;
  private JPanel listContainer;
  private JList<DTOClient> clientList;

  private DurakStatusBar statusBar;
  private JPanel gameButtonsPanel;

  /* Methods */

  public <DTOClient> void updateClients(List<DTOClient> clients) {
    final DefaultListModel<DTOClient> listModel =
        ((DefaultListModel<DTOClient>) clientList.getModel());
    listModel.clear();

    if(clients != null) {
      for (DTOClient client : clients)
        listModel.add(listModel.size(), client);
    }
  }

  /* Getter and Setter */

//    takeCardsButton = WidgetCreator.makeButton(null,
//        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.take.cards"),
//        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.take.cards"), null,
//        new ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//            if (nextRoundRequest(true)) {
//              takeCardsButton.setEnabled(false);
//              roundDoneButton.setEnabled(false);
//              update.updateGamePanel(new ArrayList<DTOCard>(), new ArrayList<DTOCard>(), null);
//            }
//          }
//        });
//    roundDoneButton = WidgetCreator.makeButton(null,
//        I18nSupport.getValue(CLIENT_BUNDLE,"button.text.finish.round"),
//        I18nSupport.getValue(CLIENT_BUNDLE,"button.tooltip.finish.round"), null,
//        new ActionListener() {
//          public void actionPerformed(ActionEvent e) {
//            if(nextRoundRequest(false)) {
//              takeCardsButton.setEnabled(false);
//              roundDoneButton.setEnabled(false);
//            }
//          }
//        });
//    takeCardsButton.setEnabled(false);
//    roundDoneButton.setEnabled(false);

  /**
   * Returns a default instance of the CardStackPanel class. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}.
   * @return Return a CardStackPanel object.
   */
  public CardStackPanel getCardStackContainer() {
    if(panel != null)
      return panel;

    panel = new CardStackPanel();
    panel.setBackground(GAME_TABLE_COLOUR);

    return panel;
  }

  /**
   * Returns a JPanel that contains no buttons. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}.
   * @return The game buttons container.
   */
  public JPanel getGameButtonsContainer() {
    if(gameButtonsPanel != null)
      return gameButtonsPanel;

    gameButtonsPanel = new JPanel();
    gameButtonsPanel.setBackground(GAME_TABLE_COLOUR);

    return gameButtonsPanel;
  }

  /**
   * Returns a default instance of the OpponentsPanel class. The background is
   * {@link ClientGUIConstants#GAME_TABLE_COLOUR}.
   * @return The a OpponentsPanel object.
   */
  public OpponentsPanel getOpponentsContainer() {
    if(opponentsPanel != null)
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
   * @return A GamePanel object.
   */
  public GamePanel getGameProcessContainer() {
    if(gamePanel != null)
      return gamePanel;

    gamePanel = new GamePanel(false);
    gamePanel.setBackground(GAME_TABLE_COLOUR);
    //TODO in subklassen den listenertype setzen
    return gamePanel;
  }

  /**
   * Returns a default instance of the DurakStatusBar class with the preferred width of 0 and
   * a preferred height of {@link ClientGUIConstants#STATUS_BAR_HEIGHT}.
   * @return A DurakStatusBar object.
   */
  public DurakStatusBar getStatusBarContainer() {
    if(statusBar != null)
      return statusBar;

    statusBar = new DurakStatusBar();
    statusBar.setPreferredSize(new Dimension(0, STATUS_BAR_HEIGHT));

    return statusBar;
  }

  /**
   * Returns a JPanel instance that contains a JList to display the clients. The JList
   * can be scrolled.
   * @return A panel displaying the client list.
   */
  public JPanel getClientListContainer() {
    if(listContainer != null)
      return listContainer;

    listContainer = new JPanel();
    final JList<DTOClient> clientsList = new JList<DTOClient>(new DefaultListModel<DTOClient>());
    final JScrollPane listScrollPane = new JScrollPane(clientsList);

    listContainer.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE, "border.title.opponents")));
    listContainer.add(listScrollPane);

    clientsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
    clientsList.setCellRenderer(new ClientInfoCellRenderer());

    return listContainer;
  }

  /* Inner classes */

  private class ClientInfoCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component superComponent = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);

      if(value ==null)
        return this;

      final DTOClient client = (DTOClient) value;
      final Color foreground;
      if(client.spectating) {
        foreground = new Color(164, 164, 164);
        this.setToolTipText(I18nSupport.getValue(CLIENT_BUNDLE,"list.tooltip.audience"));
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
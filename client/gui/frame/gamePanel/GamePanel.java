package client.gui.frame.gamePanel;

import client.gui.widget.card.CardMoveListener;
import client.gui.widget.card.GameCardWidget;
import common.game.GameCard;
import common.utilities.constants.PlayerConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:13
 *
 * Panel that draws the players hand and the attack/defense phase.
 */
public class GamePanel extends JPanel implements CardContainer<GameCardWidget>{
  private static final Float USER_CARDS_PANEL_HEIGHT = 0.4f;

  private boolean handCardsVisible;

  private ClientWidgetHolder clientWidgets;
  private InGamePanel inGamePanel;
  private Rectangle ingameArea;

  /* Constructors */

  public GamePanel(boolean handCardsVisible) {
    inGamePanel= new InGamePanel();
    CardReplacer cardReplacer = new CardReplacer();
    clientWidgets = new ClientWidgetHolder(this);
    ingameArea = new Rectangle();

    setHandCardsVisible(handCardsVisible);
    add(inGamePanel);
    addComponentListener(cardReplacer);
  }

  /* Methods */
  /**
   * Depending on the parameter the hand cards panel will be visible or not.
   * @param visible Indicates the hand cards visibility.
   */
  public void setHandCardsVisible(boolean visible) {
    handCardsVisible = visible;
    if(visible) {
      setLayout(null);
      inGamePanel.setLocation(0,0);
    } else {
      setLayout(new BorderLayout());
    }
  }

  /**
   * Places every card of {@code attackerCards} to the panel and lays every card of
   * {@code defenderCards} with the same index a little shifted over the attacker card.
   * If {@code attackCards} is null the field will be cleared, independent of the second
   * parameter.
   * @param attackerCards Cards of the attacker.
   * @param defenderCards Cards of the defender.
   */
  public void setIngameCards(List<GameCard> attackerCards, List<GameCard> defenderCards) {
    if(attackerCards == null)
      inGamePanel.clearField();
    else {
      inGamePanel.setAttackCards(attackerCards);
      inGamePanel.setDefenseCards(defenderCards);
      inGamePanel.updateCards();
    }
    validate();
  }

  /**
   * Places the client cards. If the parameter is null, the client card area will be emptied.
   * @param cards Client cards to place.
   */
  public void setHandCards(List<GameCard> cards) {
    clientWidgets.setClientCards(cards);
    /* To ensure the accurate drawing of the cards */
    /* Changing the size calls the CardReplacers componentResized method */
    /* which updates the hand cards */
    setSize(getWidth() - 1, getHeight() - 1);
    setSize(getWidth() + 1, getHeight() + 1);
    validate();
    repaint();
  }

  public Rectangle computeClientCardArea() {
    if(handCardsVisible)
      return new Rectangle(0, (int) (getHeight()*(1.0f-USER_CARDS_PANEL_HEIGHT)),
        getWidth(), (int) (getHeight()*USER_CARDS_PANEL_HEIGHT));
    else return new Rectangle(0, getHeight(), getWidth(), 0);
  }

  public void deleteCards() {
    clientWidgets.clear();
    inGamePanel.clearField();
    validate();
  }

  public boolean hasInGameCards() {
    return inGamePanel.getCardPanels().size() > 0;
  }

  public boolean inGameCardsAreCovered() {
    return inGamePanel.allCardCovered();
  }

  public void setListenerType(PlayerConstants.PlayerType type) {
    final CardMoveListener cardManager;

    if (PlayerConstants.PlayerType.FIRST_ATTACKER.equals(type) ||
        PlayerConstants.PlayerType.SECOND_ATTACKER.equals(type)) {
      cardManager = CardMoveListener.getAttackerInstance(this, ingameArea,
          inGamePanel, 2.0f*ClientWidgetHolder.DISTANCE_CARD_Y);
    } else if (PlayerConstants.PlayerType.DEFENDER.equals(type)) {
      cardManager = CardMoveListener.getDefenderInstance(inGamePanel.getCardPanels(), this);
    } else {
      cardManager = CardMoveListener.getDefaultInstance();
    }
    clientWidgets.setCardManager(cardManager);
  }

  @SuppressWarnings("UnusedDeclaration")
  public void updateCards() {
    clientWidgets.updateCards(computeClientCardArea());
    inGamePanel.updateCards();
  }

  public boolean removeCard(GameCardWidget card) {
    if(clientWidgets.remove(card)) {
      clientWidgets.updateCards(computeClientCardArea());
      return true;
    } else return false;
  }

  public boolean addCard(GameCardWidget card) {
    if(clientWidgets.add(card)) {
      clientWidgets.updateCards(computeClientCardArea());
      return true;
    } else return false;
  }

  public boolean cardExists(GameCardWidget card) {
    return clientWidgets.contains(card);
  }

  /* Getter and Setter */

  /* Inner Classes */

  private class CardReplacer implements ComponentListener {
    public void componentResized(ComponentEvent e) {
      final Rectangle area = computeClientCardArea();
      ingameArea.setSize(new Dimension(getWidth(), getHeight() - area.height));
      inGamePanel.setSize(ingameArea.getSize());
      if(handCardsVisible)
        clientWidgets.updateCards(area);
    }

    public void componentMoved(ComponentEvent e) {}

    public void componentShown(ComponentEvent e) {}

    public void componentHidden(ComponentEvent e) {}
  }
}

class ClientWidgetHolder {
  public static final Float DISTANCE_CARD_X = 0.05f;
  public static final Float DISTANCE_CARD_Y = 0.08f;

  private CardMoveListener cardManager;
  private List<GameCardWidget> widgets;
  private List<GameCard> clientCards;
  private JComponent parent;

  /* Constructors */

  ClientWidgetHolder(JComponent parent) {
    this.parent = parent;
    widgets = new ArrayList<GameCardWidget>();
    cardManager = CardMoveListener.getDefaultInstance();
    clientCards = new ArrayList<GameCard>();
  }

  /* Methods */

  void updateCards(Rectangle area) {
    if(area != null && !area.isEmpty()) {
      final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);
      final double distance = computeRelativeCardToCardDistanceX(area, rect);
      updateWidgets(rect, distance);
    }
    cleanUpWidgetList();
    assert widgets.size() != clientCards.size();
  }

  private void updateWidgets(Rectangle rect, double distance) {
    for (int index = 0; index < clientCards.size(); index++) {
      if(widgets.size() > index) {
        widgets.get(index).setCard(clientCards.get(index));
        if(index > 0)
          rect.x = (int) (rect.x + rect.width*distance);
        widgets.get(index).setBounds(rect);
        parent.setComponentZOrder(widgets.get(index), 0);
      } else {
        rect.x = (int) (rect.x + rect.width * distance);
        addWidget(rect, clientCards.get(index));
      }
    }
  }

  private double computeRelativeCardToCardDistanceX(Rectangle cardArea, Rectangle cardBounds) {
    double distance = 1.0;
    final Integer cardCount = widgets.size();
    if(cardCount > 1) {
      final double width = cardArea.width - (cardArea.width*2.0*DISTANCE_CARD_X);
      if(cardCount*cardBounds.width > width)
        distance = (width-cardBounds.width)/(cardBounds.width*(cardCount-1));
      else distance = 1.0;
    }
    return distance;
  }

  private Rectangle getFirstCardBounds(Rectangle region, Float widthToHeight) {
    int height = (int) (region.height*(1.0f-2.0f*DISTANCE_CARD_Y));
    int width = (int) (height*widthToHeight);

    return new Rectangle(new Point(
        (int) (DISTANCE_CARD_X*region.getWidth()+region.x),
        (int) (2.0f*DISTANCE_CARD_Y*region.getHeight()+region.y)),
        new Dimension(width,height));
  }

  public boolean add(GameCardWidget widget) {
    parent.add(widget);
    widgets.add(widget);
    return clientCards.add(widget.getCardInfo());
  }

  public boolean remove(GameCardWidget widget) {
    parent.remove(widget);
    widgets.remove(widget);
    return clientCards.remove(widget.getCardInfo());
  }

  public void clear() {
    for (GameCardWidget widget : widgets)
      parent.remove(widget);
    clientCards.clear();
    widgets.clear();
  }

  private void cleanUpWidgetList() {
    for (int index = clientCards.size(); index < widgets.size(); index++) {
      parent.remove(widgets.get(index));
      widgets.remove(index);
    }
  }

  private void addWidget(Rectangle rect, GameCard card) {
    final GameCardWidget widget = new GameCardWidget(card);
    widget.setBounds(rect);
    widget.setCardMoveListener(cardManager);
    widget.setMovable(true);
    parent.add(widget);
    widgets.add(widget);
    parent.setComponentZOrder(widget, 0);
  }

  public boolean contains(GameCardWidget card) {
    return widgets.contains(card);
  }

  /* Getter and Setter */

  public void setClientCards(List<GameCard> cards) {
    if(cards != null)
      this.clientCards = cards;
  }

  public void setCardManager(CardMoveListener cardManager) {
    this.cardManager = cardManager;
    for (GameCardWidget widget : widgets) {
      widget.setCardMoveListener(cardManager);
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public CardMoveListener getCardManager() {
    return cardManager;
  }
}

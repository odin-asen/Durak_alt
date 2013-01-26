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
    inGamePanel.clearField();
    if(attackerCards != null) {
      inGamePanel.setAttackCards(attackerCards);
      inGamePanel.setDefenseCards(defenderCards);
      inGamePanel.setCards();
    }
    validate();
    repaint();
  }

  /**
   * Places the client cards. If the parameter is null, the client card area will be emptied.
   * @param cards Client cards to place.
   */
  public void setHandCards(List<GameCard> cards) {
    clientWidgets.clear();
    if(cards != null) {
      if(clientWidgets.setClientCards(cards))
        clientWidgets.updateCards(computeClientCardArea());

      /* To ensure the accurate drawing of the cards */
      /* Changing the size calls the CardReplacers componentResized method */
      setSize(getWidth() - 1, getHeight() - 1);
      setSize(getWidth() + 1, getHeight() + 1);
    }
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
    revalidate();
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

  public void updateCards() {
    clientWidgets.updateCards(computeClientCardArea());
    inGamePanel.refreshGrids();
    revalidate();
    repaint();
  }

  public boolean removeCard(GameCardWidget card) {
    return clientWidgets.remove(card);
  }

  public boolean addCard(GameCardWidget card) {
    return clientWidgets.add(card);
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
      if(handCardsVisible) {
        clientWidgets.replaceCards(area);
        clientWidgets.resizeCards(area);
      }
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
      setWidgets(area);
      replaceCards(area);
      resizeCards(area);
    }
  }

  void replaceCards(Rectangle area) {
    final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);
    final double distance = computeRelativeCardToCardDistanceX(area, rect);

    for (int index = 0; index < widgets.size(); index++) {
      final GameCardWidget widget = widgets.get(index);
      if(index > 0)
        rect.x = (int) (rect.x + rect.width*distance);
      widget.setLocation(rect.getLocation());
      parent.setComponentZOrder(widget, 0);
    }
  }

  void resizeCards(Rectangle area) {
    final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);
    for (GameCardWidget gameCardWidget : widgets) {
      gameCardWidget.setSize(rect.getSize());
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
    clientCards.add(widget.getCardInfo());
    return widgets.add(widget);
  }

  public boolean remove(GameCardWidget widget) {
    parent.remove(widget);
    clientCards.remove(widget.getCardInfo());
    return widgets.remove(widget);
  }

  public void clear() {
    for (GameCardWidget widget : widgets) {
      parent.remove(widget);
    }
    clientCards.clear();
    widgets.clear();
  }

  private void cleanUpWidgetList() {
    if(widgets.size() > clientCards.size()) {
      for (int index = clientCards.size(); index < widgets.size(); index++) {
        remove(widgets.get(index));
      }
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

  public boolean setClientCards(List<GameCard> cards) {
    if(!this.clientCards.equals(cards) && cards != null) {
      this.clientCards = cards;
      return true;
    } else return false;
  }

  private void setWidgets(Rectangle region) {
    final Rectangle rect = getFirstCardBounds(region, GameCardWidget.WIDTH_TO_HEIGHT);
    final double distance = computeRelativeCardToCardDistanceX(region, rect);

    for (int index = 0; index < clientCards.size(); index++) {
      if(widgets.size() > index)
        widgets.get(index).setCard(clientCards.get(index));
      else {
        if(index > 0)
          rect.x = (int) (rect.x + rect.width * distance);
        addWidget(rect, clientCards.get(index));
      }
    }
    cleanUpWidgetList();
  }

  public void setCardManager(CardMoveListener cardManager) {
    this.cardManager = cardManager;
    for (GameCardWidget widget : widgets) {
      widget.setCardMoveListener(cardManager);
    }
  }

  public CardMoveListener getCardManager() {
    return cardManager;
  }
}

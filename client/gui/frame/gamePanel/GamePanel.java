package client.gui.frame.gamePanel;

import client.gui.frame.ClientGUIConstants;
import client.gui.widget.card.CardMoveListener;
import client.gui.widget.card.GameCardWidget;
import common.dto.DTOCard;
import common.utilities.Converter;
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
public class GamePanel extends JPanel {
  private static final Float USER_CARDS_PANEL_HEIGHT = 0.4f;
  private static final Float DISTANCE_CARD_X = 0.05f;
  private static final Float DISTANCE_CARD_Y = 0.08f;

  private boolean handCardsVisible;

  private CardMoveListener cardManager;

  private List<GameCardWidget> clientWidgets;
  private InGamePanel inGamePanel;
  private final GamePanel.CardReplacer cardReplacer;

  /* Constructors */

  public GamePanel(boolean handCardsVisible) {
    inGamePanel= new InGamePanel();
    cardReplacer = new CardReplacer();
    clientWidgets = new ArrayList<GameCardWidget>();
    cardManager = CardMoveListener.getDefaultInstance(this);

    setHandCardsVisible(handCardsVisible);
    setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
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
  public void placeInGameCards(List<DTOCard> attackerCards, List<DTOCard> defenderCards) {
    inGamePanel.placeCards(attackerCards, defenderCards);
    validate();
    repaint();
  }

  /**
   * Places the client cards. If the parameter is null, the client card area will be emptied.
   * @param cards Client cards to place.
   */
  public void placeClientCards(List<DTOCard> cards) {
    clearClientCards();
    if(cards != null) {
      setClientCards(computeClientCardArea(), cards);
    }
    validate();
    repaint();
  }

  private void setClientCards(Rectangle region, List<DTOCard> cards) {
    final Rectangle rect = getFirstCardBounds(region, GameCardWidget.WIDTH_TO_HEIGHT);
    final double distance = computeRelativeCardToCardDistanceX(rect, cards.size());

    for (int index = 0; index < cards.size(); index++) {
      final GameCardWidget widget = new GameCardWidget(cards.get(index));
      if(index > 0)
        rect.x = (int) (rect.x + rect.width * distance);
      widget.setBounds(rect);
      widget.setCardMoveListener(cardManager);
      widget.setMovable(true);
      addCard(widget);
      widget.getParent().setComponentZOrder(widget, 0);
    }

    /* To ensure the accurate drawing of the cards */
    /* Changing the size calls the CardReplacers
    componentResized method */
    setSize(getWidth() - 1, getHeight() - 1);
    setSize(getWidth() + 1, getHeight() + 1);
  }

  private Rectangle getFirstCardBounds(Rectangle region, Float widthToHeight) {
    int height = (int) (region.height*(1.0f-2.0f*DISTANCE_CARD_Y));
    int width = (int) (height*widthToHeight);

    return new Rectangle(new Point(
        (int) (DISTANCE_CARD_X*region.getWidth()+region.x),
        (int) (2.0f*DISTANCE_CARD_Y*region.getHeight()+region.y)),
        new Dimension(width,height));
  }

  public void addInGameCards(CombatCardPanel panel) {
    inGamePanel.addInGameCards(panel);
  }

  public void paintInGameCurtain(Boolean paint) {
    inGamePanel.setPaintCurtain(paint);
  }

  public Rectangle computeClientCardArea() {
    if(handCardsVisible)
      return new Rectangle(0, (int) (getHeight()*(1.0f-USER_CARDS_PANEL_HEIGHT)),
          getWidth(), (int) (getHeight()*USER_CARDS_PANEL_HEIGHT));
    else return new Rectangle(0,0,0,0);
  }

  private double computeRelativeCardToCardDistanceX(Rectangle cardBounds, Integer cardCount) {
    double distance = 1.0;
    if(cardCount > 1) {
      final Rectangle cardArea = computeClientCardArea();
      final double width = cardArea.width - (cardArea.width*2.0*DISTANCE_CARD_X);
      if(cardCount*cardBounds.width > width)
        distance = (width-cardBounds.width)/(cardBounds.width*(cardCount-1));
      else distance = 1.0;
    }
    return distance;
  }

  public void addCard(GameCardWidget widget) {
    this.add(widget);
    clientWidgets.add(widget);
  }

  public void removeCard(GameCardWidget widget) {
    this.remove(widget);
    clientWidgets.remove(widget);
    replaceCards(computeClientCardArea());
    this.validate();
  }

  public void deleteCards() {
    clearClientCards();
    inGamePanel.clearField();
    this.repaint();
  }

  public void clearClientCards() {
    for (GameCardWidget widget : clientWidgets) {
      remove(widget);
    }
    clientWidgets.clear();
  }


  public Boolean hasInGameCards() {
    return inGamePanel.getCardPanels().size() > 0;
  }

  public Boolean inGameCardsAreCovered() {
    if(inGamePanel.getCardPanels().size() <= 0)
      return false;

    for (CombatCardPanel panel : inGamePanel.getCardPanels()) {
      if(!panel.isComplete())
        return false;
    }
    return true;
  }

  private void replaceCards(Rectangle area) {
    final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);
    final double distance = computeRelativeCardToCardDistanceX(rect, clientWidgets.size());

    for (int index = 0; index < clientWidgets.size(); index++) {
      final GameCardWidget widget = clientWidgets.get(index);
      if(index > 0)
        rect.x = (int) (rect.x + rect.width*distance);
      widget.setLocation(rect.getLocation());
      this.setComponentZOrder(widget, 0);
    }
  }

  private void resizeCards(Rectangle area) {
    final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);

    for (GameCardWidget gameCardWidget : clientWidgets) {
      gameCardWidget.setSize(rect.getSize());
    }
  }

  public Rectangle getInGameArea() {
    return inGamePanel.getBounds();
  }

  public void setListenerType(PlayerConstants.PlayerType type) {
    if (PlayerConstants.PlayerType.FIRST_ATTACKER.equals(type) ||
        PlayerConstants.PlayerType.SECOND_ATTACKER.equals(type)) {
      cardManager = CardMoveListener.getAttackerInstance(this, 2.0f*DISTANCE_CARD_Y);
    } else if (PlayerConstants.PlayerType.DEFENDER.equals(type)) {
      cardManager = CardMoveListener.getDefenderInstance(this, inGamePanel.getCardPanels());
    } else {
      cardManager = CardMoveListener.getDefaultInstance(this);
    }

    for (GameCardWidget widget : clientWidgets) {
      widget.setCardMoveListener(cardManager);
    }
  }

  public List<List<DTOCard>> getInGameCards() {
    final List<List<DTOCard>> cards = new ArrayList<List<DTOCard>>(2);
    for (CombatCardPanel panel : inGamePanel.getCardPanels()) {
      cards.get(0).add(Converter.toDTO(panel.getAttackerCard().getCardInfo()));
      cards.get(1).add(Converter.toDTO(panel.getDefenderCard().getCardInfo()));
    }
    return cards;
  }

  public List<DTOCard> getClientCards() {
    final List<DTOCard> cards = new ArrayList<DTOCard>();
    for (GameCardWidget widget : clientWidgets) {
      cards.add(Converter.toDTO(widget.getCardInfo()));
    }
    return cards;
  }

  /* Getter and Setter */

  /* Inner Classes */
  private class CardReplacer implements ComponentListener {
    public void componentResized(ComponentEvent e) {
      final Rectangle area = computeClientCardArea();
      inGamePanel.setSize(new Dimension(getWidth(), getHeight()-area.height));
      replaceCards(area);
      resizeCards(area);
    }

    public void componentMoved(ComponentEvent e) {}

    public void componentShown(ComponentEvent e) {}

    public void componentHidden(ComponentEvent e) {}
  }
}

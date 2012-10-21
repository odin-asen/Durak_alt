package client.gui.frame;

import client.gui.widget.card.CardMoveListener;
import client.gui.widget.card.GameCardListener;
import client.gui.widget.card.GameCardWidget;
import dto.DTOCard;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:13
 *
 * Panel that draws the players hand and the attack/defense phase.
 */
public class GamePanel extends JPanel {
  private CardMoveListener cardManager;
  private Set<GameCardWidget> widgetSet;

  /* Constructors */
  public GamePanel() {
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    this.setLayout(null);
    widgetSet = new HashSet<GameCardWidget>();
  }

  /* Methods */
  public void placeCards(List<DTOCard> cards, Rectangle cardRectangle) {
    int i = 0;
    final GameCardListener cardListener = new GameCardListener();
    for (DTOCard card : cards){
      final GameCardWidget widget = new GameCardWidget(card);
      final Rectangle rect = new Rectangle(cardRectangle);
      rect.x = rect.x +i++*10;
      widget.setBounds(rect);
      widget.addGameCardListener(cardListener);
      widget.setMovable(true);
      this.addCard(widget);
    }

    cardManager = new CardMoveListener(widgetSet);
    repaintCards();
  }

  public void paint(Graphics g) {
    super.paint(g);

  }

  public void repaintCards() {
    for (GameCardWidget gameCardWidget : widgetSet) {
      gameCardWidget.moveInArea(this.getSize());
      gameCardWidget.repaint();
    }
  }

  public void addCard(GameCardWidget widget) {
    this.add(widget);
    widgetSet.add(widget);
  }

  public void removeCard(GameCardWidget widget) {
    this.remove(widget);
    widgetSet.remove(widget);
  }

  /* Getter and Setter */
}

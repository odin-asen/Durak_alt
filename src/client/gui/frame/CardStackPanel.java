package client.gui.frame;

import client.gui.widget.card.CardStackWidget;
import game.GameCardStack;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class CardStackPanel extends JPanel {
  private CardStackWidget cardStack;

  /* Constructors */
  public CardStackPanel() {
    this.setBackground(Color.BLACK);
  }

  /* Methods */
  /**
   * This method is equivalent to {@code setStack(card,null)}.
   * @param cards Number of cards that will be shown on the stack. One of the stack is
   *              the trump card.
   */
  public void setStack(int cards) {
    this.setStack(cards, null);
  }

  /**
   * Sets a stack on the panel.
   * @param cards Number of cards that will be shown on the stack. One of the stack is
   *              the trump card.
   * @param constraints Constraints that can be passed, if the currently set
   *                    LayoutManager provides one.
   */
  public void setStack(int cards, Object constraints) {
    this.cardStack = CardStackWidget.getInstance();
    for(int i = 0; i < cards; i++) {
      cardStack.pushCard();
    }
    if(constraints == null)
      this.add(cardStack);
    else
      this.add(cardStack, constraints);
  }

  public void setTrumpCard(ImageIcon trump) {
    if(cardStack == null)
      cardStack = CardStackWidget.getInstance();
    this.cardStack.setTrumpCard(trump);
  }

  public void setCardBack(ImageIcon cardBack) {
    this.cardStack.setCardBack(cardBack);
  }

  public void updateStack(GameCardStack gameCardStack) {
    //TODO stack daten ändern
  }
  /* Getter and Setter */
}

package client.gui.frame;

import client.gui.widget.card.CardStackWidget;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class CardStackPanel extends JPanel {
  private CardStackWidget cardStack;

  public CardStackPanel() {
    this.setBackground(Color.BLACK);
  }

  public void addStack(int cards) {
    this.addStack(cards, null);
  }

  public void addStack(int cards, Object constraints) {
    ImageIcon cardBack = new ImageIcon("/home/chewbacca/Development/Java/Durak/src/resources/icons/cards/back.png");
    ImageIcon trumpCard = new ImageIcon("/home/chewbacca/Development/Java/Durak/src/resources/icons/cards/ace.png");

    final float widthLimit = 1.0f-2.0f*Math.abs(1.0f-CardStackWidget.RATIO_RIGHT_MARGIN);
    this.cardStack = new CardStackWidget(cardBack, trumpCard, CardStackWidget.ORIENTATION_VERTICAL,
        (int) (ClientFrame.CARDSTACK_PANEL_WIDTH*widthLimit));
    for(int i = 0; i < cards; i++) {
      cardStack.pushCard();
    }
    if(constraints == null)
      this.add(cardStack);
    else
      this.add(cardStack, constraints);
  }
}

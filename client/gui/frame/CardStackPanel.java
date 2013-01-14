package client.gui.frame;

import client.gui.widget.card.CardStackWidget;
import common.dto.DTOCardStack;

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
    cardStack = new CardStackWidget(CardStackWidget.ORIENTATION_VERTICAL);
    setLayout(new BorderLayout());
  }

  /* Methods */

  /**
   * This method is equivalent to {@code setStack(card,null)}.
   * @param cards Number of cards that will be shown on the stack. One of the stack is
   *              the trump card.
   */
  @SuppressWarnings("UnusedDeclaration")
  private void setStack(DTOCardStack stack) {
    setStack(stack, null);
  }

  /**
   * Adds a stack on the panel.
   * @param cards Number of cards that will be shown on the stack. One of the stack is
   *              the trump card.
   * @param constraints Constraints that can be passed, if the currently set
   *                    LayoutManager provides one.
   */
  private void setStack(DTOCardStack stack, Object constraints) {
    remove(cardStack);

    cardStack.setCardStack(stack);

    if(constraints == null)
      this.add(cardStack);
    else
      this.add(cardStack, constraints);
  }

  public void updateStack(DTOCardStack stack) {
    setStack(stack, BorderLayout.CENTER);
    cardStack.repaint();
    cardStack.updateTooltip();
    repaint();
    validate();
  }

  public void deleteCards() {
    cardStack.setCardCount(0);
    cardStack.repaint();
  }

  /* Getter and Setter */
}

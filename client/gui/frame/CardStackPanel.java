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
    add(cardStack);
  }

  /* Methods */

  /**
   * This method is equivalent to {@code setStack(card,null)}.
   * @param stack Parameter values for the shown stack.
   */
  @SuppressWarnings("UnusedDeclaration")
  private void setStack(DTOCardStack stack) {
    setStack(stack, null);
  }

  /**
   * Adds a stack on the panel.
   * @param stack Parameter values for the shown stack.
   * @param constraints Constraints that can be passed, if the currently set LayoutManager
   *                    provides one.
   */
  private void setStack(DTOCardStack stack, Object constraints) {
    remove(cardStack);

    cardStack.setCardStack(stack);

    if(constraints == null)
      add(cardStack);
    else
      add(cardStack, constraints);
  }

  public void updateStack(DTOCardStack stack) {
    cardStack.setCardStack(stack);
    validate();
    repaint();
  }

  public void deleteCards() {
    cardStack.setCardCount(0);
    cardStack.repaint();
  }

  /* Getter and Setter */
}

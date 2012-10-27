package client.gui.frame;

import client.gui.widget.card.CardStackWidget;
import dto.DTOCard;
import dto.DTOCardStack;

import javax.swing.*;
import java.awt.*;

import static client.gui.frame.ClientGUIConstants.CARD_BACK;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class CardStackPanel extends JPanel {
  private CardStackWidget cardStack;

  /* Constructors */
  public CardStackPanel() {
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    cardStack = CardStackWidget.getInstance();
  }

  /* Methods */
  public void initialiseStack(Integer stackSize, DTOCard trump) {
    setStack(stackSize, BorderLayout.CENTER);
    setCardBack(CARD_BACK);
    setTrumpCard(trump);
    cardStack.repaint();
  }

  /**
   * This method is equivalent to {@code setStack(card,null)}.
   * @param cards Number of cards that will be shown on the stack. One of the stack is
   *              the trump card.
   */
  private void setStack(int cards) {
    this.setStack(cards, null);
  }

  /**
   * Adds a stack on the panel.
   * @param cards Number of cards that will be shown on the stack. One of the stack is
   *              the trump card.
   * @param constraints Constraints that can be passed, if the currently set
   *                    LayoutManager provides one.
   */
  private void setStack(Integer cards, Object constraints) {
    this.cardStack = CardStackWidget.getInstance();
    this.cardStack.setCardCount(cards);

    if(constraints == null)
      this.add(cardStack);
    else
      this.add(cardStack, constraints);
  }

  public void setTrumpCard(DTOCard trump) {
    this.cardStack.setTrumpCard(trump);
  }

  public void setCardBack(ImageIcon cardBack) {
    this.cardStack.setCardBack(cardBack);
  }

  public void updateStack(DTOCardStack cardStack) {
    this.cardStack.setCardCount(cardStack.getSize());
    this.cardStack.updateTooltip();
  }

  public void deleteCards() {
    this.cardStack.setCardCount(0);
    this.cardStack.repaint();
  }

  /* Getter and Setter */
}

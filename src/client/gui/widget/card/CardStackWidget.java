package client.gui.widget.card;

import client.gui.frame.ClientGUIConstants;
import dto.DTOCard;
import resources.ResourceGetter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * User: Timm Herrmann
 * Date: 06.10.12
 * Time: 03:57
 */
public class CardStackWidget extends JComponent {
  public static final int ORIENTATION_HORIZONTAL = 0;
  public static final int ORIENTATION_VERTICAL = 1;
  public static final float ALIGNMENT_CARD_HEIGHT = 0.5f;
  public static final float RATIO_WIDTH_TO_HEIGHT = 0.69f;
  /* Defines the relative distance to the right border of the component */
  public static final float RATIO_RIGHT_MARGIN = 0.9f;

  private ImageIcon cardBack;
  private ImageIcon trumpCard;
  private int cardCount;
  private AffineTransform affineTransform;
  private int heightLimit;

  /* Constructors */
  /**
   * Constructs a card stack widget.
   * @param orientation The orientation sets the orientation of back cards.
   * @param heightLimit Limits the height of the back cards in pixel.
   */
  public CardStackWidget(int orientation, int heightLimit) {
    this.heightLimit = heightLimit;
    this.cardBack = new ImageIcon();
    this.cardBack.setDescription("Back");
    this.trumpCard = new ImageIcon();
    this.trumpCard.setDescription("Trump");

    initAffineTransform(orientation);
    this.cardCount = 0;
    this.setBackground(Color.BLACK);
  }

  public CardStackWidget(ImageIcon cardBack, ImageIcon trumpCard, int orientation,
                         int heightLimit) {
    this(orientation, heightLimit);
    this.cardBack = cardBack;
    this.trumpCard = trumpCard;
  }

  public static CardStackWidget getInstance() {
    final float freeSpaceWidth = 1.0f-2.0f*Math.abs(1.0f- CardStackWidget.RATIO_RIGHT_MARGIN);
    return new CardStackWidget(CardStackWidget.ORIENTATION_VERTICAL,
        (int) (ClientGUIConstants.CARD_STACK_PANEL_WIDTH *freeSpaceWidth));
  }

  /* Methods */
  private void initAffineTransform(int orientation) {
    this.affineTransform = new AffineTransform();
    if(orientation == ORIENTATION_HORIZONTAL)
      this.affineTransform.rotate(Math.PI/2);
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    final int cardHeight;
    if((int) (getHeight()* ALIGNMENT_CARD_HEIGHT)> heightLimit)
      cardHeight = heightLimit;
    else
      cardHeight = (int) (getHeight()* ALIGNMENT_CARD_HEIGHT);
    final int cardWidth = (int) (cardHeight*RATIO_WIDTH_TO_HEIGHT);
    final int cardX = (int) (getWidth()*RATIO_RIGHT_MARGIN) - cardHeight;
    final int cardY = getHeight()/2-cardHeight/2;

    if(cardCount > 0) {
      g2D.drawImage(trumpCard.getImage(), cardX, cardY + cardHeight/2-cardWidth/2,
        cardWidth, cardHeight, this);
      for(int i = 0; i < cardCount-1; i++)
        g2D.drawImage(cardBack.getImage(), cardX + cardHeight-cardWidth+i*cardWidth/700, cardY, cardWidth, cardHeight,this);
    }
  }

  public void updateTooltip() {
    if(cardCount == 0)
      this.setToolTipText("");
    else if(cardCount == 1)
      this.setToolTipText("Noch 1 Karte auf dem Stapel");
    else
      this.setToolTipText("Noch "+cardCount+" Karten auf dem Stapel");
  }

  /* Getter and Setter */
  public void setCardCount(int cardCount) {
    this.cardCount = cardCount;
  }

  public int getCardCount() {
    return cardCount;
  }

  public void setTrumpCard(DTOCard trump) {
    final String text = trump.cardColour + " "+trump.cardValue;
    this.trumpCard = ResourceGetter.getCardImage(trump.cardColour,trump.cardValue, text);
  }

  public void setCardBack(ImageIcon cardBack) {
    this.cardBack = cardBack;
  }
}

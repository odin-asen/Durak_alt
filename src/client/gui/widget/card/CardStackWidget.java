package client.gui.widget.card;

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
  public static final float ALIGNMENT_CARD_HEIGHT = 0.9f;
  public static final float TRUMP_VISIBILITY = 0.5f;
  public static final float RATIO_WIDTH_TO_HEIGHT = 0.69f;
  /* Defines the relative distance to the borders */
  public static final float BORDER_MARGIN = 0.05f;

  private DTOCard trump;
  private ImageIcon cardBack;
  private ImageIcon trumpCard;
  private int cardCount;
  private AffineTransform cardBackTransform;
  private AffineTransform trumpCardTransform;
  private int orientation;

  /* Constructors */
  /**
   * Constructs a card stack widget.
   * @param orientation The orientation sets the orientation of back cards.
   * @param heightLimit Limits the height of the back cards in pixel.
   */
  public CardStackWidget(int orientation) {
    this.cardBack = new ImageIcon();
    this.cardBack.setDescription("Back");
    this.trumpCard = new ImageIcon();
    this.trumpCard.setDescription("Trump");
    this.trump = new DTOCard();
    this.orientation = orientation;

    cardBackTransform = new AffineTransform();
    this.cardCount = 0;
    this.setBackground(Color.BLACK);
    updateTooltip();
  }

  public CardStackWidget(ImageIcon cardBack, ImageIcon trumpCard, int orientation) {
    this(orientation);
    this.cardBack = cardBack;
    this.trumpCard = trumpCard;
  }

  public static CardStackWidget getInstance() {
    return new CardStackWidget(CardStackWidget.ORIENTATION_VERTICAL);
  }

  /* Methods */
  private void calculateCardBackTransform(Point backPoint, int width, int height,
                                          int cardWidth, int cardHeight, int cardNumber) {
    double theta = 0.0;
    double scale[] = computeScaling(cardBack.getIconWidth(), cardBack.getIconHeight(),
        cardWidth, cardHeight);
    double x = backPoint.x + cardNumber*cardWidth/700;
    double y = backPoint.y;
    if(orientation == CardStackWidget.ORIENTATION_HORIZONTAL) { //stimmt nicht mehr unbedingt, verbessern, wenn genug nerven übrig sind
      theta = Math.PI/2;
      x = width/2+cardHeight/2;
      y = height*(1.0f-BORDER_MARGIN) - cardWidth + cardNumber*cardWidth/700;
    }
    final double[] matrix = new double[]{
        Math.cos(theta)*scale[0],  Math.sin(theta)*scale[0],  //m00(x), m10(x)   Drehmatrix: m00, m01, 0
        -Math.sin(theta)*scale[1], Math.cos(theta)*scale[1],  //m01(y), m11(y)               m10, m11, 0
        x,y};                                                 //m02, m12                     0  , 0  , 1
    cardBackTransform = new AffineTransform(matrix);
  }

  private void calculateTrumpCardTransform(Point backPoint, int width, int height,
                                           int cardWidth, int cardHeight) {
    double theta = -Math.PI/2;
    double scale[] = computeScaling(trumpCard.getIconWidth(), trumpCard.getIconHeight(),
        cardWidth, cardHeight);
    double x = backPoint.x - (cardHeight/2.0);
    double y = backPoint.y + (cardHeight+cardWidth)/2.0;

    if(orientation == CardStackWidget.ORIENTATION_HORIZONTAL) { //stimmt nicht mehr unbedingt, verbessern, wenn genug nerven übrig sind
      theta = 0.0;
      x = width/2-cardWidth/2;
      y = height*(1.0f-BORDER_MARGIN) - cardWidth;
      y = y-(3.0*cardHeight/5.0);
    }

    final double[] matrix = new double[]{
        Math.cos(theta)*scale[0],  Math.sin(theta)*scale[0],  //m00(x), m10(x)   Drehmatrix: m00, m01, 0
        -Math.sin(theta)*scale[1], Math.cos(theta)*scale[1],  //m01(y), m11(y)               m10, m11, 0
        x, y};                                                //m02, m12                     0  , 0  , 1
    trumpCardTransform = new AffineTransform(matrix);
  }

  private double[] computeScaling(int iconWidth, int iconHeight, int cardWidth, int cardHeight) {
    return new double[]{((double) cardWidth)/iconWidth, ((double) cardHeight)/iconHeight};
  }

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;
    final int width = getWidth();
    final int height = getHeight();
    final int cardHeight = computeCardHeight(width, height);
    final int cardWidth = (int) (cardHeight*RATIO_WIDTH_TO_HEIGHT);
    final Point backPoint = getBackPoint(width, height, cardWidth, cardHeight);

    calculateTrumpCardTransform(backPoint, width, height, cardWidth, cardHeight);
    if(cardCount > 0) {
      g2D.drawImage(trumpCard.getImage(), trumpCardTransform, this);
      for(int i = 0; i < cardCount-1; i++) {
        calculateCardBackTransform(backPoint, width, height, cardWidth, cardHeight, i);
        g2D.drawImage(cardBack.getImage(), cardBackTransform, this);
      }
    }
  }

  private Point getBackPoint(int width, int height, int cardWidth, int cardHeight) {
    int x = width/2 + (int) (cardHeight*TRUMP_VISIBILITY/2) - cardWidth/2;
    int y = height/2-cardHeight/2;
    return new Point(x,y);
  }

  private int computeCardHeight(int width, int height) {
    final float heightRatio = (1.0f-2.0f*BORDER_MARGIN)/(RATIO_WIDTH_TO_HEIGHT+TRUMP_VISIBILITY);
    int cardHeight = (int) (height*(1.0f-2.0f*BORDER_MARGIN));
    int cardWidth = computeCardWidth(cardHeight);
    if((cardHeight*TRUMP_VISIBILITY+cardWidth)>(1.0f-2.0f*BORDER_MARGIN)*width)
      return (int) (heightRatio*width);
    else return cardHeight;
  }

  private int computeCardWidth(int cardHeight) {
    return (int) (cardHeight*RATIO_WIDTH_TO_HEIGHT);
  }

  public void updateTooltip() {
    if(cardCount == 0)
      this.setToolTipText("");
    else if(cardCount == 1)
      this.setToolTipText("Noch 1 Karte auf dem Stapel;\nTrumpf: "+trump.cardColour.getName());
    else
      this.setToolTipText("Noch "+cardCount+" Karten auf dem Stapel;\nTrumpf: "
          +trump.cardColour.getName());
  }

  /* Getter and Setter */
  public void setCardCount(int cardCount) {
    this.cardCount = cardCount;
    updateTooltip();
  }

  public int getCardCount() {
    return cardCount;
  }

  public void setTrumpCard(DTOCard trump) {
    final String text = trump.cardColour + " "+trump.cardValue;
    this.trumpCard = ResourceGetter.getCardImage(trump.cardColour,trump.cardValue, text);
    this.trump = trump;
    updateTooltip();
  }

  public void setCardBack(ImageIcon cardBack) {
    this.cardBack = cardBack;
  }
}

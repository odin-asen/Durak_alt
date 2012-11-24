package client.gui.widget.card;

import client.gui.frame.ClientGUIConstants;
import common.dto.DTOCard;
import common.dto.DTOCardStack;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * User: Timm Herrmann
 * Date: 06.10.12
 * Time: 03:57
 */
public class CardStackWidget extends JComponent {
  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
  public static final int ORIENTATION_HORIZONTAL = 0;
  public static final int ORIENTATION_VERTICAL = 1;
  public static final float ALIGNMENT_CARD_HEIGHT = 0.9f;
  public static final float TRUMP_VISIBILITY = 0.5f;
  public static final float RATIO_WIDTH_TO_HEIGHT = 0.69f;
  /* Defines the relative distance to the borders */
  public static final float BORDER_MARGIN = 0.05f;

  private static CardStackWidget stackWidget;

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
    cardBack = ClientGUIConstants.CARD_BACK;
    cardBack.setDescription(I18nSupport.getValue(BUNDLE_NAME,"image.description.card.back"));
    trumpCard = new ImageIcon();
    trumpCard.setDescription(I18nSupport.getValue(BUNDLE_NAME,"image.description.card.trump"));
    trump = new DTOCard();
    this.orientation = orientation;
    cardBackTransform = new AffineTransform();
    trumpCardTransform = new AffineTransform();

    cardCount = 0;
    updateTooltip();
  }

  public CardStackWidget(ImageIcon cardBack, ImageIcon trumpCard, int orientation) {
    this(orientation);
    this.cardBack = cardBack;
    this.trumpCard = trumpCard;
  }

  public static CardStackWidget getInstance() {
    if(stackWidget == null)
      stackWidget = new CardStackWidget(CardStackWidget.ORIENTATION_VERTICAL);
    return stackWidget;
  }

  /* Methods */
  private void calculateCardBackTransform(Point backPoint, int width, int height,
                                          int cardWidth, int cardHeight, int cardNumber) {
    double theta = 0.0;
    double scale[] = computeScaling(cardBack.getIconWidth(), cardBack.getIconHeight(),
        cardWidth, cardHeight);
    double x = backPoint.x + cardNumber*cardWidth/700;
    double y = backPoint.y;
    if(orientation == CardStackWidget.ORIENTATION_HORIZONTAL) { //stimmt nicht mehr unbedingt, verbessern, wenn genug nerven \u00fcbrig sind
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

    if(orientation == CardStackWidget.ORIENTATION_HORIZONTAL) { //stimmt nicht mehr unbedingt, verbessern, wenn genug nerven \u00fcbrig sind
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
    this.setToolTipText(I18nSupport.getValue(BUNDLE_NAME,"html.card.number.0.trump.colour.1",
        cardCount, trump.cardColour.getName()));
  }

  /* Getter and Setter */
  public void setCardStack(DTOCardStack cardStack) {
    if(cardStack.getSize() > 0)
      setTrumpCard(cardStack.getCardStack().getLast());
    setCardCount(cardStack.getSize());
  }

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

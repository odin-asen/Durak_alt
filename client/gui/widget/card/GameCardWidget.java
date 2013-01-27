package client.gui.widget.card;

import client.gui.frame.ClientGUIConstants;
import client.gui.frame.gamePanel.CurtainWidget;
import common.dto.DTOCard;
import common.game.GameCard;
import common.resources.ResourceGetter;
import common.utilities.Converter;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:24
 *
 * This class provides the graphical handling of the card, such as drag-and-drop.
 */
public class GameCardWidget extends JComponent implements CurtainWidget {
  public static final float WIDTH_TO_HEIGHT = 0.69f;

  private Image cardImage;
  private GameCard cardInfo;
  private boolean paintCurtain;
  private CardMoveListener cardMoveListener;
  private boolean movable;

  private Point lastLocation;
  private int lastZOrderIndex;

  /* Constructors */

  public GameCardWidget() {
    this((GameCard) null);
  }

  public GameCardWidget(GameCard cardInfo) {
    paintCurtain = false;
    movable = false;
    setCard(cardInfo);
  }

  @SuppressWarnings("UnusedDeclaration")
  public GameCardWidget(DTOCard dtoCard) {
    this(Converter.fromDTO(dtoCard));
  }

  /* Methods */

  public void paint(Graphics g) {
    Graphics2D g2D = (Graphics2D) g;

    drawCard(g2D, getSize());

    if(paintCurtain) {
      paintCurtain(g2D, getSize());
    }
  }

  private void drawCard(Graphics2D g2D, Dimension cardDim) {
    setSize(cardDim);
    g2D.drawImage(cardImage, 0, 0, cardDim.width, cardDim.height, this);
  }

  private void paintCurtain(Graphics2D g2D, Dimension cardDim) {
    final Color oldColor = g2D.getColor();
    g2D.setColor(ClientGUIConstants.CURTAIN_COLOUR);
    g2D.fillRect(0, 0, cardDim.width, cardDim.height);
    g2D.setColor(oldColor);
  }

  public void setCardMoveListener(CardMoveListener listener) {
    removeMouseListener(cardMoveListener);
    removeMouseMotionListener(cardMoveListener);
    removeComponentListener(cardMoveListener);

    cardMoveListener = listener;
    addMouseMotionListener(listener);
    addMouseListener(listener);
    addComponentListener(listener);
  }

  /* Getter and Setter */

  public void paintCurtain(boolean paint) {
    if(paintCurtain != paint) {
      paintCurtain = paint;
      repaint();
    } else paintCurtain = false;
  }

  public void setCard(GameCard card) {
    this.cardInfo = card;
    if(card != null) {
      setToolTipText(cardInfo.getColourAndValue());
      cardImage = ResourceGetter.getCardImage(cardInfo.getCardColour(),
          cardInfo.getCardValue()).getImage();
      repaint();
    } else {
      setToolTipText(null);
      cardImage = null;
    }
  }

  public GameCard getCardInfo() {
    return cardInfo;
  }

  public void setMovable(boolean movable) {
    if(movable ^ this.movable) {
      this.movable = movable;

      if(isMovable()) {
        setCardMoveListener(cardMoveListener);
      } else {
        this.removeMouseListener(cardMoveListener);
        this.removeMouseMotionListener(cardMoveListener);
        this.removeComponentListener(cardMoveListener);
      }
    }
  }

  public String toString() {
    return "GameCardWidget{" +
        "cardImage=" + cardImage +
        ", cardInfo=" + cardInfo +
        ", paintCurtain=" + paintCurtain +
        ", cardMoveListener=" + cardMoveListener +
        ", movable=" + movable +
        ", lastLocation=" + lastLocation +
        ", lastZOrderIndex=" + lastZOrderIndex +
        '}';
  }

  public boolean isMovable() {
    return movable;
  }

  public Point getLastLocation() {
    return lastLocation;
  }

  public void setLastLocation(Point lastLocation) {
    this.lastLocation = lastLocation;
  }

  public int getLastZOrderIndex() {
    return lastZOrderIndex;
  }

  public void setLastZOrderIndex(int lastZOrderIndex) {
    this.lastZOrderIndex = lastZOrderIndex;
  }
}

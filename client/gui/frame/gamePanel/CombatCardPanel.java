package client.gui.frame.gamePanel;

import client.gui.frame.ClientGUIConstants;
import client.gui.widget.card.GameCardWidget;
import common.i18n.I18nSupport;

import javax.swing.*;
import java.awt.*;

import static common.i18n.BundleStrings.GUI_COMPONENT;

/**
 * User: Timm Herrmann
 * Date: 30.10.12
 * Time: 20:43
 * This panel can contain two GameCardWidgets. It should be used to display an
 * attacker card and a defender card.
 */
public class CombatCardPanel extends JPanel implements CurtainWidget {
  private GameCardWidget attackerCard;
  private GameCardWidget defenderCard;

  private static final Float DISTANCE_DEFENDER_X = 0.2f;
  private static final Float DISTANCE_DEFENDER_Y = 0.2f;

  private Dimension cardDimension;
  private boolean paintCurtain;

  /* Constructors */

  public CombatCardPanel() {
    paintCurtain = false;
    cardDimension = new Dimension();
    setAttackerCard(null);
    setDefenderCard(null);
    this.setLayout(null);
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
  }

  public void paint(Graphics g) {
    super.paint(g);
    placeCards();
    if(attackerCard == null && paintCurtain) {
      final Rectangle curtain = new Rectangle(computeAttackerPosition(),
          computeCardDimension());
      final Color oldColour = g.getColor();
      g.setColor(ClientGUIConstants.CURTAIN_COLOUR);
      g.drawRect(curtain.x, curtain.y, curtain.width, curtain.height);
      g.setColor(oldColour);
    }
  }

  /* Methods */
  public void placeCards() {
    final Point attackerPoint = computeAttackerPosition();
    final Point defenderPoint = computeDefenderPosition();
    final Dimension cardDim = computeCardDimension();

    if(defenderCard != null) {
      final GameCardWidget widget = defenderCard;
      widget.setBounds(new Rectangle(defenderPoint, cardDim));
      this.setComponentZOrder(widget,0);
    }

    if(attackerCard != null) {
      final GameCardWidget widget = attackerCard;
      widget.setBounds(new Rectangle(attackerPoint, cardDim));
    }
  }

  private Dimension computeCardDimension() {
    int height = (int) (getHeight()*(1.0f));
    height = (int) (height*(1.0f-DISTANCE_DEFENDER_Y));
    int width = (int) (height*GameCardWidget.WIDTH_TO_HEIGHT);
    if(width*(1.0f+DISTANCE_DEFENDER_X) >= getWidth()) {
      width = (int) (getWidth()*(1.0f-DISTANCE_DEFENDER_X));
      height = (int) (width/ GameCardWidget.WIDTH_TO_HEIGHT);
    }
    cardDimension = new Dimension(width,height);

    return cardDimension;
  }

  public Point computeAttackerPosition() {
    int x = (int) (getWidth()/2-(cardDimension.width*(1.0f+DISTANCE_DEFENDER_X))/2);
    int y = (int) (getHeight()/2-(cardDimension.height*(1.0f+DISTANCE_DEFENDER_Y))/2);
    return new Point(x,y);
  }

  public Point computeDefenderPosition() {
    final Point attackerPoint = computeAttackerPosition();
    return new Point(attackerPoint.x+(int) (DISTANCE_DEFENDER_X * cardDimension.width),
        attackerPoint.y+(int) (DISTANCE_DEFENDER_Y * cardDimension.height));
  }

  private GameCardWidget addCard(GameCardWidget oldWidget, GameCardWidget newWidget) {
    if (oldWidget != null)
      remove(oldWidget);
    if(newWidget == null)
      newWidget = new GameCardWidget();
    newWidget.setCardMoveListener(null);
    newWidget.setMovable(false);
    add(newWidget);

    return newWidget;
  }

  private String createToolTipText() {
    if(attackerCard != null) {
      if(defenderCard == null)
        return I18nSupport.getValue(GUI_COMPONENT, "tooltip.card.0.has.to.be.beaten",
            attackerCard.getToolTipText());
      else return I18nSupport.getValue(GUI_COMPONENT, "tooltip.card.0.beats.1",
          defenderCard.getToolTipText(), attackerCard.getToolTipText());
    } else return null;
  }

  /**
   * Complete means that an attacker card is covered by a defender card.
   * @return Returns true or false whether this panel is complete or not.
   */
  public boolean isComplete() {
    return hasAttackerCard() && hasDefenderCard();
  }

  public boolean hasDefenderCard() {
    return (defenderCard.getCardInfo() != null);
  }

  public boolean hasAttackerCard() {
    return (attackerCard.getCardInfo() != null);
  }

  public String toString() {
    String attackerString = null, defenderString = null;
    if(attackerCard != null && attackerCard.getCardInfo() != null)
      attackerString = attackerCard.getCardInfo().toString();
    if(defenderCard != null && defenderCard.getCardInfo() != null)
      defenderString = defenderCard.getCardInfo().toString();
    return "CombatCardPanel{" +
        "attackerCard=" + attackerString +
        ", defenderCard=" + defenderString +
        ", cardDimension=" + cardDimension +
        ", paintCurtain=" + paintCurtain +
        '}';
  }

  /* Getter and Setter */

  public GameCardWidget getAttackerCard() {
    return attackerCard;
  }

  public void setAttackerCard(GameCardWidget attackerCard) {
    this.attackerCard = addCard(this.attackerCard, attackerCard);
    setToolTipText(createToolTipText());
  }

  public GameCardWidget getDefenderCard() {
    return defenderCard;
  }

  public void setDefenderCard(GameCardWidget defenderCard) {
    this.defenderCard = addCard(this.defenderCard, defenderCard);
    setToolTipText(createToolTipText());
  }

  public void paintCurtain(boolean paint) {
    if(attackerCard != null) {
      if(paint && !isComplete())
        attackerCard.paintCurtain(true);
      else attackerCard.paintCurtain(false);
    }
    if(defenderCard != null)
      defenderCard.paintCurtain(false);
  }
}

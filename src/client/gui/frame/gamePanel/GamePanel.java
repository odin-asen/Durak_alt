package client.gui.frame.gamePanel;

import client.gui.frame.ClientGUIConstants;
import client.gui.widget.card.CardMoveListener;
import client.gui.widget.card.GameCardListener;
import client.gui.widget.card.GameCardWidget;
import dto.DTOCard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:13
 *
 * Panel that draws the players hand and the attack/defense phase.
 */
public class GamePanel extends JPanel {
  private static final Float USER_CARDS_PANEL_HEIGHT = 0.4f;
  private static final Float DISTANCE_CARD_X = 0.05f;
  private static final Float DISTANCE_CARD_Y = 0.05f;
  public static final double CARD_TO_CARD_DISTANCE_X = 0.18;

  private JPanel inGamePanel;

  private CardMoveListener cardManager; //TODO implementieren, damit karten aufeinander reagieren können

  private Set<GameCardWidget> clientWidgets;

  /* Constructors */
  public GamePanel() {
    inGamePanel = new JPanel();
    inGamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    inGamePanel.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    inGamePanel.setLayout(new GridLayout(1,1));
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    this.setLayout(null);
    this.add(inGamePanel);
    this.addComponentListener(new CardReplacer());

    clientWidgets = new HashSet<GameCardWidget>();
  }

  /* Methods */
  /**
   * Places every card of {@code attackCards} to the panel and lays every card of
   * {@code defenderCards} with the same index a little shifted over the attacker card.
   * @param attackCards Cards of the attacker.
   * @param defenderCards Cards of the defender.
   */
  public void placeInGameCards(List<DTOCard> attackCards, List<DTOCard> defenderCards) {
    clearInGameField();

    List<CombatCardPanel>  cardPanels = new ArrayList<CombatCardPanel>(attackCards.size());
    //TODO panels fangen ab, wenn eine Karte im Bereich ist, sodass die Karte als Verteidigungskarte hinzugefügt wird

    final Integer[] gridValues = computePanelGrid(inGamePanel, attackCards.size(), GameCardWidget.WIDTH_TO_HEIGHT);
    inGamePanel.setLayout(new GridLayout(gridValues[0], gridValues[1]));
    for (DTOCard card : attackCards) {
      final CombatCardPanel panel = new CombatCardPanel();
      panel.setAttackerCard(new GameCardWidget(card));
      cardPanels.add(panel);
      inGamePanel.add(panel);
    }

    for (int index = 0; index < defenderCards.size(); index++) {
      cardPanels.get(index).setDefenderCard(new GameCardWidget(defenderCards.get(index)));
    }

    for (CombatCardPanel cardPanel : cardPanels) {
      cardPanel.placeCards();
    }
  }

  /**
   * Returns the number of rows and columns for the GridLayout of the parent Panel.
   * @param parent Panel where the GridLayout should be used.
   * @param panelCount Number of panels to be displayed at the {@code parent}
   * @param widthHeightRatio Ration between the width and the height of a panel
   *                         in the {@link client.gui.frame.gamePanel.GamePanel#inGamePanel}
   * @return An array of the class Integer with the length of two where the first value is
   * the number of columns for the GridLayout and the second value is the number of rows.
   */
  private Integer[] computePanelGrid(JPanel parent, Integer panelCount, Float widthHeightRatio) {
    if(parent == null || panelCount < 1 ||
       parent.getHeight() < panelCount || parent.getWidth() < panelCount)
      return new Integer[]{1,1};

    /* Compute the height space that the panels have to fill */
    int width = parent.getWidth()/panelCount;
    int height = (int) (width/widthHeightRatio);
    if((height) > parent.getHeight())
      height = parent.getHeight();

    /* Compute rows and columns */
    Integer rows = parent.getHeight()/height;
    Integer columns = panelCount/rows+1;

    while((rows-1)*columns >= panelCount)
      rows--;

    return new Integer[]{rows, columns};
  }

  private void clearInGameField() {
    inGamePanel.removeAll();
    this.repaint();
  }

  public void placeClientCards(List<DTOCard> cards) {
    clientWidgets = new HashSet<GameCardWidget>();

    setClientCards(computeClientCardArea(), cards);
  }

  private void setClientCards(Rectangle region, List<DTOCard> cards) {
    final Rectangle rect = getFirstCardBounds(region, GameCardWidget.WIDTH_TO_HEIGHT);
    final Double heightLimit = 1.0-rect.getHeight()/getHeight();
    final GameCardListener cardListener = new GameCardListener();

    cardListener.setMoveArea(0.0,1.0,heightLimit,1.0);

    for (DTOCard card : cards) {
      final GameCardWidget widget = new GameCardWidget(card);
      rect.x = (int) (rect.x + rect.width * CARD_TO_CARD_DISTANCE_X);
      widget.setBounds(rect);
      widget.setGameCardListener(cardListener);
      widget.setMovable(true);
      addCard(clientWidgets, widget);
      widget.getParent().setComponentZOrder(widget, 0);
    }
  }

  private Rectangle getFirstCardBounds(Rectangle region, Float widthToHeight) {
    int height = (int) (region.height*(1.0f-2.0f*DISTANCE_CARD_Y));
    int width = (int) (height*widthToHeight);

    return new Rectangle(new Point(
        (int) (DISTANCE_CARD_X*region.getWidth()+region.x),
        (int) (DISTANCE_CARD_Y*region.getHeight()+region.y)),
        new Dimension(width,height));
  }

  public Rectangle computeClientCardArea() {
    return new Rectangle(0, (int) (getHeight()*(1.0f-USER_CARDS_PANEL_HEIGHT)),
    getWidth(), (int) (getHeight()*USER_CARDS_PANEL_HEIGHT));
  }

  public void addCard(Set<GameCardWidget> widgetSet, GameCardWidget widget) {
    this.add(widget);
    widgetSet.add(widget);
  }

  public void removeCard(Set<GameCardWidget> widgetSet, GameCardWidget widget) {
    this.remove(widget);
    widgetSet.remove(widget);
  }

  public void deleteCards() {
    //this.removeAll();
    this.repaint();
  }

  /* paint-Methode */
  public void paint(Graphics g) {
    super.paint(g);

    final Rectangle area = computeClientCardArea();

    resizePanels(new Dimension(getWidth(), getHeight()-area.height));
    resizeClientCards(area);

    g.setColor(Color.WHITE);
    g.drawRect(area.x, area.y+1, area.width - 1, area.height);
  }

  private void replaceCards(Rectangle area) {
    final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);

    for (GameCardWidget widget : clientWidgets) {
      rect.x = (int) (rect.x + rect.width*CARD_TO_CARD_DISTANCE_X);
      widget.setBounds(rect);
      this.setComponentZOrder(widget,0);
    }
  }

  private void resizePanels(Dimension inGameDimension) {
    inGamePanel.setSize(inGameDimension);
    final int componentCount = inGamePanel.getComponentCount();
    final Integer[] gridValues = computePanelGrid(inGamePanel, componentCount, GameCardWidget.WIDTH_TO_HEIGHT);
    inGamePanel.setLayout(new GridLayout(gridValues[0],gridValues[1]));
  }

  private void resizeClientCards(Rectangle area) {
    final Rectangle rect = getFirstCardBounds(area, GameCardWidget.WIDTH_TO_HEIGHT);
    for (GameCardWidget gameCardWidget : clientWidgets) {
      gameCardWidget.setSize(rect.width,rect.height);
    }
  }
  /* Getter and Setter */

  /* Inner Classes */
  private class CardReplacer implements ComponentListener {
    public void componentResized(ComponentEvent e) {
      replaceCards(computeClientCardArea());
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentShown(ComponentEvent e) {
    }

    public void componentHidden(ComponentEvent e) {
    }
  }
}

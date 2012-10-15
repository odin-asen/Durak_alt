package client.gui.frame;

import client.gui.widget.card.CardMoveListener;
import client.gui.widget.card.GameCardListener;
import client.gui.widget.card.GameCardWidget;
import resources.ResourceGetter;
import utilities.constants.GameCardConstants;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:13
 *
 * Panel that draws the players hand and the attack/defense phase.
 */
public class GamePanel extends JPanel {
  private CardMoveListener cardManager;
  private Set<GameCardWidget> widgetSet;
  private GameCardWidget testWidget;

  public GamePanel() {
    this.setBackground(ClientGUIConstants.GAME_TABLE_COLOUR);
    this.setLayout(null);
  }

  public void placeCards() {
    Random random = new Random();

    GameCardWidget widget;
    widgetSet = new HashSet<GameCardWidget>();

    for(int i = 0; i < 3; i++) {
      final int number = random.nextInt(13);
      final ImageIcon image = ResourceGetter.getCardImage(
          ResourceGetter.STRING_CARD_COLOUR_HEARTS, GameCardConstants.CardValue.values()[number],
          "Herz "+GameCardConstants.CardValue.values()[number].getValueName());
      widget = new GameCardWidget(image.getImage());
      widget.setBounds(10+i*10,10,10,10);
      widget.addGameCardListener(new GameCardListener());
      widget.setMovable(true);
      this.add(widget);
      widgetSet.add(widget);
      if(i == 0)
        testWidget = widget;
    }

    cardManager = new CardMoveListener(widgetSet);
  }

  public void paint(Graphics g) {
    super.paint(g);

  }

  public void repaintCards() {
    for (GameCardWidget gameCardWidget : widgetSet) {
      gameCardWidget.moveInArea(this.getSize());
      gameCardWidget.repaint();
    }
  }
}

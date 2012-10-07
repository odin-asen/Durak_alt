package client.gui.frame;

import client.gui.widget.card.OpponentHandWidget;

import javax.swing.*;
import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 16:12
 */
public class OpponentsPanel extends JPanel {
  public OpponentsPanel() {
    this.setBackground(Color.BLACK);
  }

  public void addOpponent(String name, int cardCount) {
    addOpponent(name, cardCount, null);
  }

  public void addOpponent(String name, int cardCount, Object constraints) {
    ImageIcon image = new ImageIcon("/home/chewbacca/Development/Java/Durak/src/resources/icons/cards/back.png");
    OpponentHandWidget oHWidget = new OpponentHandWidget(image, name);
    oHWidget.setCardCount(cardCount);

    if(constraints == null)
      this.add(oHWidget);
    else
      this.add(oHWidget, constraints);
  }
}
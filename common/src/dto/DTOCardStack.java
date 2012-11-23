package dto;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * User: Timm Herrmann
 * Date: 11.10.12
 * Time: 18:06
 */
public class DTOCardStack implements Serializable {
  private Deque<DTOCard> cardStack;

  /* Constructors */
  public DTOCardStack() {
    cardStack = new ArrayDeque<DTOCard>(0);
  }

  /* Methods */
  public Integer getSize() {
    return cardStack.size();
  }

  @SuppressWarnings("HardCodedStringLiteral")
  public String toString() {
    String string = "DTOCardStack {\ncardStack=";
    for (DTOCard dtoCard : cardStack) {
      string = string + '\n' + dtoCard;
    }
    string = string + "\n}";
    return string;
  }

  /* Getter and Setter */
  public Deque<DTOCard> getCardStack() {
    return cardStack;
  }

  public void setCardStack(Deque<DTOCard> cardStack) {
    this.cardStack = cardStack;
  }
}

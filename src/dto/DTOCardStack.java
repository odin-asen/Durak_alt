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
  public Deque<DTOCard> cardStack;

  /* Constructors */
  public DTOCardStack() {
    cardStack = new ArrayDeque<DTOCard>(0);
  }

  /* Methods */
  public String toString() {
    String string = "DTOCardStack {\ncardStack=";
    for (DTOCard dtoCard : cardStack) {
      string = string + '\n' + dtoCard;
    }
    string = string + "\n}";
    return string;
  }

  /* Getter and Setter */
}

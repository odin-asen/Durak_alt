package common.dto;

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
  public DTOCard trumpCard;

  /* Constructors */
  public DTOCardStack() {
    cardStack = new ArrayDeque<DTOCard>(0);
    trumpCard = new DTOCard();
  }

  /* Methods */

  @SuppressWarnings("ALL")
  public String toString() {
    String string = "DTOCardStack = {";
    for (DTOCard dtoCard : cardStack) {
      string = string + '\n' + dtoCard;
    }
    string = string + "}";
    return string;
  }
}

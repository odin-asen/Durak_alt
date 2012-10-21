package dto;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 12.10.12
 * Time: 17:12
 */
public class ClientInfo implements Serializable {
  private String id;
  private String clientName;
  private int cardCount;

  /* Constructors */
  public ClientInfo(String name) {
    id = Integer.toString(this.hashCode());
    this.clientName = name;
    this.cardCount = 0;
  }

  /* Methods */
  public String toString() {
    return clientName+id;
  }

  public boolean equalsID(ClientInfo info) {
    return info.id.equals(this.id);
  }

  /* Getter and Setter */
  public String getClientName() {
    return clientName;
  }

  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  public int getCardCount() {
    return cardCount;
  }

  public void setCardCount(int cardCount) {
    this.cardCount = cardCount;
  }
}

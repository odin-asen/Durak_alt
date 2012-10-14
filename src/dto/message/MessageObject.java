package dto.message;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:12
 */
public class MessageObject implements Serializable {
  private Enum<?> type;
  private Object sendingObject;

  /* Constructors */
  public MessageObject(Enum<?> type) {
    this(type, null);
  }

  public MessageObject(Enum<?> type, Object sendingObject) {
    this.type = type;
    this.sendingObject = sendingObject;
  }

  /* Methods */

  /* Getter and Setter */
  public Enum<?> getType() {
    return type;
  }

  public void setType(Enum<?> type) {
    this.type = type;
  }

  public Object getSendingObject() {
    return sendingObject;
  }

  public void setSendingObject(Object sendingObject) {
    this.sendingObject = sendingObject;
  }
}

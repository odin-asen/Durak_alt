package dto.message;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 23:12
 */
public class MessageObject implements Serializable {
  private MessageType type;
  private Object sendingObject;

  /* Constructors */
  public MessageObject(MessageType type) {
    this.type = type;
    this.sendingObject = null;
  }

  /* Methods */

  /* Getter and Setter */
  public MessageType getType() {
    return type;
  }

  public void setType(MessageType type) {
    this.type = type;
  }

  public Object getSendingObject() {
    return sendingObject;
  }

  public void setSendingObject(Object sendingObject) {
    this.sendingObject = sendingObject;
  }
}

package common.dto.message;

import common.dto.ClientInfo;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 23.10.12
 * Time: 23:43
 */
public class ChatMessage implements Serializable {
  private Long sendingTime;
  private ClientInfo sender;
  private String message;

  /* Constructors */
  public ChatMessage(Long sendingTime, ClientInfo sender, String message) {
    this.sendingTime = sendingTime;
    this.sender = sender;
    this.message = message;
  }
  /* Methods */
  /* Getter and Setter */
  public ClientInfo getSender() {
    return sender;
  }

  public void setSender(ClientInfo sender) {
    this.sender = sender;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Long getSendingTime() {
    return sendingTime;
  }

  public void setSendingTime(Long sendingTime) {
    this.sendingTime = sendingTime;
  }
}

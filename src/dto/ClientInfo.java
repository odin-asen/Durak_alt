package dto;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 12.10.12
 * Time: 17:12
 */
public class ClientInfo implements Serializable {
  public String clientName;
  public Boolean startPlayingFlag;

  /* Constructors */
  public ClientInfo(String name) {
    this.clientName = name;
    this.startPlayingFlag = false;
  }

  /* Methods */
  /* Getter and Setter */
}

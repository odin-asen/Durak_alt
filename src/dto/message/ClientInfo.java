package dto.message;

import java.io.Serializable;

/**
 * User: Timm Herrmann
 * Date: 12.10.12
 * Time: 17:12
 */
public class ClientInfo implements Serializable {
  public String clientName;
  public String password;

  public Boolean startPlayingFlag;

  /* Constructors */
  public ClientInfo(String name, String password) {
    this.clientName = name;
    this.password = password;
  }

  /* Methods */
  /* Getter and Setter */
}

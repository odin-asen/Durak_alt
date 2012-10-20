package client.business;

/**
 * User: Timm Herrmann
 * Date: 19.10.12
 * Time: 23:12
 */
public class ConnectionInfo {
  private String ipAddress;
  private Integer port;
  private String password;
  /* Constructors */
  /* Methods */
  /* Getter and Setter */
  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }
}

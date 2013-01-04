package client.business;

/**
 * User: Timm Herrmann
 * Date: 19.10.12
 * Time: 23:12
 */
public class ConnectionInfo {
  private String clientAddress;
  private Integer clientPort;
  private String serverAddress;
  private Integer serverPort;
  private String password;

  public String getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getServerPort() {
    return serverPort;
  }

  public void setServerPort(Integer serverPort) {
    this.serverPort = serverPort;
  }

  public String getClientAddress() {
    return clientAddress;
  }

  public void setClientAddress(String clientAddress) {
    this.clientAddress = clientAddress;
  }

  public Integer getClientPort() {
    return clientPort;
  }

  public void setClientPort(Integer clientPort) {
    this.clientPort = clientPort;
  }
}

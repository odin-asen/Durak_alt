package client.business;

import client.gui.frame.ClientGUIConstants;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameConfigurationConstants;

import java.net.Inet4Address;
import java.net.InetAddress;

/**
 * User: Timm Herrmann
 * Date: 19.10.12
 * Time: 23:12
 *
 * This class is a singleton class to be sure that each program has one and only one
 * connection information holder.
 */

public class ConnectionInfo {
  private static ConnectionInfo ownConnectionInfo;

  private String serverAddress;
  private Integer serverPort;
  private String password;

  private ConnectionInfo() {
    try {
      final InetAddress address = Miscellaneous.getHostInetAddress(Inet4Address.class);
      setServerAddress(address.getHostAddress());
    } catch (Exception e) {
      setServerAddress(InetAddress.getLoopbackAddress().getHostAddress());
    }
    setServerPort(GameConfigurationConstants.DEFAULT_PORT);
    setPassword("");
  }

  public static ConnectionInfo getOwnInstance() {
    if(ownConnectionInfo == null) {
      ownConnectionInfo = new ConnectionInfo();
    }
    return ownConnectionInfo;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(String serverAddress) {
    if(serverAddress == null)
      serverAddress = ClientGUIConstants.DEFAULT_IP_ADDRESS;
    this.serverAddress = serverAddress;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    if(password == null)
      password = "";
    this.password = password;
  }

  public Integer getServerPort() {
    return serverPort;
  }

  public void setServerPort(Integer serverPort) {
    if(serverPort < 0 || serverPort == null)
      serverPort = 0;
    this.serverPort = serverPort;
  }
}

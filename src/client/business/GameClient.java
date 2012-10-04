package client.business;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient {
  private String serverAddress;
  private int port;
  private Socket socket;
  private static final int SERVER_PING = 0;

  public GameClient(String serverAddress, int port) throws IOException {
    this.serverAddress = serverAddress;
    this.port = port;
    this.socket = new Socket(serverAddress, port);
  }

  public void closeSession() throws IOException {
    socket.close();
  }

  public String getIsRunningMessage() {
    if(socket == null)
      return "Es wurde keine Verbindung hergestellt!";
    final InputStream in;
    final OutputStream out;
    String message = "";
    try {
      in = socket.getInputStream();
      out = socket.getOutputStream();

      out.write(SERVER_PING);

      int read = in.read();
      while(read != -1) {
        message = message + (char) read;
        read = in.read();
      }

      in.close();
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return message;
  }
}

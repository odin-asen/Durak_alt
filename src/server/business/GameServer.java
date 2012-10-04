package server.business;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 */
public class GameServer extends Thread {
  private boolean running;
  private String serverAddress;
  private int port;
  private Vector<ServerThread> serverThreads;
  private ServerSocket serverSocket;

  public GameServer(String serverAddress, int port) throws IOException {
    running = true;
    this.serverAddress = serverAddress;
    this.port = port;
    this.serverThreads = new Vector<ServerThread>();
    serverSocket = establishServer();
  }

  public void run() {
    if (serverSocket == null)
      running = false;

    while (running) {
      try {
        final ServerThread thread = new ServerThread(serverSocket.accept());
        thread.start();
        serverThreads.add(thread);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      closeServerThreads();
      if (serverSocket != null) {
        serverSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public void setConnection(String serverAddress, int port) {
    if (!running) {
      this.serverAddress = serverAddress;
      this.port = port;
    }
  }

  public int getPort() {
    return port;
  }

  private void closeServerThreads() {
    for (ServerThread serverThread : serverThreads) {
      serverThread.stopRunning();
    }
    serverThreads.removeAllElements();
  }

  private ServerSocket establishServer() throws IOException {
    return new ServerSocket(port);
  }

  public void stopRunning() {
    if (running)
      running = false;
  }


}

class ServerThread extends Thread {
  private boolean running;
  private Socket socket;

  ServerThread(Socket socket) {
    running = true;
    this.socket = socket;
  }

  public void run() {
    while (running) {

    }

    try {
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void stopRunning() {
    running = false;
  }
}
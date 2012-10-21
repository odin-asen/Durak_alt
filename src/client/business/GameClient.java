package client.business;

import dto.message.MessageObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dto.message.GUIObserverType.*;

/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient extends Observable implements Runnable {
  private static Logger LOGGER = Logger.getLogger(GameClient.class.getName());

  private static GameClient client;

  public static final String DEFAULT_SERVER_ADDRESS = "localhost";
  public static final int DEFAULT_SERVER_PORT = 1025;

  private String serverAddress;
  private int port;
  private Socket socket;
  private ObjectInputStream socketIn;
  private ObjectOutputStream socketOut;

  /* Constructors */
  public static GameClient getClient() {
    if (client == null) {
      client = new GameClient(DEFAULT_SERVER_ADDRESS, DEFAULT_SERVER_PORT);
    }
    return client;
  }

  private GameClient(String serverAddress, int port) {
    this.serverAddress = serverAddress;
    this.port = port;
  }

  /* Methods */
  /**
   * The thread is running to receive update messages and error notifications
   * from the server.
   */
  public void run() {
    while (isConnected()) {
      receiveServerMessages();
    }

    closeSocket();
  }

  private void setChangeAndNotify(MessageObject object) {
    this.setChanged();
    this.notifyObservers(object);
  }

  private void receiveServerMessages() {
    try {
      final MessageObject serverMessage = (MessageObject) socketIn.readObject();
      setChangeAndNotify(serverMessage);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Propably lost connection with " + getSocketAddress());
      disconnect();
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
      disconnect();
    }
  }

  public void connect() {
    if (!isConnected()) {
      try {
        this.socket = new Socket(serverAddress, port);
        getSocketStreams();

        new Thread(client).start();

        this.setChanged();
        this.notifyObservers(new MessageObject(CONNECTED, this.getSocketAddress()));
      } catch (IOException ex) {
        LOGGER.log(Level.INFO, ex.getMessage());
        this.setChanged();
        this.notifyObservers(new MessageObject(CONNECTION_FAIL,
            "Es konnte kein Server für die Adresse " + this.getSocketAddress() + " gefunden werden"));
      }
    }
  }

  public void disconnect() {
    if (socket != null) {
      closeSocket();
      this.setChanged();
      this.notifyObservers(new MessageObject(DISCONNECTED));
    }
  }

  public boolean isConnected() {
    return socket != null && !socket.isClosed();
  }

  private void getSocketStreams() {
    try {
      socketIn = new ObjectInputStream(socket.getInputStream());
      socketOut = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not get the streams!");
    }
  }

  private void closeSocket() {
    try {
      socketIn.close();
      socketOut.close();
      socket.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error closing socket!");
    }
  }

  /**
   * Sends a message to the server and returns the answer of it.
   *
   * @param message Message to send.
   * @return A MessageObject as answer of the server.
   */
  public MessageObject send(MessageObject message) {
    MessageObject answer = null;

    try {
      socketOut.writeObject(message);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
    }

    return answer;
  }

  /* Getter and Setter */
  private String getSocketAddress() {
    return serverAddress + ":" + port;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public void setPort(int port) {
    this.port = port;
  }
}
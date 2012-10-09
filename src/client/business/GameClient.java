package client.business;

import dto.message.MessageObject;
import dto.observer.GUIObserverConstants;
import dto.observer.ObserverUpdateObject;

import java.io.*;
import java.net.Socket;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dto.observer.GUIObserverConstants.*;
/**
 * User: Timm Herrmann
 * Date: 04.10.12
 * Time: 01:37
 */
public class GameClient extends Observable {
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
  public void connect() {
    if(!isConnected()) {
      try {
        this.socket = new Socket(serverAddress, port);
        getSocketStreams();

        this.setChanged();
        this.notifyObservers(new ObserverUpdateObject(CONNECTED, this.getSocketAddress()));
      } catch (IOException ex) {
        LOGGER.log(Level.INFO, ex.getMessage());
        this.setChanged();
        this.notifyObservers(new ObserverUpdateObject(CONNECTION_FAIL,
            "Es konnte kein Server für die Adresse "+this.getSocketAddress()+" gefunden werden"));
      }
    }
  }

  public void disconnect() {
    if (socket != null) {
      closeSocket();
      this.setChanged();
      this.notifyObservers(new ObserverUpdateObject(DISCONNECTED));
    }
  }

  public boolean isConnected() {
    if (socket != null) {
      return !socket.isClosed();
    } else return false;
  }

  private void getSocketStreams() {
    try{
      socketIn = new ObjectInputStream(socket.getInputStream());
      socketOut = new ObjectOutputStream(socket.getOutputStream());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not get the streams!");
    }
  }

  private void closeSocket() {
    try{
      socketIn.close();
      socketOut.close();
      socket.close();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error closing socket!");
    }
  }

  /**
   * Sends a message to the server and returns the answer of it.
   * @param message Message to send.
   * @return A MessageObject as answer of the server.
   */
  public MessageObject send(MessageObject message) {
    MessageObject answer = null;

    try {
      socketOut.writeObject(message);
      answer = ((MessageObject) socketIn.readObject());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
    } catch (ClassNotFoundException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
    }

    return answer;
  }

  private String getSocketAddress() {
    return serverAddress + ":" + port;
  }
}

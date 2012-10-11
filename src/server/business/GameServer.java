package server.business;

import dto.message.MessageObject;
import dto.message.MessageType;
import dto.observer.ObserverUpdateObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dto.observer.GUIObserverConstants.*;
/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 */
public class GameServer extends Observable implements Runnable {
  private static Logger LOGGER = Logger.getLogger(GameServer.class.getName());

  private static GameServer gameServer;

  private boolean running;
  private String serverAddress;
  private int port;
  private Vector<ServerThread> serverThreads;
  private ServerSocket serverSocket;

  /* Constructors */
  public static GameServer getServerInstance() throws IOException {
    if(gameServer == null) {
      gameServer = new GameServer("localhost", 1025);
    }
    return gameServer;
  }

  private GameServer(String serverAddress, int port) throws IOException {
    running = true;
    this.serverAddress = serverAddress;
    this.port = port;
    this.serverThreads = new Vector<ServerThread>();
    serverSocket = establishServer();
  }

  public void run() {
    if (serverSocket == null)
      running = false;

    if(running) {
      this.setChanged();
      this.notifyObservers(new ObserverUpdateObject(SERVER_START));
    }

    while (running) {
      try {
        final Socket socket = serverSocket.accept();
        final ServerThread thread = new ServerThread(socket);
        new Thread(thread).start();
        serverThreads.add(thread);

        this.setChanged();
        this.notifyObservers(new ObserverUpdateObject(CLIENT_CONNECTED));
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Could not establish server connection!", serverSocket);
      }
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

  private void closeServer() throws IOException {
    for (ServerThread serverThread : serverThreads) {
      serverThread.stopRunning();
    }
    serverThreads.removeAllElements();
    if (serverSocket != null) {
      serverSocket.close();

      this.setChanged();
      this.notifyObservers(new ObserverUpdateObject(SERVER_STOP));
    }
  }

  private ServerSocket establishServer() throws IOException {
    return new ServerSocket(port);
  }

  public void stopRunning() {
    if(!running)
      return;

    running = false;

    try {
      closeServer();
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error while closing the connection!", serverSocket);
    }

  }

  public void removeThread(ServerThread thread) {
    this.serverThreads.remove(thread);
  }
}

class ServerThread extends Observable implements Runnable {
  private static Logger LOGGER = Logger.getLogger(ServerThread.class.getName());

  private boolean running;
  private Socket socket;
  private ObjectInputStream socketIn;
  private ObjectOutputStream socketOut;

  /* Constructors */
  public ServerThread(Socket socket) {
    running = true;
    this.socket = socket;
  }

  /* Methods */
  public void run() {
    getSocketStreams();

    while (running) {
      try{
        final MessageObject mo = (MessageObject) socketIn.readObject();
        socketOut.writeObject(MessageHandler.getAnswer(mo));
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Propably lost connection with " + getSocketAddress());
        running = false;
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
        running = false;
      }
    }

    closeSocket();
  }

  private String getSocketAddress() {
    return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
  }

  private void getSocketStreams() {
    try{
      socketOut = new ObjectOutputStream(socket.getOutputStream());
      socketIn = new ObjectInputStream(socket.getInputStream());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not get the streams!");
    }
  }

  private void closeSocket() {
    try{
      socketIn.close();
      socketOut.close();
      socket.close();

      this.setChanged();
      this.notifyObservers(new ObserverUpdateObject(CLIENT_DISCONNECTED));
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error closing socket!");
    }
  }

  public void stopRunning() {
    running = false;
  }

  /* Getter and Setter */
}

class MessageHandler {
  @SuppressWarnings("UnusedDeclaration")
  private Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

  static MessageObject getAnswer(MessageObject messageObject) {
    final MessageType type = messageObject.getType();
    final MessageObject answer = new MessageObject(type);

    if(MessageType.SERVER_PING.equals(type)) {
      answer.setSendingObject("Hello! Is it me you're looking for?");
    }
    //TODO Spielfelddaten im Server einstellen, und zugehörige KLassen initialisieren (z.B. GameCardStack)
    //TODO Spieler Spielfelddaten senden, wenn dieser sich im Spielraum anmeldet
    //TODO Spieler initialisieren, wenn dieser dem Spiel beitreten will
    //TODO zu diesen Nachrichten enums hinzufügen
    //TODO Spielaktion (z.B. Angriffskarte legen) auswerten und antwort schicken, ob es möglich ist
    return answer;
  }
}
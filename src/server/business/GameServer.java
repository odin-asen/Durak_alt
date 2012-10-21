package server.business;

import dto.ClientInfo;
import dto.message.BroadcastType;
import dto.message.GUIObserverType;
import dto.message.MessageObject;
import dto.message.MessageType;
import game.GameCardStack;
import game.GameProcess;
import game.Player;
import server.business.exception.GameServerException;
import utilities.Converter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dto.message.BroadcastType.LOGIN_LIST;
import static dto.message.GUIObserverType.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 21:57
 */
public class GameServer extends Observable implements Runnable {
  private static Logger LOGGER = Logger.getLogger(GameServer.class.getName());

  private static GameServer gameServer;
  public static final String SERVER_PING_ANSWER = "Hello! Is it me you're looking for?";

  private boolean running;
  private int port;
  private List<ServerThread> serverThreads;
  private ServerSocket serverSocket;

  /* Constructors */
  public static GameServer getServerInstance() {
    if(gameServer == null) {
      gameServer = new GameServer(1025);
    }
    return gameServer;
  }

  private GameServer(int port) {
    this.port = port;
    this.serverThreads = new ArrayList<ServerThread>();
  }

  void setChangedAndNotify(GUIObserverType type) {
    this.setChanged();
    this.notifyObservers(new MessageObject(type));
  }

  public void run() {
    if (serverSocket == null)
      running = false;

    if(running) {
      setChangedAndNotify(SERVER_START);
    }

    while (running) {
      try {
        final Socket socket = serverSocket.accept();
        final ServerThread thread = new ServerThread(socket);
        new Thread(thread).start();
        serverThreads.add(thread);
      } catch (IOException e) {
        LOGGER.log(Level.INFO, e.getMessage(), serverSocket);
      }
    }
  }

  public void setPort(int port) {
    if (!running) {
      this.port = port;
    }
  }

  public int getPort() {
    return port;
  }

  public void startServer() {
    try {
      serverSocket = new ServerSocket(port);
      running = true;
      new Thread(gameServer).start();
    } catch (IOException ex) {
      LOGGER.log(Level.SEVERE, ex.getMessage());
      setChangedAndNotify(SERVER_FAIL);
    }
  }

  private void closeServer() throws IOException {
    for (ServerThread serverThread : serverThreads) {
      serverThread.stopRunning();
    }
    serverThreads.clear();
    if (serverSocket != null) {
      serverSocket.close();

      setChangedAndNotify(SERVER_STOP);
    }
  }

  void setChangedAndNotify(GUIObserverType type, Object sendingObject) {
    this.setChanged();
    this.notifyObservers(new MessageObject(type, sendingObject));
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

  public List<ClientInfo> getClients() {
    List<ClientInfo> clients = new ArrayList<ClientInfo>();
    for (ServerThread serverThread : serverThreads) {
      final ClientInfo client = serverThread.getClientInfo();
      if(client != null)
        clients.add(client);
    }

    return clients;
  }

  public void broadcastMessage(Enum<?> type, Object sendingObject) {
    for (ServerThread serverThread : serverThreads) {
      serverThread.sendMessage(new MessageObject(type, sendingObject));
    }
  }

  public void broadcastMessage(Enum<?> type) {
    for (ServerThread serverThread : serverThreads) {
      serverThread.sendMessage(new MessageObject(type, null));
    }
  }

  /**
   * Sends a MessageObject object to all clients. For each client the MessageObject
   * object contains as sendingObject the corresponding object of the list
   * {@code sendingObjects}.
   * @param type Type of the message.
   * @param sendingObjects Sending objects for the clients.
   * @throws GameServerException The number of server threads and the size of
   * {@code sendingObjects} is not equal.
   */
  public void broadcastArray(Enum<?> type, List<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != serverThreads.size())
      throw new GameServerException("The number of server threads and sending objects is not equal!");
    for (int i = 0; i < sendingObjects.size(); i++) {
      serverThreads.get(i).sendMessage(new MessageObject(type, sendingObjects.get(i)));
    }
  }
}

class ServerThread implements Runnable {
  private static Logger LOGGER = Logger.getLogger(ServerThread.class.getName());

  private boolean running;
  private Socket socket;
  private ObjectInputStream socketIn;
  private ObjectOutputStream socketOut;
  private ClientInfo clientInfo;

  /* Constructors */
  public ServerThread(Socket socket) {
    running = true;
    this.socket = socket;
  }

  /* Methods */
  /**
   * Sends a message to the client.
   * @param message Message to send.
   */
  public void sendMessage(MessageObject message) {
    try {
      socketOut.writeObject(message);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, e.getMessage());
      GameServer.getServerInstance().removeThread(this);
    }
  }

  public void run() {
    getSocketStreams();

    while (running) {
      try{
        final MessageObject mo = (MessageObject) socketIn.readObject();
        sendMessage(MessageHandler.getAnswer(mo, this));
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
        running = false;
      } catch (ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
        running = false;
      } catch (GameServerException e) {
        LOGGER.log(Level.INFO, e.getMessage());
      }
    }

    closeSocket();
  }

  @SuppressWarnings("UnusedDeclaration")
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
      GameServer server = GameServer.getServerInstance();
      server.removeThread(this);
      server.setChangedAndNotify(CLIENT_DISCONNECTED, getClientInfo());
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error closing socket!");
    }
  }

  public void stopRunning() {
    running = false;
  }

  /* Getter and Setter */
  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  public void setClientInfo(ClientInfo clientInfo) {
    this.clientInfo = clientInfo;
  }
}

class MessageHandler {
  private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

  static MessageObject getAnswer(MessageObject messageObject, ServerThread serverThread) throws GameServerException {
    final Enum<?> type = messageObject.getType();
    final MessageObject answer = new MessageObject(type);
    Object sendingObject = null;
    final GameServer gameServer = GameServer.getServerInstance();

    if(MessageType.SERVER_PING.equals(type)) { /* Answer the ping request */
      sendingObject = GameServer.SERVER_PING_ANSWER;
    } else if(MessageType.LOGIN.equals(type)) {
      /* Provide the user information of the server and its logged clients */
      /* and add it to the list, update all other user */
      sendingObject = loginUserToServer(messageObject, serverThread);
      gameServer.broadcastMessage(LOGIN_LIST, gameServer.getClients());
    } else if(MessageType.CHAT_MESSAGE.equals(type)) {
      sendingObject = clientChatAnswer(serverThread.getClientInfo(),
          messageObject.getSendingObject().toString());
      gameServer.broadcastMessage(BroadcastType.CHAT_MESSAGE, sendingObject);
    } else if(MessageType.GAME_ACTION.equals(type)) {
      //TODO Kartenaktion prüfen und antwort senden, z.B. angriff nicht zulässig
    } else {
      throw new GameServerException("Unknown MessageType to handle: "+type);
    }
    answer.setSendingObject(sendingObject);
    return answer;
  }

  private static Object clientChatAnswer(ClientInfo client, String message) {
    List<Object> sendingObject = new ArrayList<Object>();
    sendingObject.add(new Long(System.currentTimeMillis()));
    sendingObject.add(client);
    sendingObject.add(message);
    return sendingObject;
  }

  /**
   * Adds a client to the server.
   * @return Returns information for the client like, e.g. which clients are
   * also logged in.
   */
  private static Object loginUserToServer(MessageObject messageObject, ServerThread serverThread) {
    final GameServer gameServer = GameServer.getServerInstance();
    final List<Object> list = new ArrayList<Object>();

    serverThread.setClientInfo((ClientInfo) messageObject.getSendingObject());
    GameServer.getServerInstance().setChangedAndNotify(GUIObserverType.CLIENT_CONNECTED, messageObject.getSendingObject());
    GameProcess.getInstance().addPlayer(new Player(
        ((ClientInfo) messageObject.getSendingObject()).getClientName()));
    list.add(Converter.toDTO(GameCardStack.getInstance()));
    list.add(gameServer.getClients());

    return list;
  }
}
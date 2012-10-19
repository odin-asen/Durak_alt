package server.business;

import dto.message.*;
import game.GameCardStack;
import game.GameProcess;
import game.Player;
import server.business.exception.GameServerException;
import utilities.Converter;
import utilities.constants.GameConfigurationConstants;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static dto.message.BroadcastType.LOGIN_LIST;
import static dto.message.GUIObserverType.*;
import static dto.message.MessageType.*;

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

  private void setChangeAndNotify(GUIObserverType type) {
    this.setChanged();
    this.notifyObservers(new MessageObject(type));
  }

  public void run() {
    if (serverSocket == null)
      running = false;

    if(running) {
      setChangeAndNotify(SERVER_START);
    }

    while (running) {
      try {
        final Socket socket = serverSocket.accept();
        final ServerThread thread = new ServerThread(socket);
        new Thread(thread).start();
        serverThreads.add(thread);

        setChangeAndNotify(CLIENT_CONNECTED);
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
      setChangeAndNotify(SERVER_FAIL);
    }
  }

  private void closeServer() throws IOException {
    for (ServerThread serverThread : serverThreads) {
      serverThread.stopRunning();
    }
    serverThreads.clear();
    if (serverSocket != null) {
      serverSocket.close();

      this.setChanged();
      this.notifyObservers(new MessageObject(SERVER_STOP));
    }
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

  public boolean readyToPlay() {
    for (ServerThread serverThread : serverThreads) {
      if(!serverThread.getClientInfo().startPlayingFlag)
        return false;
    }

    return true;
  }

  public void broadcastMessage(BroadcastType type, Object sendingObject) {
    for (ServerThread serverThread : serverThreads) {
      serverThread.sendMessage(new MessageObject(type, sendingObject));
    }
  }

  public void broadcastMessage(MessageType type) {
    for (ServerThread serverThread : serverThreads) {
      serverThread.sendMessage(new MessageObject(type, null));
    }
  }

  public void broadcastArray(MessageType type, List<?> sendingObjects)
      throws GameServerException {
    if(sendingObjects.size() != serverThreads.size())
      throw new GameServerException("The number of server threads and sending objects is not equal!");
    for (int i = 0; i < sendingObjects.size(); i++) {
      serverThreads.get(i).sendMessage(new MessageObject(type, sendingObjects.get(i)));
    }
  }
}

class ServerThread extends Observable implements Runnable {
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
      GameServer.getServerInstance().removeThread(this);
      setChangeAndNotify(CLIENT_DISCONNECTED);
    } catch (IOException e) {
      LOGGER.log(Level.WARNING, "Error closing socket!");
    }
  }

  private void setChangeAndNotify(GUIObserverType type) {
    this.setChanged();
    this.notifyObservers(new MessageObject(type));
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

  private static final DateFormat format = new SimpleDateFormat("[EEE d-MMM HH:mm]");
  private static final Calendar calendar = GregorianCalendar.getInstance(Locale.GERMANY);

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
    } else if(MessageType.START_GAME_SIGNAL.equals(type)) {
      /* Add player in playing list and initialise game after last player */
      startGame(serverThread, gameServer);
    } else if(MessageType.QUIT_GAME_SIGNAL.equals(type)) {
      serverThread.getClientInfo().startPlayingFlag = false;
    } else if(MessageType.CHAT_MESSAGE.equals(type)) {
      sendingObject = getChatMessageTimeStamp((String) messageObject.getSendingObject());
      gameServer.broadcastMessage(BroadcastType.CHAT_MESSAGE, sendingObject);
    } else if(MessageType.GAME_ACTION.equals(type)) {
      //TODO Kartenaktion prüfen und antwort senden, z.B. angriff nicht zulässig
    } else {
      throw new GameServerException("Unknown MessageType to handle: "+type);
    }
    answer.setSendingObject(sendingObject);
    return answer;
  }

  private static String getChatMessageTimeStamp(String message) {
    calendar.setTimeInMillis(System.currentTimeMillis());
    return format.format(calendar.getTime()) + ": "+message+'\n';
  }

  private static void startGame(ServerThread serverThread, GameServer gameServer) {
    serverThread.getClientInfo().startPlayingFlag = true;
    if(GameServer.getServerInstance().readyToPlay()) {
      final GameProcess gameProcess = GameProcess.getInstance();
      gameProcess.initialiseNewGame(GameConfigurationConstants.DEFAULT_COLOUR_CARD_COUNT);
      final List<Player> playerList = gameProcess.getPlayerList();
      try {
        gameServer.broadcastArray(INITIAL_CARDS, Converter.playerCardsToDTO(playerList));
      } catch (GameServerException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
      }
    } else {
      gameServer.broadcastMessage(WAIT_FOR_PLAYER);
    }
  }

  /**
   * Adds a client to the server.
   * @return Returns information for the client like the card stack and which clients are
   * also logged in.
   */
  private static Object loginUserToServer(MessageObject messageObject, ServerThread serverThread) {
    final GameServer gameServer = GameServer.getServerInstance();
    final List<Object> list = new ArrayList<Object>();

    serverThread.setClientInfo((ClientInfo) messageObject.getSendingObject());

    list.add(Converter.toDTO(GameCardStack.getInstance()));
    list.add(gameServer.getClients());
    return list;
  }
}
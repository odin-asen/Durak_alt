package server.business.rmiImpl;

import dto.ClientInfo;
import dto.message.BroadcastType;
import dto.message.ChatMessage;
import rmi.ChatHandler;
import server.business.GameServer;

import java.rmi.RemoteException;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 23:08
 */
public class ChatHandlerImpl implements ChatHandler {
  /* Constructors */
  /* Methods */
  public void sendMessage(ClientInfo client, String message) throws RemoteException {
    ChatMessage chatMessage = new ChatMessage(new Long(System.currentTimeMillis()),
        client, message);
    GameServer.getServerInstance().broadcastMessage(BroadcastType.CHAT_MESSAGE, chatMessage);
  }

  /* Getter and Setter */
}

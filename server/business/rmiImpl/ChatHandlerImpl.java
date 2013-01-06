package server.business.rmiImpl;

import common.dto.DTOClient;
import common.dto.message.BroadcastType;
import common.dto.message.ChatMessage;
import common.rmi.ChatHandler;
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
  public void sendMessage(DTOClient client, String message) throws RemoteException {
    ChatMessage chatMessage = new ChatMessage(new Long(System.currentTimeMillis()),
        client, message);
    GameServer.getServerInstance().broadcastMessage(BroadcastType.CHAT_MESSAGE, chatMessage);
  }

  /* Getter and Setter */
}

package client.gui;

import client.business.ConnectionInfo;
import client.business.Client;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.ClientFrame;
import common.dto.DTOClient;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.LoggingUtility;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 06.01.13
 * Time: 02:18
 *
 * This class is a factory that provides different Action objects. These objects are used
 * for different controls that provide the same funcionality.
 */
public class ActionFactory {
  public static Action getConnectAction() {
    return new ConnectionAction(true);
  }

  public static Action getDisconnectAction() {
    return new ConnectionAction(false);
  }

  public static void doAction(Object source, Action action) {
    final ActionEvent event = new ActionEvent(source, 0,
        (String) action.getValue(Action.ACTION_COMMAND_KEY));
    action.actionPerformed(event);
  }

  public static void initialiseAction(Action action, KeyStroke accelerator,
    String longDescription, Integer mnemonicVirtualKey, String actionCommand, String text,
    String shortDescription, Icon smallIcon) {
    action.putValue(Action.ACCELERATOR_KEY, accelerator);
    action.putValue(Action.LONG_DESCRIPTION, longDescription);
    action.putValue(Action.MNEMONIC_KEY, mnemonicVirtualKey);
    action.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
    action.putValue(Action.NAME, text);
    action.putValue(Action.SHORT_DESCRIPTION, shortDescription);
    action.putValue(Action.SMALL_ICON, smallIcon);
  }
}

class ConnectionAction extends AbstractAction {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MSGS_BUNDLE = "user.messages"; //NON-NLS
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionAction.class.getName());

  private static final String AC_CONNECT = "connect"; //NON-NLS
  private static final String AC_DISCONNECT = "disconnect"; //NON-NLS
  private ClientFrame mainFrame;

  ConnectionAction(boolean connect) {
    mainFrame = ClientFrame.getInstance();
    if(connect)
      ActionFactory.initialiseAction(this, null, null, null, AC_CONNECT,
          I18nSupport.getValue(CLIENT_BUNDLE, "action.name.connect"),
          I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.connect"),
          ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK));
    else ActionFactory.initialiseAction(this, null, null, null, AC_DISCONNECT,
        I18nSupport.getValue(CLIENT_BUNDLE, "action.name.disconnect"),
        I18nSupport.getValue(CLIENT_BUNDLE, "action.tooltip.disconnect"),
        ResourceGetter.getImage(ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE));
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getActionCommand().equals(AC_CONNECT)) {
      connectClient();
    } else if(e.getActionCommand().equals(AC_DISCONNECT)) {
      disconnectClient();
    }
  }

  private void disconnectClient() {
    GameClient.getClient().disconnect(false);
    mainFrame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.has.been.disconnected"),
        false, "");
    mainFrame.clearClients();
  }

  private void connectClient() {
    final GameClient gameClient = GameClient.getClient();
    final Client client = Client.getOwnInstance();
    final DTOClient dtoClient = client.toDTO();

    if(gameClient.isConnected())
      gameClient.disconnect(false);

    try {
      final ConnectionInfo connection = ConnectionInfo.getOwnInstance();
      gameClient.setConnection(connection.getServerAddress(),connection.getServerPort());

      final String socketString =
          "["+connection.getServerAddress()+":"+ connection.getServerPort()+"]";
      if(gameClient.connect(dtoClient, connection.getPassword()))
        mainFrame.setStatus(I18nSupport.getValue(MSGS_BUNDLE, "status.connected"),
            true, socketString);
    } catch (GameClientException e) {
      LOGGER.info("Connect action failed: "+e.getMessage());
      mainFrame.setStatus(e.getMessage(), false, "");
    }
  }
}

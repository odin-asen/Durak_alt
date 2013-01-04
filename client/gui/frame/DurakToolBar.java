package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.business.client.GameClientException;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.setup.SetupFrame;
import common.i18n.I18nSupport;
import common.resources.ResourceList;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 02.10.12
 * Time: 20:44
 */
public class DurakToolBar extends JToolBar {
  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String MESSAGE_BUNDLE = "user.messages"; //NON-NLS

  private static final Logger LOGGER = Logger.getLogger(DurakToolBar.class.getName());
  
  private static final String ACTION_COMMAND_CLOSE = "close";  //NON-NLS
  private static final String ACTION_COMMAND_CONNECT = "connect";  //NON-NLS
  private static final String ACTION_COMMAND_DISCONNECT = "disconnect";  //NON-NLS
  private static final String ACTION_COMMAND_SETUP = "setup";  //NON-NLS
  private static final String ACTION_COMMAND_CHAT = "chat";  //NON-NLS

  private ClientFrame parent;
  private JButton connectionButton;

  public DurakToolBar(ClientFrame parent) {
    this.parent = parent;
    setMargin(new Insets(5, 5, 5, 5));
    setRollover(true);

    addButtons();
  }

  private void addButtons() {
    final ActionListener listener = new ToolBarComponentAL();
    connectionButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_NETWORK,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.connect"), ACTION_COMMAND_CONNECT,
        I18nSupport.getValue(CLIENT_BUNDLE, "image.description.connect"), listener, KeyEvent.VK_V);
    JButton setUpButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_PINION,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.open.setup"), ACTION_COMMAND_SETUP,
        I18nSupport.getValue(CLIENT_BUNDLE,"image.description.setup"), listener, KeyEvent.VK_E);
    JButton chatButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CHAT,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.open.close.chat.frame"), ACTION_COMMAND_CHAT,
        I18nSupport.getValue(CLIENT_BUNDLE,"image.description.chat"), listener, KeyEvent.VK_C);
    JButton closeButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CLOSE,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.close.application"), ACTION_COMMAND_CLOSE,
        I18nSupport.getValue(CLIENT_BUNDLE,"image.description.close"), listener, KeyEvent.VK_Q);

    add(connectionButton);
    addSeparator();
    add(setUpButton);
    addSeparator();
    add(chatButton);
    add(Box.createGlue());
    addSeparator();
    add(closeButton);
  }

  public void setConnection(boolean connected) {
    if(connected) {
      WidgetCreator.changeButton(connectionButton,ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE,
          ACTION_COMMAND_DISCONNECT, I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.disconnect"),
          I18nSupport.getValue(CLIENT_BUNDLE,"image.description.disconnect"));
    } else {
      WidgetCreator.changeButton(connectionButton, ResourceList.IMAGE_TOOLBAR_NETWORK,
          ACTION_COMMAND_CONNECT, I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.connect"),
          I18nSupport.getValue(CLIENT_BUNDLE,"image.description.connect"));
    }
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        close();
      } else if(ACTION_COMMAND_CONNECT.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        setConnection(connectClient(client));
      } else if(ACTION_COMMAND_DISCONNECT.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        setConnection(disconnectClient(client));
      } else if(ACTION_COMMAND_SETUP.equals(e.getActionCommand())) {
        SetupFrame frame = SetupFrame.getInstance();
        if(!frame.isVisible())
          frame.setVisible(true);
        else frame.setVisible(false);
      } else if(ACTION_COMMAND_CHAT.equals(e.getActionCommand())) {
        ChatFrame frame = ChatFrame.getFrame();
        if(!frame.isVisible())
          frame.setVisible(true);
        else frame.setVisible(false);
      }
    }

    private void close() {
      disconnectClient(GameClient.getClient());
      parent.setVisible(false);
      parent.dispose();
      System.exit(0);
    }

    private Boolean disconnectClient(GameClient client) {
      try {
        final SetupFrame frame = SetupFrame.getInstance();
        client.disconnect(frame.getClientInfo());
        frame.setConnectionEnabled(true);
        parent.setStatus(I18nSupport.getValue(MESSAGE_BUNDLE, "status.has.been.disconnected"), false, "");
        parent.clearClients();
      } catch (GameClientException e) {
        LOGGER.severe(e.getMessage());
      }

      return !client.isConnected();
    }

    private Boolean connectClient(GameClient client) {
      final SetupFrame setup = SetupFrame.getInstance();

      try {
        final ConnectionInfo connection = setup.getConnectionInfo();
        client.setConnection(connection);

        setup.getClientInfo().ipAddress = connection.getClientAddress();
        setup.getClientInfo().port = connection.getClientPort();

        final String socketString =
            "[" + connection.getServerAddress()+":"+ connection.getServerPort()+"]";
        client.connect(setup.getClientInfo(), connection.getPassword());
        parent.setStatus(I18nSupport.getValue(MESSAGE_BUNDLE, "status.connected"),
            true, socketString);
        setup.setConnectionEnabled(false);
      } catch (GameClientException e) {
        parent.setStatus(e.getMessage(), false, "");
      } catch (Exception e) {
        LOGGER.severe(e.getMessage());
        parent.setStatus(I18nSupport.getValue(MESSAGE_BUNDLE, "status.connection.failed"), false, "");
      }

      return client.isConnected();
    }
  }
}

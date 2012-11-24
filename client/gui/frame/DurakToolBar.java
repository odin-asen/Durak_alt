package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.setup.SetupFrame;
import common.dto.ClientInfo;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.resources.ResourceList;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 02.10.12
 * Time: 20:44
 */
public class DurakToolBar extends JToolBar {
  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
  private static final Logger LOGGER = Logger.getLogger(DurakToolBar.class.getName());
  
  private static final String ACTION_COMMAND_CLOSE = "close";  //NON-NLS
  private static final String ACTION_COMMAND_CONNECT = "connect";  //NON-NLS
  private static final String ACTION_COMMAND_DISCONNECT = "disconnect";  //NON-NLS
  private static final String ACTION_COMMAND_SETUP = "setup";  //NON-NLS
  private static final String ACTION_COMMAND_CHAT = "chat";  //NON-NLS

  private ClientFrame parent;


  public DurakToolBar(ClientFrame parent) {
    this.parent = parent;
    setMargin(new Insets(5, 5, 5, 5));
    setRollover(true);

    addButtons();
  }

  private void addButtons() {
    final ActionListener listener = new ToolBarComponentAL();
    JButton connectionButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_NETWORK,
        I18nSupport.getValue(BUNDLE_NAME,"tooltip.connect"), ACTION_COMMAND_CONNECT,
        I18nSupport.getValue(BUNDLE_NAME,"image.description.connect"), listener, KeyEvent.VK_V);
    JButton setUpButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_PINION,
        I18nSupport.getValue(BUNDLE_NAME,"tooltip.open.setup"), ACTION_COMMAND_SETUP,
        I18nSupport.getValue(BUNDLE_NAME,"image.description.setup"), listener, KeyEvent.VK_E);
    JButton chatButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CHAT,
        I18nSupport.getValue(BUNDLE_NAME,"tooltip.open.close.chat.frame"), ACTION_COMMAND_CHAT,
        I18nSupport.getValue(BUNDLE_NAME,"image.description.chat"), listener, KeyEvent.VK_C);
    JButton closeButton = WidgetCreator.makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CLOSE,
        I18nSupport.getValue(BUNDLE_NAME,"tooltip.close.application"), ACTION_COMMAND_CLOSE,
        I18nSupport.getValue(BUNDLE_NAME,"image.description.close"), listener, KeyEvent.VK_Q);

    add(connectionButton);
    addSeparator();
    add(setUpButton);
    addSeparator();
    add(chatButton);
    add(Box.createHorizontalGlue());
    addSeparator();
    add(closeButton);
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        close();
      } else if(ACTION_COMMAND_CONNECT.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        if(connectClient(client)) {
          changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE,
              ACTION_COMMAND_DISCONNECT, I18nSupport.getValue(BUNDLE_NAME,"tooltip.disconnect"),
              I18nSupport.getValue(BUNDLE_NAME,"image.description.disconnect"));
        }
      } else if(ACTION_COMMAND_DISCONNECT.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        if(disconnectClient(client)) {
          changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_NETWORK,
              ACTION_COMMAND_CONNECT, I18nSupport.getValue(BUNDLE_NAME,"tooltip.connect"),
              I18nSupport.getValue(BUNDLE_NAME,"image.description.connect"));
        }
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

    private void changeButton(JButton button, String pictureName, String actionCommand,
                              String toolTipText, String alternativeText) {
      button.setActionCommand(actionCommand);
      if(pictureName != null) {
        ImageIcon icon = ResourceGetter.getImage(pictureName, alternativeText);
        button.setIcon(icon);
      }
      button.setToolTipText(toolTipText);
    }

    private void close() {
      disconnectClient(GameClient.getClient());
      parent.setVisible(false);
      parent.dispose();
      System.exit(0);
    }

    private Boolean disconnectClient(GameClient client) {
      try {
        final ClientInfo info = SetupFrame.getInstance().getClientInfo();
        client.disconnect(info);
        SetupFrame.getInstance().setConnectionEnabled(true);
        parent.setStatusBarText(I18nSupport.getValue(BUNDLE_NAME,"status.text.has.been.disconnected"), false, "");
        parent.clearClientList();
      } catch (NotBoundException e) {
        LOGGER.severe(e.getMessage());
      } catch (RemoteException e) {
        LOGGER.severe(e.getMessage());
      }

      return !client.isConnected();
    }

    private Boolean connectClient(GameClient client) {
      client.setPort(SetupFrame.getInstance().getConnectionInfo().getPort());
      client.setServerAddress(SetupFrame.getInstance().getConnectionInfo().getIpAddress());

      try {
        final SetupFrame setup = SetupFrame.getInstance();
        setup.setConnectionEnabled(false);
        final ConnectionInfo connection = setup.getConnectionInfo();
        final String socketString =
            "[" + connection.getIpAddress()+":"+ connection.getPort()+"]";
        if(client.connect(setup.getClientInfo(), connection.getPassword())) {
          parent.setStatusBarText(I18nSupport.getValue(BUNDLE_NAME,"status.message.connected"), true, socketString);
        } else {
          parent.setStatusBarText(I18nSupport.getValue(BUNDLE_NAME,"status.message.permission.denied"), false, "");
        }
      } catch (Exception e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(I18nSupport.getValue(BUNDLE_NAME,"status.message.connection.failed"), false, "");
      }

      return client.isConnected();
    }
  }
}

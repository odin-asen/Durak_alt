package client.gui.frame;

import client.business.client.GameClient;
import client.gui.frame.chat.ChatFrame;
import client.gui.frame.setup.SetUpFrame;
import dto.ClientInfo;
import resources.ResourceGetter;
import resources.ResourceList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.logging.Logger;

import static client.gui.frame.ClientGUIConstants.*;

/**
 * User: Timm Herrmann
 * Date: 02.10.12
 * Time: 20:44
 */
public class DurakToolBar extends JToolBar {
  private static final Logger LOGGER = Logger.getLogger(DurakToolBar.class.getName());

  private ClientFrame parent;

  public DurakToolBar(ClientFrame parent) {
    this.parent = parent;
    JButton connectionButton = makeToolBarButton(ResourceList.IMAGE_TOOLBAR_NETWORK,
        TOOLTIP_CONNECT, ACTION_COMMAND_CONNECT, ALTERNATIVE_CONNECT, KeyEvent.VK_V);
    JButton setUpButton = makeToolBarButton(ResourceList.IMAGE_TOOLBAR_PINION,
        TOOLTIP_SETUP, ACTION_COMMAND_SETUP, ALTERNATIVE_SETUP,
        KeyEvent.VK_E);
    JButton chatButton = makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CHAT,
        TOOLTIP_CHAT, ACTION_COMMAND_CHAT, ALTERNATIVE_CHAT, KeyEvent.VK_C);
    JButton closeButton = makeToolBarButton(ResourceList.IMAGE_TOOLBAR_CLOSE,
        TOOLTIP_CLOSE, ACTION_COMMAND_CLOSE, ALTERNATIVE_CLOSE, KeyEvent.VK_Q);
//    JButton testButton = makeToolBarButton(null, "test", "","test", 0);

    this.setMargin(new Insets(5,5,5,5));
    this.setRollover(true);

    this.add(connectionButton);
    this.addSeparator();
    this.add(setUpButton);
    this.addSeparator();
    this.add(chatButton);
//    this.addSeparator();
//    this.add(testButton);
    this.add(Box.createHorizontalGlue());
    this.addSeparator();
    this.add(closeButton);
  }

  private JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, String alternativeText,
                                    int virtualKey) {
    JButton button = new JButton();
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setMnemonic(virtualKey);
    button.addActionListener(new ToolBarComponentAL());
    button.setIcon(ResourceGetter.getImage(pictureName, alternativeText));

    if(button.getIcon() == null)
      button.setText(alternativeText);

    return button;
  }

  private class ToolBarComponentAL implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        close();
      } else if(ACTION_COMMAND_CONNECT.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        if(connectClient(client)) {
          changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_NETWORK_CLOSE,
              ACTION_COMMAND_DISCONNECT, TOOLTIP_DISCONNECT, ALTERNATIVE_DISCONNECT);
        }
      } else if(ACTION_COMMAND_DISCONNECT.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        if(disconnectClient(client)) {
          changeButton((JButton) e.getSource(), ResourceList.IMAGE_TOOLBAR_NETWORK,
              ACTION_COMMAND_CONNECT, TOOLTIP_CONNECT, ALTERNATIVE_CONNECT);
        }
      } else if(ACTION_COMMAND_SETUP.equals(e.getActionCommand())) {
        SetUpFrame frame = SetUpFrame.getInstance();
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
        final ClientInfo info = SetUpFrame.getInstance().getClientInfo();
        client.disconnect(info);
        SetUpFrame.getInstance().setConnectionEnabled(true);
        parent.setStatusBarText(false, "", "");
        parent.clearClientList();
      } catch (NotBoundException e) {
        LOGGER.severe(e.getMessage());
      } catch (RemoteException e) {
        LOGGER.severe(e.getMessage());
      }

      return !client.isConnected();
    }

    private Boolean connectClient(GameClient client) {
      client.setPort(SetUpFrame.getInstance().getConnectionInfo().getPort());
      client.setServerAddress(SetUpFrame.getInstance().getConnectionInfo().getIpAddress());

      try {
        client.connect();
        SetUpFrame setup = SetUpFrame.getInstance();
        setup.setConnectionEnabled(false);
        if(client.getAuthenticator().login(setup.getClientInfo(), "")) {
          parent.setStatusBarText(true, STATUS_CONNECTED,
              setup.getConnectionInfo().getIpAddress());
        } else {
          parent.setStatusBarText(false, STATUS_PERMISSION_DENIED, "");
        }
      } catch (RemoteException e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(false, STATUS_CONNECTION_FAIL, "");
        e.printStackTrace();
      } catch (NotBoundException e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(false, STATUS_CONNECTION_FAIL, "");
        e.printStackTrace();
      } catch (ServerNotActiveException e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(false, STATUS_CONNECTION_FAIL, "");
        e.printStackTrace();
      }

      return client.isConnected();
    }
  }
}

package client.gui.frame;

import client.business.client.GameClient;
import client.gui.frame.setup.SetUpFrame;
import dto.ClientInfo;
import resources.ResourceGetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.logging.Logger;

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
    JButton connectionButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_NETWORK, "Verbindung zu Server aufbauen",
        ClientGUIConstants.ACTION_COMMAND_CONNECTION, "Verbindung", KeyEvent.VK_V);
    JButton setUpButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_PINION, "\u00d6ffnet Fenster f\u00fcr Einstellungen",
        ClientGUIConstants.ACTION_COMMAND_SETUP, "Einstellungen", KeyEvent.VK_E);
    JButton closeButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE, "Schlie\u00dft die Anwendung",
        ClientGUIConstants.ACTION_COMMAND_CLOSE, "Schlie\u00dfen", KeyEvent.VK_Q);

    this.setMargin(new Insets(5,5,5,5));
    this.setRollover(true);

    this.add(connectionButton);
    this.addSeparator();
    this.add(setUpButton);
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
      if(ClientGUIConstants.ACTION_COMMAND_CLOSE.equals(e.getActionCommand())) {
        close();
      } else if(ClientGUIConstants.ACTION_COMMAND_CONNECTION.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        if(!client.isConnected()) {
          connectClient(client);
        } else {
          disconnectClient(client);
        }
      } else if(ClientGUIConstants.ACTION_COMMAND_SETUP.equals(e.getActionCommand())) {
        SetUpFrame frame = SetUpFrame.getInstance();
        if(!frame.isVisible() || !frame.isActive())
          frame.setVisible(true);
      }
    }

    private void close() {
      disconnect(GameClient.getClient());
      parent.setVisible(false);
      parent.dispose();
      System.exit(0);
    }

    private void disconnect(GameClient client) {
      try {
        ClientInfo info = SetUpFrame.getInstance().getClientInfo();
        client.disconnect(info);
      } catch (NotBoundException e) {
        LOGGER.severe(e.getMessage());
      } catch (RemoteException e) {
        LOGGER.severe(e.getMessage());
      }
    }

    private void disconnectClient(GameClient client) {
      disconnect(client);
      SetUpFrame.getInstance().setConnectionEnabled(true);
      parent.setStatusBarText(false, "", "");
      parent.clearClientList();
    }

    private void connectClient(GameClient client) {
      client.setPort(SetUpFrame.getInstance().getConnectionInfo().getPort());
      client.setServerAddress(SetUpFrame.getInstance().getConnectionInfo().getIpAddress());

      try {
        client.connect();
        SetUpFrame setup = SetUpFrame.getInstance();
        setup.setConnectionEnabled(false);
        if(client.getAuthenticator().login(setup.getClientInfo(), "")) {
          parent.setStatusBarText(true, ClientGUIConstants.STATUS_CONNECTED,
              setup.getConnectionInfo().getIpAddress());
        } else {
          parent.setStatusBarText(false, ClientGUIConstants.STATUS_PERMISSION_DENIED, "");
        }
      } catch (RemoteException e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(false, ClientGUIConstants.STATUS_CONNECTION_FAIL, "");
        e.printStackTrace();
      } catch (NotBoundException e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(false, ClientGUIConstants.STATUS_CONNECTION_FAIL, "");
        e.printStackTrace();
      } catch (ServerNotActiveException e) {
        LOGGER.severe(e.getMessage());
        parent.setStatusBarText(false, ClientGUIConstants.STATUS_CONNECTION_FAIL, "");
        e.printStackTrace();
      }
    }
  }
}

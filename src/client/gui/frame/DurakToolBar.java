package client.gui.frame;

import client.business.GameClient;
import client.gui.frame.setup.SetUpFrame;
import dto.ClientInfo;
import dto.message.MessageObject;
import dto.message.MessageType;
import resources.ResourceGetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * User: Timm Herrmann
 * Date: 02.10.12
 * Time: 20:44
 */
public class DurakToolBar extends JToolBar {
  private ClientFrame parent;
  private JButton connectionButton;
  private JButton setUpButton;
  private JButton closeButton;

  public DurakToolBar(ClientFrame parent) {
    this.parent = parent;
    connectionButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_NETWORK, "Verbindung zu Server aufbauen",
        ClientGUIConstants.ACTION_COMMAND_CONNECTION, "Verbindung", KeyEvent.VK_V);
    setUpButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_PINION, "\u00d6ffnet Fenster f\u00fcr Einstellungen",
        ClientGUIConstants.ACTION_COMMAND_SETUP, "Einstellungen", KeyEvent.VK_E);
    closeButton = makeToolBarButton(ResourceGetter.STRING_IMAGE_CLOSE,"Schlie\u00dft die Anwendung",
        ClientGUIConstants.ACTION_COMMAND_CLOSE,"Schlie\u00dfen", KeyEvent.VK_Q);

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
        parent.setVisible(false);
        parent.dispose();
        System.exit(0);
      } else if(ClientGUIConstants.ACTION_COMMAND_CONNECTION.equals(e.getActionCommand())) {
        GameClient client = GameClient.getClient();
        if(!client.isConnected()) {
          client.setPort(SetUpFrame.getInstance().getConnectionInfo().getPort());
          client.setServerAddress(SetUpFrame.getInstance().getConnectionInfo().getIpAddress());
          client.connect();

          ClientInfo info = SetUpFrame.getInstance().getClientInfo();
          System.out.println(info);
          client.send(new MessageObject(MessageType.LOGIN, info));
        } else {
          client.disconnect();
        }
      } else if(ClientGUIConstants.ACTION_COMMAND_SETUP.equals(e.getActionCommand())) {
        SetUpFrame frame = SetUpFrame.getInstance();
        if(!frame.isVisible() || !frame.isActive())
          frame.setVisible(true);
      }
    }
  }
}

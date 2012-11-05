package client.gui.frame.setup;

import client.business.ConnectionInfo;
import client.gui.frame.ClientGUIConstants;
import dto.ClientInfo;
import utilities.gui.FramePosition;
import utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 30.09.12
 * Time: 21:13
 */
public class SetUpFrame extends JDialog {
  @SuppressWarnings("UnusedDeclaration")
  private static final Logger LOGGER = Logger.getLogger(SetUpFrame.class.getName());

  private static SetUpFrame setUpFrame;

  private JTabbedPane superPane;
  private JPanel buttonPanel;

  private ButtonListener buttonListener;

  private ClientInfoTab clientInfoPanel;
  private ConnectionInfoTab connectionInfoTab;

  public static void main(String[] args) {
    SetUpFrame.getInstance().setVisible(true);
  }

  /* Constructors */
  private SetUpFrame() {
    final FramePosition position = FramePosition.createFensterPositionen(
        ClientGUIConstants.SET_UP_FRAME_SCREEN_SIZE_WIDTH, ClientGUIConstants.SET_UP_FRAME_SCREEN_SIZE_HEIGHT);
    buttonListener = new ButtonListener();

    this.setBounds(position.getRectangle());
    this.setTitle(ClientGUIConstants.SET_UP_TITLE);
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
    getContentPane().add(getSuperPane());
    getContentPane().add(getButtonPane());

    for (Component component : getButtonPane().getComponents()) {
      if(((JButton) component).getActionCommand().equals(ClientGUIConstants.ACTION_COMMAND_APPLY))
        component.setEnabled(false);
    }
  }

  public static SetUpFrame getInstance() {
    if(setUpFrame == null) {
      setUpFrame = new SetUpFrame();
    }

    return setUpFrame;
  }

  /* Methods */
  public void setConnectionEnabled(boolean enabled) {
    for (Component component : connectionInfoTab.getComponents()) {
      component.setEnabled(enabled);
    }
  }

  public void setClientInfoEnabled(boolean enabled) {
    for (Component component : getClientInfoTab().getComponents()) {
      component.setEnabled(enabled);
    }
  }

  public void setChanged(boolean changed) {
    System.out.println("setChanged");
    JButton button = new JButton();
    for (Component component : buttonPanel.getComponents()) {
      if(((JButton) component).getActionCommand().equals(ClientGUIConstants.ACTION_COMMAND_APPLY))
        button = (JButton) component;
    }

    button.setEnabled(!changed);

    if(changed) {
      setTitle(ClientGUIConstants.SET_UP_TITLE + "(Daten ver\u00e4ndert");
    } else {
      setTitle(ClientGUIConstants.SET_UP_TITLE);
    }
  }

  /* Getter and Setter */
  private JPanel getButtonPane() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();

    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(WidgetCreator.makeButton(null, ClientGUIConstants.SET_UP_OKAY_TEXT,
        ClientGUIConstants.SET_UP_OKAY_TOOLTIP, ClientGUIConstants.ACTION_COMMAND_OKAY,
        buttonListener));
    buttonPanel.add(WidgetCreator.makeButton(null, ClientGUIConstants.SET_UP_CANCEL_TEXT,
        ClientGUIConstants.SET_UP_CANCEL_TOOLTIP, ClientGUIConstants.ACTION_COMMAND_CANCEL,
        buttonListener));
    buttonPanel.add(WidgetCreator.makeButton(null, ClientGUIConstants.SET_UP_APPLY_TEXT,
        ClientGUIConstants.SET_UP_APPLY_TOOLTIP, ClientGUIConstants.ACTION_COMMAND_APPLY,
        buttonListener));

    return buttonPanel;
  }

  private JTabbedPane getSuperPane() {
    if(superPane != null)
      return superPane;

    superPane = new JTabbedPane();
    JScrollPane scroll = new JScrollPane();
    scroll.setViewportView(getConnectionTab());
    superPane.addTab(ClientGUIConstants.TITLE_CONNECTION, scroll);
    scroll = new JScrollPane();
    scroll.setViewportView(getClientInfoTab());
    superPane.addTab(ClientGUIConstants.TITLE_INFORMATION, scroll);

    return superPane;
  }

  private JPanel getClientInfoTab() {
    if(clientInfoPanel != null)
      return clientInfoPanel;

    clientInfoPanel = new ClientInfoTab();

    return clientInfoPanel;
  }

  private JPanel getConnectionTab() {
    if(connectionInfoTab != null)
      return connectionInfoTab;

    connectionInfoTab = new ConnectionInfoTab();

    return connectionInfoTab;
  }

  public ClientInfo getClientInfo() {
    return clientInfoPanel.getClientInfo();
  }

  public ConnectionInfo getConnectionInfo() {
    return connectionInfoTab.getConnectionInfo();
  }

  public void updateClientInfo(ClientInfo info) {
    clientInfoPanel.setClientInfo(info);
  }

  /* Inner Classes */
  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(ClientGUIConstants.ACTION_COMMAND_APPLY)) {
        clientInfoPanel.fillClientInfo();
        connectionInfoTab.fillConnectionInfo();
      } else if(e.getActionCommand().equals(ClientGUIConstants.ACTION_COMMAND_CANCEL)) {
        SetUpFrame.getInstance().dispose();
      } else if(e.getActionCommand().equals(ClientGUIConstants.ACTION_COMMAND_OKAY)) {
        clientInfoPanel.fillClientInfo();
        connectionInfoTab.fillConnectionInfo();
        SetUpFrame.getInstance().dispose();
      }
    }
  }
}

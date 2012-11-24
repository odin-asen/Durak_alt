package client.gui.frame.setup;

import client.business.ConnectionInfo;
import client.gui.frame.ClientGUIConstants;
import common.dto.ClientInfo;
import common.i18n.I18nSupport;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * User: Timm Herrmann
 * Date: 30.09.12
 * Time: 21:13
 */
public class SetupFrame extends JDialog {
  private static final String BUNDLE_NAME = "client.client"; //NON-NLS
  private static final String ACTION_COMMAND_APPLY = "apply";  //NON-NLS
  private static final String ACTION_COMMAND_OKAY = "okay"; //NON-NLS
  private static final String ACTION_COMMAND_CANCEL = "cancel";  //NON-NLS

  private static SetupFrame SETUP_FRAME;

  private JTabbedPane superPane;
  private JPanel buttonPanel;

  private ButtonListener buttonListener;

  private ClientInfoTab clientInfoTab;
  private ConnectionInfoTab connectionInfoTab;

  public static void main(String[] args) {
    SetupFrame.getInstance().setVisible(true);
  }

  /* Constructors */
  private SetupFrame() {
    final FramePosition position = FramePosition.createFensterPositionen(
        ClientGUIConstants.SETUP_FRAME_SCREEN_SIZE_WIDTH, ClientGUIConstants.SETUP_FRAME_SCREEN_SIZE_HEIGHT);
    buttonListener = new ButtonListener();

    this.setBounds(position.getRectangle());
    this.setTitle(I18nSupport.getValue(BUNDLE_NAME,"frame.title.setup"));
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
    getContentPane().add(getSuperPane());
    getContentPane().add(getButtonPane());

    for (Component component : getButtonPane().getComponents()) {
      if(((JButton) component).getActionCommand().equals(ACTION_COMMAND_APPLY))
        component.setEnabled(false);
    }
  }

  public static SetupFrame getInstance() {
    if(SETUP_FRAME == null) {
      SETUP_FRAME = new SetupFrame();
    }

    return SETUP_FRAME;
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
    JButton button = new JButton();
    for (Component component : buttonPanel.getComponents()) {
      if(((JButton) component).getActionCommand().equals(ACTION_COMMAND_APPLY))
        button = (JButton) component;
    }

    button.setEnabled(!changed);

    if(changed) {
      setTitle(I18nSupport.getValue(BUNDLE_NAME,"0.title.changed", I18nSupport.getValue(BUNDLE_NAME,"frame.title.setup")));
    } else {
      setTitle(I18nSupport.getValue(BUNDLE_NAME,"frame.title.setup"));
    }
  }

  /* Getter and Setter */
  private JPanel getButtonPane() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();

    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));
    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.add(WidgetCreator.makeButton(null, I18nSupport.getValue(BUNDLE_NAME,"button.text.okay"),
        I18nSupport.getValue(BUNDLE_NAME,"button.tooltip.okay"), ACTION_COMMAND_OKAY,
        buttonListener));
    buttonPanel.add(WidgetCreator.makeButton(null, I18nSupport.getValue(BUNDLE_NAME,"button.text.cancel"),
        I18nSupport.getValue(BUNDLE_NAME,"button.tooltip.cancel"), ACTION_COMMAND_CANCEL,
        buttonListener));
    buttonPanel.add(WidgetCreator.makeButton(null, I18nSupport.getValue(BUNDLE_NAME,"button.text.apply"),
        I18nSupport.getValue(BUNDLE_NAME,"button.tooltip.apply"), ACTION_COMMAND_APPLY,
        buttonListener));

    return buttonPanel;
  }

  private JTabbedPane getSuperPane() {
    if(superPane != null)
      return superPane;

    superPane = new JTabbedPane();
    JScrollPane scroll = new JScrollPane();
    scroll.setViewportView(getConnectionTab());
    superPane.addTab(I18nSupport.getValue(BUNDLE_NAME,"tab.title.connection"), scroll);
    scroll = new JScrollPane();
    scroll.setViewportView(getClientInfoTab());
    superPane.addTab(I18nSupport.getValue(BUNDLE_NAME,"tab.title.client"), scroll);

    return superPane;
  }

  private JPanel getClientInfoTab() {
    if(clientInfoTab != null)
      return clientInfoTab;

    clientInfoTab = new ClientInfoTab();

    return clientInfoTab;
  }

  private JPanel getConnectionTab() {
    if(connectionInfoTab != null)
      return connectionInfoTab;

    connectionInfoTab = new ConnectionInfoTab();

    return connectionInfoTab;
  }

  public ClientInfo getClientInfo() {
    return clientInfoTab.getClientInfo();
  }

  public ConnectionInfo getConnectionInfo() {
    return connectionInfoTab.getConnectionInfo();
  }

  public void updateClientInfo() {
    clientInfoTab.updateGUISettings();
  }

  /* Inner Classes */
  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(ACTION_COMMAND_APPLY)) {
        clientInfoTab.fillClientInfo();
        connectionInfoTab.fillConnectionInfo();
      } else if(e.getActionCommand().equals(ACTION_COMMAND_CANCEL)) {
        SetupFrame.getInstance().dispose();
      } else if(e.getActionCommand().equals(ACTION_COMMAND_OKAY)) {
        clientInfoTab.fillClientInfo();
        connectionInfoTab.fillConnectionInfo();
        SetupFrame.getInstance().dispose();
      }
    }
  }
}

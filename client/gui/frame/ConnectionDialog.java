package client.gui.frame;

import client.business.ConnectionInfo;
import client.business.client.Client;
import client.gui.ActionFactory;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 05.01.13
 * Time: 22:15
 */
public class ConnectionDialog extends JDialog {
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionDialog.class.getName());

  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String ACTION_COMMAND_CONNECT = "connect"; //NON-NLS
  private static final String ACTION_COMMAND_CANCEL = "cancel";  //NON-NLS
  private static final String ACTION_COMMAND_CLOSE_SAVE = "closeSave";  //NON-NLS
  private static final String ACTION_COMMAND_CLOSE = "close"; //NON-NLS
  private static final int STRUT_HEIGHT = 5;
  private static final int STRUT_WIDTH = 5;

  /* Some parameters have a component that will be used to edit this parameter and
   * another component if the parameter should be not editable. E.g. for the
   * server address, the editable component is a combo box and the non editable is a
   * label. */
  private JComboBox<String> serverAddressCombo;
  private JLabel serverAddressLabel;
  private JTextField serverPortField;
  private JLabel serverPortLabel;
  private JTextField passwordField;
  private JComboBox<String> clientAddressCombo;
  private JLabel clientAddressLabel;
  private JTextField clientPortField;
  private JLabel clientPortLabel;
  private JTextField nameField;
  private JLabel nameLabel;
  private JCheckBox spectatorCheckBox;
  private JLabel spectatorLabel;
  private JPanel buttonPanel;
  private ButtonListener buttonListener;

  private boolean editable;

  /* Constructors */

  public ConnectionDialog(boolean editable) {
    super();

    this.editable = editable;

    /* initialise gui stuff */
    buttonListener = new ButtonListener();

    /* initialise fields */
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
    getContentPane().add(getServerInfoPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getClientInfoPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getButtonPanel());
    resetFields();

    /* initialise frame */
    final FramePosition position = FramePosition.createFramePositions(
        getContentPane().getPreferredSize().width,
        getContentPane().getPreferredSize().height);

    this.setBounds(position.getRectangle());

    this.setTitle(I18nSupport.getValue(CLIENT_BUNDLE, "frame.title.setup"));
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
  }

  public static void main(String[] args) {
    new ConnectionDialog(true).setVisible(true);
    new ConnectionDialog(false).setVisible(true);
  }


  /* Methods */

  private void fillConnectionInfo() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    connectionInfo.setServerAddress(serverAddressCombo.getSelectedItem().toString());
    connectionInfo.setServerPort(Integer.parseInt(serverPortField.getText()));
    connectionInfo.setPassword(passwordField.getText());
    connectionInfo.setClientAddress(clientAddressCombo.getSelectedItem().toString());
    connectionInfo.setClientPort(Integer.parseInt(clientPortField.getText()));
  }

  private void fillClientInfo() {
    final Client client = Client.getOwnInstance();
    client.setName(nameField.getText());
    client.setSpectating(spectatorCheckBox.isSelected());
    client.setIpAddress(clientAddressCombo.getSelectedItem().toString());
    client.setPort(Integer.parseInt(clientPortField.getText()));
  }

  /* Getter and Setter */

  private JPanel getServerInfoPanel() {
    final JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE,"border.title.server.settings")));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getServerAddressPanel());
    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getServerPortPanel());
    if(editable) {
      mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
      mainPanel.add(getPasswordPanel());
    }

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    return mainPanel;
  }

  private JPanel getPasswordPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.password"));
    label.setMaximumSize(label.getPreferredSize());
    try {
      passwordField = WidgetCreator.makeTextField(JPasswordField.class,
          ClientGUIConstants.PREFERRED_FIELD_WIDTH,
          I18nSupport.getValue(CLIENT_BUNDLE, "check.box.tooltip.show.password"));
      panel.setLayout(new GridLayout(1, 2));
      panel.add(label);
      panel.add(passwordField);
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    } catch (Exception e) {
      LOGGER.severe("Error creating password panel.\nMessage: "+e.getMessage());
    }
    return panel;
  }

  private JPanel getServerPortPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.port"));
    label.setMaximumSize(label.getPreferredSize());
    serverPortField = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.server.port"));
    serverPortLabel = new JLabel("");

    panel.setLayout(new GridLayout(1, 2));
    panel.add(label);
    if(editable)
      panel.add(serverPortField);
    else panel.add(serverPortLabel);

    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel getServerAddressPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.address"));
    label.setMaximumSize(label.getPreferredSize());
    final Vector<String> comboBoxContent = new Vector<String>();
    serverAddressCombo = WidgetCreator.makeComboBox(comboBoxContent, 3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.server.address"));
    serverAddressCombo.setMaximumSize(serverAddressCombo.getPreferredSize());
    serverAddressCombo.addActionListener(new IPComboBoxListener(serverAddressCombo, comboBoxContent));
    serverAddressLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    panel.add(label);
    if(editable)
      panel.add(serverAddressCombo);
    else panel.add(serverAddressLabel);

    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel getClientInfoPanel() {
    final JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE, "border.title.client.settings")));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getClientAddressPanel());
    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getClientPortPanel());
    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getClientNamePanel());
    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));
    mainPanel.add(getSpectatorPanel());

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    return mainPanel;
  }

  private JPanel getSpectatorPanel() {
    final JPanel panel = new JPanel();
    spectatorCheckBox = new JCheckBox(
        I18nSupport.getValue(CLIENT_BUNDLE,"check.box.spectator"));
    spectatorCheckBox.setSelected(false);
    spectatorLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    if(editable) {
      panel.add(Box.createHorizontalStrut(STRUT_WIDTH));
      panel.add(spectatorCheckBox);
    } else {
      panel.add(new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.status")));
      panel.add(spectatorLabel);
    }
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getClientNamePanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.player.name"));
    label.setMaximumSize(label.getPreferredSize());
    nameField = new JTextField(Client.getOwnInstance().getName());
    nameLabel = new JLabel("");

    panel.setLayout(new GridLayout(1, 2));
    panel.add(label);
    if(editable)
      panel.add(nameField);
    else panel.add(nameLabel);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getClientPortPanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.port"));
    label.setMaximumSize(label.getPreferredSize());
    clientPortField = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.client.port"));
    clientPortLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    panel.add(label);
    if(editable)
      panel.add(clientPortField);
    else panel.add(clientPortLabel);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getClientAddressPanel() {
    final JPanel panel = new JPanel();
    final Vector<String> comboBoxContent = new Vector<String>(3);
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.address"));
    label.setMaximumSize(label.getPreferredSize());
    clientAddressCombo = WidgetCreator.makeComboBox(comboBoxContent,3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.client.address"));
    clientAddressCombo.addActionListener(new IPComboBoxListener(clientAddressCombo, comboBoxContent));
    clientAddressLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    panel.add(label);
    if(editable)
      panel.add(clientAddressCombo);
    else panel.add(clientAddressLabel);

    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    return panel;
  }

  private JPanel getButtonPanel() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();

    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
    if(editable) {
      /* It seems that the action listener that will be added last, will be executed first.
       * Therefore the fill-methods listener will be added last. */
      JButton connectButton = new JButton(ActionFactory.getConnectAction());
      connectButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          fillClientInfo();
          fillConnectionInfo();
        }
      });
      buttonPanel.add(connectButton);
      buttonPanel.add(WidgetCreator.makeButton(null,
          I18nSupport.getValue(CLIENT_BUNDLE, "button.text.close.save"),
          I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.close.save"),
          ACTION_COMMAND_CLOSE_SAVE, buttonListener));
      buttonPanel.add(WidgetCreator.makeButton(null,
          I18nSupport.getValue(CLIENT_BUNDLE, "button.text.cancel"),
          I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.cancel"),
          ACTION_COMMAND_CANCEL, buttonListener));
    } else {
      buttonPanel.add(WidgetCreator.makeButton(null,
          I18nSupport.getValue(CLIENT_BUNDLE, "button.text.close"), null,
          ACTION_COMMAND_CLOSE, buttonListener));
    }

    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

    return buttonPanel;
  }

  private void saveAndClose() {
    fillClientInfo();
    fillConnectionInfo();
    setVisible(false);
    dispose();
  }

  public void resetFields() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    final Client client = Client.getOwnInstance();

    if(editable) {
      /* server fields */
      serverAddressCombo.setSelectedItem(connectionInfo.getServerAddress());
      serverPortField.setText(connectionInfo.getServerPort().toString());
      passwordField.setText(connectionInfo.getPassword());
      /* client fields */
      clientAddressCombo.setSelectedItem(client.getIpAddress());
      clientPortField.setText(Integer.toString(client.getPort()));
      nameField.setText(client.getName());
      spectatorCheckBox.setSelected(client.getSpectating());
    } else {
      /* server fields */
      serverAddressLabel.setText(connectionInfo.getServerAddress());
      serverPortLabel.setText(connectionInfo.getServerPort().toString());
      /* client fields */
      clientAddressLabel.setText(client.getIpAddress());
      clientPortLabel.setText(client.getPort().toString());
      nameLabel.setText(client.getName());
      String key = "label.text.spectator."+client.getSpectating().toString(); //NON-NLS
      spectatorLabel.setText(I18nSupport.getValue(CLIENT_BUNDLE, key));
    }
  }

  /* Inner Classes */

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(ACTION_COMMAND_CANCEL) ||
         e.getActionCommand().equals(ACTION_COMMAND_CLOSE)) {
        resetFields();
        setVisible(false);
        dispose();
      } else if(e.getActionCommand().equals(ACTION_COMMAND_CLOSE_SAVE)) {
        saveAndClose();
      }
    }
  }
}

class IPComboBoxListener implements ActionListener {
  private JComboBox<String> comboBox;
  private Vector<String> comboBoxContent;
  IPComboBoxListener(JComboBox<String> comboBox, Vector<String> comboBoxContent) {
    this.comboBox = comboBox;
    this.comboBoxContent = comboBoxContent;
  }

  public void actionPerformed(ActionEvent e) {
    final String selected = comboBox.getSelectedItem().toString();
    try {
      InetAddress address = InetAddress.getByName(selected);
      final String hostAddress = address.getHostAddress();
      if (!comboBoxContent.contains(hostAddress)) {
        comboBox.addItem(hostAddress);
        comboBox.setSelectedItem(hostAddress);
      } else comboBox.setSelectedIndex(comboBoxContent.indexOf(hostAddress));
    } catch (UnknownHostException e1) {
      comboBox.setSelectedIndex(comboBox.getItemCount()-1);
    }
  }
}

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
  private static final int STRUT_HEIGHT = 5;
  private static final int STRUT_WIDTH = 5;

  private JComboBox<String> serverAddressCombo;
  private JTextField serverPortField;
  private JTextField passwordField;
  private JComboBox<String> clientAddressCombo;
  private JTextField clientPortField;
  private JTextField nameField;
  private JCheckBox spectatorCheckBox;
  private JPanel buttonPanel;
  private ButtonListener buttonListener;

  /* Constructors */
  public ConnectionDialog(boolean editable) { //TODO editable = true -> alles editierbar, ansonsten nur anzeigen
    super();

    /* initialise gui stuff */
    buttonListener = new ButtonListener();

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
    getContentPane().add(getServerInfoPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getClientPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getButtonPanel());

    final FramePosition position = FramePosition.createFramePositions(
        ClientGUIConstants.SETUP_FRAME_SCREEN_SIZE_WIDTH,
        ClientGUIConstants.SETUP_FRAME_SCREEN_SIZE_HEIGHT);

    this.setBounds(position.getRectangle());
    if(position.getWidth() < buttonPanel.getPreferredSize().width)
      this.setSize(buttonPanel.getPreferredSize().width, position.getHeight());

    this.setTitle(I18nSupport.getValue(CLIENT_BUNDLE, "frame.title.setup"));
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    resetFields();
  }

  public static void main(String[] args) {
    new ConnectionDialog(true).setVisible(true);
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

    JLabel label;
    JPanel panel;

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE,"border.title.server.settings")));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* server address panel */
    panel = new JPanel();
    panel.setLayout(new GridLayout(1,2));
    label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.address"));
    label.setMaximumSize(label.getPreferredSize());
    final Vector<String> comboBoxContent = new Vector<String>();
    serverAddressCombo = WidgetCreator.makeComboBox(comboBoxContent, 3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.server.address"));
    serverAddressCombo.setMaximumSize(serverAddressCombo.getPreferredSize());
    serverAddressCombo.addActionListener(new IPComboBoxListener(serverAddressCombo,comboBoxContent));
    panel.add(label);
    panel.add(serverAddressCombo);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    mainPanel.add(panel);

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* server port panel */
    panel = new JPanel();
    panel.setLayout(new GridLayout(1,2));
    label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.port"));
    label.setMaximumSize(label.getPreferredSize());
    serverPortField = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.server.port"));
    panel.add(label);
    panel.add(serverPortField);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    mainPanel.add(panel);

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* password panel */
    try {
      panel = new JPanel();
      panel.setLayout(new GridLayout(1,2));
      label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.password"));
      label.setMaximumSize(label.getPreferredSize());
      passwordField = WidgetCreator.makeTextField(JPasswordField.class,
          ClientGUIConstants.PREFERRED_FIELD_WIDTH,
          I18nSupport.getValue(CLIENT_BUNDLE, "check.box.tooltip.show.password"));
      panel.add(label);
      panel.add(passwordField);
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
      mainPanel.add(panel);
    } catch (Exception e) {
      LOGGER.severe("Error creating password panel.\nMessage: "+e.getMessage());
    }

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    return mainPanel;
  }

  private JPanel getClientPanel() {
    final JPanel mainPanel = new JPanel();

    JLabel label;
    JPanel panel;

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_BUNDLE,"border.title.client.settings")));

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* client address panel */
    panel = new JPanel();
    panel.setLayout(new GridLayout(1,2));
    final Vector<String> comboBoxContent = new Vector<String>(3);
    label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.address"));
    label.setMaximumSize(label.getPreferredSize());
    clientAddressCombo = WidgetCreator.makeComboBox(comboBoxContent,3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "combo.box.tooltip.client.address"));
    clientAddressCombo.addActionListener(new IPComboBoxListener(clientAddressCombo,comboBoxContent));
    panel.add(label);
    panel.add(clientAddressCombo);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    mainPanel.add(panel);

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* client port panel */
    panel = new JPanel();
    panel.setLayout(new GridLayout(1,2));
    label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.port"));
    label.setMaximumSize(label.getPreferredSize());
    clientPortField = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_BUNDLE, "field.tooltip.client.port"));
    panel.add(label);
    panel.add(clientPortField);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    mainPanel.add(panel);

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* client name panel */
    panel = new JPanel();
    panel.setLayout(new GridLayout(1,2));
    label = new JLabel(I18nSupport.getValue(CLIENT_BUNDLE, "label.text.player.name"));
    label.setMaximumSize(label.getPreferredSize());
    nameField = new JTextField(Client.getOwnInstance().getName());
    panel.add(label);
    panel.add(nameField);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    mainPanel.add(panel);

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    /* spectator check box */
    panel = new JPanel();
    panel.setLayout(new GridLayout(1,2));
    spectatorCheckBox = new JCheckBox(
        I18nSupport.getValue(CLIENT_BUNDLE,"check.box.spectator"));
    spectatorCheckBox.setSelected(false);

    panel.add(Box.createHorizontalStrut(STRUT_WIDTH));
    panel.add(spectatorCheckBox);
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
    mainPanel.add(panel);

    mainPanel.add(Box.createVerticalStrut(STRUT_HEIGHT));

    return mainPanel;
  }
  //TODO neues Bild für Verbindungsdialog öffnen, kleinere Bilder für Toolbar,
  private JPanel getButtonPanel() {
    if(buttonPanel != null)
      return buttonPanel;

    buttonPanel = new JPanel();

    buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
    buttonPanel.add(new JButton(ActionFactory.getConnectAction()));
    buttonPanel.add(WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.close.save"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.close.save"),
        ACTION_COMMAND_CLOSE_SAVE, buttonListener));
    buttonPanel.add(WidgetCreator.makeButton(null,
        I18nSupport.getValue(CLIENT_BUNDLE, "button.text.cancel"),
        I18nSupport.getValue(CLIENT_BUNDLE, "button.tooltip.cancel"), ACTION_COMMAND_CANCEL,
        buttonListener));

    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

    return buttonPanel;
  }

  public void resetFields() {
    /* server fields */
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    serverAddressCombo.setSelectedItem(connectionInfo.getServerAddress());
    serverPortField.setText(connectionInfo.getServerPort().toString());
    passwordField.setText(connectionInfo.getPassword());

    /* client fields */
    final Client client = Client.getOwnInstance();
    clientAddressCombo.setSelectedItem(client.getIpAddress());
    clientPortField.setText(Integer.toString(client.getPort()));
    nameField.setText(client.getName());
    spectatorCheckBox.setSelected(client.getSpectating());
  }

  /* Inner Classes */

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(ACTION_COMMAND_CANCEL)) {
        resetFields();
        setVisible(false);
        dispose();
      } else if(e.getActionCommand().equals(ACTION_COMMAND_CLOSE_SAVE)) {
        fillClientInfo();
        fillConnectionInfo();
        setVisible(false);
        dispose();
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

package client.gui.frame;

import client.business.ConnectionInfo;
import client.gui.ActionFactory;
import common.dto.ClientInfo;
import common.i18n.I18nSupport;
import common.utilities.LoggingUtility;
import common.utilities.Miscellaneous;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.gui.FramePosition;
import common.utilities.gui.WidgetCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Inet4Address;
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
  private static ConnectionDialog DIALOG;
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionDialog.class.getName());

  private static final String CLIENT_BUNDLE = "client.client"; //NON-NLS
  private static final String ACTION_COMMAND_CONNECT = "connect"; //NON-NLS
  private static final String ACTION_COMMAND_CANCEL = "cancel";  //NON-NLS
  private static final String ACTION_COMMAND_CLOSE_SAVE = "closeSave";  //NON-NLS
  private static final int STRUT_HEIGHT = 5;
  private static final int STRUT_WIDTH = 5;

  private ClientInfo clientInfo;
  private ConnectionInfo connectionInfo;

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
  private ConnectionDialog() {
    super();
    /* initialise client info and connection info */
    String defaultName = System.getProperty("user.name");
    if (defaultName == null)
      defaultName = I18nSupport.getValue(CLIENT_BUNDLE,"default.player.name");
    clientInfo = new ClientInfo(defaultName);
    connectionInfo = new ConnectionInfo();

    /* initialise gui stuff */
    buttonListener = new ButtonListener();

    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
    getContentPane().add(getServerInfoPanel());
    getContentPane().add(Box.createGlue());
    getContentPane().add(getClientInfoPanel(defaultName));
    getContentPane().add(Box.createGlue());
    getContentPane().add(getButtonPanel());

    final FramePosition position = FramePosition.createFramePositions(
        ClientGUIConstants.SETUP_FRAME_SCREEN_SIZE_WIDTH,
        ClientGUIConstants.SETUP_FRAME_SCREEN_SIZE_HEIGHT);

    this.setBounds(position.getRectangle());
    this.setTitle(I18nSupport.getValue(CLIENT_BUNDLE, "frame.title.setup"));
    this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    fillConnectionInfo();
    fillClientInfo();
  }

  public static ConnectionDialog getInstance() {
    if(DIALOG == null) {
      DIALOG = new ConnectionDialog();
    }
    return DIALOG;
  }

  public static ConnectionDialog getInstance(boolean editable) {
    final ConnectionDialog dialog = getInstance();

    if(!editable) //TODO verbindungsinformationen nur anzeigen lassen
      return dialog;

    return dialog;
  }

  public static void main(String[] args) {
    ConnectionDialog.getInstance().setVisible(true);
  }


  /* Methods */

  private void fillConnectionInfo() {
    connectionInfo.setServerAddress(serverAddressCombo.getSelectedItem().toString());
    connectionInfo.setServerPort(Integer.parseInt(serverPortField.getText()));
    connectionInfo.setPassword(passwordField.getText());
    connectionInfo.setClientAddress(clientAddressCombo.getSelectedItem().toString());
    connectionInfo.setClientPort(Integer.parseInt(clientPortField.getText()));
  }

  private void fillClientInfo() {
    clientInfo.name = nameField.getText();
    clientInfo.spectating = spectatorCheckBox.isSelected();
    clientInfo.ipAddress = clientAddressCombo.getSelectedItem().toString();
    clientInfo.port = Integer.parseInt(clientPortField.getText());
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
    try {
      final InetAddress address = Miscellaneous.getHostInetAddress(Inet4Address.class);
      serverAddressCombo.addItem(address.getHostAddress());
    } catch (Exception e) {
      serverAddressCombo.addItem(InetAddress.getLoopbackAddress().getHostAddress());
    }
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
    serverPortField = WidgetCreator.makeIntegerTextField(
        GameConfigurationConstants.DEFAULT_PORT_STRING,
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
          ClientGUIConstants.PREFERRED_FIELD_WIDTH, I18nSupport.getValue(CLIENT_BUNDLE, "check.box.tooltip.show.password"));
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

  private JPanel getClientInfoPanel(String nameText) {
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
    try {
      final InetAddress address = Miscellaneous.getHostInetAddress(Inet4Address.class);
      clientAddressCombo.addItem(address.getHostAddress());
    } catch (Exception e) {
      clientAddressCombo.addItem(InetAddress.getLoopbackAddress().getHostAddress());
    }
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
    clientPortField = WidgetCreator.makeIntegerTextField(
        GameConfigurationConstants.DEFAULT_PORT_STRING,
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
    nameField = new JTextField(nameText);
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
  //TODO Bug: Abbrechen Knopf wird am Anfang nicht gezeigt
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
        I18nSupport.getValue(CLIENT_BUNDLE,"button.text.cancel"),
        I18nSupport.getValue(CLIENT_BUNDLE,"button.tooltip.cancel"), ACTION_COMMAND_CANCEL,
        buttonListener));

    buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height));

    return buttonPanel;
  }

  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  public ConnectionInfo getConnectionInfo() {
    return connectionInfo;
  }

  public void resetFields() {
    /* server fields */
    serverAddressCombo.setSelectedItem(connectionInfo.getServerAddress());
    serverPortField.setText(connectionInfo.getServerPort().toString());
    passwordField.setText(connectionInfo.getPassword());

    /* client fields */
    clientAddressCombo.setSelectedItem(clientInfo.ipAddress);
    clientPortField.setText(Integer.toString(clientInfo.port));
    nameField.setText(clientInfo.name);
    spectatorCheckBox.setSelected(clientInfo.spectating);
  }

  /* Inner Classes */

  private class ButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if(e.getActionCommand().equals(ACTION_COMMAND_CANCEL)) {
        resetFields();
        DIALOG.setVisible(false);
        DIALOG.dispose();
      } else if(e.getActionCommand().equals(ACTION_COMMAND_CLOSE_SAVE)) {
        fillClientInfo();
        fillConnectionInfo();
        DIALOG.setVisible(false);
        DIALOG.dispose();
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

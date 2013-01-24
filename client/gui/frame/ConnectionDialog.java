package client.gui.frame;

import client.business.Client;
import client.business.ConnectionInfo;
import client.business.client.GameClient;
import client.gui.ActionCollection;
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

import static common.i18n.BundleStrings.CLIENT_GUI;

/**
 * User: Timm Herrmann
 * Date: 05.01.13
 * Time: 22:15
 *
 * This dialog shows the information of the {@link client.business.Client} and
 * {@link client.business.ConnectionInfo} objects. It can either be a dialog that makes the
 * objects of the Client and ConnectionInfo classes modifyable or it just shows their attributes.
 */
public class ConnectionDialog extends AbstractDefaultDialog {
  private static final Logger LOGGER =
      LoggingUtility.getLogger(ConnectionDialog.class.getName());

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
  private JTextField nameField;
  private JLabel nameLabel;
  private JCheckBox spectatorCheckBox;
  private JLabel spectatorLabel;
  private JPanel buttonPanel;

  private boolean editable;

  /* Constructors */

  public ConnectionDialog(boolean editable) {
    this.editable = editable;

    /* initialise gui stuff */
    final JPanel dialogContent = getDialogContent();

    dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.PAGE_AXIS));
    dialogContent.add(getServerInfoPanel());
    dialogContent.add(getClientInfoPanel());

    if(editable) setConnectionButton();
    withApplyButton(editable);
    withOkayButton(editable);

    resetContent();

    /* initialise frame */
    final FramePosition position = FramePosition.createFramePositions(
        getContentPane().getPreferredSize().width,
        getContentPane().getPreferredSize().height);

    setBounds(position.getRectangle());
    setResizable(false);
    setTitle(I18nSupport.getValue(CLIENT_GUI, "frame.title.setup"));
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    pack();
  }

  public static void main(String[] args) {
    new ConnectionDialog(true).setVisible(true);
    new ConnectionDialog(false).setVisible(true);
  }

  /* Methods */

  private void setConnectionButton() {
    for (ActionListener listener : okayButton.getActionListeners()) {
      okayButton.removeActionListener(listener);
    }
    /* It seems that the action listener that will be added last, will be executed first. */
    /* Therefore the fill-methods listener will be added last. */
    okayButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(GameClient.getClient().isConnected()) {
          closeDialog();
        }
      }
    });
    okayButton.setAction(ActionCollection.CONNECT);
    okayButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveContent();
      }
    });
  }

  void saveContent() {
    /* save connection info */
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    connectionInfo.setServerAddress(serverAddressCombo.getSelectedItem().toString());
    connectionInfo.setServerPort(Integer.parseInt(serverPortField.getText()));
    connectionInfo.setPassword(passwordField.getText());

    /* save client info */
    final Client client = Client.getOwnInstance();
    client.setName(nameField.getText());
    client.setSpectating(spectatorCheckBox.isSelected());
  }

  void resetContent() {
    final ConnectionInfo connectionInfo = ConnectionInfo.getOwnInstance();
    final Client client = Client.getOwnInstance();

    if(editable) {
      /* server fields */
      serverAddressCombo.setSelectedItem(connectionInfo.getServerAddress());
      serverPortField.setText(connectionInfo.getServerPort().toString());
      passwordField.setText(connectionInfo.getPassword());
      /* client fields */
      nameField.setText(client.getName());
      spectatorCheckBox.setSelected(client.getSpectating());
    } else {
      /* server fields */
      serverAddressLabel.setText(connectionInfo.getServerAddress());
      serverPortLabel.setText(connectionInfo.getServerPort().toString());
      /* client fields */
      nameLabel.setText(client.getName());
      String key = "label.text.spectator."+client.getSpectating().toString(); //NON-NLS
      spectatorLabel.setText(I18nSupport.getValue(CLIENT_GUI, key));
    }
  }

  void closeDialog() {
    setVisible(false);
    dispose();
  }

  /* Getter and Setter */

  private JPanel getServerInfoPanel() {
    final JPanel mainPanel = new JPanel();

    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_GUI, "border.settings.server")));

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
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_GUI, "label.text.password"));
    label.setMaximumSize(label.getPreferredSize());
    try {
      passwordField = WidgetCreator.makeTextField(JPasswordField.class,
          ClientGUIConstants.PREFERRED_FIELD_WIDTH,
          I18nSupport.getValue(CLIENT_GUI, "checkbox.tooltip.show.password"));
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
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_GUI, "label.text.port"));
    label.setMaximumSize(label.getPreferredSize());
    serverPortField = WidgetCreator.makeIntegerTextField("",
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_GUI, "field.tooltip.server.port"));
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
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_GUI, "label.text.address"));
    label.setMaximumSize(label.getPreferredSize());
    final Vector<String> comboBoxContent = new Vector<String>();
    serverAddressCombo = WidgetCreator.makeComboBox(comboBoxContent, 3,
        ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        I18nSupport.getValue(CLIENT_GUI, "combobox.tooltip.server.address"));
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
        I18nSupport.getValue(CLIENT_GUI, "border.settings.client")));

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
        I18nSupport.getValue(CLIENT_GUI, "checkbox.text.spectator"));
    spectatorCheckBox.setSelected(false);
    spectatorLabel = new JLabel("");

    panel.setLayout(new GridLayout(1,2));
    if(editable) {
      panel.add(Box.createHorizontalStrut(STRUT_WIDTH));
      panel.add(spectatorCheckBox);
    } else {
      panel.add(new JLabel(I18nSupport.getValue(CLIENT_GUI, "label.text.status")));
      panel.add(spectatorLabel);
    }
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));

    return panel;
  }

  private JPanel getClientNamePanel() {
    final JPanel panel = new JPanel();
    JLabel label = new JLabel(I18nSupport.getValue(CLIENT_GUI, "label.text.player.name"));
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

  /* Inner Classes */
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
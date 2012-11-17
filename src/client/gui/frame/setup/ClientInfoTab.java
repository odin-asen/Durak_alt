package client.gui.frame.setup;

import client.gui.frame.ClientGUIConstants;
import dto.ClientInfo;
import utilities.constants.GameConfigurationConstants;
import utilities.gui.Constraints;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientInfoTab extends JPanel {
  private JTextField nameField;
  private JCheckBox spectatorCheckBox;

  private ClientInfo clientInfo;
  private JLabel nameLabel;

  /* Constructors */
  ClientInfoTab() {
    final int labelIPadX = 5;

    this.setLayout(new GridBagLayout());
    initComponents();
    GridBagConstraints constraints;

    constraints = Constraints.getDefaultFieldConstraintLeft(0, 0, 2, 1);
    constraints.ipadx = labelIPadX;
    add(nameLabel, constraints);
    constraints = Constraints.getDefaultFieldConstraintLeft(3, 0, 2, 1);
    add(nameField, constraints);
    constraints = Constraints.getDefaultFieldConstraintLeft(3, 1, 2, 1);
    add(spectatorCheckBox, constraints);
    clientInfo = new ClientInfo("", GameConfigurationConstants.NO_LOGIN_NUMBER);
    fillClientInfo();
  }

  /* Methods */
  void fillClientInfo() {
    clientInfo.name = nameField.getText();
    clientInfo.spectating = spectatorCheckBox.isSelected();
  }

  void initComponents() {
    nameLabel = new JLabel("Spielername:");
    int preferredHeight = nameLabel.getPreferredSize().height;
    nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));

    String defaultText = System.getProperty("user.name");
    if (defaultText == null)
      defaultText = "anonymus";

    nameField = new JTextField(defaultText);
    preferredHeight = nameField.getPreferredSize().height;
    nameField.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        preferredHeight));
    nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
    spectatorCheckBox = new JCheckBox("Nur zuschauen");
    preferredHeight = spectatorCheckBox.getPreferredSize().height;
    spectatorCheckBox.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH,
        preferredHeight));
    spectatorCheckBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredHeight));
    spectatorCheckBox.setSelected(false);
    spectatorCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        clientInfo.spectating = spectatorCheckBox.isSelected();
      }
    });
  }

  /* Getter and Setter */
  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  public void updateClientInfo(ClientInfo info) {
    clientInfo.playerType = info.playerType;
    clientInfo.spectating = info.spectating;
  }
}
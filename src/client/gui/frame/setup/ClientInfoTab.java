package client.gui.frame.setup;

import client.gui.frame.ClientGUIConstants;
import dto.ClientInfo;
import utilities.constants.GameConfigurationConstants;
import utilities.gui.Constraints;

import javax.swing.*;
import java.awt.*;

public class ClientInfoTab extends JPanel {
  private JTextField nameField;

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
    this.add(nameLabel, constraints);
    constraints = Constraints.getDefaultFieldConstraintLeft(3, 0, 2, 1);
    this.add(nameField, constraints);

    clientInfo = new ClientInfo("", GameConfigurationConstants.NO_LOGIN_NUMBER);
    fillClientInfo();
  }

  /* Methods */
  void fillClientInfo() {
    clientInfo.setName(nameField.getText());
  }

  void initComponents() {
    nameLabel = new JLabel("Spielername:");
    nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameLabel.getPreferredSize().height));

    String defaultText = System.getProperty("user.name");
    if (defaultText == null)
      defaultText = "anonymus";

    nameField = new JTextField(defaultText);
    nameField.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH, nameField.getPreferredSize().height));
    nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, nameField.getPreferredSize().height));
  }

  /* Getter and Setter */
  public ClientInfo getClientInfo() {
    return clientInfo;
  }

  public void updateClientInfo(ClientInfo info) {
    clientInfo.setPlayerType(info.getPlayerType());
  }
}
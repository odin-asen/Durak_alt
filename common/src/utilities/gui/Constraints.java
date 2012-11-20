package utilities.gui;

import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 20.10.12
 * Time: 00:37
 */
public class Constraints {
  /* Constructors */
  /* Methods */
  public static GridBagConstraints getDefaultFieldConstraintLeft(int gridx, int gridy, int gridwidth, int gridheight) {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = gridx;
    constraints.gridy = gridy;
    constraints.gridwidth = gridwidth;
    constraints.gridheight = gridheight;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.LINE_START;
    return constraints;
  }
}

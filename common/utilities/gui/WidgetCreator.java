package common.utilities.gui;

import common.resources.ResourceGetter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Vector;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 20.10.12
 * Time: 01:06
 */
public class WidgetCreator {
  private static final Logger LOGGER = Logger.getLogger(WidgetCreator.class.getName());

  public static JComboBox<String> makeComboBox(Vector<String> comboBoxContent, int maxRowCount,
                                               int preferredWidth, String toolTipText) {
    final JComboBox<String> comboBox = new JComboBox<String>(comboBoxContent);

    comboBox.setEditable(true);
    comboBox.setToolTipText(toolTipText);
    comboBox.setPreferredSize(new Dimension(preferredWidth, comboBox.getPreferredSize().height));
    comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBox.getPreferredSize().height));
    comboBox.setMaximumRowCount(maxRowCount);

    return comboBox;
  }

  public static JTextField makeTextField(Class<? extends JTextField> fieldClass, int preferredWidth, String toolTipText)
      throws IllegalAccessException, InstantiationException {
    JTextField field = fieldClass.newInstance();
    field.setPreferredSize(new Dimension(preferredWidth, field.getPreferredSize().height));
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
    field.setToolTipText(toolTipText);

    return field;
  }

  public static JTextField makeIntegerTextField(String text, int preferredWidth, String toolTipText) {
    final NumberFormat format = NumberFormat.getNumberInstance();
    final JTextField field = new JFormattedTextField(format);

    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);

    field.setText(text);
    field.setToolTipText(toolTipText);
    field.setPreferredSize(new Dimension(preferredWidth, field.getPreferredSize().height));
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));

    return field;
  }

  public static JButton makeToolBarButton(String pictureName, String toolTipText,
                                    String actionCommand, String alternativeText,
                                    ActionListener listener, int virtualKey) {
    JButton button = new JButton();
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setMnemonic(virtualKey);
    button.addActionListener(listener);
    button.setIcon(ResourceGetter.getImage(pictureName, alternativeText));

    if (button.getIcon() == null)
      button.setText(alternativeText);

    return button;
  }

  public static JButton makeButton(Icon icon, String text, String toolTipText,
                                   String actionCommand, ActionListener listener) {
    JButton button = new JButton();

    button.setText(text);
    button.setToolTipText(toolTipText);
    button.setActionCommand(actionCommand);
    button.setIcon(icon);
    button.addActionListener(listener);

    return button;
  }

  public static Border createStatusBorder() {
    final Border lowered = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
    return lowered;
  }
}

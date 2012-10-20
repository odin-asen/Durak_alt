package utilities.gui;

import client.gui.frame.ClientGUIConstants;
import resources.ResourceGetter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 20.10.12
 * Time: 01:06
 */
public class WidgetCreator {
  private static final Logger LOGGER = Logger.getLogger(WidgetCreator.class.getName());

  public static JComboBox<String> makeComboBox(Vector<String> comboBoxContent, String toolTipText) {
    final JComboBox<String> comboBox = new JComboBox<String>(comboBoxContent);

    comboBox.setEditable(true);
    comboBox.setToolTipText(toolTipText);
    comboBox.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH, comboBox.getPreferredSize().height));
    comboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, comboBox.getPreferredSize().height));

    return comboBox;
  }

  public static JTextField makeTextField(Class<? extends JTextField> fieldClass, String toolTipText)
      throws IllegalAccessException, InstantiationException {
    JTextField field = fieldClass.newInstance();
    field.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH, field.getPreferredSize().height));
    field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
    field.setToolTipText(toolTipText);

    return field;
  }

  public static JTextField makeIntegerTextField(String text, String toolTipText) {
    final NumberFormat format = NumberFormat.getNumberInstance();
    final JTextField field = new JFormattedTextField(format);

    format.setMaximumFractionDigits(0);
    format.setGroupingUsed(false);

    field.setText(text);
    field.setToolTipText(toolTipText);
    field.setPreferredSize(new Dimension(ClientGUIConstants.PREFERRED_FIELD_WIDTH, field.getPreferredSize().height));
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

  public static InputMethodListener createTextChangeNotifyListener(
      final Class<?> objectClass,final String methodName,
      final Class<?>[] parameterTypes, final Object firstObject,
      final Object ...parameterObjects) {
    InputMethodListener listener = new InputMethodListener() {
      public void inputMethodTextChanged(InputMethodEvent event) {
        try {
          System.out.println("blabla");
          final Method method = objectClass.getMethod(methodName, parameterTypes);
          method.invoke(firstObject, parameterObjects);
        } catch (NoSuchMethodException e) {
          LOGGER.log(Level.SEVERE, "Method "+methodName+" in class "+objectClass+
              " with parameter list "+parameterTypes.toString()+" not found!");
        } catch (InvocationTargetException e) {
          LOGGER.log(Level.SEVERE, e.getMessage());
        } catch (IllegalAccessException e) {
          LOGGER.log(Level.SEVERE, e.getMessage());
        }
      }

      public void caretPositionChanged(InputMethodEvent event) {
      }
    };

    return listener;
  }
}

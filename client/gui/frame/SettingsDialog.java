package client.gui.frame;

import client.data.GlobalSettings;
import common.i18n.I18nSupport;
import common.resources.ResourceGetter;
import common.utilities.LoggingUtility;
import common.utilities.gui.FramePosition;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import static common.i18n.BundleStrings.CLIENT_GUI;

/**
 * User: Timm Herrmann
 * Date: 22.01.13
 * Time: 19:13
 * <p/>
 * This dialog changes the global settings.
 */
public class SettingsDialog extends AbstractDefaultDialog {
  private static final Logger LOGGER = LoggingUtility.getLogger(SettingsDialog.class.getName());
  private JPanel generalPanel;
  private JPanel popupsPanel;
  private JPanel soundPanel;

  private JCheckBox checkboxPopups;
  private PopupSettingPanel panelPopupChat;
  private PopupSettingPanel panelPopupGame;
  private PopupSettingPanel panelPopupRule;
  private JComboBox<FlagLocale> comboboxLanguage;
  private JCheckBox checkboxSoundRule;

  /* Constructors */

  public SettingsDialog() {
    final GlobalSettings settings = GlobalSettings.getInstance();

    final JPanel dialogContent = getDialogContent();

    dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.PAGE_AXIS));
    dialogContent.add(getGeneralPanel());
    dialogContent.add(getSoundPanel());
    dialogContent.add(getPopupsPanel());

    /* initialise frame */
    final FramePosition position = FramePosition.createFramePositions(
        getContentPane().getPreferredSize().width,
        getContentPane().getPreferredSize().height);

    setBounds(position.getRectangle());
    setResizable(false);
    setTitle(I18nSupport.getValue(CLIENT_GUI, "dialog.title.settings"));
    resetContent();
    pack();
  }

  /* Methods */

  public static void main(String[] args) throws IOException {
    GlobalSettings.getInstance().readGlobalSettings();
    new SettingsDialog().setVisible(true);
  }

  private JPanel getGeneralPanel() {
    if(generalPanel != null)
      return generalPanel;

    generalPanel = new JPanel();
    comboboxLanguage = new JComboBox<FlagLocale>(FlagLocale.values());
    comboboxLanguage.setRenderer(new LocaleRenderer());

    generalPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_GUI, "border.settings.general")));
    generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.LINE_AXIS));

    generalPanel.add(new JLabel(I18nSupport.getValue(CLIENT_GUI, "label.text.language")));
    generalPanel.add(Box.createGlue());
    generalPanel.add(comboboxLanguage);

    return generalPanel;
  }

  private JPanel getPopupsPanel() {
    if(popupsPanel != null)
      return popupsPanel;

    final JPanel chatPanel = new JPanel();
    final JPanel gamePanel = new JPanel();
    final JPanel rulePanel = new JPanel();

    popupsPanel = new JPanel();

    checkboxPopups = new JCheckBox(I18nSupport.getValue(CLIENT_GUI, "checkbox.text.enabled"));
    checkboxPopups.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final boolean selected = checkboxPopups.isSelected();
        panelPopupChat.setEnabled(selected);
        panelPopupGame.setEnabled(selected);
        panelPopupRule.setEnabled(selected);
      }
    });
    panelPopupChat = new PopupSettingPanel(I18nSupport.getValue(CLIENT_GUI, "border.chat"));
    panelPopupGame = new PopupSettingPanel(I18nSupport.getValue(CLIENT_GUI, "border.ingame"));
    panelPopupRule = new PopupSettingPanel(I18nSupport.getValue(CLIENT_GUI, "border.rule"));

    popupsPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_GUI, "border.settings.popups")));
    popupsPanel.setLayout(new BoxLayout(popupsPanel, BoxLayout.PAGE_AXIS));

    final JPanel panel = new JPanel();
    panel.setLayout(new FlowLayout(FlowLayout.LEADING));
    panel.add(checkboxPopups);

    popupsPanel.add(panel);
    popupsPanel.add(panelPopupChat);
    popupsPanel.add(panelPopupGame);
    popupsPanel.add(panelPopupRule);

    return popupsPanel;
  }

  private JPanel getSoundPanel() {
    if(soundPanel != null)
      return soundPanel;

    soundPanel = new JPanel();

    checkboxSoundRule = new JCheckBox(
        I18nSupport.getValue(CLIENT_GUI, "checkbox.text.rule.exception"));

    soundPanel.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_GUI, "border.settings.sound")));
    soundPanel.setLayout(new GridLayout());
    soundPanel.add(checkboxSoundRule);

    return soundPanel;
  }

  void saveContent() {
    final GlobalSettings settings = GlobalSettings.getInstance();

    settings.popup.setEnabled(checkboxPopups.isSelected());
    settings.popup.getChat().setEnabled(panelPopupChat.isSelected());
    settings.popup.getChat().setDuration(panelPopupChat.getDuration());
    settings.popup.getGame().setEnabled(panelPopupGame.isSelected());
    settings.popup.getGame().setDuration(panelPopupGame.getDuration());
    settings.popup.getRule().setEnabled(panelPopupRule.isSelected());
    settings.popup.getRule().setDuration(panelPopupRule.getDuration());

    settings.sound.setRuleException(checkboxSoundRule.isSelected());

    settings.general.setLocale(((FlagLocale) comboboxLanguage.getSelectedItem()).locale);

    try {
      settings.writeGlobalSettings();
    } catch (IOException e) {
      LOGGER.warning("Could not save settings!");
    }
  }

  void resetContent() {
    final GlobalSettings settings = GlobalSettings.getInstance();

    final boolean popupsEnabled = settings.popup.isEnabled();
    checkboxPopups.setSelected(popupsEnabled);
    panelPopupChat.setEnabled(popupsEnabled);
    panelPopupGame.setEnabled(popupsEnabled);
    panelPopupRule.setEnabled(popupsEnabled);

    panelPopupChat.setSelected(settings.popup.getChat().isEnabled());
    panelPopupChat.setDuration((int) settings.popup.getChat().getDuration());
    panelPopupGame.setSelected(settings.popup.getGame().isEnabled());
    panelPopupGame.setDuration((int) settings.popup.getGame().getDuration());
    panelPopupRule.setSelected(settings.popup.getRule().isEnabled());
    panelPopupRule.setDuration((int) settings.popup.getRule().getDuration());

    checkboxSoundRule.setSelected(settings.sound.getRuleException());

    final FlagLocale item = FlagLocale.findVlaue(settings.general.getLocale());
    if(item != null)
      comboboxLanguage.setSelectedItem(item);
    else comboboxLanguage.setSelectedIndex(0);
  }

  void closeDialog() {
    setVisible(false);
    dispose();
  }

  /* Inner classes */

  private class LocaleRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList<?> list, Object value,
                                        int index, boolean isSelected, boolean cellHasFocus) {
      final Component superComponent =
          super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if(value == null)
        return this;
      final FlagLocale flagLocale = (FlagLocale) value;
      this.setIcon(flagLocale.flag);
      this.setText(flagLocale.locale.getDisplayCountry()
          + " ("+ flagLocale.locale.getDisplayLanguage()+")");

      return this;
    }
  }

  enum FlagLocale {
    GERMANY_GERMAN(new Locale("de", "de"), ResourceGetter.getGeneralIcon("flag.de")), //NON-NLS
    UNITED_KINGDOM_ENGLISH(new Locale("en", "gb"), ResourceGetter.getGeneralIcon("flag.gb")); //NON-NLS

    private Locale locale;
    private Icon flag;
    private FlagLocale(Locale locale, Icon flag) {
      this.locale = locale;
      this.flag = flag;
    }

    public static FlagLocale findVlaue(Locale locale) {
      for (FlagLocale flagLocale : FlagLocale.values()) {
        if(flagLocale.locale.getLanguage().equals(locale.getLanguage())
           && flagLocale.locale.getCountry().equals(locale.getCountry()))
          return flagLocale;
      }
      return null;
    }
  }
}

class PopupSettingPanel extends JPanel {
  private static final int HORIZONTAL_STRUT = 10;
  private static final int DURATION_MAXIMUM = 10;
  private static final int DURATION_MINIMUM = 0;

  private JCheckBox checkboxEnabled;
  private JPanel panelDuration;
  private JSlider sliderDuration;
  private JSpinner spinnerDuration;

  PopupSettingPanel(String borderLabel) {
    setBorder(BorderFactory.createTitledBorder(borderLabel));
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
    add(Box.createHorizontalStrut(HORIZONTAL_STRUT));
    add(getCheckboxEnabled());
    add(Box.createGlue());
    add(getPanelDuration());
  }

  private JCheckBox getCheckboxEnabled() {
    if(checkboxEnabled != null)
      return checkboxEnabled;

    checkboxEnabled = new JCheckBox(I18nSupport.getValue(CLIENT_GUI, "checkbox.text.enabled"));
    checkboxEnabled.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final boolean enabled = checkboxEnabled.isSelected() && isEnabled();
        panelDuration.setEnabled(enabled);
        spinnerDuration.setEnabled(enabled);
        sliderDuration.setEnabled(enabled);
      }
    });
    return checkboxEnabled;
  }

  private JPanel getPanelDuration() {
    if(panelDuration != null)
      return panelDuration;

    panelDuration = new JPanel();
    initDurationComponents();

    panelDuration.setLayout(new FlowLayout(FlowLayout.LEADING));
    panelDuration.setBorder(BorderFactory.createTitledBorder(
        I18nSupport.getValue(CLIENT_GUI, "border.duration.seconds")));
    panelDuration.add(sliderDuration);
    panelDuration.add(spinnerDuration);
    panelDuration.setMaximumSize(panelDuration.getPreferredSize());

    return panelDuration;
  }

  private void initDurationComponents() {
    final int minorTickSpacing = 1;
    spinnerDuration = new JSpinner(new SpinnerNumberModel(
        DURATION_MINIMUM, DURATION_MINIMUM, DURATION_MAXIMUM, minorTickSpacing));
    sliderDuration = new JSlider();

    sliderDuration.setMaximum(DURATION_MAXIMUM);
    sliderDuration.setMinimum(DURATION_MINIMUM);
    sliderDuration.setMajorTickSpacing(2);
    sliderDuration.setPaintLabels(true);
    sliderDuration.setSnapToTicks(true);
    sliderDuration.setMinorTickSpacing(minorTickSpacing);
    sliderDuration.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        spinnerDuration.setValue(sliderDuration.getValue());
      }
    });
    spinnerDuration.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        sliderDuration.setValue((Integer) spinnerDuration.getValue());
      }
    });
  }

  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    checkboxEnabled.setEnabled(enabled);
    panelDuration.setEnabled(enabled);
    spinnerDuration.setEnabled(enabled);
    sliderDuration.setEnabled(enabled);
  }

  public boolean isSelected() {
    return checkboxEnabled.isSelected();
  }

  public int getDuration() {
    return sliderDuration.getValue();
  }

  public void setSelected(boolean enabled) {
    checkboxEnabled.setSelected(enabled);
  }

  public void setDuration(int duration) {
    sliderDuration.setValue(duration);
  }
}

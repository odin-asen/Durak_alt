package client.data;

import client.data.xStreamModel.GeneralSettings;
import client.data.xStreamModel.PopupSetting;
import client.data.xStreamModel.PopupSettings;
import client.data.xStreamModel.SoundSettings;
import com.thoughtworks.xstream.XStream;

import java.io.*;

/**
 * User: Timm Herrmann
 * Date: 21.01.13
 * Time: 19:25
 */
public class GlobalSettings {
  private static final String ALIAS_GLOBAL = "durakSettings"; //NON-NLS
  private static final String ALIAS_POPUP = "popup"; //NON-NLS
  private static final String ALIAS_SOUND = "sound"; //NON-NLS
  private static final String ALIAS_GENERAL = "general"; //NON-NLS

  private static GlobalSettings globalSettings;

  public GeneralSettings general;
  public SoundSettings sound;
  public PopupSettings popup;

  /* Constructors */

  private GlobalSettings() {
    popup = new PopupSettings();
    general = new GeneralSettings();
    sound = new SoundSettings();
  }

  public static GlobalSettings getInstance() {
    if (globalSettings == null) {
      globalSettings = new GlobalSettings();
    }
    return globalSettings;
  }

  /* Methods */

  /**
   * Reads the global settings from a specified file.
   * @param filePath File path of the settings file.
   * @throws IOException see {@link java.io.FileReader#FileReader(String)} or if the reader
   * couldn't be closed.
   */
  public void readGlobalSettings(String filePath) throws IOException {
    final Reader reader = new FileReader(filePath);
    final XStream xStream = initialiseXStream();
    globalSettings = (GlobalSettings) xStream.fromXML(reader);
    reader.close();
  }

  /**
   * Writes the current global settings to the specified path.
   * @param filePath Specified path of the settings file.
   * @throws IOException see {@link java.io.FileWriter#FileWriter(String)}
   */
  public void writeGlobalSettings(String filePath) throws IOException {
    final XStream xStream = initialiseXStream();
    final String settings = xStream.toXML(globalSettings);
    final Writer writer = new FileWriter(filePath);
    xStream.toXML(globalSettings, writer);
    writer.close();
  }

  private XStream initialiseXStream() {
    final XStream xStream = new XStream();

    xStream.alias(ALIAS_GLOBAL, GlobalSettings.class);
    xStream.useAttributeFor(PopupSettings.class, "enabled"); //NON-NLS
    xStream.alias(ALIAS_POPUP, PopupSetting.class);
    xStream.useAttributeFor(PopupSetting.class, "enabled"); //NON-NLS
    xStream.alias(ALIAS_SOUND, SoundSettings.class);
    xStream.alias(ALIAS_GENERAL, GeneralSettings.class);

    return xStream;
  }
}

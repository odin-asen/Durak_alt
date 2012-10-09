package resources;

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:59
 */
public class ResourceGetter {
  public static final String RESOURCES_ROOT = "";
  public static final String PICTURES_ROOT = RESOURCES_ROOT + "icons/";
  public static final String SOUNDS_ROOT = RESOURCES_ROOT + "sounds/";

  public static final String STRING_IMAGE_CLOSE = "Close.png";
  public static final String STRING_IMAGE_NETWORK = "Network.png";
  public static final String STRING_IMAGE_PINION = "Pinion.png";
  public static final String STRING_IMAGE_PLAY = "Play.png";
  public static final String STRING_IMAGE_STOPPLAYER = "Stop Player.png";
  public static final String STRING_IMAGE_CONNECTED = "Connected.png";
  public static final String STRING_IMAGE_DISCONNECTED = "Disconnected.png";
  public static final String STRING_CARD_ACE = "ace.png";
  public static final String STRING_CARD_BACK = "back.png";

  public static ImageIcon loadImage(String imageName, String alternativeText)
    throws ResourceGetterException {
    ImageIcon image = null;

    try {
      final String path = ResourceGetter.PICTURES_ROOT + imageName;
      final URL url = ResourceGetter.class.getResource(path);
      if(url != null)
        image = new ImageIcon(url, alternativeText);
      else
        throw new ResourceGetterException("Could not find an URL for the path "+path);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return image;
  }

  public static void playSound(int soundNummer) {
    final int nummer = soundNummer;
    try {
      new Thread(new Runnable() {
        public void run() {
          try {
            SoundPlayer.playSound(nummer);
          } catch (Exception e) {
            System.err.println("Fehler beim Abspielen des Sounds: " + e.getMessage());
          }
        }
      }).start();
    } catch (Exception e) {
      System.err.println("Fehler beim Starten des Soundthreads: " + e.getMessage());
    }
  }
}

class SoundPlayer {
  public static void playSound(int soundNummer)
      throws IOException, LineUnavailableException, UnsupportedAudioFileException, InterruptedException {
    URL url = null;
    /*Für einen neuen Sound soll eine Nummer angelegt werden und in einer if-anweisung
    * abgefragt werden, für den Fall der ersten Nummer wird das erste if verwendet. */
    if(true) {
    } else {
      throw new IllegalArgumentException("Unbekannte Soundnummer übergeben.");
    }

    playSoundFile(url);
  }

  private static void playSoundFile(URL url)
      throws UnsupportedAudioFileException, IOException, LineUnavailableException, InterruptedException {
    final SourceDataLine line;
    final AudioInputStream ais = AudioSystem.getAudioInputStream(url);
    final AudioFormat format = ais.getFormat();
    final DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

    line = (SourceDataLine) AudioSystem.getLine(info);
    line.open(format);

    playData(ais, line, format.getFrameSize());

    // Line blockieren bis die gepufferten Daten zu Ende gespielt werden
    line.drain();

    line.close();
    ais.close();
  }

  private static void playData(AudioInputStream ais, SourceDataLine line, int framesize) throws IOException {
    // Allocate a buffer for reading from the input stream and writing
    // to the line.  Make it large enough to hold 4k audio frames.
    // Note that the SourceDataLine also has its own internal buffer.
    byte[] buffer = new byte[4 * 1024 * framesize];
    int bytesread = ais.read(buffer, 0, buffer.length);
    while (bytesread != -1) {
      line.start();
      line.write(buffer, 0, bytesread);
      bytesread = ais.read(buffer, 0, buffer.length);
    }
  }
}

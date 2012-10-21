package resources;

import utilities.constants.GameCardConstants;
import utilities.constants.GameConfigurationConstants;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:59
 */
public class ResourceGetter {
  private static Logger LOGGER = Logger.getLogger(ResourceGetter.class.getName());

  private static final int CARD_STRIPE_X_AXIS_GAP = 1;

  public static final String RESOURCES_ROOT = "";
  public static final String PICTURES_ROOT = RESOURCES_ROOT + "icons/";
  public static final String CARDS_ROOT = PICTURES_ROOT + "cards/";
  public static final String SOUNDS_ROOT = RESOURCES_ROOT + "sounds/";

  public static final String STRING_IMAGE_CLOSE = "Close.png";
  public static final String STRING_IMAGE_NETWORK = "Network.png";
  public static final String STRING_IMAGE_PINION = "Pinion.png";
  public static final String STRING_IMAGE_PLAY = "Play.png";
  public static final String STRING_IMAGE_STOP_PLAYER = "Stop Player.png";
  public static final String STRING_IMAGE_CONNECTED = "Connected.png";
  public static final String STRING_IMAGE_DISCONNECTED = "Disconnected.png";

  public static final String STRING_CARD_BACK = "back.png";
  public static final String STRING_CARD_COLOUR_CLUBS = "clubs.png";
  public static final String STRING_CARD_COLOUR_DIAMONDS = "diamonds.png";
  public static final String STRING_CARD_COLOUR_HEARTS = "hearts.png";
  public static final String STRING_CARD_COLOUR_SPADES = "spades.png";

  private static final int MAGIC_X_AXIS_GAP_CONSTANT = 2;

  public static ImageIcon getCardImage(GameCardConstants.CardColour colour, GameCardConstants.CardValue cardValue, String alternativeText) {
    ImageIcon image = null;

    try {
      final String path = ResourceGetter.CARDS_ROOT + getStringCardColour(colour);
      image = getCardFromStripe(loadImage(path, alternativeText), cardValue.getValue());
    } catch (ResourceGetterException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return image;
  }

  private static String getStringCardColour(GameCardConstants.CardColour colour) {
    final String string;

    if(GameCardConstants.CardColour.CLUBS.equals(colour)) {
      string = STRING_CARD_COLOUR_CLUBS;
    } else if(GameCardConstants.CardColour.DIAMONDS.equals(colour)) {
      string = STRING_CARD_COLOUR_DIAMONDS;
    } else if(GameCardConstants.CardColour.HEARTS.equals(colour)) {
      string = STRING_CARD_COLOUR_HEARTS;
    } else {
      string = STRING_CARD_COLOUR_SPADES;
    }

    return string;
  }

  private static ImageIcon getCardFromStripe(ImageIcon imageIcon, Integer cardNumber) {
    final Integer cardWidth = imageIcon.getIconWidth()/GameConfigurationConstants.MAXIMUM_COLOUR_CARD_COUNT - CARD_STRIPE_X_AXIS_GAP;
    final Integer xPos = cardWidth*cardNumber + CARD_STRIPE_X_AXIS_GAP*cardNumber + MAGIC_X_AXIS_GAP_CONSTANT;
    final BufferedImage imagePart;
    final BufferedImage bufferedImage = new BufferedImage(imageIcon.getIconWidth(),imageIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    bufferedImage.getGraphics().drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
    imagePart = bufferedImage.getSubimage(xPos,0,cardWidth,bufferedImage.getHeight());

    return new ImageIcon(imagePart);
  }

  public static ImageIcon getImage(String imageName, String alternativeText) {
    ImageIcon image = null;

    try {
      final String path = ResourceGetter.PICTURES_ROOT + imageName;
      image = loadImage(path, alternativeText);
    } catch (ResourceGetterException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return image;
  }

  private static ImageIcon loadImage(String imageURL, String alternativeText)
    throws ResourceGetterException {
    final ImageIcon image;

    final URL url = ResourceGetter.class.getResource(imageURL);
    if(url != null)
      image = new ImageIcon(url, alternativeText);
    else
      throw new ResourceGetterException("Could not find an URL for the path "+imageURL);

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

  public static ImageIcon getBackCard() {
    return getImage("cards/"+STRING_CARD_BACK, "Back");
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

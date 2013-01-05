package common.resources;

import common.utilities.LoggingUtility;
import common.utilities.constants.GameCardConstants;
import common.utilities.constants.GameConfigurationConstants;
import common.utilities.constants.PlayerConstants;
import common.utilities.gui.Compute;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static common.resources.ResourceList.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:59
 */
public class ResourceGetter {
  private static final Logger LOGGER = LoggingUtility.getLogger(ResourceGetter.class.getName());

  private static final int CARD_STRIPE_X_AXIS_GAP = 1;
  private static final int MAGIC_X_AXIS_GAP_CONSTANT = 2;

  public static ImageIcon getCardImage(GameCardConstants.CardColour colour, GameCardConstants.CardValue cardValue, String alternativeText) {
    ImageIcon image = null;

    try {
      final String path = CARDS_ROOT + getStringCardColour(colour);
      image = getCardFromStripe(loadImage(path, alternativeText), cardValue.getValue());
    } catch (ResourceGetterException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return image;
  }

  private static String getStringCardColour(GameCardConstants.CardColour colour) {
    final String string;

    if(GameCardConstants.CardColour.CLUBS.equals(colour)) {
      string = CARD_COLOUR_CLUBS;
    } else if(GameCardConstants.CardColour.DIAMONDS.equals(colour)) {
      string = CARD_COLOUR_DIAMONDS;
    } else if(GameCardConstants.CardColour.HEARTS.equals(colour)) {
      string = CARD_COLOUR_HEARTS;
    } else {
      string = CARD_COLOUR_SPADES;
    }

    return string;
  }

  private static ImageIcon getCardFromStripe(ImageIcon imageIcon, Integer cardNumber) {
    final int maxColours = GameConfigurationConstants.MAXIMUM_COLOUR_CARD_COUNT;
    final Integer cardWidth = imageIcon.getIconWidth()/ maxColours - CARD_STRIPE_X_AXIS_GAP;
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
      image = loadImage(imageName, alternativeText);
    } catch (ResourceGetterException e) {
      LOGGER.info(e.getMessage());
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
            LOGGER.warning("Fehler beim Abspielen des Sounds: " + e.getMessage());
          }
        }
      }).start();
    } catch (Exception e) {
      LOGGER.warning("Fehler beim Starten des Soundthreads: " + e.getMessage());
    }
  }

  public static ImageIcon getBackCard() {
    final String back = "Back"; //NON-NLS
    final ImageIcon icon = getImage(CARDS_ROOT+CARD_BACK, back);
    if(icon != null)
      return icon;
    else return new ImageIcon("", back);
  }

  public static ImageIcon getPlayerTypeIcon(PlayerConstants.PlayerType type, Integer height) {
    final String text = type.getDescription();
    final ImageIcon statusIcon;
    if (PlayerConstants.PlayerType.FIRST_ATTACKER.equals(type))
      statusIcon = getImage(IMAGE_STAR_GREEN, text);
    else if (PlayerConstants.PlayerType.SECOND_ATTACKER.equals(type))
      statusIcon = getImage(IMAGE_STAR_RED, text);
    else if (PlayerConstants.PlayerType.DEFENDER.equals(type))
      statusIcon = getImage(IMAGE_DEFENDER, text);
    else if (PlayerConstants.PlayerType.NOT_LOSER.equals(type))
      statusIcon = getImage(IMAGE_CROWN, text);
    else if (PlayerConstants.PlayerType.LOSER.equals(type))
      statusIcon = getImage(IMAGE_RED_CROSS, text);
    else statusIcon = null;

    if(statusIcon != null) {
      return Compute.getScaledImage(statusIcon, null, height);
    } else return null;
  }

  public static List<? extends Image> getApplicationIcons() {
    final String[] imageSizes = {"16","32","64","128","256"};
    final String suffix = ".png"; //NON-NLS
    final List<Image> images = new ArrayList<Image>(imageSizes.length);
    for (String size : imageSizes) {
      try {
        images.add(loadImage(APPLICATION_BASE_PATH + size + suffix, "").getImage());
      } catch (ResourceGetterException e) {
        LOGGER.info(e.getMessage());
      }
    }

    return images;
  }
}

class SoundPlayer {
  public static void playSound(int soundNummer)
      throws IOException, LineUnavailableException, UnsupportedAudioFileException, InterruptedException {
    URL url = null;
    /*Für einen neuen Sound soll eine Nummer angelegt werden und in einer if-anweisung
    * abgefragt werden, für den Fall der ersten Nummer wird das erste if verwendet. */
//    if(true) {
//    } else {
//      throw new IllegalArgumentException("Unbekannte Soundnummer übergeben.");
//    }

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

    /* Block the Line until the buffered data are played till the end */
    line.drain();

    line.close();
    ais.close();
  }

  private static void playData(AudioInputStream ais, SourceDataLine line, int framesize) throws IOException {
    /* Allocate a buffer for reading from the input stream and writing
     * to the line.  Make it large enough to hold 4k audio frames.
     * Note that the SourceDataLine also has its own internal buffer. */
    byte[] buffer = new byte[4 * 1024 * framesize];
    int bytesread = ais.read(buffer, 0, buffer.length);
    while (bytesread != -1) {
      line.start();
      line.write(buffer, 0, bytesread);
      bytesread = ais.read(buffer, 0, buffer.length);
    }
  }
}

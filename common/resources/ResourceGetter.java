package common.resources;

import common.i18n.I18nSupport;
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
import java.util.logging.Logger;

import static common.i18n.BundleStrings.RESOURCES_IMAGES;
import static common.i18n.BundleStrings.RESOURCES_SOUNDS;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:59
 */
public class ResourceGetter {
  private static final String PICTURES_ROOT = "icons/"; //NON-NLS
  private static final String TOOLBAR_ROOT = PICTURES_ROOT + "toolbar/"; //NON-NLS
  private static final String STATUS_ROOT = PICTURES_ROOT + "status/"; //NON-NLS
  private static final String CARDS_ROOT = PICTURES_ROOT + "cards/"; //NON-NLS
  private static final String GENERAL_ROOT = PICTURES_ROOT + "general/"; //NON-NLS

  private static final Logger LOGGER = LoggingUtility.getLogger(ResourceGetter.class.getName());

  private static final int CARD_STRIPE_X_AXIS_GAP = 1;
  private static final int MAGIC_X_AXIS_GAP_CONSTANT = 2;

  /* Loads an image from the specified path and adds the */
  /* surpassed extension if it is not null */
  private static ImageIcon getImage(String imageName, String extension) {
    ImageIcon image = null;

    if(extension == null) extension = "";
    else if(!extension.isEmpty()) extension = "."+extension;

    try {
      image = loadImage(imageName+extension); //NON-NLS
    } catch (ResourceGetterException e) {
      LOGGER.warning(e.getMessage());
    }

    return image;
  }

  private static ImageIcon loadImage(String imageURL)
      throws ResourceGetterException {
    final ImageIcon image;

    final URL url = ResourceGetter.class.getResource(imageURL);
    if(url != null)
      image = new ImageIcon(url);
    else
      throw new ResourceGetterException("Could not find an URL for the path "+imageURL);

    return image;
  }

  /********************************/
  /* Different Image Load Methods */
  /********************************/

  public static ImageIcon getBackCard() {
    return getImage(CARDS_ROOT+I18nSupport.getValue(RESOURCES_IMAGES,"card.back"), "png"); //NON-NLS
  }

  public static ImageIcon getPlayerTypeIcon(PlayerConstants.PlayerType type, Integer height) {
    final String iconName;
    if (PlayerConstants.PlayerType.FIRST_ATTACKER.equals(type))
      iconName = "status.star.green";
    else if (PlayerConstants.PlayerType.SECOND_ATTACKER.equals(type))
      iconName = "status.star.red";
    else if (PlayerConstants.PlayerType.DEFENDER.equals(type))
      iconName = "status.defender";
    else if (PlayerConstants.PlayerType.NOT_LOSER.equals(type))
      iconName = "status.crown";
    else if (PlayerConstants.PlayerType.LOSER.equals(type))
      iconName = "status.ivan.durak";
    else iconName = null;

    if(iconName != null) {
      return Compute.getScaledImage(
          getImage(STATUS_ROOT+I18nSupport.getValue(RESOURCES_IMAGES, iconName), "png"), //NON-NLS
          null, height);
    } else return null;
  }

  public static List<? extends Image> getApplicationIcons() {
    final String[] imageSizes = {"16","32","64","128","256"};
    final String suffix = "png"; //NON-NLS
    final String basePath = PICTURES_ROOT + "application/DurakIcon"; //NON-NLS

    final List<Image> images = new ArrayList<Image>(imageSizes.length);
    for (String size : imageSizes) {
      images.add(getImage(basePath + size, suffix).getImage());
    }

    return images;
  }

  public static ImageIcon getCardImage(GameCardConstants.CardColour colour,
                                       GameCardConstants.CardValue cardValue) {

    return getCardFromStripe(
        getImage(CARDS_ROOT + getStringCardColour(colour), "png"),
        cardValue.getValue());
  }

  private static String getStringCardColour(GameCardConstants.CardColour colour) {
    final String string;

    if(GameCardConstants.CardColour.CLUBS.equals(colour)) {
      string = "card.colour.clubs";
    } else if(GameCardConstants.CardColour.DIAMONDS.equals(colour)) {
      string = "card.colour.diamonds";
    } else if(GameCardConstants.CardColour.HEARTS.equals(colour)) {
      string = "card.colour.hearts";
    } else {
      string = "card.colour.spades";
    }

    return I18nSupport.getValue(RESOURCES_IMAGES, string);
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

  public static ImageIcon getToolbarIcon(String toolbarBundleKey, Object... params) {
    return getImage(TOOLBAR_ROOT+I18nSupport.getValue(RESOURCES_IMAGES, toolbarBundleKey, params),
        "png"); //NON-NLS
  }

  public static ImageIcon getStatusIcon(String statusBundleKey, Object... params) {
    return getImage(STATUS_ROOT+I18nSupport.getValue(RESOURCES_IMAGES, statusBundleKey, params),
        "png"); //NON-NLS
  }

  public static ImageIcon getGeneralIcon(String bundleKey, Object... params) {
    return getImage(GENERAL_ROOT+I18nSupport.getValue(RESOURCES_IMAGES, bundleKey, params),
        "png"); //NON-NLS
  }

  /**********************/
  /* Sound Load Methods */
  /**********************/

  public static void playSound(String soundBundleKey, Object... params) {
    final String path = "sounds/"+I18nSupport.getValue(RESOURCES_SOUNDS, soundBundleKey, params); //NON-NLS
    try {
      new Thread(new Runnable() {
        public void run() {
          try {
            SoundPlayer.playSoundFile(ResourceGetter.class.getResource(path));
          } catch (Exception e) {
            LOGGER.warning("Error playing the sound! Message: " + e.getMessage());
          }
        }
      }).start();
    } catch (Exception e) {
      LOGGER.warning("Error starting the sound thread! Message: " + e.getMessage());
    }
  }
}

class SoundPlayer {
  public static void playSoundFile(URL url)
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

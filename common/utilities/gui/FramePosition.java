package common.utilities.gui;

import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:44
 */
public class FramePosition {
  private int posX;
  private int posY;
  private int width;
  private int height;
  private Rectangle rectangle;

  /* Constructors */

  private FramePosition(Dimension screenSize, float screenSizeFrameWidth,
                        float screenSizeFrameHeight) {
    if(screenSize == null)
      screenSize = new Dimension(1366,768);

    initAttributes(screenSize, screenSizeFrameWidth, screenSizeFrameHeight);
  }

  private FramePosition(Dimension screenSize, int frameWidth, int frameHeight) {
    if(screenSize == null)
      screenSize = new Dimension(1366,768);
    float screenSizeFrameWidth = (float) (frameWidth/screenSize.getWidth());
    float screenSizeFrameHeight = (float) (frameHeight/screenSize.getHeight());
    initAttributes(screenSize,screenSizeFrameWidth,screenSizeFrameHeight);
  }

  public static FramePosition createFramePositions(float screenSizeFrameWidth,
                                                   float screenSizeFrameHeight) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new FramePosition(screenSize, screenSizeFrameWidth, screenSizeFrameHeight);
  }

  public static FramePosition createFramePositions(int frameWidth, int frameHeight) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new FramePosition(screenSize, frameWidth, frameHeight);
  }

  /* Methods */

  private void initAttributes(Dimension screenSize, float screenSizeFrameWidth,
                              float screenSizeFrameHeight) {
    width = (int) (screenSize.width* Math.abs(screenSizeFrameWidth));
    height = (int) (screenSize.height* Math.abs(screenSizeFrameHeight));
    posX = (int) (screenSize.width*0.5f- width *0.5f);
    posY = (int) (screenSize.height*0.5f- height *0.5f);
    rectangle = new Rectangle(posX,posY, width, height);
  }

  /* Getter and Setter */

  public int getPosX() {
    return posX;
  }

  public int getPosY() {
    return posY;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @SuppressWarnings({"", "HardCodedStringLiteral"})
  public String toString() {
    return "PosX" +posX+"; "+
        "PosY: "+posY+"; "+
        "Width: "+ width +"; "+
        "Height: "+ height +"; ";
  }

  public Rectangle getRectangle() {
    return rectangle;
  }
}

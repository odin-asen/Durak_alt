package common.utilities.gui;

import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:44
 */
public class FramePosition {
  private final int posX;
  private final int posY;
  private final int width;
  private final int height;
  private final Rectangle rectangle;

  private FramePosition(Dimension screenSize, float screenSizeFrameWidth,
                        float screenSizeFrameHeight) {
    if(screenSize == null)
      screenSize = new Dimension(1366,768);

    width = (int) (screenSize.width* Math.abs(screenSizeFrameWidth));
    height = (int) (screenSize.height* Math.abs(screenSizeFrameHeight));
    posX = (int) (screenSize.width*0.5f- width *0.5f);
    posY = (int) (screenSize.height*0.5f- height *0.5f);
    rectangle = new Rectangle(posX,posY, width, height);
  }

  public static FramePosition createFramePositions(float screenSizeFrameWidth,
                                                   float screenSizeFrameHeight) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new FramePosition(screenSize, screenSizeFrameWidth, screenSizeFrameHeight);
  }

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

package utilities.gui;

import java.awt.*;

/**
 * User: Timm Herrmann
 * Date: 03.10.12
 * Time: 19:44
 */
public class FramePosition {
  private int posX;
  private int posY;
  private int breite;
  private int hoehe;
  private Rectangle rectangle;

  private FramePosition(Dimension screenSize, float screenSizeFensterBreite, float screenSizeFensterHoehe) {
    if(screenSize == null)
      screenSize = new Dimension(1366,768);

    breite = (int) (screenSize.width* Math.abs(screenSizeFensterBreite));
    hoehe= (int) (screenSize.height* Math.abs(screenSizeFensterHoehe));
    posX = (int) (screenSize.width*0.5f-breite*0.5f);
    posY = (int) (screenSize.height*0.5f-hoehe*0.5f);
    rectangle = new Rectangle(posX,posY,breite,hoehe);
  }

  public static FramePosition createFensterPositionen(float screenSizeFensterBreite, float screenSizeFensterHoehe) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new FramePosition(screenSize, screenSizeFensterBreite, screenSizeFensterHoehe);
  }

  public int getPosX() {
    return posX;
  }

  public int getPosY() {
    return posY;
  }

  public int getBreite() {
    return breite;
  }

  public int getHoehe() {
    return hoehe;
  }

  public String toString() {
    return "PosX: "+posX+"; "+
        "PosY: "+posY+"; "+
        "Breite: "+breite+"; "+
        "Hoehe: "+hoehe+"; ";
  }

  public Rectangle getRectangle() {
    return rectangle;
  }
}

package dto;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 14:34
 */
public class DTOCard {
  public short cardType;
  public boolean movable;
  public short cardValue;
  public short cardColor;

  public String toString() {
    return "DTOCard{" +
        "cardType=" + cardType +
        ", movable=" + movable +
        ", cardValue=" + cardValue +
        ", cardColor=" + cardColor +
        '}';
  }
}

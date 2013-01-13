package client.gui.frame.gamePanel;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 23:31
 */
public interface CardContainer<T> {
  boolean removeCard(T card);
  boolean addCard(T card);
  boolean cardExists(T card);
}

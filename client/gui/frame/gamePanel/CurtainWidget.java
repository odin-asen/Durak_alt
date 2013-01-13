package client.gui.frame.gamePanel;

/**
 * User: Timm Herrmann
 * Date: 12.01.13
 * Time: 22:08
 *
 * This interface is for widgets that can paint curtains. The paintCurtain method should call
 * the implementation to paint a curtain, however it will do it.
 */
public interface CurtainWidget {
  /**
   * Calls the widget to paint a curtain.
   * @param paint If true, the curtain shuold be painted.
   */
  void paintCurtain(boolean paint);
}

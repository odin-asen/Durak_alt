package common.utilities;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * User: Timm Herrmann
 * Date: 05.01.13
 * Time: 19:29
 */
public class LoggingUtility {
  private static final String DEFAULT_NAME = "defaultLog.txt"; //NON-NLS
  private static Handler handler = null;
  private static boolean logFileChangable = true;

  /**
   * Sets the file name for the logging file handler. It can be set only one time. Once the
   * method was called the path can not be changed for the handler until the program will be
   * restarted.
   * @param fileName Path of the logging file.
   * @return Returns true, if the path was changed and false if it wasn't.
   */
  public static boolean setFirstTimeLoggingFile(String fileName) {
    if(!logFileChangable)
      return false;

    try {
      final SimpleFormatter formatter = new SimpleFormatter();
      handler = new FileHandler(fileName);
      handler.setFormatter(formatter);
      logFileChangable = false;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return true;
  }

  private static Handler createDefaultHandler() {
    final Handler handler;
    try {
      handler = new FileHandler(DEFAULT_NAME);
      handler.setFormatter(new SimpleFormatter());
      return handler;
    } catch (IOException e) {e.printStackTrace();}
    return null;
  }

  /**
   * Returns or creates a logger for the specified name. The handler,
   * specified by the {@link #setFirstTimeLoggingFile} method, will be added to the
   * logger if it does not exist already.
   * @param name A name for the logger. This should be a dot-separated name and
   *             should normally be based on the package name or class name of
   *             the subsystem, such as java.net or javax.swing
   * @return A Logger object.
   */
  public static Logger getLogger(String name) {
    final Logger logger = Logger.getLogger(name);
    boolean handlerExists = false;
    if(handler == null)
      handler = createDefaultHandler();

    for (Handler h : logger.getHandlers())
      handlerExists = handlerExists | h.equals(handler);
    if(!handlerExists)
      logger.addHandler(handler);
    return logger;
  }
}
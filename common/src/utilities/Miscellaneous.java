package utilities;

import java.util.Collection;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 18:37
 */
public class Miscellaneous {

  private static final String CLOSE_BRACKET = "]";
  private static final String OPEN_BRACKET = "[";
  private static final String MESSAGE_START = CLOSE_BRACKET+": ";

  public static String getChatMessage(String inBrackets, String message) {
    return OPEN_BRACKET +inBrackets+MESSAGE_START+message;
  }

  public static String changeChatMessageInBrackets(String newInBrackets, String message) {
    StringBuilder builder = new StringBuilder(message);
    return builder.replace(1,message.indexOf(MESSAGE_START),newInBrackets).toString();
  }

  public static <T> void addAllToCollection(Collection<T> addIn,
                              Collection<T> collectionToAdd) {
    if(collectionToAdd == null)
      return;

    for (T element : collectionToAdd) {
      addIn.add(element);
    }
  }
}

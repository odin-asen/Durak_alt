package common.game.rules;

/**
 * User: Timm Herrmann
 * Date: 29.10.12
 * Time: 15:02
 * <p/>
 * This exception should be thrown, if a rule has been broken. A message should always
 * been set to inform the client why the rule was broken.
 */
public class RuleException extends Exception {
  /* Constructors */
  public RuleException(String message) {
    super(message);
  }
}

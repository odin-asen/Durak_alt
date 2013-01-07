package common.simon.action;

import common.dto.DTOCard;
import common.dto.DTOClient;

import java.io.Serializable;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.01.13
 * Time: 00:41
 *
 * This class is immutable.
 */
public abstract class GameAction implements Serializable {
  public enum ActionType {CARD_ACTION, ROUND_REQUEST}

  protected ActionType type;
  protected DTOClient executor;

  /* Constructors */

  public GameAction(DTOClient executor, ActionType type) {
    this.executor = new DTOClient("");
    this.executor.setClientInfo(executor);
    this.type = type;
  }

  /* Getter and Setter */

  public ActionType getActionType() {
    return type;
  }

  public DTOClient getExecutor() {
    final DTOClient other = new DTOClient("");
    other.setClientInfo(other);
    return other;
  }
}
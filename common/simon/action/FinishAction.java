package common.simon.action;

import common.dto.DTOClient;

/**
 * User: Timm Herrmann
 * Date: 10.11.12
 * Time: 02:56
 *
 * This class is immutable if GameAction is, too.
 */
public final class FinishAction extends GameAction {
  public enum FinishType {GO_TO_NEXT_ROUND, NOT_FINISHING, TAKE_CARDS}

  private FinishType finishType;

  public FinishAction(FinishType finishType, DTOClient executor, GameAction.ActionType type) {
    super(executor, type);
    this.finishType = finishType;
  }

  public FinishType getFinishType() {
    return finishType;
  }

  public ActionType getActionType() {
    return ActionType.ROUND_REQUEST;
  }
}

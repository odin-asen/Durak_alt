package client.business;

import dto.DTOCard;

import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 07.10.12
 * Time: 17:20
 */
public class RuleChecker {
  public RuleChecker() {

  }

  public boolean attack(DTOCard attackerCard, List<DTOCard> currentCards) {
    return false;
  }

  public boolean defend(DTOCard defenderCard, DTOCard attackerCard) {
    return false;
  }
}

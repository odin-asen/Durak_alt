package common.rmi;

import common.rmi.Authenticator;
import common.rmi.ChatHandler;
import common.rmi.GameAction;
import common.rmi.RMIObservable;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 22:38
 */
@SuppressWarnings("HardCodedStringLiteral")
public enum RMIService {
  AUTHENTICATION("Authenticator", Authenticator.class),
  OBSERVER("RMIObservation", RMIObservable.class),
  ATTACK_ACTION("AttackAction", GameAction.class),
  DEFENSE_ACTION("DefenseAction", GameAction.class),
  ROUND_STATE_ACTION("RoundStateAction", GameAction.class),
  CHAT("ChatHandler", ChatHandler.class);

  private final String serviceName;
  private final Class serviceClass;

  RMIService(String serviceName, Class serviceClass) {
    this.serviceName = serviceName;
    this.serviceClass = serviceClass;
  }

  public String getServiceName() {
    return serviceName;
  }

  public Class getServiceClass() {
    return serviceClass;
  }

  public String toString() {
    return "RMIService{" +
        "serviceClass=" + serviceClass +
        ", serviceName='" + serviceName + '\'' +
        '}';
  }
}

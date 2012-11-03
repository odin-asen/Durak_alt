package rmi;

/**
 * User: Timm Herrmann
 * Date: 22.10.12
 * Time: 22:38
 */
public enum RMIService {
  AUTHENTICATION("Authenticator", Authenticator.class),
  OBSERVER("RMIObservation", RMIObservable.class),
  ATTACK_ACTION("AttackAction", GameAction.class),
  DEFENSE_ACTION("DefenseAction", GameAction.class),
  CHAT("ChatHandler", ChatHandler.class);

  private String serviceName;
  private Class serviceClass;

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
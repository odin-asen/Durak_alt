package dto.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 08.10.12
 * Time: 22:10
 * <p>
 * This class can be used as object that will be given from the {@link java.util.Observable}
 * extending class to its observers when they were notified.
 * </p>
 */
public class ObserverUpdateObject {
  private Enum<?> observerConstant;
  private Object information;

  /* Constructors */
  public ObserverUpdateObject(Enum<?> observerConstant, Object object) {
    this.observerConstant = observerConstant;
    this.information = object;
  }

  public ObserverUpdateObject(Enum<?> observerConstant) {
    this.observerConstant = observerConstant;
  }

  /* Methods */

  /* Getter and Setter */
  public Enum<?> getObserverConstant() {
    return observerConstant;
  }

  public void setObserverConstant(Enum<?> observerConstant) {
    this.observerConstant = observerConstant;
  }

  public Object getInformation() {
    return information;
  }

  public void setInformation(Object information) {
    this.information = information;
  }
}

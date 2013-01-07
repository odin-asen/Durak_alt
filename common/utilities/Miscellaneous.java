package common.utilities;

import common.dto.DTOClient;

import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * User: Timm Herrmann
 * Date: 21.10.12
 * Time: 18:37
 */
public class Miscellaneous {

  /**
   * The client comparator comparates two clients with their ip address and port
   * attributes. The compare method returns 0 if the clients have the same ip address
   * and port or both are null. If one ip address or port differs from the other, either
   * a value less than 0 or bigger than 0 will be returned.
   */
  public static final Comparator<DTOClient> CLIENT_COMPARATOR = new Comparator<DTOClient>() {
    public int compare(DTOClient o1, DTOClient o2) {
      if(o1 == null && o2 == null) return 0;
      if(o1 == null && o2 != null) return -1;
      if(o1 != null && o2 == null) return 1;

      int result = -1;
      if(o1.ipAddress != null && o2.ipAddress != null) {
        if(o1.ipAddress.compareTo(o2.ipAddress) == 0)
          result = o1.port - o2.port;
      } else if(o1.ipAddress == null && o2.ipAddress == null) {
        result = o1.port - o2.port;
      }

      return result;
    }
  };

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

  public static <T> void addAllToCollection(Collection<T> to, Collection<T> from) {
    if(from == null)
      return;

    for (T element : from) {
      to.add(element);
    }
  }

  public static <T> int findIndex(List<T> list, T element) {
    for (int index = 0; index < list.size(); index++) {
      if(list.get(index).equals(element))
        return index;
    }
    return -1;
  }

  public static <T> int findIndex(List<T> list, T element, Comparator<T> comparator) {
    for (int index = 0; index < list.size(); index++) {
      if(comparator.compare(list.get(index),element) == 0)
        return index;
    }
    return -1;
  }

  public static Registry getSafeRegistry(int port) throws RemoteException {
    try {
      return LocateRegistry.createRegistry(port);
    } catch (RemoteException e) {
      return LocateRegistry.getRegistry(port);
    }
  }

  /**
   * Looks for the host address of the device and returns the object of the specified class.
   * of the specified class.
   * @param inetAddressClass Subclass of InetAddress. Specifies which representation should
   *                         be returned.
   * @return InetAddress object that represents the host address. If no representation could be found
   * the loopback address will be returned.
   * @throws SocketException Refer to {@link java.net.NetworkInterface#getNetworkInterfaces()}
   */
  public static <T extends InetAddress> InetAddress getHostInetAddress(
      Class<T> inetAddressClass) throws SocketException {
    final Enumeration<NetworkInterface> faces = NetworkInterface.getNetworkInterfaces();
    /* run through all network interfaces and look for the host address
     * of the specified class */
    while(faces.hasMoreElements()) {
      final NetworkInterface face = faces.nextElement();
      for(Enumeration<InetAddress> addresses = face.getInetAddresses();
          addresses.hasMoreElements();) {
        final InetAddress address = addresses.nextElement();
        if(!address.isLoopbackAddress())
          if(inetAddressClass.isInstance(address))
            return address;
      }
    }
    return InetAddress.getLoopbackAddress();
  }

  public static boolean containsClientAddress(List<DTOClient> list, DTOClient client) {
    boolean exists = false;
    for (DTOClient info : list) {
      exists = exists | (CLIENT_COMPARATOR.compare(info,client) == 0);
    }
    return exists;
  }
}

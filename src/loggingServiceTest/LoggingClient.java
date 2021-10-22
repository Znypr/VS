package loggingServiceTest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;

public class LoggingClient {
  private static final int PORT = 4711;
  private static final int BUFSIZE = 2048;
  private static final int TIMEOUT = 2000;

  public static void main(String[] args) {

    if (args.length != 8) {
      throw new RuntimeException("Es wurden nicht alle 8 benötigten Argumente eingegeben. Bitte prüfen Sie ihre Eingaben und die Dokumentation.");
    }
    
    // PRI berechnen
    int faciliyCode = getFacilityCode(args[0]);
    int severityCode = getSeverityCode(args[1]);
    int priorityValue = faciliyCode * 8 + severityCode;
    
    String PRI = "<" + priorityValue + ">";
    
    String VERSION = getValidVersion(args[2]);
    
    String HOSTNAME = getHostName(args[3]);
    
    String APPNAME = getValidArgument(args[4]);
    
    String PROCID = getValidArgument(args[5]);
    
    String MSGID = getValidArgument(args[6]);
    
    String MSG = args[7];
    
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(TIMEOUT);

      InetAddress discoveryIaddr = InetAddress.getByName("255.255.255.255");
      DatagramPacket discoveryPacketOut = new DatagramPacket(new byte[0], 0, discoveryIaddr, PORT);
      socket.send(discoveryPacketOut);

      System.out.println("Sende autodiscovery message.");

      DatagramPacket discoveryPacketIn = new DatagramPacket(new byte[0], 0);
      socket.receive(discoveryPacketIn);
      InetAddress loggingServerIaddr = discoveryPacketIn.getAddress();

      System.out.println(
          "IP vom Server gefunden: " + loggingServerIaddr.getHostAddress() + " . Sende Syslog-Nachricht...");

      String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSXXX").format(new Date());

      String log = PRI;
      log += VERSION;
      log += " " + timeStamp;
      log += " " + HOSTNAME;
      log += " " + APPNAME;
      log += " " + PROCID;
      log += " -"; // keine structured data
      log += " " + MSGID;
      log += " " + MSG;

      byte[] data = log.getBytes();

      if (data.length > BUFSIZE) {
        throw new RuntimeException("Die Syslog Nachricht ist mit " + data.length
            + " Bytes zu lang! Maximal " + BUFSIZE + " Bytes zulässig.");
      } else {
        DatagramPacket packetOut = new DatagramPacket(data, data.length, loggingServerIaddr, PORT);
        socket.send(packetOut);
      }

    } catch (SocketTimeoutException e) {
      System.err.println("Timeout: " + e.getMessage());
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  private static String getValidVersion(String version) {
    if (version.length() == 0) {
      throw new RuntimeException("Version darf kein leerer String sein!");
    } else {
      return version;
    }
  }
  
  private static String getValidArgument(String argument) {
    if (argument.length() == 0) {
      return "-"; // NILVALUE
    } else {
      return argument;
    }
  }
  
  private static String getHostName(String hostname) {
    if (hostname.length() == 0) {
      try {
        return InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
        return "-"; // NILVALUE
      }
    } else {
      return hostname;
    }
  }
  
  private static int getFacilityCode(String facility) {
    switch (facility) {
    case "0":
      return 0;
    case "kernel messages":
      return 0;
    case "1":
      return 1;
    case "user-level messages":
      return 1;
    case "2":
      return 2;
    case "mail system":
      return 2;
    case "3":
      return 3;
    case "system daemons":
      return 3;
    case "4":
      return 4;
    case "security/authorization messages":
      return 4;
    case "5":
      return 5;
    case "messages generated internally by syslogd":
      return 5;
    case "6":
      return 6;
    case "line printer subsystem":
      return 6;
    case "7":
      return 7;
    case "network news subsystem":
      return 7;
    case "8":
      return 8;
    case "UUCP subsystem":
      return 8;
    case "9":
      return 9;
    case "clock daemon":
      return 9;
    case "10":
      return 10;
    case "11":
      return 11;
    case "FTP daemon":
      return 11;
    case "12":
      return 12;
    case "NTP subsystem":
      return 12;
    case "13":
      return 13;
    case "log audit":
      return 13;
    case "14":
      return 14;
    case "log alert":
      return 14;
    case "15":
      return 15;
    case "clock daemon (note 2)":
      return 15;
    case "16":
      return 16;
    case "local0":
      return 16;
    case "17":
      return 17;
    case "local1":
      return 17;
    case "18":
      return 18;
    case "local2":
      return 18;
    case "19":
      return 19;
    case "local3":
      return 19;
    case "20":
      return 20;
    case "local4":
      return 20;
    case "21":
      return 21;
    case "local5":
      return 21;
    case "22":
      return 22;
    case "local6":
      return 22;
    case "23":
      return 23;
    case "local7":
      return 23;
    default:
      throw new RuntimeException(facility + " ist keine valide Eingabe für die facility!");
    }

  }

  private static int getSeverityCode(String severity) {
    switch (severity) {
    case "0":
      return 0;
    case "Emergency":
      return 0;
    case "1":
      return 1;
    case "Alert":
      return 1;
    case "2":
      return 2;
    case "Critical":
      return 2;
    case "3":
      return 3;
    case "Error":
      return 3;
    case "4":
      return 4;
    case "Warning":
      return 4;
    case "5":
      return 5;
    case "Notice":
      return 5;
    case "6":
      return 6;
    case "Informational":
      return 6;
    case "7":
      return 7;
    case "Debug":
      return 7;
    default:
      throw new RuntimeException(severity + " ist keine valide Eingabe für severity!");
    }
  }
}

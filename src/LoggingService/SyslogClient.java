package LoggingService;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyslogClient {

	private static int VERSION = 1;

	private static final String BOM = "BOM";

	private static final int PORT = 4711;

	private static final int BUFSIZE = 2048;

	private static final int TIMEOUT = 2000;

	private static String getPID() {
		// gibt die ProcessID des aktuellen Prozesses zurück, da dies für als PROCID im header der Syslog-Nachricht verwendet wird
		return Long.toString(ProcessHandle.current().pid());
	}

	private static String getTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").format(new Date());
	}

	private static String getPRI(String facility, String severity) {

		int faciliyCode = getFacilityCode(facility);
		int severityCode = getSeverityCode(severity);
		int priorityValue = faciliyCode * 8 + severityCode;

		return "<" + priorityValue + ">";
	}

	private static String createHeader(String pri, String timestamp, String hostname, String appname, String procid,
			String msgid) {
		return pri + VERSION + " " + timestamp + " " + hostname + " " + appname + " " + procid + " " + msgid + " ";
	}

	private static String getHostName() {
		// es wird versucht, den hostname automatisch zu ermitteln. Wenn dies fehlschlägt, wird versucht die IP automatisch zu ermitteln.
		// Falls beides fehlschlägt, wird die Nilvalue für den hostname verwendet.
		try {
			return InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			try {
				return InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e2) {
				return "-"; // NILVALUE
			}
		}
	}

	private static String getValidArgument(String argument) {
		if (argument.isEmpty()) {
			return "-"; // NILVALUE
		} else {
			return argument;
		}
	}

	private static String buildMsg(String msg) {
		// BOM steht vor der Nachricht, da die Nachricht in UTF-8 codiert wird.
		return " " + BOM + msg;
	}

	private static byte[] buildSyslogMsg(String header, String sd, String msg) {

		ByteArrayOutputStream con = new ByteArrayOutputStream();

		// header wird in ASCII codiert und die message in UTF-8
		byte[] byteheader = header.getBytes(StandardCharsets.US_ASCII);
		byte[] byteSD = sd.getBytes();
		byte[] byteMsg = msg.getBytes(StandardCharsets.UTF_8);

		try {
			con.write(byteheader);
			con.write(byteSD);
			con.write(byteMsg);
		} catch (Exception e) {
			System.err.println("Error while building the SyslogMsg: " + e.getMessage());
		}

		return con.toByteArray();
	}

	public static void main(String[] args) {

		if (args.length != 5) {
			throw new RuntimeException(
					"Es wurden nicht alle 5 benötigten Kommandozeilenparameter eingegeben. Bitte prüfen Sie Ihre Kommandozeilenparameter und ggf. die Dokumentation dieser Anwendung.");
		}

		try (DatagramSocket socket = new DatagramSocket()) {
			socket.setSoTimeout(TIMEOUT);

			System.out.println("Sende autodiscovery message.");
			InetAddress discoveryIaddr = InetAddress.getByName("255.255.255.255");
			DatagramPacket discoveryPacketOut = new DatagramPacket(new byte[0], 0, discoveryIaddr, PORT);
			socket.send(discoveryPacketOut);

			DatagramPacket discoveryPacketIn = new DatagramPacket(new byte[0], 0);
			socket.receive(discoveryPacketIn);
			InetAddress loggingServerIaddr = discoveryPacketIn.getAddress();

			System.out.println("IP vom Logging-Server gefunden: " + loggingServerIaddr.getHostAddress()
					+ " . Sende Syslog-Nachricht...");

			String PRI = getPRI(args[0], args[1]);
			String TIMESTAMP = getTimeStamp();
			String HOSTNAME = getHostName();
			String APPNAME = getValidArgument(args[2]);
			String PROCID = getPID();
			String MSGID = getValidArgument(args[3]);

			// HEADER
			String header = createHeader(PRI, TIMESTAMP, HOSTNAME, APPNAME, PROCID, MSGID);

			// STRUCTURED DATA
			String sd = "-";

			// MSG
			String msg = buildMsg(args[4]);

			// SYSLOG FORMAT MESSAGE
			byte[] syslogMsg = buildSyslogMsg(header, sd, msg);

			if (syslogMsg.length > BUFSIZE) {
				throw new RuntimeException("Die Syslog Nachricht ist mit " + syslogMsg.length
						+ " Bytes zu lang! Maximal " + BUFSIZE + " Bytes zulässig.");
			} else {
				DatagramPacket packetOut = new DatagramPacket(syslogMsg, syslogMsg.length, loggingServerIaddr, PORT);
				socket.send(packetOut);
			}

		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	// Validiert und wandelt die User-Eingabe im Kommandozeilenparameter für die facility bei der PRI in den passenden numerischen Code um
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

	// Validiert und wandelt die User-Eingabe im Kommandozeilenparameter für die severity bei der PRI in den passenden numerischen Code um
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

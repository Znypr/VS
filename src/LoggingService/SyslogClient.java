package LoggingService;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyslogClient {

	private static int VERSION = 1;
	
	private static final String HOST = getFQDN();
	
	private static final String BOM = "BOM";
	
	private static final String APPNAME = SyslogClient.class.getName();

	private static final int PORT = 4712;

	private static final int BUFSIZE = 2048;

	private static final int TIMEOUT = 2000;
	
	
	
	private static String getFQDN() {
		
		String fqdn = "";
		
		try {
			fqdn = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {
			System.err.println("Timeout: " + e.getMessage());
		}
		return fqdn;
	}
	
	private static String getPID() {
		//return ManagementFactory.getRuntimeMXBean().getName();
		return Long.toString(ProcessHandle.current().pid());
	}
	
	private static String getTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
	}

	private static String getPRI(String facility, String severity) {

		Integer fV = Integer.valueOf(facility);
		Integer sV = Integer.valueOf(severity);
		int pri = fV*8+sV;
		
		return "<" + pri + ">";
	}

	private static String createHeader(String facility, String severity, String msgID) {
		
		String pri = getPRI(facility, severity);
		String timestamp = getTimeStamp();
		String pid = getPID();

		return pri + VERSION + " " + timestamp + " " + HOST + " " + APPNAME + " " + pid + " " + msgID + " ";
	}
	
	private static String buildMsg(String msg) {
		return " " + BOM + msg;
	}
	
	private static byte[] buildSyslogMsg(String header, String sd, String msg) {
		
		ByteArrayOutputStream con = new ByteArrayOutputStream();
		
		byte[] byteheader = header.getBytes(StandardCharsets.US_ASCII);
		byte[] byteSD = sd.getBytes();
		byte[] byteMsg = msg.getBytes(StandardCharsets.UTF_8);
		
		try {
			con.write(byteheader);
			con.write(byteSD);
			con.write(byteMsg);
		} catch (Exception e) {
			System.err.println("Timeout: " + e.getMessage());
		}
		
		return con.toByteArray();
	}
	
	private static DatagramPacket createIpRequest(int Port) {
		
		InetAddress broadcast = null;
		try {
			broadcast = InetAddress.getByName("255.255.255.255");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(e);
		}
		return new DatagramPacket(new byte[0], 0, broadcast, PORT);
	}
	
	
	
	public static void main(String[] args) {
		
		try (DatagramSocket socket = new DatagramSocket()) {
						
			// HEADER
			String header = createHeader(args[0], args[1], args[2]);
			
			
			// STRUCTURED DATA
			String sd = "-";
			
			
			// MSG
			String msg = buildMsg(args[3]);
			
			
			// SYSLOG FORMAT MESSAGE
			byte[] syslogMsg = buildSyslogMsg(header, sd, msg);
			
			
			// AUTO DISCOVERY
			socket.send(createIpRequest(PORT));
			System.out.println("start autodiscovery..");
			
			DatagramPacket ipAnswer = new DatagramPacket(new byte[0], 0);
		    socket.receive(ipAnswer);
		    System.out.println("received: " + ipAnswer.getAddress());
		    InetAddress serverAddr = ipAnswer.getAddress();
			
		    
			// TRANSPORT
			DatagramPacket packetOut = new DatagramPacket(syslogMsg, syslogMsg.length, serverAddr, PORT);
			if (syslogMsg.length <= BUFSIZE) {				
				socket.send(packetOut);
			}
			
		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}



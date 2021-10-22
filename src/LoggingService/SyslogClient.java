package LoggingService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SyslogClient {

	private static int version = 1;
	
	private static final String HOST = "localhost";
	
	private static String appName = SyslogClient.class.getName();

	private static final int PORT = 4712;

	private static final int BUFSIZE = 2048;

	private static final int TIMEOUT = 2000;
	
	private static String BOM = "BOM";
	
	
	private static String getFQDN() {
		
		String domainname = "";
		
		try {
			domainname = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (Exception e) {
			System.err.println("Timeout: " + e.getMessage());
		}
		return domainname;
	}
	
    private static InetAddress getBroadcast() {
        
        NetworkInterface nif =null;
        try {
            nif = NetworkInterface.getByIndex(1);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        List<java.net.InterfaceAddress> list = nif.getInterfaceAddresses();          
        try {
            return InetAddress.getByName(list.get(0).getBroadcast().toString().replaceFirst("/", ""));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        
        return null;
    }
	
	private static String getPID() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	private static String getTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
	}

	private static String getPRI(String facility, String severity) {

		int fV = Integer.valueOf(facility);
		int sV = Integer.valueOf(severity);
		
		return "<" + fV*8+sV + ">";
	}

	private static String createHeader(String facility, String severity, String msgID) {
		
		String pri = getPRI(facility, severity);

		String timestamp = getTimeStamp();
		String pid = getPID();

		
		return pri + version + " " + timestamp + " " + getFQDN() + " " + appName + " " + pid + " " + msgID + " ";
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
	
	public static void main(String[] args) {
		
		try (DatagramSocket socket = new DatagramSocket()) {
						
			// HEADER
			String header = createHeader(args[0], args[1], args[2]);
			
			// STRUCTURED DATA
			String sd = "-";
			
			// MSG
			String msg = buildMsg(args[3]);
			
			// Syslog Formatted Message
			byte[] syslogMsg = buildSyslogMsg(header, sd, msg);
			
			
			// AUTO DISCOVERY
			InetAddress iaddr = getBroadcast();
			
			DatagramPacket packetOut = new DatagramPacket(syslogMsg, syslogMsg.length, iaddr, PORT);
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



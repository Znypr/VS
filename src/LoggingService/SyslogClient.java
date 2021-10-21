package LoggingService;

import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyslogClient {

	private static int version = 1;
	
	private static final String HOST = "localhost";
	
	private static String appName = SyslogClient.class.getName();

	private static final int PORT = 4712;

	private static final int BUFSIZE = 512;

	private static final int TIMEOUT = 2000;
	
	private static String getPID() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}
	
	private static String getTimeStamp() {
		return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(new Date());
	}

	private static String getPRI(int facility, int severity) {
		return "<" + facility*8+severity + ">";
	}


	public static void main(String[] args) {
		
		ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode("");


		try (DatagramSocket socket = new DatagramSocket()) {
			String pri = getPRI(0,0);
			String hostname = InetAddress.getLocalHost().getHostName();
			String timestamp = getTimeStamp();
			String pid = getPID();
			String msgID = ""; 
			
			socket.setSoTimeout(TIMEOUT); 
			InetAddress iaddr = InetAddress.getByName(HOST);
			DatagramPacket packetOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
			socket.send(packetOut);
			
		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}
	}

}



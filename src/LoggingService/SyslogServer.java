package LoggingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyslogServer {


	private static final int PORT = 4712; 

	private static final int BUFSIZE = 2048;


	public static void main(String[] args) {
		
		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			
			System.out.println("start server..");
			
			DatagramPacket ipAnswer = new DatagramPacket(new byte[0], 0);			
			DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
			
			while (true) {

				socket.receive(packetIn);
				
				// autodiscover handler
				if (packetIn.getLength() == 0) {
					ipAnswer.setSocketAddress(packetIn.getSocketAddress());
					socket.send(ipAnswer);
				} 
				// syslog message handler
				else {
					System.out.println("message from " + packetIn.getAddress().getHostAddress() + ":" + packetIn.getPort());
					System.out.println("  " + new String(packetIn.getData()));
				}
			}
		} catch (final IOException e) {
			System.err.println(e);
		}
	}

}



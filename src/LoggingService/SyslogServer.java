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

	private static final int BUFSIZE = 2048; //SHOULD msg length


	public static void main(String[] args) {
		
		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			
			DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);

			while (true) {
				socket.receive(packetIn);
				System.out.println(packetIn.getAddress().getHostAddress() + ":" + packetIn.getPort());
				System.out.println(new String(packetIn.getData()));
			}
		} catch (final IOException e) {
			System.err.println(e);
		}
	}

}



package LoggingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;

public class SyslogServer {


	private static final int PORT = 4712; 

	private static final int BUFSIZE = 512;


	public static void main(String[] args) {
		
		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			
			DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
			// kein packetOut notwendig, da keine Antwort erwartet wird

			System.out.println("Server gestartet ...");

			while (true) {
				socket.receive(packetIn);
				System.out.println(
						"Received from: " + packetIn.getAddress().getHostAddress() + ":" + packetIn.getPort());
				String jetzt = (new Date()).toString();

			}
		} catch (final IOException e) {
			System.err.println(e);
		}
	}

}



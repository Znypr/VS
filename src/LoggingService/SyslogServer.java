package LoggingService;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;

public class SyslogServer {
	private static final int PORT = 4711;
	private static final int BUFSIZE = 2048;

	public static void main(final String[] args) {
		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
			DatagramPacket discoveryPacketOut = new DatagramPacket(new byte[0], 0);

			System.out.println("Server gestartet ...");

			while (true) {

				socket.receive(packetIn);

				if (packetIn.getLength() == 0) { // discovery packet
					discoveryPacketOut.setSocketAddress(packetIn.getSocketAddress());
					socket.send(discoveryPacketOut);
				} else { // logging packet
					String log = new String(packetIn.getData(), 0, packetIn.getLength());

					System.out.println("Neue Syslog-Nachricht von " + packetIn.getAddress().getHostAddress()
							+ " (Hostname: " + packetIn.getAddress().getHostName() + ") erhalten: ");
					System.out.println(log);
				}

			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}
}

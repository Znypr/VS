package SpeedTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SpeedtestServer {

	private static final int PORT = 4711;

	private static final int BUFSIZE = 65000;

	public static void main(String[] args) {

		try (DatagramSocket socket = new DatagramSocket(PORT)) {

			System.out.println("Server gestartet ...");

			DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
			DatagramPacket packetOut;

			while (true) {
				socket.receive(packetIn);

				// Latenz
				if (packetIn.getLength() == 0) {
					packetOut = new DatagramPacket(new byte[0], 0);
				} 
				
				// Upload Test
				else if (packetIn.getLength() == BUFSIZE) {
					packetOut = new DatagramPacket(new byte[0], 0);
				} 
				// Download Test
				else {
					packetOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
				}
				
				packetOut.setSocketAddress(packetIn.getSocketAddress());
				socket.send(packetOut);
			}

		} catch (final IOException e) {
			System.err.println(e);
		}

	}

}

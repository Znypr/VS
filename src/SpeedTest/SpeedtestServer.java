package SpeedTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SpeedtestServer {

	private static final int PORT = 4711;

	private static final int BUFSIZE = 65000;

	public static void main(String[] args) {

		try (DatagramSocket socket = new DatagramSocket(PORT)) {

			System.out.println("starting server..\n");

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

//			while (true) {
//				
//			// LATENCY 
//			
//				DatagramPacket latencyIn = new DatagramPacket(new byte[0], 0);
//				socket.receive(latencyIn);
//				
//				DatagramPacket latencyOut = new DatagramPacket(new byte[0], 0);
//				latencyOut.setSocketAddress(latencyIn.getSocketAddress());
//				socket.send(latencyOut);
//			
//			
//				
//			// UPLOAD 
//			
//				DatagramPacket uploadIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
//				socket.receive(uploadIn);
//							
//				DatagramPacket uploadOut = new DatagramPacket(new byte[0], 0);
//				uploadOut.setSocketAddress(uploadIn.getSocketAddress());
//				socket.send(uploadOut);
//			
//			
//			
//			// DOWNLOAD 
//			
//				DatagramPacket downloadIn = new DatagramPacket(new byte[0], 0);
//				socket.receive(downloadIn);
//							
//				DatagramPacket downloadOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
//				downloadOut.setSocketAddress(downloadIn.getSocketAddress());
//				socket.send(downloadOut);
//				
//
//
//			}
		} catch (final IOException e) {
			System.err.println(e);
		}

	}

}

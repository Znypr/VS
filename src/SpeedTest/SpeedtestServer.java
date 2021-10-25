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
			DatagramPacket packetOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);

		      while (true) {
		          
		          socket.receive(packetIn);
		          
		          // Download-Messung
		          if (packetIn.getLength() == 1) {
		            packetOut.setData(new byte[BUFSIZE]);
		            packetOut.setLength(BUFSIZE);
		          } 
		          // Upload-Messung
		          else if (packetIn.getLength() == BUFSIZE) {
		            packetOut.setData(new byte[0]);
		            packetOut.setLength(0);
		          } 
		          // Latenz-Messung
		          else {      
		        	  packetOut.setData(new byte[0]);
			          packetOut.setLength(0);
		          }
		          
		          packetOut.setSocketAddress(packetIn.getSocketAddress());
		          
		          socket.send(packetOut);
		          
		        }

		} catch (final IOException e) {
			System.err.println(e);
		}

	}

}

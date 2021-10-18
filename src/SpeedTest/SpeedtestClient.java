package SpeedTest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

public class SpeedtestClient {
	
	
	  private static final String HOST = "localhost";

	  private static final int PORT = 4712;

	  private static final int BUFSIZE = 512;

	  private static final int TIMEOUT = 2000;

	  
	public static void main(String[] args) {
		
	    try (DatagramSocket socket = new DatagramSocket()) {
	        socket.setSoTimeout(TIMEOUT); 
	        InetAddress iaddr = InetAddress.getByName(HOST);
	        DatagramPacket packetOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
	        socket.send(packetOut);
	        DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
	        socket.receive(packetIn);
	        String received = new String(packetIn.getData(), 0, packetIn.getLength());
	        System.out.println("Received: " + received);
	      } catch (SocketTimeoutException e) {
	        System.err.println("Timeout: " + e.getMessage());
	      } catch (Exception e) {
	        System.err.println(e);
	      }

	}

}

package SpeedTest;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class SpeedtestClient {
	
	
	  private static final String HOST = "localhost";

	  private static final int PORT = 4712;

	  private static final String BUFSIZE = "256";
	  
	  private static int byteLength = 3;

	  private static final int TIMEOUT = 200000;
	  
	  
	  private static int getBufSize() {
		  return Integer.valueOf(BUFSIZE);
	  }

	  
	public static void main(String[] args) {
		
	    try (DatagramSocket socket = new DatagramSocket()) {
	    	
	        socket.setSoTimeout(TIMEOUT); 
	        
	        // send test request #1
	        InetAddress iaddr = InetAddress.getByName(HOST);	        	        
	        byte[] bytes = BUFSIZE.getBytes();	       
	        DatagramPacket testRequest = new DatagramPacket(bytes, byteLength, iaddr, PORT);
	        socket.send(testRequest);
	        
	        // receive test acknowledgement #4
	        DatagramPacket packetIn = new DatagramPacket(new byte[byteLength], byteLength);
	        socket.receive(packetIn);
	        System.out.println("received");

	        // send data #5
	        DatagramPacket packetOut = new DatagramPacket(new byte[getBufSize()], getBufSize(), iaddr, PORT);
	        socket.send(packetOut);

	       
	      } catch (SocketTimeoutException e) {
	        System.err.println("Timeout: " + e.getMessage());
	      } catch (Exception e) {
	        System.err.println(e);
	      }
 
	}
}

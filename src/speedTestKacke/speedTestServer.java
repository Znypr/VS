package speedTestKacke;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class speedTestServer {
  private static final int PORT = 4711;
  private static final int BUFSIZE = 64000;
  
  public static void main(final String[] args) throws InterruptedException {
    try (DatagramSocket socket = new DatagramSocket(PORT)) {
      DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
      DatagramPacket packetOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);

      System.out.println("Server gestartet ...");

      while (true) {
        
        socket.receive(packetIn);
        System.out.println("Nachricht erhalten");
        
        if (packetIn.getLength() == 0) {
          packetOut.setData(new byte[64000]);
          packetOut.setLength(64000);
        } else if (packetIn.getLength() == BUFSIZE) {
          packetOut.setData(new byte[0]);
          packetOut.setLength(0);
        } else {      
          packetOut.setData(packetIn.getData());
          packetOut.setLength(packetIn.getLength());
        }
        
        packetOut.setSocketAddress(packetIn.getSocketAddress());
        
        socket.send(packetOut);
        
      }
    } catch (IOException e) {
      System.err.println(e);
    }
  }

}

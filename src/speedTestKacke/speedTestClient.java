package speedTestKacke;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Date;

public class speedTestClient {

  private static final String HOST = "localhost";
  private static final int PORT = 4711;
  private static final int BUFSIZE = 64000;
  private static final int TIMEOUT = 2000;

  private static final int RUNCOUNT = 5000;

  public static void main(String[] args) {
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(TIMEOUT);
      InetAddress iaddr = InetAddress.getByName(HOST);

      long[] latencies = new long[RUNCOUNT];

      for (int i = 0; i < RUNCOUNT; i++) {
        DatagramPacket packetOut = new DatagramPacket(new byte[1], 1, iaddr, PORT);
        DatagramPacket packetIn = new DatagramPacket(new byte[1], 1);

        long sendtime = new Date().getTime();
        socket.send(packetOut);

        socket.receive(packetIn);
        long receiveTime = new Date().getTime();

        latencies[i] = receiveTime - sendtime;

      }

      long averageLatency = calculateAverageLatency(latencies);
      System.out.println("Average Latency mit leeren Datagram Paketen: " + averageLatency + "ms");

      // Upload

      latencies = new long[RUNCOUNT];

      for (int i = 0; i < RUNCOUNT; i++) {
        DatagramPacket packetOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
        DatagramPacket packetIn = new DatagramPacket(new byte[0], 0);

        long sendtime = System.nanoTime();
        socket.send(packetOut);

        socket.receive(packetIn);
        long receiveTime = System.nanoTime();

        latencies[i] = receiveTime - sendtime;

      }

      averageLatency = calculateAverageLatency(latencies);

      if (averageLatency == 0) {
        averageLatency = 1; // to stop divisions by zero
      }

      float averageUploadMBPerSecond = ((float) 1e9 / averageLatency * BUFSIZE) / 1024 / 1024;

      System.out
          .println("Average Upload Latency mit " + BUFSIZE + " Byte Daten: " + averageLatency + "ms");
      System.out.println("Average Upload speed: " + averageUploadMBPerSecond + "MB/s");

      
      // download
      
      latencies = new long[RUNCOUNT];

      for (int i = 0; i < RUNCOUNT; i++) {
        DatagramPacket packetOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
        DatagramPacket packetIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);

        long sendtime = new Date().getTime();
        socket.send(packetOut);

        socket.receive(packetIn);
        long receiveTime = new Date().getTime();

        latencies[i] = receiveTime - sendtime;

      }

      averageLatency = calculateAverageLatency(latencies);

      if (averageLatency == 0) {
        averageLatency = 1; // to stop divisions by zero
      }

      float averageDownloadMBPerSecond = ((float) 1000 / averageLatency * BUFSIZE) / 1024 / 1024;

      System.out
          .println("Average Download Latency mit " + BUFSIZE + " Byte Daten: " + averageLatency + "ms");
      System.out.println("Average Download speed: " + averageDownloadMBPerSecond + "MB/s");

    } catch (SocketTimeoutException e) {
      System.err.println("Timeout: " + e.getMessage());
    } catch (Exception e) {
      System.err.println(e);
    }
  }

  private static long calculateAverageLatency(long[] latencies) {
    int latencySum = 0;

    for (long latency : latencies) {
      latencySum += latency;
    }

    return latencySum / RUNCOUNT;
  }

}

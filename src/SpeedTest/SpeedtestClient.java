package SpeedTest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;

public class SpeedtestClient {


	private static final String HOST = "79.199.69.122";

	private static final int PORT = 4711;

	private static final int BUFSIZE = 65000;

	private static final int TIMEOUT = 2000;

	private static final int RUNCOUNT = 5;

	private static final DecimalFormat f = new DecimalFormat("##.00");


	private static float getAverage(long[] array) {
		long avg = 0;

		for (int i = 0; i < array.length; i++) {
			avg += array[i];
		}

		return avg/array.length;
	}

	public static void main(String[] args) {

		long startTime = 0;
		long stopTime = 0;

		try (DatagramSocket socket = new DatagramSocket()) {

			socket.setSoTimeout(TIMEOUT); 
			InetAddress iaddr = InetAddress.getByName(HOST);	     

			// LATENCY 
			long[] latency = new long[RUNCOUNT];

			for(int i = 0; i < RUNCOUNT; i++) {

				DatagramPacket latencyOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
				startTime = System.nanoTime();
				socket.send(latencyOut);

				DatagramPacket latencyIn = new DatagramPacket(new byte[0], 0);
				socket.receive(latencyIn);
				stopTime = System.nanoTime();

				latency[i] = stopTime-startTime;
			}
			float latency_ms = (float) (getAverage(latency)/1e6);
			System.out.println("Latency:        " + f.format(latency_ms) + "ms");


			// UPLOAD 

			long[] uploadTime = new long[RUNCOUNT];

			for (int i = 0; i < RUNCOUNT; i++) {

				DatagramPacket uploadOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
				startTime = System.nanoTime();
				socket.send(uploadOut);

				DatagramPacket uploadIn = new DatagramPacket(new byte[0], 0);
				socket.receive(uploadIn);
				stopTime = System.nanoTime();

				uploadTime[i] = stopTime-startTime;
			}
			float uploadSpeed = ((float) 1e9 / getAverage(uploadTime) * BUFSIZE) / 1000 / 1000;
			System.out.println("Upload Speed:   " + f.format(uploadSpeed) + "MBps");


			// DOWNLOAD 

			long[] downloadTime = new long[RUNCOUNT];

			for (int i = 0; i < RUNCOUNT; i++) {

				DatagramPacket downloadOut = new DatagramPacket(new byte[1], 1, iaddr, PORT);
				startTime = System.nanoTime();
				socket.send(downloadOut);

				DatagramPacket downloadIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
				socket.receive(downloadIn);
				stopTime = System.nanoTime();

				downloadTime[i] = stopTime-startTime;
			}
			float downloadSpeed = ((float) 1e9 / getAverage(downloadTime) * BUFSIZE) / 1000 / 1000;
			System.out.println("Download Speed: " + f.format(downloadSpeed) + "MBps");

		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}

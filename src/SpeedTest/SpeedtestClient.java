package SpeedTest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.util.Date;

public class SpeedtestClient {

	private static final String HOST = "localhost";

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

			// Latenz-Messung
			long[] latencies = new long[RUNCOUNT];

			// An den Server wird ein Datagram-Pakete mit einer Länge von 0 Byte an der Server geschickt. Der Server antwortet auch mit einer Länge von 0 Bytes.
			// Es wird gemessen, wie viele Nanosekunden dies dauert
			// Je nach "RUNCOUNT" wird dies mehrmals wiederholt um die Latenz im Durchschnitt zu berechnen.
			for(int i = 0; i < RUNCOUNT; i++) {
				DatagramPacket latencyOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
				DatagramPacket latencyIn = new DatagramPacket(new byte[0], 0);

				startTime = System.nanoTime();
				socket.send(latencyOut);

				socket.receive(latencyIn);
				stopTime = System.nanoTime();

				latencies[i] = stopTime-startTime;
			}
			// Aus dem Durchschnitt wird dann die durchschnittliche Latenz in Millisekunden berechnet
			float latency_ms = (float) (getAverage(latencies) / 1e6);
			System.out.println("Latency:        " + f.format(latency_ms) + "ms");



			// Upload-Messung
			long[] uploadTimes = new long[RUNCOUNT];

			// Um den Upload zu testen wird an den Server ein Datagram-Pakete mit einer Länge von BUFSIZE (in diesem Fall 65.000 Bytes) gesendet.
			// Der Server antwortet mit einem Datagram-Pakete mit 0 Bytes, da ja nur der Upload getestet werden soll 
			// und der Download deshalb entsprechend schnell sein sollte um das Ergebnis nicht zu stark zu verfälschen.
			// Dabei wird in Nanosekunden gemessen, wie lange dieser Vorgang dauert.
			// Dieser Vorgang wird je nach "RUNCOUNT" mehrmals wiederholt.
			for (int i = 0; i < RUNCOUNT; i++) {
				
				DatagramPacket uploadOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
				DatagramPacket uploadIn = new DatagramPacket(new byte[0], 0);

				startTime = System.nanoTime();
				socket.send(uploadOut);

				socket.receive(uploadIn);
				stopTime = System.nanoTime();

				uploadTimes[i] = stopTime-startTime;
			}
			// Aus der gemessenen durchschnittlichen Dauer wird dann die Upload-Geschwindigkeit in Megabyte pro Sekunden berechnet.
			float uploadSpeed = ((float) 1e9 / getAverage(uploadTimes) * BUFSIZE) / 1000 / 1000;
			System.out.println("Upload Speed:   " + f.format(uploadSpeed) + "MBps");


			
			// Download-Messung
			long[] downloadTimes = new long[RUNCOUNT];
			
			// Um den Download zu testen wird an den Server ein Datagram-Pakete mit einer Länge von einem Byte gesendet, da ja nur der Download getestet werden soll 
			// und der Upload deshalb entsprechend schnell sein sollte um das Ergebnis nicht zu stark zu verfälschen
			// Der Server antwortet mit einem Datagram-Pakete mit BUFSIZE (in diesem Fall 65.000 Bytes) Bytes.
			// Dabei wird in Nanosekunden gemessen, wie lange dieser Vorgang dauert.
			// Dieser Vorgang wird je nach "RUNCOUNT" mehrmals wiederholt.
			for (int i = 0; i < RUNCOUNT; i++) {
				DatagramPacket downloadOut = new DatagramPacket(new byte[1], 1, iaddr, PORT);
				DatagramPacket downloadIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);

				startTime = System.nanoTime();
				socket.send(downloadOut);

				socket.receive(downloadIn);
				stopTime = System.nanoTime();

				downloadTimes[i] = stopTime-startTime;
			}
			// Aus der gemessenen durchschnittlichen Dauer wird dann die Download-Geschwindigkeit in Megabyte pro Sekunden berechnet.
			float downloadSpeed = ((float) 1e9 / getAverage(downloadTimes) * BUFSIZE) / 1000 / 1000;
			System.out.println("Download Speed: " + f.format(downloadSpeed) + "MBps");

		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}

package SpeedTest;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;

public class SpeedtestClient {


	private static final String HOST = "localhost";

	private static final int PORT = 4712;

	private static final int BUFSIZE = 65000;

	private static final int TIMEOUT = 2000;
	
	private static final int RUNS = 1;
	
	private static final DecimalFormat f = new DecimalFormat("##.00");
	
	
	private static float getAverage(long[] array) {
		 long avg = 0;
		 
		 for(int i=0; i<array.length; i++) {
			 avg += array[i];
		 }
		 
		 return avg/array.length;
	}

	private static float byte_to_megabit(int b) {
		return (float) (b*8/1e6);
	}

	public static void main(String[] args) {
		
		long startTime = 0;
		long stopTime = 0;

		try (DatagramSocket socket = new DatagramSocket()) {

			socket.setSoTimeout(TIMEOUT); 
			InetAddress iaddr = InetAddress.getByName(HOST);	     
			
			
			// LATENCY 
			{
				long[] latency = new long[RUNS];
				
				for(int i=0; i<RUNS; i++) {
					
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
			}
		
			// UPLOAD 
			{		
				long[] uploadTime = new long[RUNS];
						
				for(int i=0; i<RUNS; i++) {
						
					DatagramPacket uploadOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
					startTime = System.nanoTime();
					socket.send(uploadOut);
					
					DatagramPacket uploadIn = new DatagramPacket(new byte[0], 0);
					socket.receive(uploadIn);
					stopTime = System.nanoTime();
							
					uploadTime[i] = stopTime-startTime;
				}
				float uploadSpeed = ((float) 1e9 / getAverage(uploadTime) * BUFSIZE) / 1024 / 1024;
				System.out.println("Upload Speed:   " + f.format(uploadSpeed) + "Mbps");
			}
			
			// DOWNLOAD 
			{
				long[] downloadTime = new long[RUNS];
						
				for(int i=0; i<RUNS; i++) {
						
					DatagramPacket downloadOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
					startTime = System.nanoTime();
					socket.send(downloadOut);
					
					DatagramPacket downloadIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
					socket.receive(downloadIn);
					stopTime = System.nanoTime();
					
					downloadTime[i] = stopTime-startTime;
				}
				float downloadSpeed = ((float) 1e9 / getAverage(downloadTime) * BUFSIZE) / 1024 / 1024;
				System.out.println("Download Speed: " + f.format(downloadSpeed) + "Mbps");
				
			}


		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}

	}
}

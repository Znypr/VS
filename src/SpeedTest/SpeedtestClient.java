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
	
	private static final int RUNS = 5000;
	
	private static final DecimalFormat f = new DecimalFormat("##.00");
	
	
	private static float getMedian(float[] array) {
		 float median = 0;
		 
		 for(int i=0; i<array.length; i++) {
			 median += array[i];
		 }
		 
		 return median/array.length;
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
				float[] latency = new float[RUNS];
				
				for(int i=0; i<RUNS; i++) {
					
					DatagramPacket latencyOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
					startTime = System.nanoTime();
					socket.send(latencyOut);
					
					DatagramPacket latencyIn = new DatagramPacket(new byte[0], 0);
					socket.receive(latencyIn);
					stopTime = System.nanoTime();
					
					latency[i] = (float) ((stopTime-startTime)/1e3);
				}
				System.out.println("Latency:        " + f.format(getMedian(latency)) + "ms");
			}
		
			// UPLOAD 
			{		
				float[] upload = new float[RUNS];
						
				for(int i=0; i<RUNS; i++) {
						
					DatagramPacket uploadOut = new DatagramPacket(new byte[BUFSIZE], BUFSIZE, iaddr, PORT);
					startTime = System.nanoTime();
					socket.send(uploadOut);
					
					DatagramPacket uploadIn = new DatagramPacket(new byte[0], 0);
					socket.receive(uploadIn);
					stopTime = System.nanoTime();
					
					float uploadTime_in_s = (float) (((float) (stopTime-startTime))/ 1e6);
					upload[i] = byte_to_megabit(BUFSIZE) / uploadTime_in_s;
				}
				System.out.println("Upload Speed:   " + f.format(getMedian(upload)) + "Mbps");
			}
			
			// DOWNLOAD 
			{
				float[] download = new float[RUNS];
						
				for(int i=0; i<RUNS; i++) {
						
					DatagramPacket downloadOut = new DatagramPacket(new byte[0], 0, iaddr, PORT);
					startTime = System.nanoTime();
					socket.send(downloadOut);
					
					DatagramPacket downloadIn = new DatagramPacket(new byte[BUFSIZE], BUFSIZE);
					socket.receive(downloadIn);
					stopTime = System.nanoTime();
					
					float downloadTime_in_s = (float) (((float) (stopTime-startTime))/ 1e6);
					download[i] = byte_to_megabit(BUFSIZE) / downloadTime_in_s;
				}
				System.out.println("Download Speed: " + f.format(getMedian(download)) + "Mbps");
				
			}


		} catch (SocketTimeoutException e) {
			System.err.println("Timeout: " + e.getMessage());
		} catch (Exception e) {
			System.err.println(e);
		}

	}
}

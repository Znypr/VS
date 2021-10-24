package SpeedTestFirstKonzept;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.text.DecimalFormat;


public class SpeedtestServer {


	private static final int PORT = 4712;

	private static final int BUFSIZE = 256;
	
	private static final int MAXBUF = 3;
	
	private static int BUF = MAXBUF;


	public static void main(String[] args) {

		try (DatagramSocket socket = new DatagramSocket(PORT)) {
			
			System.out.println("starting server..\n");		
			long startingTime = 0;
			
			
			while (true) {
				
				DatagramPacket packetIn = new DatagramPacket(new byte[BUF], BUF);
				
				socket.receive(packetIn);
				System.out.println("message received: l-" + packetIn.getLength());	
				long timestamp = System.nanoTime();
				int dataSize = 0;
				
				if (BUF == MAXBUF) {
					
					byte[] bytes = packetIn.getData();
					String data = new String(packetIn.getData());
					dataSize = Integer.valueOf(data);
				}
				
				// receive test request #2
				if (packetIn.getData().length  <= MAXBUF) {
					
					// send test acknowledgement #3
					if (dataSize <= BUFSIZE) {
						DatagramPacket packetOut = new DatagramPacket(new byte[BUF], BUF);
						packetOut.setSocketAddress(packetIn.getSocketAddress());
						packetOut.setData(packetIn.getData());
						socket.send(packetOut);
						System.out.println("achknowledgement sent");	
						BUF = dataSize;
						startingTime = System.nanoTime();
					} else {
						System.out.println("Request denied");
					}
				} 
				// receive data #6
				else {
					double delta = (timestamp - startingTime) / 1e6;
					System.out.println("Results: ");
					System.out.println("  " + packetIn.getLength() + " bytes received in " + delta + "ms.");
					DecimalFormat f = new DecimalFormat("##.00");
					String speed = f.format(BUF/delta*8);
					System.out.println("  Speed: " + speed + " MB/s");
					BUF = MAXBUF;
				}
				
				

			}
		} catch (final IOException e) {
			System.err.println(e);
		}

	}
}
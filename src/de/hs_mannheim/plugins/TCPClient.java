package de.hs_mannheim.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {

	private static final String HOST = "localhost";
	private static final int PORT = 8888;
	public static void main(String[] args) {

		try (Socket socket = new Socket(HOST, PORT);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in))) {

			// Begrüßung vom Server empfangen und auf Konsole ausgeben
			String msg = in.readLine();
			System.out.println(msg);
			
			// Nebenläufiger Thread, derr UserInput aus der Console einliest und an den TCP-Server schickt.
			// Wird nebenläufig realisiert, da "in.readLine()" in der unteren while-Schleife blockiert
			Thread userInputThread = new Thread(() -> {
				while(true) {
					try {
						if (stdin.ready()) {
							String line = stdin.readLine();
							
							if ("q".equals(line)) {
								System.out.println("Programm beendet.");
								System.exit(0);
							}
							out.println(line);
						}
					} catch (IOException e) {
						break;
					}
				}
			});
			// Daemon, da dieser Thread auch enden soll, sobald der MainThread endet
			userInputThread.setDaemon(true);
			userInputThread.start();
			
			// Falls vom TCP-Server eine Nachricht kommt, wird diese ausgegeben
			while (true) {			
				System.out.println(in.readLine());
			}
			
		} catch (Exception e) {
			System.err.println(e);
		} finally {
			System.out.println("Die Verbindung zum Server wurde geschlossen.");
		}
	}
}

package JMSAdapter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TCPServer implements MessageListener {

	private int port;
	private Connection connection;
	private Session session;
	private MessageConsumer consumer;
	private MessageProducer producer;

	private volatile boolean newMessage;
	private String messageText;

	public TCPServer(int port) throws NamingException, JMSException {
		Context ctx = new InitialContext();
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		this.connection = factory.createConnection();
		this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = (Queue) ctx.lookup("JMSAdapter.spectatorQueue");
		this.consumer = this.session.createConsumer(queue);
		this.consumer.setMessageListener(this);
		this.connection.start();
		this.port = port;

		Destination queueClient = (Destination) ctx.lookup("JMSAdapter.clientPushQueue");
		this.producer = this.session.createProducer(queueClient);
	}

	@Override
	public void onMessage(Message message) {
		try {
			if (message instanceof TextMessage) {
				TextMessage textMessage = (TextMessage) message;

				if (textMessage.getText().equals("")) {
					messageText = "BIN";
					newMessage = true;
				} else {
					messageText = textMessage.getText();
					newMessage = true;
				}
			} else if (!(message instanceof TextMessage)) {
				messageText = "BIN";
				newMessage = true;
			}
		} catch (JMSException e) {
			System.err.println(e);
		}

	}

	public void start() {
		try (ServerSocket serverSocket = new ServerSocket(this.port)) {
			System.out.println("EchoServer (threaded) auf " + serverSocket.getLocalSocketAddress() + " gestartet ...");
			while (true) {
				Socket socket = serverSocket.accept();
				new EchoThread(socket).start();
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private class EchoThread extends Thread {

		private final Socket socket;

		public EchoThread(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			SocketAddress socketAddress = this.socket.getRemoteSocketAddress();
			int a = this.socket.getLocalPort();
			System.out.println("Verbindung zu " + socketAddress + " aufgebaut + port vom server" + a);
			try (BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
					PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true)) {
				out.println("Server ist bereit ...");
				
				while (true) {
					
					if (newMessage) {
						out.println(messageText);
						newMessage = false;
					} 
					
					if (in.ready()){
						System.out.println("blocking lol");
						String input = in.readLine();
						System.out.println("In die Que reingeschrieben: " + input);
						sendMessage(input, "low");
					}
					
				}
			} catch (Exception e) {
				System.err.println(e);
			} finally {
				System.out.println("Verbindung zu " + socketAddress + " abgebaut");
			}
		}

	}

	public void sendMessage(String text, String priority) throws JMSException {
		TextMessage message = this.session.createTextMessage();
		message.setText(text);
		message.setStringProperty("Priority", priority);
		this.producer.send(message);
	}

	public static void main(String[] args) {
		TCPServer tcpServer = null;
		try {
			tcpServer = new TCPServer(8888);
			tcpServer.start();
		} catch (NamingException | JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if ((tcpServer != null) && (tcpServer.consumer != null)) {
					tcpServer.consumer.close();
				}
				if ((tcpServer != null) && (tcpServer.session != null)) {
					tcpServer.session.close();
				}
				if ((tcpServer != null) && (tcpServer.connection != null)) {
					tcpServer.connection.close();
				}
			} catch (JMSException e) {
				System.err.println(e);
			}
		}

	}

}

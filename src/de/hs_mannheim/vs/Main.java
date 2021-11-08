package de.hs_mannheim.vs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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

public class Main {
	
	private final int PORT = 8888;
	private final String RECEIVE_DESTINATION = Conf.TOPIC;
	private final String SEND_DESTINATION = Conf.QUEUE;
	private final Executor threadPool;
	
	private Connection connection;
	private Session session;
	private MessageConsumer consumer;
	private MessageProducer producer;
	private Destination destIn;

	
	public Main() throws NamingException, JMSException {
		Context ctx = new InitialContext();
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		this.connection = factory.createConnection();
		this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		this.destIn = (Destination) ctx.lookup(RECEIVE_DESTINATION);
		Destination destOut = (Destination) ctx.lookup(SEND_DESTINATION);
		this.producer = this.session.createProducer(destOut);
		this.connection.start();
	
		this.threadPool = Executors.newCachedThreadPool();
	}

	public void start() throws JMSException {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("EchoServer (threaded) auf " + serverSocket.getLocalSocketAddress() + " gestartet ...");
			while (true) {
				Socket socket = serverSocket.accept();
				
				ConsumerThread newThread = new ConsumerThread(socket);
				this.threadPool.execute(newThread);				
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	private class ConsumerThread implements Runnable, MessageListener {

		private final Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		
		private MessageConsumer consumer;
		
		public ConsumerThread(Socket socket) throws JMSException {
			this.consumer = session.createConsumer(destIn);
			this.consumer.setMessageListener(this);
			this.socket = socket;
			
			try {
				this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				this.out = new PrintWriter(this.socket.getOutputStream(), true);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onMessage(Message message) {
			try {
				String messageText;
				
				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;

					if (textMessage.getText().equals("")) {
						messageText = "BIN";
					} else {
						messageText = textMessage.getText();
					}
				} else {
					messageText = "BIN";
				}
				
				this.out.print(messageText);
				this.out.print("\r\n\r\n");
				this.out.flush();
			} catch (JMSException e) {
				System.err.println(e);
			}
		}

		@Override
		public void run() {
			SocketAddress socketAddress = this.socket.getRemoteSocketAddress();
			System.out.println("Verbindung zu " + socketAddress + " aufgebaut");
			try {
				this.out.println("Server ist bereit ...");
				String input;
				while ((input = in.readLine()) != null) {
					System.out.println("In die Queue reingeschrieben: " + input);
					sendMessage(input, "low");
				}
			} catch (Exception e) {
				System.err.println(e);
			} finally {
				try {
					this.in.close();
					this.out.close();
					this.socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		Main tcpServer = null;
		try {
			tcpServer = new Main();
			tcpServer.start();
		} catch (NamingException | JMSException e) {
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

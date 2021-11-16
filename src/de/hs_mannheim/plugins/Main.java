package de.hs_mannheim.plugins;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;
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
			System.out.println("Adapter (mit Plugins, threaded) auf " + serverSocket.getLocalSocketAddress() + " gestartet ...");
			while (true) {
				Socket socket = serverSocket.accept();
				
				// für jeden TCP-Client wird ein neuer Thread erstellt
				ConsumerThread newThread = new ConsumerThread(socket);
				this.threadPool.execute(newThread);				
			}
		} catch (IOException e) {
			System.err.println(e);
		}
	}

	// Erstellt die passende Plugin-Instanz anhand der gesetzten Property in der plugins.properties Datei
	public PluginInterface getPlugin(String property) {
		File file = new File("src/de/hs_mannheim/plugins/plugins.properties");
		String propertyFilePath = file.getAbsolutePath();
		
		PluginInterface plugin = null;
		
		try (InputStream input = new FileInputStream(propertyFilePath)){
			
			Properties properties = new Properties();
			properties.load(input);
			String pluginName = properties.getProperty(property);
			
			if (pluginName.equals("None")) {
				return null;
			}
			
			Class<?> pluginClass = Class.forName(pluginName);
			Constructor<?> constructor = pluginClass.getConstructor();
			
			plugin = (PluginInterface) constructor.newInstance();
		} catch (IOException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return plugin;
	}
	
	// Für jede Verbindung mit einem TCP-Client wird ein neuer Thread erstellt, der gleichzeitig auch MessageConsumer und somit MessageListener im topic ist
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
						
						// passendes Plugin laden
						PluginInterface plugin = getPlugin("JMSToClientPlugin");
						if (plugin != null) {
							messageText = plugin.transformString(messageText);					
						}
					}
				} else {
					messageText = "BIN";
				}	
				
				// Nachricht wird an den TCP-Client gesendet, gefolgt von einer Leerzeile (CR LF CR LF)
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
				// Begrüßungsnachricht an den Client, damit der Client weiß, dass der Server bereit ist
				this.out.println("Server ist bereit ...");
				
				String input;
				while ((input = in.readLine()) != null) {
					System.out.println("Neue Nachricht von " + socketAddress + " erhalten: " + input);
					sendMessage(input);
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
	
	public void sendMessage(String text) throws JMSException {
		String message = text;
		// passendes Plugin laden
		PluginInterface plugin = getPlugin("clientToJMSPlugin");
		if (plugin != null) {
			message = plugin.transformString(text);
			System.out.println("Nachricht wurde transformiert und wird in die Queue (" + SEND_DESTINATION + ") geschrieben: " + message);
		} else {
			System.out.println("Nachricht wird in die Queue (" + SEND_DESTINATION + ") geschrieben: " + message);
		}
		
		TextMessage textMessage = this.session.createTextMessage();
		textMessage.setText(message);
		this.producer.send(textMessage);
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

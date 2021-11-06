package de.hs_mannheim.vs;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
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



public class ClientThreadpool {

	private Executor threadPool;

	public ClientThreadpool() {
		threadPool = Executors.newFixedThreadPool(5);
	}

	public void start() {

		try {
			for(int i=0; i<5; i++) {
				threadPool.execute(new Client());
			}
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
		}

	}


	private class Client implements Runnable, MessageListener {

		private Connection connection;
		private Session session;
		private MessageProducer producer;
		private MessageConsumer consumer;

		public Client() throws NamingException, JMSException {

			Context ctx = new InitialContext();
			ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Queue destOut = (Queue) ctx.lookup("de.hs_mannheim.vs.queue1");
			producer = session.createProducer(destOut);

			Queue destIn = (Queue) ctx.lookup(Conf.QUEUE);
			consumer = session.createConsumer(destIn);
			consumer.setMessageListener(this);

			connection.start();
		}

		@Override
		public void run() {

			try {
				//TODO send individual lines of text per message
				String text = "this is a test";
				for (char c : text.toCharArray()) {
					
					TextMessage msg = session.createTextMessage();
					msg.setText(""+c);
					producer.send(msg);
				}

			} catch (JMSException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onMessage(Message msg) {

			try {
				if (msg instanceof TextMessage) {
					String response = ((TextMessage) msg).getText();
					System.out.println("Received from " + Conf.QUEUE + ": "+ response);
				}

			} catch (JMSException e) {
				System.err.println(e);
			}
		}

	}

	public static void main(String[] args) {

		new ClientThreadpool().start();
	}
}



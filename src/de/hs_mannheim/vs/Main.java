package de.hs_mannheim.vs;

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


public class Main implements MessageListener {

	private Connection connection;
	private Session session;
	private MessageProducer producer;
	private MessageConsumer consumer;

	public Main(String sendDest, String receiveDest) throws NamingException, JMSException {
		Context ctx = new InitialContext();
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		connection = factory.createConnection();
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		Queue destIn = (Queue) ctx.lookup(receiveDest);
		consumer = session.createConsumer(destIn);
		consumer.setMessageListener(this);

		Queue destOut = (Queue) ctx.lookup(sendDest);
		producer = session.createProducer(destOut);

		connection.start();
	}

	@Override
	public void onMessage(Message msg) {

		try {
			if (msg instanceof TextMessage) {
				if (((TextMessage) msg).getText().length() == 0) {

					TextMessage response1 = session.createTextMessage();
					response1.setText("BIN");
					producer.send(response1);

					TextMessage response2 = session.createTextMessage();
					response2.setText("\r\n");
					producer.send(response2);

				} else {

					producer.send(msg);

					TextMessage response = session.createTextMessage();
					response.setText("\r\n");
					producer.send(response);
				}			
			} 

			//nicht eindeutig formuliert

			//			else {
			//				if (((TextMessage) msg).getText().length() == 0) {
			//			
			//					TextMessage response1 = session.createTextMessage();
			//					response1.setText("BIN");
			//					producer.send(response1);
			//					
			//					TextMessage response2 = session.createTextMessage();
			//					response2.setText("\r\n");
			//					producer.send(response2);
			//									
			//				}
			//			}
		} catch (JMSException e) {
			System.err.println(e);
		}

	}

	public static void main(String[] args) {

		Conf.QUEUE = args[0];
		String receiveDest = "de.hs_mannheim.vs.queue1";

		Main adapter = null;
		try {
			adapter = new Main(Conf.QUEUE, receiveDest);
			while(true) {
				// run adapter
			}

		} catch (NamingException | JMSException e) {
			System.err.println(e);
		} finally {
			try {
				if ((adapter != null) && (adapter.producer != null)) {
					adapter.producer.close();
				}
				if ((adapter != null) && (adapter.consumer != null)) {
					adapter.consumer.close();
				}
				if ((adapter != null) && (adapter.session != null)) {
					adapter.session.close();
				}
				if ((adapter != null) && (adapter.connection != null)) {
					adapter.connection.close();
				}
			} catch (JMSException e) {
				System.err.println(e);
			}
		}

	}

}

package de.hs_mannheim.vs;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ProducerNode {
	private final String SEND_DESTINATION = Conf.TOPIC;
	
	private Connection connection;
	private Session session;
	private MessageProducer producer;
	
	public ProducerNode() throws NamingException, JMSException {
		Context ctx = new InitialContext();
		ConnectionFactory factory = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		this.connection = factory.createConnection();
		this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		Destination topic = (Destination) ctx.lookup(SEND_DESTINATION);
		this.producer = this.session.createProducer(topic);
	}
	
	public void sendMessage(String text) throws JMSException {
		TextMessage message = this.session.createTextMessage();
		message.setText(text);
		this.producer.send(message);
	}

	public static void main(String[] args) {
		ProducerNode node = null;
		try {
			node = new ProducerNode();
			// Die Nachricht muss als Kommandozeilenparameter übergeben werden
			node.sendMessage(args[0]);
		} catch (NamingException | JMSException e) {
			System.err.println(e);
		} finally {
			try {
				if ((node != null) && (node.producer != null)) {
					node.producer.close();
				}
				if ((node != null) && (node.session != null)) {
					node.session.close();
				}
				if ((node != null) && (node.connection != null)) {
					node.connection.close();
				}
			} catch (JMSException e) {
				System.err.println(e);
			}
		}
	}
}

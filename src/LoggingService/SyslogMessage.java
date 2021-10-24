package loggingdienst;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class SyslogMessage {

	private static final char SP = ' ';
	private static final String NIL = "-";
	private static final int MAXLEN = 2048;
	private static final byte[] BOM = { (byte) 0xef, (byte) 0xbb, (byte) 0xbf };

	private static int version = 1;

	private int facility;
	private int severity;
	private int pval;
	private OffsetDateTime timestamp;
	private String host;
	private String appName;
	private String procID;
	private String msgID;
	private byte[] msg;


	// Default-Konstruktor ohne Parameter
	public SyslogMessage() {

	}

	// Konstruktor mit übergebenen Parametern
	public SyslogMessage(int facility, int severity, String host, String appName, String procID, String msgID,
			String msg) throws Exception {

		if ((facility >= 0 && facility <= 23) && (severity >= 0 && severity <= 7)) {

			this.timestamp = OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS);

			this.setPval((facility * 8) + severity);
			this.setHost(host);
			this.setAppName(appName);
			this.setProcID(procID);
			this.setMsgID(msgID);

			// Hier wird das Encoding durch Transfer in ein Byte-Array in UTF-8 umgewandelt
			this.setMsg(msg.getBytes("UTF-8"));

		} else {
			throw new Exception("The data you have entered is invalid, please try again");
		}

	}

	// Verwandelt ein Objekt der Klasse SyslogMessage in einen String, der auf der
	// Kommandozeile ausgegeben werden kann
	String messageToString() {
		String messageString = "";

		messageString = ("<" + this.pval + ">" + this.version + " " + this.timestamp.toString() + " " + this.host + " "
				+ this.appName + " " + this.procID + " " + this.msgID + " " + BOM + this.msg);

		return messageString;
	}

	// Kürzen zu langer Nachrichten
	SyslogMessage truncate() {

		SyslogMessage truncatedMessage = new SyslogMessage();
		return truncatedMessage;

	}
	
	public int getFacility() {
		return facility;
	}

	public void setFacility(int facility) {
		this.facility = facility;
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}

	public int getPval() {
		return pval;
	}

	public void setPval(int pval) {
		this.pval = pval;
	}

	public OffsetDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(OffsetDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getProcID() {
		return procID;
	}

	public void setProcID(String procID) {
		this.procID = procID;
	}

	public String getMsgID() {
		return msgID;
	}

	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public byte[] getMsg() {
		return msg;
	}

	public void setMsg(byte[] msg) {
		this.msg = msg;
	}

}

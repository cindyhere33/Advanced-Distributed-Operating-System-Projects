import java.io.Serializable;

public class Message implements Serializable {

	public enum Type {
		REQUEST, INQUIRE, YIELD, FAIL, RELEASE, GRANT
	}

	private static final long serialVersionUID = 2L;

	// The node that generated this message
	private String originNode;

	// Type of message
	private Type messageType;

	// Destination of the message
	private String destinationNode;
	
	private Integer timestamp;

	public Message(String sender, String receiver, Type messageType, Integer timestamp) {
		this.originNode = sender;
		this.messageType = messageType;
		this.destinationNode = receiver;
		this.timestamp = timestamp;
	}
	
	

	public Integer getTimestamp() {
		return timestamp;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Type getMessageType() {
		return messageType;
	}

	public String getDestinationNode() {
		return destinationNode;
	}

	public String getOriginNode() {
		return originNode;
	}

	public void setOriginNode(String originNode) {
		this.originNode = originNode;
	}

}

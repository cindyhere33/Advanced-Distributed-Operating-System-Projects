package kootoueg;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 2L;

	// The node that generated this message
	private Integer originNode;

	// Destination of the message
	private Integer destinationNode;

	private Integer label;

	public enum TypeOfMessage {
		APPLICATION, CHECKPOINT, RECOVERY
	}

	private TypeOfMessage messageType;

	public Message(Integer sender, Integer receiver, Integer label, TypeOfMessage messageType) {
		this.originNode = sender;
		this.messageType = messageType;
		this.destinationNode = receiver;
		this.label = label;
	}

	public Integer getLabel() {
		return label;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public TypeOfMessage getMessageType() {
		return messageType;
	}

	public Integer getDestinationNode() {
		return destinationNode;
	}

	public Integer getOriginNode() {
		return originNode;
	}

	public void setOriginNode(Integer originNode) {
		this.originNode = originNode;
	}

}

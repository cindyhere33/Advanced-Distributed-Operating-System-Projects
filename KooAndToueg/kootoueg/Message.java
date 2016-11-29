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
		APPLICATION, CHECKPOINT_INITIATION, CHECKPOINT_OK, CHECKPOINT_FINAL, RECOVERY
	}

	private TypeOfMessage messageType;

	private Integer initiator;
	
	private Integer[] vectorClock;

	public Message(Integer sender, Integer receiver, Integer label, TypeOfMessage messageType, Integer initiator, Integer[] vectorClock) {
		this.originNode = sender;
		this.messageType = messageType;
		this.destinationNode = receiver;
		this.label = label;
		this.initiator = initiator;
		this.vectorClock = vectorClock;
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

	public Integer[] getVectorClock() {
		return vectorClock;
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

	public Integer getInitiator() {
		return initiator;
	}

	public void setInitiator(Integer initiator) {
		this.initiator = initiator;
	}

}

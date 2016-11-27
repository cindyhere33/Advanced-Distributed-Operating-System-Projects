package kootoueg;

import kootoueg.Main.EventType;

public class EventSequence {
	Main.EventType type;
	Integer nodeId;

	public EventSequence(String eventType, Integer nodeId) {
		if(eventType.contains("c")) type=EventType.CHECKPOINT;
		else type=EventType.RECOVERY;
		this.nodeId = nodeId;
	}

}

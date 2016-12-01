package kootoueg;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class CheckpointingUtils {

	public static void makeCheckpointPermanent() {
		Utils.log("About to make checkpoint permanent");
		if (Main.temporaryCheckpoint != null) {
			Main.checkpointsTaken.add(Main.temporaryCheckpoint);
			Utils.log("Made checkpoint permanent : " + Main.temporaryCheckpoint.getSequenceNumber());
			Utils.logVectorClock();
		}
		Utils.logVectors();
	}

	/*
	 * Set checkpointintInProgress to true. Send messages to all neighbours to
	 * start checkpointing
	 */
	public static boolean hasSentCheckpointingRequests() {
		boolean sentRequests = false;
		for (Integer id : Main.myNode.neighbours) {
			if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id] > -1) {
				Message msg = new Message(Main.myNode.getId(), id,
						Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id], TypeOfMessage.CHECKPOINT_INITIATION,
						Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
				Main.confirmationsPending.put(id, false);
				sentRequests = true;
			}
		}
		return sentRequests;
	}

	public static void takeTentativeCheckpoint() {
		if (Main.temporaryCheckpoint == null)
			Main.temporaryCheckpoint = new Checkpoint(Main.checkpointsTaken.size(), Main.vectors);
		Utils.log("Temporary checkpoint taken");
		Utils.updateVectors(EventType.CHECKPOINT, null);
	}

	public static boolean needsToTakeCheckpoint(Integer sender, Integer lastLabelReceived) {
		return Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][sender] > -1
				&& Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][sender] < lastLabelReceived.intValue();
	}

	public static void announceCheckpointProtocolTermination() {
		for (Integer id : Main.myNode.getNeighbours()) {
			Message msg = new Message(Main.myNode.getId(), id, Main.checkpointRecoverySequence.size(),
					TypeOfMessage.CHECKPOINT_FINAL, Main.myNode.getId(), null);
			Client.sendMessage(msg);
		}
		Main.checkpointingInProgress = false;
		Main.myCheckpointOrRecoveryInitiator = null;
		Main.temporaryCheckpoint = null;
	}

	public static void initiateCheckpointProtocol() {
		Utils.logVectors();
		if (hasSentCheckpointingRequests()) {
			Main.checkpointingInProgress = true;
			takeTentativeCheckpoint();
			Main.myCheckpointOrRecoveryInitiator = Main.myNode.getId();
		} else {
			if (Main.checkpointRecoverySequence.size() > 0) {
				Main.checkpointRecoverySequence.remove(0);
			}
			announceCheckpointProtocolTermination();
			Main.initiateCheckpointOrRecoveryIfMyTurn();
		}
	}
}

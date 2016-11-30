package kootoueg;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class CheckpointingUtils {

	public static void makeCheckpointPermanent() {
		if (Main.temporaryCheckpoint != null) {
			Main.checkpointsTaken.add(Main.temporaryCheckpoint);
			Utils.logVectorClock();
		}
		announceCheckpointProtocolTermination();
		Main.checkpointingInProgress = false;
		Main.temporaryCheckpoint = null;
		Utils.logVectors();
	}

	

	/*
	 * Set checkpointintInProgress to true. Send messages to all neighbours to
	 * start checkpointing
	 */
	public static boolean hasSentCheckpointingRequests() {
		boolean shouldTakeCheckpoint = false;
		for (Integer id : Main.myNode.neighbours) {
			if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id.intValue()] > -1) {
				Message msg = new Message(Main.myNode.getId(), id,
						Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id], TypeOfMessage.CHECKPOINT_INITIATION,
						Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
				Main.confirmationsPending.put(id, false);
				shouldTakeCheckpoint = true;
			}
		}
		if (shouldTakeCheckpoint) {
			takeCheckpoint();
			return true;
		} else {
			return false;
		}
	}

	public static void takeCheckpoint() {
		if (Main.temporaryCheckpoint == null)
			Main.temporaryCheckpoint = new Checkpoint(Main.checkpointsTaken.size(), Main.vectors);
//		Utils.logVectors();
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
	}

	public static void initiateCheckpointProtocol() {
		//Utils.logVectors();
		if (hasSentCheckpointingRequests()) {
			Main.checkpointingInProgress = true;
		} else {
			if (Main.checkpointRecoverySequence.size() > 0) {
				Main.checkpointRecoverySequence.remove(0);
			}
			announceCheckpointProtocolTermination();
			Main.checkpointingInProgress = false;
			Main.initiateCheckpointOrRecoveryIfMyTurn();
		}
	}
}

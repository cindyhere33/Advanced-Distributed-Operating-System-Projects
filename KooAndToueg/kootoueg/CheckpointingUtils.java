package kootoueg;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class CheckpointingUtils {

	public static void makeCheckpointPermanent() {
		if (Main.temporaryCheckpoint != null) {
			Main.checkpointsTaken.add(Main.temporaryCheckpoint);
			Utils.logCheckpoint();
		}
	}

	/*
	 * Set checkpointintInProgress to true. Send messages to all neighbours to
	 * start checkpointing
	 */
	public static boolean hasSentCheckpointingRequests() {
		boolean sentRequests = false;
		for (Integer id : Main.myNode.neighbours) {
			if (id.equals(Main.myCheckpointOrRecoveryInitiator))
				continue;
			if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id].intValue() > -1) {
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
		Main.confirmationsPending.clear();
	}

	public static void initiateCheckpointProtocol() {
		if (hasSentCheckpointingRequests()) {
			Main.checkpointingInProgress = true;
			takeTentativeCheckpoint();
		} else {
			if (Main.checkpointRecoverySequence.size() > 0) {
				Main.checkpointRecoverySequence.remove(0);
			}
			announceCheckpointProtocolTermination();
			Main.initiateCheckpointOrRecoveryIfMyTurn();
		}
	}

	public static void onAllConfirmationsReceived() {
		boolean allConfirmationsReceived = true;
		for (Integer id : Main.confirmationsPending.keySet()) {
			if (!Main.confirmationsPending.get(id))
				allConfirmationsReceived = false;
		}

		if (allConfirmationsReceived) {
			if (Main.myCheckpointOrRecoveryInitiator.equals(Main.myNode.getId())) {
				if (Main.checkpointRecoverySequence.size() > 0) {
					Main.checkpointRecoverySequence.remove(0);
				}
				CheckpointingUtils.makeCheckpointPermanent();
				CheckpointingUtils.announceCheckpointProtocolTermination();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			} else {
				Message msg = new Message(Main.myNode.getId(), Main.myCheckpointOrRecoveryInitiator, 0,
						TypeOfMessage.CHECKPOINT_OK, 0, Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
			}
		}
	}

}

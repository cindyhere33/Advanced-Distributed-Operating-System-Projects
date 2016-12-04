package kootoueg;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class CheckpointingUtils {

	public static synchronized void makeCheckpointPermanent() {
		if (Main.temporaryCheckpoint != null) {
			Main.checkpointsTaken.add(Main.temporaryCheckpoint);
		}
	}

	/*
	 * Set checkpointintInProgress to true. Send messages to all neighbours to
	 * start checkpointing
	 */
	public static synchronized boolean hasSentCheckpointingRequests() {
		boolean sentRequests = false;
		for (Integer id : Main.myNode.neighbours) {
			if (id.equals(Main.myCheckpointOrRecoveryInitiator))
				continue;
			Utils.logDebugStatements("Checking whether to send checkpointing request to " + id + "\t Initiator = "
					+ Main.myCheckpointOrRecoveryInitiator);
			if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id].intValue() > -1) {
				Message msg = new Message(Main.myNode.getId(), id,
						Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id], TypeOfMessage.CHECKPOINT_INITIATION,
						Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
				Main.confirmationsPending.put(id, false);
				Utils.logDebugStatements("Checkpointing request sent to  " + id + "\t Initiator = "
						+ Main.myCheckpointOrRecoveryInitiator);
				sentRequests = true;
			}
		}
		return sentRequests;
	}

	public static synchronized void takeTentativeCheckpoint() {
		if (Main.temporaryCheckpoint == null)
			Main.temporaryCheckpoint = new Checkpoint(Main.checkpointsTaken.size(), Main.vectors);
		Utils.logDebugStatements("Temporary checkpoint taken");
		Utils.updateVectors(EventType.CHECKPOINT, null);
	}

	public static synchronized boolean needsToTakeCheckpoint(Integer sender, Integer lastLabelReceived) {
		return Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][sender] > -1
				&& Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][sender] < lastLabelReceived.intValue();
	}

	public static synchronized void announceCheckpointProtocolTermination() {
		Utils.logDebugStatements("Announcing termination\t Initiator = " + Main.myCheckpointOrRecoveryInitiator);
		for (Integer id : Main.myNode.getNeighbours()) {
			if (id.equals(Main.myCheckpointOrRecoveryInitiator))
				continue;
			Message msg = new Message(Main.myNode.getId(), id, Main.checkpointRecoverySequence.size(),
					TypeOfMessage.CHECKPOINT_FINAL, Main.myNode.getId(), null);
			Client.sendMessage(msg);
		}
		Main.checkpointingInProgress = false;
		Main.myCheckpointOrRecoveryInitiator = null;
		Main.temporaryCheckpoint = null;
		Main.confirmationsPending.clear();

	}

	public static synchronized void initiateCheckpointProtocol() {
		System.out.println("-------------------------------------\n");
		Utils.log("CHECKPOINTING INITIATED");
		if (hasSentCheckpointingRequests()) {
			Main.checkpointingInProgress = true;
			takeTentativeCheckpoint();
		} else {
			if (Main.checkpointRecoverySequence.size() > 0) {
				Main.checkpointRecoverySequence.remove(0);
			}
			announceCheckpointProtocolTermination();
			Utils.logCheckpoint();
			Main.initiateCheckpointOrRecoveryIfMyTurn();
		}
	}

	public static void onConfirmationsReceived() {
		if (Utils.areAllConfirmationsReceived()) {
			if (Main.myCheckpointOrRecoveryInitiator == null)
				Utils.logDebugStatements("Checkpoint initiator is null");
			else if (Main.myNode == null)
				Utils.logDebugStatements("myNode is null");
			else if (Main.myNode.getId() == null) {
				Utils.logDebugStatements("My Node id is null");
			}
			if (Main.myCheckpointOrRecoveryInitiator.equals(Main.myNode.getId())) {
				if (Main.checkpointRecoverySequence.size() > 0) {
					Main.checkpointRecoverySequence.remove(0);
				}
				CheckpointingUtils.makeCheckpointPermanent();
				Utils.logCheckpoint();
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

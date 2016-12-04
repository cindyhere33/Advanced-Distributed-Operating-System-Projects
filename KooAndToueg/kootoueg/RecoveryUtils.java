package kootoueg;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class RecoveryUtils {

	public static synchronized void initiateRecoveryProtocol() {
		System.out.println("-------------------------------------\n");
		Utils.log("RECOVERY INITIATED");
		Utils.logDebugStatements("Checkpoints taken so far .. " + Main.checkpointsTaken.size());
		if (Main.checkpointsTaken.size() > 0) {
			rollback();
			Utils.logCheckpoint();
			if (hasSentRecoveryRequest()) {
				return;
			}
		}
		if (Main.checkpointRecoverySequence.size() > 0) {
			Main.checkpointRecoverySequence.remove(0);
		}
		announceRecoveryProtocolTermination();
		Utils.logCheckpoint();
		Main.initiateCheckpointOrRecoveryIfMyTurn();
	}

	public static synchronized void announceRecoveryProtocolTermination() {
		for (Integer id : Main.myNode.neighbours) {
			Message msg = new Message(Main.myNode.getId(), id, Main.checkpointRecoverySequence.size(),
					TypeOfMessage.RECOVERY_CONCLUDED, Main.myNode.getId(), null);
			Client.sendMessage(msg);
		}
	}

	public static synchronized boolean hasSentRecoveryRequest() {
		boolean sentRequests = false;
		for (Integer id : Main.myNode.neighbours) {
			if (id.equals(Main.myCheckpointOrRecoveryInitiator))
				continue;
			Utils.logDebugStatements("Checking whether to send recovery request to " + id + "\t Initiator = "
					+ Main.myCheckpointOrRecoveryInitiator);
			Message msg = new Message(Main.myNode.getId(), id, Main.vectors[VectorType.LAST_LABEL_SENT.ordinal()][id],
					TypeOfMessage.RECOVERY_INITIATION, Main.myNode.getId(),
					Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
			Client.sendMessage(msg);
			Main.confirmationsPending.put(id, false);
			Utils.logDebugStatements(
					"Recovery request sent to  " + id + "\t Initiator = " + Main.myCheckpointOrRecoveryInitiator);
			sentRequests = true;
		}
		return sentRequests;
	}

	public static synchronized void rollback() {
		Utils.updateVectors(EventType.RECOVERY, null);
		if (Main.checkpointsTaken.size() > 0) {
			Checkpoint lastStableCheckpoint = Main.checkpointsTaken.get(Main.checkpointsTaken.size() - 1);
			for (int i = 0; i < 4; i++) {
				System.arraycopy(lastStableCheckpoint.vectors[i], 0, Main.vectors[i], 0, Main.noNodes);
			}
			Utils.log("Rolled back");
		}
	}

	public static synchronized boolean needsToRollback(Integer sender, Integer lastLabelSent) {
		return Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][sender] > lastLabelSent;
	}

	public static synchronized void onConfirmationsReceived() {
		if (Utils.areAllConfirmationsReceived()) {
			if (Main.myCheckpointOrRecoveryInitiator.equals(Main.myNode.getId())) {
				if (Main.checkpointRecoverySequence.size() > 0) {
					Main.checkpointRecoverySequence.remove(0);
				}
				announceRecoveryProtocolTermination();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			} else {
				Message msg = new Message(Main.myNode.getId(), Main.myCheckpointOrRecoveryInitiator, 0,
						TypeOfMessage.RECOVERY_OK, 0, Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
			}
		}
	}
}

package kootoueg;

import java.util.Timer;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class CheckpointingUtils {

	// TODO: Temporary checkpoint null on initiation!
	public static void makeCheckpointPermanent() {
		if (Main.temporaryCheckpoint != null) {
			Main.checkpointsTaken.add(Main.temporaryCheckpoint);
		}
		announceCheckpointProtocolTermination();
		Main.checkpointingInProgress = false;
		Main.temporaryCheckpoint = null;
		Utils.logVectors();
	}

	/*
	 * TODO: 1. Checkpoint request is sent only to processes that need to take a
	 * checkpoint. So the number of CHECKPOINT_OK will not be q equal to number
	 * of neighbours 2.If you need not take a checkpoint, send checkpoint_final
	 * to all other processes so that they know they are up next
	 */
	public static void initiateCheckpointingIfMyTurn() {
		if (Main.checkpointRecoverySequence.size() > 0
				&& Main.checkpointRecoverySequence.get(0).nodeId.equals(Main.myNode.getId())) {
			Utils.log("My Checkpointing turn: ");
			Timer timer = new Timer();
			timer.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					if (Main.checkpointRecoverySequence.size() > 0
							&& Main.checkpointRecoverySequence.get(0).nodeId.equals(Main.myNode.getId())) {
						if (Main.checkpointRecoverySequence.get(0).type == EventType.CHECKPOINT) {
							Utils.log("Initiating checkpointing protocol");
							initiateCheckpointProtocol();
						} else {
							// initiateRecovery();
						}

					}
				}
			}, Utils.getExponentialDistributedValue(Main.instanceDelay));

		}
	}

	/*
	 * Set checkpointintInProgress to true. Send messages to all neighbours to
	 * start checkpointing
	 */
	public static boolean hasSentCheckpointingRequests() {
		boolean shouldTakeCheckpoint = false;
		for (Integer id : Main.myNode.neighbours) {
			if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id.intValue()] > -1) {
				Utils.log("Send checkpoint initiation to : " + id);
				Message msg = new Message(Main.myNode.getId(), id,
						Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id], TypeOfMessage.CHECKPOINT_INITIATION,
						Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
				Main.checkpointConfirmationsReceived.put(id, false);
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
		Utils.updateVectors(EventType.CHECKPOINT, null);
	}

	public static boolean needsToTakeCheckpoint(Integer sender, Integer lastLabelReceived) {
		return Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][sender] > -1
				&& Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][sender] < lastLabelReceived.intValue();
	}

	public static void announceCheckpointProtocolTermination() {
		for (Integer id : Main.myNode.getNeighbours()) {
			Message msg = new Message(Main.myNode.getId(), id, Main.checkpointRecoverySequence.size(), TypeOfMessage.CHECKPOINT_FINAL, Main.myNode.getId(),
					null);
			Client.sendMessage(msg);
		}
		Main.checkpointingInProgress = false;
	}

	public static void initiateCheckpointProtocol() {
		Utils.logVectors();
		if (hasSentCheckpointingRequests()) {
			Main.checkpointingInProgress = true;
			Utils.log("Set checkpointingInProgress to true");
		} else {
			if (Main.checkpointRecoverySequence.size() > 0){
				Main.checkpointRecoverySequence.remove(0);
			}
			announceCheckpointProtocolTermination();
			Main.checkpointingInProgress = false;
		}
	}
}

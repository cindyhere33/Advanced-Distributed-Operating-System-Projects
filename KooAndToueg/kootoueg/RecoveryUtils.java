package kootoueg;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class RecoveryUtils {

	public static void initiateRecoveryProtocol() {
		if (Main.checkpointsTaken.size() > 1) {
			rollback();
		} else {
			if (Main.checkpointRecoverySequence.size() > 0) {
				Main.checkpointRecoverySequence.remove(0);
			}
			announceRecoveryProtocolTermination();
		}
	}

	public static void announceRecoveryProtocolTermination() {
		for (Integer id : Main.myNode.neighbours) {
			Message msg = new Message(Main.myNode.getId(), id, Main.checkpointRecoverySequence.size(),
					TypeOfMessage.RECOVERY_CONCLUDED, Main.myNode.getId(), null);
			Client.sendMessage(msg);
		}
	}

	public static void sendRecoveryRequest() {
		for (Integer id : Main.myNode.neighbours) {
			Message msg = new Message(Main.myNode.getId(), id, Main.vectors[VectorType.LAST_LABEL_SENT.ordinal()][id],
					TypeOfMessage.RECOVERY_INITIATION, Main.myNode.getId(), null);
			Client.sendMessage(msg);
			Main.confirmationsPending.put(id, false);
		}
	}

	public static void rollback() {
		Utils.updateVectors(EventType.RECOVERY, null);
		if (Main.checkpointsTaken.size() > 1) {
			Main.vectors = Main.checkpointsTaken.get(Main.checkpointsTaken.size() - 1).getVectors();
			Utils.log("Rolled back");
			sendRecoveryRequest();
		}
	}

	public static boolean needsToRollback(Integer sender, Integer lastLabelSent) {
		return Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][sender] > lastLabelSent;
	}
}

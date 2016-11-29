package kootoueg;

import java.util.Timer;

import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class CheckpointingUtils {
	
	public static void makeCheckpointPermanent() {
		if (Main.temporaryCheckpoint != null) {
			Main.checkpointsTaken.add(Main.temporaryCheckpoint);
		}
		Main.checkpointingInProgress = false;
		Main.temporaryCheckpoint = null;
		Utils.logVectors();
	}


	/*TODO:
	 * 1. Checkpoint request is sent only to processes that need to take a checkpoint. So the number of CHECKPOINT_OK will not be q
	 * equal to number of neighbours
	 * 2.If you need not take a checkpoint, send checkpoint_final to all other processes so that they know they are up next 
	*/
	public static void initiateCheckpointingIfMyTurn() {
		if (Main.checkpointRecoverySequence.size() > 0
				&& Main.checkpointRecoverySequence.get(0).nodeId.equals(Main.myNode.getId())) {
			Utils.log("My Checkpointing turn: ");
			Timer timer = new Timer();
			timer.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					Main.checkpointingInProgress = true;
					Utils.log("Sending checkpoint initiation to all neighbours" );
					boolean sentCheckpointRequest = false;
					for (Integer id : Main.myNode.neighbours) {
						if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id.intValue()] > -1) {
							Utils.log("Send checkpoint initiation to : " + id);
							Message msg = new Message(Main.myNode.getId(), id,
									Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id],
									TypeOfMessage.CHECKPOINT_INITIATION, Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
							Client.sendMessage(msg);
							sentCheckpointRequest = true;
						}
					}
					if(!sentCheckpointRequest){
						for (Integer id : Main.myNode.neighbours) {
							if (Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id.intValue()] > -1) {
								Utils.log("Send checkpoint initiation to : " + id);
								Message msg = new Message(Main.myNode.getId(), id,
										Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][id],
										TypeOfMessage.CHECKPOINT_INITIATION, Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
								Client.sendMessage(msg);
								sentCheckpointRequest = true;
							}
						}
					}
				}
			}, Main.instanceDelay);
			
		}
	}

}

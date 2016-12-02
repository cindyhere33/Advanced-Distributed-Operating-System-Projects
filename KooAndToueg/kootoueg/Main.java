package kootoueg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import com.sun.nio.sctp.SctpServerChannel;

import kootoueg.Message.TypeOfMessage;

public class Main {

	// Total number of nodes in the network connected to this node
	public static Integer noNodes;

	// The Node object of this node - contains port number and host name
	public static Node myNode;

	// Map of all nodes with their identifiers as keys
	public static HashMap<Integer, Node> nodeMap = new HashMap<>();

	// Client object for this node
	public static Client client;

	// Ordered by 1.VectorClock 2.FirstLabelSent 3.LastLabelReceived
	// 4.LastLabelSent
	public static Integer[][] vectors;

	static SctpServerChannel ssc;

	static Server server;

	/*
	 * Main.vectors[0] = VECTOR_CLOCK Main.vectors[1] = FIRST_LABEL_SENT
	 * Main.vectors[2] = LAST_LABEL_RECEIVED Main.vectors[3] = LAST_LABEL_SENT
	 */
	public static enum VectorType {
		VECTOR_CLOCK, FIRST_LABEL_SENT, LAST_LABEL_RECEIVED, LAST_LABEL_SENT
	}

	public static enum EventType {
		SEND_MSG, RECEIVE_MSG, CHECKPOINT, RECOVERY
	}

	public static Boolean isFinalRun = false;

	public static Integer checkpointSequenceNumber = 0;

	public static List<Checkpoint> checkpointsTaken = new ArrayList<>();

	public static Integer instanceDelay = 0, sendDelay = 0, msgCount = 0, totalNoOfMsgs = 0;

	// Sequence given in the Config file
	public static List<EventSequence> checkpointRecoverySequence = new ArrayList<>();

	public static boolean checkpointingInProgress = false;

	public static Checkpoint temporaryCheckpoint = null;

	public static HashMap<Integer, Boolean> confirmationsPending = new HashMap<>();

	public static Integer myCheckpointOrRecoveryInitiator = null;

	public static void main(String[] args) throws IOException {
		try {
			ConfigParser parser = new ConfigParser(args[0]);
			parser.parseFile();
			Utils.setupVectors();
			server = new Server(myNode.getHostName(), myNode.getPortNo());
			server.start();
			Thread.sleep(7000);
			Client.sendMessage();
			checkpointsTaken.add(new Checkpoint(Main.checkpointSequenceNumber, Main.vectors));
			Thread.sleep(2000);
			initiateCheckpointOrRecoveryIfMyTurn();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void initiateCheckpointOrRecoveryIfMyTurn() {
		if (checkpointRecoverySequence.size() > 0 && checkpointRecoverySequence.get(0).nodeId.equals(myNode.getId())) {
			Utils.log("My Checkpointing turn: ");
			Utils.logVectors();
			Timer timer = new Timer();
			timer.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					if (checkpointRecoverySequence.size() > 0
							&& checkpointRecoverySequence.get(0).nodeId.equals(myNode.getId())) {
						Main.myCheckpointOrRecoveryInitiator = Main.myNode.getId();
						Utils.log("Checkpoint initiator = " + Main.myCheckpointOrRecoveryInitiator);
						if (checkpointRecoverySequence.get(0).type == EventType.CHECKPOINT) {
							Utils.log("Initiating checkpointing protocol");
							Main.checkpointingInProgress = true;
							CheckpointingUtils.initiateCheckpointProtocol();
						} else {
							Utils.log("Initiating recovery protocol");
							RecoveryUtils.initiateRecoveryProtocol();
						}
					}
				}
			}, Utils.getExponentialDistributedValue(Main.instanceDelay));

		}
	}

	// Check if initiator or not
	public synchronized static void handleMessage(Message message) {
		// Utils.log("Received " + message.getMessageType().name() + " from " +
		// message.getOriginNode() + " with label "
		// + message.getLabel());
		switch (message.getMessageType()) {
		case APPLICATION:
			Utils.updateVectors(EventType.RECEIVE_MSG, message);
			break;
		case CHECKPOINT_INITIATION:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			Utils.logVectors();

			if (checkpointingInProgress
					|| !CheckpointingUtils.needsToTakeCheckpoint(message.getOriginNode(), message.getLabel())) {
				Message msg = new Message(myNode.getId(), message.getOriginNode(), 0,
						TypeOfMessage.CHECKPOINT_NOT_NEEDED, message.getOriginNode(), null);
				Client.sendMessage(msg);
			} else {
				checkpointingInProgress = true;
				myCheckpointOrRecoveryInitiator = message.getOriginNode();
				Utils.log("My checkpoint initiator = " + myCheckpointOrRecoveryInitiator);
				if (!CheckpointingUtils.hasSentCheckpointingRequests()) {
					Message msg = new Message(myNode.getId(), myCheckpointOrRecoveryInitiator, 0,
							TypeOfMessage.CHECKPOINT_OK, message.getOriginNode(), null);
					Client.sendMessage(msg);
				}
				CheckpointingUtils.takeTentativeCheckpoint();
			}
			break;
		case RECOVERY_INITIATION:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			if (RecoveryUtils.needsToRollback(message.getOriginNode(), message.getLabel())) {
				myCheckpointOrRecoveryInitiator = message.getOriginNode();
				RecoveryUtils.sendRecoveryRequest();
			} else {
				Message msg = new Message(myNode.getId(), message.getOriginNode(), 0, TypeOfMessage.RECOVERY_NOT_NEEDED,
						message.getOriginNode(), null);
				Client.sendMessage(msg);
			}
			break;
		case CHECKPOINT_OK:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			confirmationsPending.put(message.getOriginNode(), true);
			boolean allConfirmationsReceived = true;
			for (Integer id : confirmationsPending.keySet()) {
				if (!confirmationsPending.get(id))
					allConfirmationsReceived = false;
			}

			if (allConfirmationsReceived) {
				Utils.logConfirmationsRecieved();
				if (myCheckpointOrRecoveryInitiator == null)
					Utils.log("Initiator is null");
				else if (myNode == null)
					Utils.log("My node is null");
				if (myCheckpointOrRecoveryInitiator.equals(myNode.getId())) {
					if (checkpointRecoverySequence.size() > 0) {
						checkpointRecoverySequence.remove(0);
					}
					CheckpointingUtils.makeCheckpointPermanent();
					CheckpointingUtils.announceCheckpointProtocolTermination();
					Main.initiateCheckpointOrRecoveryIfMyTurn();
				} else {
					Message msg = new Message(myNode.getId(), myCheckpointOrRecoveryInitiator, 0,
							TypeOfMessage.CHECKPOINT_OK, message.getOriginNode(),
							Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
					Client.sendMessage(msg);
				}
			}
			break;
		case CHECKPOINT_FINAL:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			if (checkpointRecoverySequence.size() > 0 && message.getLabel() < checkpointRecoverySequence.size()) {
				checkpointRecoverySequence.remove(0);
				CheckpointingUtils.makeCheckpointPermanent();
				CheckpointingUtils.announceCheckpointProtocolTermination();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			}
			break;
		case CHECKPOINT_NOT_NEEDED:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			if (confirmationsPending.containsKey(message.getOriginNode())) {
				confirmationsPending.remove(message.getOriginNode());
			}
			if (confirmationsPending.size() == 0) {
				Message msg = new Message(myNode.getId(), myCheckpointOrRecoveryInitiator, 0,
						TypeOfMessage.CHECKPOINT_OK, message.getOriginNode(),
						Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
			}
			break;
		case RECOVERY_CONCLUDED:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			if (checkpointRecoverySequence.size() > 0 && message.getLabel() < checkpointRecoverySequence.size()) {
				checkpointRecoverySequence.remove(0);
				myCheckpointOrRecoveryInitiator = null;
				RecoveryUtils.announceRecoveryProtocolTermination();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			}
			break;
		case RECOVERY_NOT_NEEDED:
			Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
			if (confirmationsPending.containsKey(message.getOriginNode())) {
				confirmationsPending.remove(message.getOriginNode());
			}
			if (confirmationsPending.size() == 0) {
				if (myCheckpointOrRecoveryInitiator.equals(myNode.getId())) {
					myCheckpointOrRecoveryInitiator = null;
					RecoveryUtils.announceRecoveryProtocolTermination();
					Main.initiateCheckpointOrRecoveryIfMyTurn();
				} else {
					Message msg = new Message(myNode.getId(), myCheckpointOrRecoveryInitiator, 0,
							TypeOfMessage.RECOVERY_NOT_NEEDED, myCheckpointOrRecoveryInitiator, null);
					Client.sendMessage(msg);
				}
			}
			break;
		default:
			break;
		}
	}

}

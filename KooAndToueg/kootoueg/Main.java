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

	public static Boolean needsToRollback = false;

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

	public static synchronized void initiateCheckpointOrRecoveryIfMyTurn() {
		if (checkpointRecoverySequence.size() > 0 && checkpointRecoverySequence.get(0).nodeId.equals(myNode.getId())) {
			Timer timer = new Timer();
			timer.schedule(new java.util.TimerTask() {
				@Override
				public void run() {
					if (checkpointingInProgress)
						return;
					if (checkpointRecoverySequence.size() > 0
							&& checkpointRecoverySequence.get(0).nodeId.equals(myNode.getId())) {
						Main.myCheckpointOrRecoveryInitiator = Main.myNode.getId();
						if (checkpointRecoverySequence.get(0).type == EventType.CHECKPOINT) {
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
		if (message.getMessageType() != TypeOfMessage.APPLICATION)
			Utils.logDebugStatements("Received " + message.getMessageType().name() + " from " + message.getOriginNode()
					+ " with label " + message.getLabel());
		switch (message.getMessageType()) {
		case APPLICATION:
			Utils.updateVectors(EventType.RECEIVE_MSG, message);
			break;
		case CHECKPOINT_INITIATION:
			if (checkpointingInProgress
					|| !CheckpointingUtils.needsToTakeCheckpoint(message.getOriginNode(), message.getLabel())) {
				Message msg = new Message(myNode.getId(), message.getOriginNode(), 0,
						TypeOfMessage.CHECKPOINT_NOT_NEEDED, message.getOriginNode(), null);
				Client.sendMessage(msg);
			} else {
				checkpointingInProgress = true;
				myCheckpointOrRecoveryInitiator = message.getOriginNode();
				if (!CheckpointingUtils.hasSentCheckpointingRequests()) {
					Message msg = new Message(myNode.getId(), myCheckpointOrRecoveryInitiator, 0,
							TypeOfMessage.CHECKPOINT_OK, message.getOriginNode(), null);
					Client.sendMessage(msg);
				}
				CheckpointingUtils.takeTentativeCheckpoint();
			}
			break;
		case RECOVERY_INITIATION:
			if (RecoveryUtils.needsToRollback(message.getOriginNode(), message.getLabel()) && !needsToRollback) {
				myCheckpointOrRecoveryInitiator = message.getOriginNode();
				needsToRollback = true;
				if (RecoveryUtils.hasSentRecoveryRequest()) {
					break;
				}
			}
			Message msg = new Message(myNode.getId(), message.getOriginNode(), 0, TypeOfMessage.RECOVERY_OK,
					message.getOriginNode(), null);
			Client.sendMessage(msg);

			break;
		case CHECKPOINT_OK:
			Utils.logConfirmationsRecieved();
			confirmationsPending.put(message.getOriginNode(), true);
			CheckpointingUtils.onConfirmationsReceived();
			break;
		case CHECKPOINT_FINAL:
			if (checkpointRecoverySequence.size() > 0 && message.getLabel() < checkpointRecoverySequence.size()) {
				checkpointRecoverySequence.remove(0);
				CheckpointingUtils.makeCheckpointPermanent();
				CheckpointingUtils.announceCheckpointProtocolTermination();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			}
			break;
		case CHECKPOINT_NOT_NEEDED:
			Utils.logConfirmationsRecieved();
			if (confirmationsPending.containsKey(message.getOriginNode())) {
				confirmationsPending.remove(message.getOriginNode());
			}
			CheckpointingUtils.onConfirmationsReceived();
			break;
		case RECOVERY_CONCLUDED:
			if (checkpointRecoverySequence.size() > 0 && message.getLabel() < checkpointRecoverySequence.size()) {
				checkpointRecoverySequence.remove(0);
				if (needsToRollback) {
					RecoveryUtils.rollback();
					needsToRollback = false;
				}
				RecoveryUtils.announceRecoveryProtocolTermination();
				myCheckpointOrRecoveryInitiator = null;
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			}
			break;
		case RECOVERY_OK:
			RecoveryUtils.onConfirmationsReceived();
			break;
		default:
			break;
		}
	}

}

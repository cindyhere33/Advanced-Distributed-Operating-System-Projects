package kootoueg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

	// Server thread that listens to this node's port
	public static Server server;

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

	public static enum VectorType {
		VECTOR_CLOCK, FIRST_LABEL_SENT, LAST_LABEL_RECEIVED, LAST_LABEL_SENT
	}

	public static enum EventType {
		SEND_MSG, RECEIVE_MSG, CHECKPOINT, RECOVERY
	}

	public static Boolean isFinalRun = true;

	public static Integer checkpointSequenceNumber = 0;

	public static List<Checkpoint> checkpointsTaken = new ArrayList<>();

	public static Integer instanceDelay = 0, sendDelay = 0, msgCount = 0, totalNoOfMsgs = 0;

	public static List<EventSequence> checkpointRecoverySequence = new ArrayList<>();

	public static boolean checkpointingInProgress = false;

	public static Checkpoint temporaryCheckpoint = null;

	public static HashMap<Integer, Boolean> checkpointConfirmationsReceived = new HashMap<>();

	public static void main(String[] args) throws IOException {
		try {
			ConfigParser parser = new ConfigParser(args[0]);
			parser.parseFile();
			Utils.setupVectors();
			server = new Server(myNode.getHostName(), myNode.getPortNo());
			server.start();
			Utils.makeCheckpointPermanent();
			Client.sendMessage();
			Utils.initiateCheckpointingIfMyTurn();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Kills the server thread and closes server socket
	 */
	@SuppressWarnings("deprecation")
	public static void killServer() {
		server.stop();
		server.destroy();
	}

}

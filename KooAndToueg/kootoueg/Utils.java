package kootoueg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import kootoueg.Main.VectorType;

public class Utils {

	static String filePath = "/home/010/s/sx/sxk159231/CS6378/Project3/KooAndToueg/";

	/*
	 * Prints msg to console
	 */
	public static synchronized void log(String msg) {
		System.out.println(Main.myNode.getId() + " : " + msg);
	}

	/*
	 * Writes to file with filename the msg
	 */
	public static void writeToFile(String filename, String content) {
		File file = new File(filePath + filename);
		file.getParentFile().mkdirs();
		try (FileOutputStream fop = new FileOutputStream(file, true)) {
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] contentInBytes = content.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized long getExponentialDistributedValue(int value) {
		// return (long) Math.floor((-value * Math.log(Math.random())));
		return value;
	}

	public static synchronized void initVector(Main.VectorType type, int val) {
		Arrays.fill(Main.vectors[type.ordinal()], val);
	}

	public static synchronized void setupVectors() {
		Main.vectors = new Integer[4][Main.noNodes];
		Utils.initVector(VectorType.VECTOR_CLOCK, 0);
		Utils.initVector(VectorType.FIRST_LABEL_SENT, -1);
		Utils.initVector(VectorType.LAST_LABEL_RECEIVED, -1);
		Utils.initVector(VectorType.LAST_LABEL_SENT, -1);
	}

	/*
	 * In case of sending message, nodeid refers to the recipient node In case
	 * of receiving message, nodeid refers to the source node
	 */
	public static synchronized void updateVectors(Main.EventType eventType, Message msg) {
		switch (eventType) {

		// First label sent updated if no messages were sent to the node after
		// taking previous checkpoint

		case SEND_MSG:
			Main.vectors[VectorType.VECTOR_CLOCK.ordinal()][Main.myNode.getId()]++;
			if (Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][msg.getDestinationNode()] == -1) {
				Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][msg.getDestinationNode()] = msg.getLabel();
			}
			Main.vectors[VectorType.LAST_LABEL_SENT.ordinal()][msg.getDestinationNode()] = msg.getLabel();
			break;
		case RECEIVE_MSG:
			for (int i = 0; i < Main.noNodes; i++) {
				if (msg.getVectorClock()[i] > Main.vectors[VectorType.VECTOR_CLOCK.ordinal()][i]) {
					Main.vectors[VectorType.VECTOR_CLOCK.ordinal()][i] = msg.getVectorClock()[i];
				}
			}
			Main.vectors[VectorType.VECTOR_CLOCK.ordinal()][Main.myNode.getId()]++;
			Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][msg.getOriginNode()] = msg.getLabel();
			break;
		case CHECKPOINT:
			Utils.initVector(VectorType.LAST_LABEL_RECEIVED, -1);
			Utils.initVector(VectorType.FIRST_LABEL_SENT, -1);
			break;
		case RECOVERY:
			Utils.initVector(VectorType.LAST_LABEL_RECEIVED, -1);
			break;
		}
	}

	public static synchronized void logVectors() {
		StringBuffer line = new StringBuffer();
		int i = 0;
		for (Integer[] vector : Main.vectors) {
			line.append(VectorType.values()[i].name() + "\n------------------------------\n");
			for (Integer vec : vector) {
				line.append(vec + "\t");
			}
			line.append("\n");
			i++;
		}
		Utils.logDebugStatements(line.toString() + "\n==================================================\n");
	}

	public static synchronized void logVectorClock() {
		StringBuffer line = new StringBuffer();
		line.append("Vector Clock: \n------------------------------\n");
		for (Integer vec : Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]) {
			line.append(vec + "\t");
		}
		line.append("\n");
		Utils.logDebugStatements(line.toString());
	}

	public static synchronized void logConfirmationsRecieved() {
		StringBuffer buff = new StringBuffer();
		for (Integer id : Main.confirmationsPending.keySet()) {
			buff.append(id + " - " + Main.confirmationsPending.get(id) + "\n");
		}
		Utils.logDebugStatements("Confirmations pending = \n " + buff);
	}

	public static synchronized void logCheckpoint() {
		if (Main.checkpointsTaken.size() > 0) {
			Checkpoint checkpoint = Main.checkpointsTaken.get(Main.checkpointsTaken.size() - 1);
			StringBuffer buff = new StringBuffer();
			buff.append("Checkpoint number: " + checkpoint.getSequenceNumber());
			buff.append("\nVECTOR CLOCK : \n");
			for (Integer label : checkpoint.getVectors()[VectorType.VECTOR_CLOCK.ordinal()]) {
				buff.append(label + "\t");
			}
			buff.append("\n");
			Utils.log(buff.toString() + "\n");
		}
	}

	public static synchronized void logDebugStatements(String msg) {
//		Utils.log(msg);
	}

	public static synchronized boolean areAllConfirmationsReceived() {
		boolean allConfirmationsReceived = true;
		for (Integer id : Main.confirmationsPending.keySet()) {
			if (!Main.confirmationsPending.get(id))
				allConfirmationsReceived = false;
		}
		return allConfirmationsReceived;
	}

}

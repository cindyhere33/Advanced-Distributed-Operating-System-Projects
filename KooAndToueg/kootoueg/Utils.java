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
	public static void log(String msg) {
		if (Main.isFinalRun) {
			if (!(msg.contains("Enter") || msg.contains("Exit"))) {
				return;
			}
		}
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

	public static long getExponentialDistributedValue(int value) {
		return (long) Math.floor((-value * Math.log(Math.random())));
	}

	
	public static void initVector(Main.VectorType type, int val) {
		Arrays.fill(Main.vectors[type.ordinal()], val);
	}

	public static void setupVectors() {
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
	public static void updateVectors(Main.EventType eventType, Message msg) {
		switch (eventType) {
		case SEND_MSG:
			Main.vectors[VectorType.VECTOR_CLOCK.ordinal()][Main.myNode.getId()]++;
			if (Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][msg.getDestinationNode()] == 0) {
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
			Main.vectors[VectorType.LAST_LABEL_RECEIVED.ordinal()][msg.getDestinationNode()] = msg.getLabel();
			break;
		case CHECKPOINT:
			Utils.initVector(VectorType.LAST_LABEL_RECEIVED, -1);
			Utils.initVector(VectorType.FIRST_LABEL_SENT, -1);
			break;
		case RECOVERY:
			Utils.initVector(VectorType.LAST_LABEL_RECEIVED, -1);
			Utils.initVector(VectorType.LAST_LABEL_SENT, -1);
			break;
		}
	}


	public static void logVectors() {
		StringBuffer line = new StringBuffer();
		int i = 0;
		for (Integer[] vector : Main.vectors) {
			line.append(VectorType.values()[i].name() + "\n");
			for (Integer vec : vector) {
				line.append(vec + "\t");
			}
			line.append("\n");
			i++;
		}
		Utils.log(line.toString());
	}
}

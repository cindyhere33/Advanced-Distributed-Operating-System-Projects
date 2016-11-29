package kootoueg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigParser {

	public ConfigParser(String myId) {
		this.myId = Integer.parseInt(myId);
	}

	Integer myId;

	String directory = "/home/010/s/sx/sxk159231/CS6378/Project3/KooAndToueg/config.txt";
	// String directory = "C:/Users/Sindhura/Documents/Subjects/Advanced
	// Operating System/Projects/KooAndToueg/config.txt";

	/*
	 * Data from config provided by launcher through command line arguments are
	 * all used to initialize node variables. Node's label value is assigned and
	 * token is generated.
	 */
	void parseFile() {
		int noNodesSet = 0;
		Main.noNodes = -1;
		Main.myNode = null;
		boolean neighboursSet = false;
		try {
			for (String line : Files.readAllLines(Paths.get(directory))) {
				if (line.startsWith("#"))
					continue;
				int x = line.indexOf('#');
				if (x > 0) {
					line = line.substring(0, line.indexOf('#'));
				}
				line = line.trim().replaceAll("\t+", " ");
				line = line.trim().replaceAll(" +", " ");
				line.trim();
				if (line.length() < 1)
					continue;
				String[] parts = line.split(" ");
				if (parts.length == 1 && parts[0].equals(""))
					continue;
				if (Main.noNodes < 0 && parts.length == 5) {
					Main.noNodes = Integer.parseInt(parts[0].trim());
					Main.instanceDelay = Integer.parseInt(parts[2].trim());
					Main.sendDelay = Integer.parseInt(parts[3].trim());
					Main.totalNoOfMsgs = Integer.parseInt(parts[4].trim());
					continue;
				}
				if (noNodesSet < Main.noNodes) {
					if (parts.length == 3) {
						Node node = new Node(Integer.parseInt(parts[0].trim()), parts[1].trim(), parts[2].trim());
						Main.nodeMap.put(node.getId(), node);
						noNodesSet++;
						continue;
					}
				}
				if (noNodesSet == Main.noNodes.intValue() && Main.myNode == null)
					Main.myNode = Main.nodeMap.get(this.myId);

				if (!neighboursSet) {
					if (parts.length > 0 && Integer.parseInt(parts[0].trim()) == myId.intValue()) {
						int noOfNeighbours = parts.length;
						for (int k = 1; k < noOfNeighbours; k++) {
							Main.myNode.neighbours.add(Integer.parseInt(parts[k].trim()));
						}
						neighboursSet = true;
					}
				} else {
					if (parts.length == 5) {
						Main.checkpointRecoverySequence
								.add(new EventSequence(parts[1].trim(), Integer.parseInt(parts[3].trim())));
					}
				}

			}
			// printAll();
		} catch (

		IOException e) {
			System.out.println("Config - File exception");
			e.printStackTrace();
		}
	}

	void printAll() {
		System.out.println("MyNode : \t" + Main.myNode.id + " \t " + Main.myNode.hostName + "\t" + Main.myNode.portNo);
		System.out.println("All Node Details : \n");
		for (Integer id : Main.nodeMap.keySet()) {
			Node node = Main.nodeMap.get(id);
			System.out.println("\t" + node.id + " \t " + node.hostName + "\t" + node.portNo + "\n");
		}
		System.out.println("Neighbours : ");
		for (Integer id : Main.myNode.neighbours) {
			System.out.println(id + "\t");
		}
		for (EventSequence seq : Main.checkpointRecoverySequence) {
			System.out.println("Action : " + seq.type.name() + " by Process : " + seq.nodeId);
		}
		System.out.println("\nInstanceDelay : " + Main.instanceDelay + " \nSend delay : " + Main.sendDelay
				+ "\nTotal msg count = " + Main.totalNoOfMsgs);
		System.out.println();

	}

}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigParser {

	public ConfigParser(String myId) {
		this.myId = myId;
	}

	String myId = "";
	String directory = "/home/010/s/sx/sxk159231/CS6378/Project2/Project2_AOS/config.txt";
	//String directory = "C:/Users/Sindhura/Documents/Subjects/Advanced Operating System/Projects/Project2_AOS/config.txt";
	Boolean nodeAddressesSet = false;

	/*
	 * Data from config provided by launcher through command line arguments are
	 * all used to initialize node variables. Node's label value is assigned and
	 * token is generated.
	 */
	void parseFile() {
		int noNodesSet = 0;
		Main.noNodes = -1;
		Main.myNode = null;
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
				if (Main.noNodes < 0 && parts.length == 4) {
					Main.noNodes = Integer.parseInt(parts[0].trim());
					Main.requestDelay = Integer.parseInt(parts[1].trim());
					Main.csExecutionTime = Integer.parseInt(parts[2].trim());
					Main.totalNoOfRequests = Integer.parseInt(parts[3].trim());
					continue;
				}
				if (noNodesSet < Main.noNodes) {
					if (parts.length == 3) {
						Node node = new Node(parts[0].trim(), parts[1].trim(), parts[2].trim());
						Main.nodeMap.put(node.getId(), node);
						noNodesSet++;
						continue;
					}
				}
				if (noNodesSet == Main.noNodes && Main.myNode == null)
					Main.myNode = Main.nodeMap.get(this.myId);
				if (parts.length > 0 && parts[0].trim().startsWith(myId)) {
					int quorumSize = parts.length;
					for (int k = 1; k < quorumSize; k++) {
						Main.myNode.quorumList.add(parts[k].trim());
					}
				} else
					continue;
			}
//			printAll();
		} catch (IOException e) {
			System.out.println("Config - File exception");
			e.printStackTrace();
		}
	}

	void printAll() {
		System.out.println("MyNode : \t" + Main.myNode.id + " \t " + Main.myNode.hostName + "\t" + Main.myNode.portNo);
		System.out.println("All Node Details : \n");
		for (String id : Main.nodeMap.keySet()) {
			Node node = Main.nodeMap.get(id);
			System.out.println("\t" + node.id + " \t " + node.hostName + "\t" + node.portNo + "\n");
		}
		System.out.println("Quorum : ");
		for(String id : Main.myNode.quorumList){
			System.out.println(id + "\t");
		}
		System.out.println();

	}

}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ConfigParser {

	public ConfigParser(String myId) {
		this.myId = myId;
	}

	String myId = "";
	 String directory = "/home/010/s/sx/sxk159231/CS6378/AOSProj1/config.txt";
//	String directory = "C:/Users/Sindhura/AdvancedOperatingSystem/AOSProj1/src/config.txt";
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
				if (x > 1) {
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
				if (Main.noNodes < 0 && parts.length == 1) {
					Main.noNodes = Integer.parseInt(parts[0].trim());
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
					int totalNoNodes = parts.length;
					List<Node> path = new ArrayList<>();
					for (int i = 1; i < totalNoNodes; i++) {
						if (parts[i].startsWith("(")) {
							parts[i] = parts[i].substring(parts[i].indexOf("(") + 1).trim();
						} else if (parts[i].endsWith(")")) {
							parts[i] = parts[i].substring(0, parts[i].indexOf(")")).trim();
						}
						if (parts[i].endsWith(",")) {
							parts[i] = parts[i].substring(0, parts[i].indexOf(",")).trim();
						}
						if (parts[i].length() < 1)
							continue;
						path.add(Main.nodeMap.get(parts[i].trim()));
					}
					List<Node> tokenPath = new ArrayList<>();
					tokenPath.add(Main.myNode);
					for (int i = path.size() - 1; i >= 0; i--) {
						tokenPath.add(path.get(i));
					}
					Main.myToken = new Token(Main.myNode, false, tokenPath, Main.labelValue);

				} else
					continue;
			}
			printAll();
		} catch (IOException e) {
			System.out.println("Config - File exception");
			e.printStackTrace();
		}
	}

	void printAll() {
		System.out.println("MyNode : \t" + Main.myNode.id + " \t " + Main.myNode.hostName + "\t" + Main.myNode.portNo);
	/*	System.out.println("All Node Details : \n");
		for (String id : Main.nodeMap.keySet()) {
			Node node = Main.nodeMap.get(id);
			System.out.println("\t" + node.id + " \t " + node.hostName + "\t" + node.portNo);
		}
		System.out.println("Path : ");
		for (Node node : Main.myToken.path) {
			System.out.print(node.id + "\t");
		}*/
	}

}


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

	// Server thread that listens to this node's port
	public static Server server;

	// Total number of nodes in the network connected to this node
	public static Integer noNodes;

	// The Node object of this node - contains port number and hostname
	public static Node myNode;

	public static Integer csExecutionTime = 0, requestDelay = 0, totalNoOfRequests = 0;

	// Map of all nodes with their identifiers as keys
	public static HashMap<String, Node> nodeMap = new HashMap<>();

	// Client object for this node
	public static Client client;

	public static Integer noOfRequestsMade = 0;

	public static String grantedToNode = "";
	
	public static Boolean finalRun=true;

	public static void main(String[] args) throws IOException {
		try {
			ConfigParser parser = new ConfigParser(args[0]);
			parser.parseFile();
			server = new Server(myNode.getHostName(), myNode.getPortNo());
			server.start();
			printQuorumMembers();
			Thread.sleep(7000);
			Client.requestCS();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int timestamp = 0;

	public static boolean lock = false;

	public static List<Request> requestQueue = new ArrayList<>();

	public static List<String> grantsReceived = new ArrayList<>();

	public static List<String> failsSent = new ArrayList<>();

	private static void printQuorumMembers() {
		StringBuilder s = new StringBuilder();
		for (String id : Main.myNode.getQuorumList()) {
			s.append(id + ",");
		}
		Utils.log("Quorum Members: " + s);
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

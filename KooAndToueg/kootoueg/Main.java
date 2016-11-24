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

	// The Node object of this node - contains port number and hostname
	public static Node myNode;

	// Map of all nodes with their identifiers as keys
	public static HashMap<String, Node> nodeMap = new HashMap<>();

	// Client object for this node
	public static Client client;
	
	public static List<String> vectorClock = new ArrayList<>();	
	
	public static Boolean isFinalRun = true;
	
	public static HashMap<String, Boolean> receivedGrants = new HashMap<>();

	public static void main(String[] args) throws IOException {
		try {
			ConfigParser parser = new ConfigParser(args[0]);
			parser.parseFile();
			setupVectors();
			server = new Server(myNode.getHostName(), myNode.getPortNo());
			server.start();
			Thread.sleep(7000);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setupVectors(){
		
	}
	
	
	public static int timestamp=0;
	
	public static boolean lock=false;
		
	public static List<String> grantsReceived = new ArrayList<>();
	
	public static List<String> failsSent = new ArrayList<>();

	/*
	 * Kills the server thread and closes server socket
	 */
	@SuppressWarnings("deprecation")
	public static void killServer() {
		server.stop();
		server.destroy();
	}

}


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Collections;

import com.sun.nio.sctp.AbstractNotificationHandler;
import com.sun.nio.sctp.AssociationChangeNotification;
import com.sun.nio.sctp.AssociationChangeNotification.AssocChangeEvent;
import com.sun.nio.sctp.HandlerResult;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;
import com.sun.nio.sctp.ShutdownNotification;

public class Server extends Thread {

	// Server socket
	SctpServerChannel ssc;

	// This node's host name
	String hostName;

	// This node's port number
	Integer portNo;

	public Server(String hostName, Integer portNo) {
		this.hostName = hostName;
		this.portNo = portNo;
	}

	/*
	 * Starts listening on the port. Handles the received tokens appropriately.
	 */
	@Override
	public void run() {
		InetSocketAddress serverAdd = new InetSocketAddress(Main.myNode.getHostName(), Main.myNode.getPortNo());
		try {
			ssc = SctpServerChannel.open();
			ssc.bind(serverAdd);
			Utils.log("Server started");
			while (true) {
				SctpChannel sc = ssc.accept();
				ByteBuffer bytebuf = ByteBuffer.allocate(10000);
				AssociationHandler assocHandler = new AssociationHandler();
				sc.receive(bytebuf, System.out, assocHandler);
				bytebuf.flip();
				if (bytebuf.remaining() > 0) {
					ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytebuf.array()));
					Message message = (Message) ois.readObject();
					handleMessage(message);
					Main.timestamp++;
					ois.close();
				}
				sc.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Server thread quit");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Takes appropriate action against received message
	 */
	private void handleMessage(Message message) {
		Utils.log("Received " + message.getMessageType().name() + " from " + message.getOriginNode());
		switch (message.getMessageType()) {

		case REQUEST:
			// Morph this request to put it into the request queue
			Request request = new Request(message.getOriginNode(), message.getTimestamp());

			// Add the new request to the queue and sort the request queue
			Main.requestQueue.add(request);
			Collections.sort(Main.requestQueue);

			// If GRANT hasn't been given to any node, then send a GRANT to
			// requesting node
			grantTopRequest();
			break;

		case GRANT:

			// Add to grantsReceived
			Main.grantsReceived.add(message.getOriginNode());
			// If GRANT has been received from all quorum members, enter
			// critical section
			if (Main.grantsReceived.size() == Main.myNode.quorumList.size()) {
				MutualExclusion.enterCS();
			}
			break;

		case RELEASE:

			// Remove the corresponding request from the requestQueue
			for (int i = 0; i < Main.requestQueue.size(); i++) {
				Request req = Main.requestQueue.get(i);
				if (req.getRequestingNode().equals(message.getOriginNode())) {
					Main.requestQueue.remove(i);
					break;
				}
			}

			// Update grantedTo node
			if (Main.grantedToNode.equals(message.getOriginNode())) {
				Main.grantedToNode = "";
			}

			grantTopRequest();
			break;

		default:
			break;

		}
		//Utils.printMessageDetails(message);
	}

	/*
	 * Send GRANT to request with least timestamp
	 */
	private void grantTopRequest() {
		if (Main.requestQueue.size() > 0 && Main.grantedToNode.length() == 0) {
			String nodeId = Main.requestQueue.get(0).getRequestingNode();
			Collections.sort(Main.requestQueue);
			if (Main.requestQueue.size() > 0 && Main.grantedToNode.length() == 0) {
				Client.constructMessage(nodeId, Message.Type.GRANT);
				Main.grantedToNode = nodeId;
			}
		}
	}

	/*
	 * Close the server socket
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void destroy() {
		super.destroy();
		try {
			ssc.close();
		} catch (IOException e) {
			Utils.log("Closed socket");
			e.printStackTrace();
		}
	}

	/*
	 * Handles associations with the Server's socket
	 */
	static class AssociationHandler extends AbstractNotificationHandler<PrintStream> {
		public HandlerResult handleNotification(AssociationChangeNotification not, PrintStream stream) {
			if (not.event().equals(AssocChangeEvent.COMM_UP)) {
			}
			return HandlerResult.CONTINUE;
		}

		public HandlerResult handleNotification(ShutdownNotification not, PrintStream stream) {
			return HandlerResult.RETURN;
		}
	}

}

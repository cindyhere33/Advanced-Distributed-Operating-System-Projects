

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

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
					Token token = (Token) ois.readObject();
					handleToken(token);
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
	 * Takes appropriate action against received token
	 */
	private void handleToken(Token token) {
		if (token.isTerminationMessage()) {
			Utils.log("Termination message from " + token.getOriginNode().getId());
			discardToken(token);
		} else if (token.isPathComplete()) {
			Utils.log("Announcing termination to all nodes");
			Utils.log("Label value : " + Main.labelValue + "; Sum = " + token.getSum());
			announceTermination(token);
		} else {
			addToSumAndPassToken(token);
		}
	}

	/*
	 * Updates the token's sum by adding this node's label value to it. Passes
	 * it on to the next node on the token's path
	 */
	private void addToSumAndPassToken(Token token) {
		if (token.getPath().get(token.getPath().size() - 1).getId().equalsIgnoreCase(Main.myNode.getId())) {
			token.setSum(token.getSum() + Main.labelValue);
			token.getPath().remove(token.getPath().size() - 1);
		} else {
			Utils.log(token.getOriginNode().getId() + "'s token received in error");
		}
		Client.sendToken(token);
	}

	/*
	 * Called when this node's token returns back to itself and it broadcasts a
	 * message to all other nodes indicating it wishes to terminate. isComplete
	 * is set to true.
	 */
	private void announceTermination(Token token) {
		Main.isComplete = true;
		Utils.writeToFile_termination(token);
		token.setTermination(true);
		Client.broadcast(token, Main.nodeMap);
	}

	/*
	 * Called when a message is received from neighboring node indicating it
	 * wishes to terminate. NoOfTerminatedProcesses is incremented by 1. If such
	 * a message has been received from all neighbouring processes and this
	 * node's token path is also completely traversed, this node kills its
	 * server thread and exits.
	 */
	private void discardToken(Token token) {
		if (!token.getOriginNode().getHostName().equals(Main.myNode.getHostName())) {
			Main.isProcessTerminated.put(token.getOriginNode().getId(), true);
			Main.noOfTerminatedProcesses++;
			if (Main.noOfTerminatedProcesses >= (Main.noNodes - 1) && Main.isComplete) {
				Utils.log("All processes terminated");
				Main.killServer();
				System.exit(0);
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

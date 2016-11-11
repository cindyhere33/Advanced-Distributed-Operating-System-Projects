
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class Client {

	/*
	 * Request critical section to all quorum members including itself. Added
	 * itself to its quorum list
	 */
	public static void requestCS() {
		if (Main.noOfRequestsMade > Main.totalNoOfRequests) {
			Utils.log("Not requesting CS anymore");
			return;
		}
		Utils.log("Requesting CS to quorum members with timestamp : " + Main.timestamp + "\n Request number: "
				+ Main.noOfRequestsMade + "\n");
		for (String nodeId : Main.myNode.quorumList) {
			Message message = new Message(Main.myNode.getId(), nodeId, Message.Type.REQUEST, Main.timestamp);
			sendMessage(message);
		}
		Main.noOfRequestsMade++;
		// update timestamp
		Main.timestamp++;
	}

	public static void constructMessage(String nodeId, Message.Type type) {
		sendMessage(new Message(Main.myNode.getId(), nodeId, type, Main.timestamp));
		Main.timestamp++;
	}

	public static void sendRelease() {
		for(String key : Main.receivedGrants.keySet()){
			Main.receivedGrants.put(key, false);
		}
		for (String id : Main.myNode.quorumList) {
			Client.constructMessage(id, Message.Type.RELEASE);
		}
	}

	/*
	 * Sends token to the next node in the token's path. Also writes to the
	 * respective token's file the path traversed s far
	 */
	public static void sendMessage(Message message) {
		Node nextNode = Main.nodeMap.get(message.getDestinationNode());
		Utils.log("Sent " + message.getMessageType() + " to -> " + nextNode.getId() + "\n");
		InetSocketAddress socketAddr = new InetSocketAddress(nextNode.getHostName(), nextNode.getPortNo());
		sendToSocket(socketAddr, message);
	}

	/*
	 * Sends the token to the address provided.
	 */
	public static void sendToSocket(InetSocketAddress socketAddr, Message message) {
		SctpChannel sc = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			ByteBuffer buf = ByteBuffer.allocateDirect(10000);
			oos.writeObject(message);
			oos.flush();
			buf.put(baos.toByteArray());
			sc = SctpChannel.open(socketAddr, 0, 0);
			buf.flip();
			sc.send(buf, MessageInfo.createOutgoing(socketAddr, 1));
			oos.close();
			sc.close();
		} catch (IOException e) {
			try {
				Utils.log("Exception occured while sending to socket");
				sc.close();
			} catch (IOException e1) {
				Utils.log("Client closed");
				e1.printStackTrace();
			}
			Utils.log("Exception occured while sending to socket");
			e.printStackTrace();
		}
	}

}

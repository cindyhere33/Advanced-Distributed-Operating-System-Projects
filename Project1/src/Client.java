

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class Client {

	static SctpChannel sc;

	/*
	 * Sends token to the next node in the token's path. Also writes to the
	 * respective token's file the path traversed s far
	 */
	public static void sendToken(Token token) {
		Node nextNode = token.getPath().get(token.getPath().size() - 1);
		Utils.log("Passing " + token.getOriginNode().getId() + "\'s token to :  " + nextNode.getId());
		Utils.writeToFile("TokenPath_" + token.getOriginNode().getId(), nextNode.getId() + " > ");
		InetSocketAddress socketAddr = new InetSocketAddress(nextNode.getHostName(), nextNode.getPortNo());
		sendToSocket(socketAddr, token);
	}

	/*
	 * Sends the token to the address provided.
	 */
	public static void sendToSocket(InetSocketAddress socketAddr, Token token) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			ByteBuffer buf = ByteBuffer.allocateDirect(10000);
			oos.writeObject(token);
			oos.flush();
			buf.put(baos.toByteArray());
			sc = SctpChannel.open(socketAddr, 0, 0);
			buf.flip();
			sc.send(buf, MessageInfo.createOutgoing(socketAddr, 1));
			oos.close();
			sc.close();
		} catch (IOException e) {
			try {
				sc.close();
			} catch (IOException e1) {
				Utils.log("Client closed");
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	/*
	 * Called when termination is announced. Broadcasts the token to all the
	 * neighboring nodes in the network
	 */
	public static void broadcast(Token token, HashMap<String, Node> neighbours) {
		for (String host : neighbours.keySet()) {
			if (neighbours.get(host).getId().equalsIgnoreCase(Main.myNode.getId()))
				continue;
			InetSocketAddress socketAddr = new InetSocketAddress(neighbours.get(host).getHostName(),
					neighbours.get(host).getPortNo());
			sendToSocket(socketAddr, token);
		}
	}

}

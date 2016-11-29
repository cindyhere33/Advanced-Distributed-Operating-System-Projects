package kootoueg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

public class Client {

	public static void sendMessage() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (!Main.checkpointingInProgress) {
					if (Main.msgCount >= Main.totalNoOfMsgs) {
						return;
					}
					Main.msgCount++;
					Message msg = new Message(Main.myNode.getId(),
							Main.myNode.neighbours.get(new Random().nextInt(Main.myNode.neighbours.size())),
							Main.msgCount, TypeOfMessage.APPLICATION, Main.myNode.getId(),
							Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
					sendMessage(msg);
					Utils.updateVectors(EventType.SEND_MSG, msg);
				}
			}
		}, 1000, Utils.getExponentialDistributedValue(Main.sendDelay));
	}

	/*
	 * Sends token to the next node in the token's path. Also writes to the
	 * respective token's file the path traversed s far
	 */
	public static void sendMessage(Message message) {
		Node nextNode = Main.nodeMap.get(message.getDestinationNode());
//		Utils.log("Sent " + message.getMessageType() + " to -> " + nextNode.getId() + "\n");
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

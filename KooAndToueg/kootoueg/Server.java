package kootoueg;

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

import kootoueg.Main.EventType;
import kootoueg.Main.VectorType;
import kootoueg.Message.TypeOfMessage;

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

	private void handleMessage(Message message) {
		switch (message.getMessageType()) {
		case APPLICATION:
			Utils.updateVectors(EventType.RECEIVE_MSG, message);
			break;
		case CHECKPOINT_INITIATION:
			Main.checkpointingInProgress = true;
			Utils.updateVectors(EventType.CHECKPOINT, null);
			if (Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][message.getOriginNode()] > -1
					&& Main.vectors[VectorType.FIRST_LABEL_SENT.ordinal()][message.getOriginNode()] < message.getLabel()
					&& Main.temporaryCheckpoint == null) {
				Main.temporaryCheckpoint = new Checkpoint(Main.checkpointSequenceNumber + 1, Main.vectors, false);
			}
			Message msg = new Message(Main.myNode.getId(), message.getOriginNode(), 0, TypeOfMessage.CHECKPOINT_OK,
					message.getOriginNode(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
			Client.sendMessage(msg);
			break;
		case RECOVERY:
			Utils.updateVectors(EventType.RECOVERY, message);
			break;
		case CHECKPOINT_OK:
			Main.checkpointConfirmationsReceived.put(message.getOriginNode(), true);
			if (Main.checkpointConfirmationsReceived.size() == Main.myNode.neighbours.size()) {
				CheckpointingUtils.makeCheckpointPermanent();
				for (Integer id : Main.myNode.neighbours) {
					Client.sendMessage(new Message(Main.myNode.getId(), id, 0, TypeOfMessage.CHECKPOINT_FINAL,
							Main.myNode.getId(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]));
				}
			}
			break;
		case CHECKPOINT_FINAL:
			CheckpointingUtils.makeCheckpointPermanent();
			CheckpointingUtils.initiateCheckpointingIfMyTurn();
			break;
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

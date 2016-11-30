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

	private synchronized void handleMessage(Message message) {
		// Utils.log("Received " + message.getMessageType().name() + " from " +
		// message.getOriginNode() + " with label "
		// + message.getLabel());
		switch (message.getMessageType()) {
		case APPLICATION:
			Utils.updateVectors(EventType.RECEIVE_MSG, message);
			break;
		case CHECKPOINT_INITIATION:
			if (Main.checkpointingInProgress)
				break;
			if (CheckpointingUtils.needsToTakeCheckpoint(message.getOriginNode(), message.getLabel())) {
				Main.checkpointingInProgress = true;
				CheckpointingUtils.hasSentCheckpointingRequests();
				Message msg = new Message(Main.myNode.getId(), message.getOriginNode(), 0, TypeOfMessage.CHECKPOINT_OK,
						message.getOriginNode(), Main.vectors[VectorType.VECTOR_CLOCK.ordinal()]);
				Client.sendMessage(msg);
			} else {
				Message msg = new Message(Main.myNode.getId(), message.getOriginNode(), 0,
						TypeOfMessage.CHECKPOINT_NOT_NEEDED, message.getOriginNode(), null);
				Client.sendMessage(msg);
			}
			break;
		case RECOVERY_INITIATION:
			if (RecoveryUtils.needsToRollback(message.getOriginNode(), message.getLabel())) {
				RecoveryUtils.rollback();
				Message msg = new Message(Main.myNode.getId(), message.getOriginNode(), 0, TypeOfMessage.RECOVERY_OK,
						message.getOriginNode(), null);
				Client.sendMessage(msg);
			} else {
				Message msg = new Message(Main.myNode.getId(), message.getOriginNode(), 0,
						TypeOfMessage.RECOVERY_NOT_NEEDED, message.getOriginNode(), null);
				Client.sendMessage(msg);
			}
			break;
		case CHECKPOINT_OK:
			Main.confirmationsPending.put(message.getOriginNode(), true);
			boolean allConfirmationsReceived = true;
			for (Integer id : Main.confirmationsPending.keySet()) {
				if (!Main.confirmationsPending.get(id))
					allConfirmationsReceived = false;
			}

			if (allConfirmationsReceived) {
				if (Main.checkpointRecoverySequence.size() > 0) {
					Main.checkpointRecoverySequence.remove(0);
				}
				CheckpointingUtils.makeCheckpointPermanent();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			}
			break;
		case CHECKPOINT_FINAL:
			if (Main.checkpointRecoverySequence.size() > 0
					&& message.getLabel() < Main.checkpointRecoverySequence.size()) {
				Main.checkpointRecoverySequence.remove(0);
				CheckpointingUtils.makeCheckpointPermanent();
				Main.initiateCheckpointOrRecoveryIfMyTurn();
			}
			break;
		case CHECKPOINT_NOT_NEEDED:
			if (Main.confirmationsPending.containsKey(message.getOriginNode())) {
				Main.confirmationsPending.remove(message.getOriginNode());
			}
			break;
		case RECOVERY_CONCLUDED:
			break;
		case RECOVERY_NOT_NEEDED:
			if (Main.confirmationsPending.containsKey(message.getOriginNode())) {
				Main.confirmationsPending.remove(message.getOriginNode());
			}
			break;
		case RECOVERY_OK:
			
			break;
		default:
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

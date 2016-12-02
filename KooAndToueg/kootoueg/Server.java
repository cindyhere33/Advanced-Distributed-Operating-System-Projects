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
					Main.handleMessage(message);
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

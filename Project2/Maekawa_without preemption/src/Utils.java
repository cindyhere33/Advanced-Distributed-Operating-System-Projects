
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

	static String filePath = "/home/010/s/sx/sxk159231/CS6378/Project2/Project2_AOS/Outputs/";

	/*
	 * Prints msg to console
	 */
	public static void log(String msg) {
		if(Main.isFinalRun){
			if(!(msg.contains("Enter") || msg.contains("Exit"))){
				return;
			}
		}
		System.out.println(Main.myNode.getId() + " : " + msg);
	}

	/*
	 * Writes to file with filename the msg
	 */
	public static void writeToFile(String filename, String content) {
		File file = new File(filePath + filename);
		file.getParentFile().mkdirs();
		try (FileOutputStream fop = new FileOutputStream(file, true)) {
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] contentInBytes = content.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void printMessageDetails(Message message) {
		StringBuilder s;
		switch (message.getMessageType()) {
		case REQUEST:
		case RELEASE:
			s = new StringBuilder();
			for (Request req : Main.requestQueue) {
				s.append("(" + req.nodeId + ", " + req.timestamp + "), ");
			}
			Utils.log("\nReceived " + message.getMessageType() + " from " + message.getOriginNode() + ";\nTimestamp : "
					+ message.getTimestamp() + "\nUpdated REQUEST QUEUE  : " + s + "\n");
			break;
		case GRANT:
			s = new StringBuilder();
			for (String id : Main.grantsReceived) {
				s.append(id + ", ");
			}
			Utils.log("\nReceived " + message.getMessageType() + " from " + message.getOriginNode() + ";\nTimestamp : "
					+ message.getTimestamp() + "\nUpdated GRANTS RECEIVED  : " + s + "\n");
			break;
		}
	}

}

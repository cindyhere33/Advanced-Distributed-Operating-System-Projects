
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

	static String filePath = "/home/010/s/sx/sxk159231/CS6378/AOSProj1/Outputs/";

	/*
	 * Writes the output of the node to respective file
	 */
	public static void writeToFile_termination(Token token) {
		File file = new File(filePath + "Output" + Main.myNode.getId() + "_");
		file.getParentFile().mkdirs();
		String content = "My label value :  " + Main.labelValue + "\n" + token.getSum();
		try (FileOutputStream fop = new FileOutputStream(file)) {
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] contentInBytes = content.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			Utils.log("SUCCESS : Token written to file " + file.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			Utils.log("FAIL : Token write to file");
		}
	}

	/*
	 * Prints msg to console
	 */
	public static void log(String msg) {
		System.out.println(Main.myNode.getId() + " : " + msg);
	}

	/*
	 * Writes to file with filename the msg
	 */
	public static void writeToFile(String filename, String msg) {
		File file = new File(filePath + filename);
		file.getParentFile().mkdirs();
		try (FileOutputStream fop = new FileOutputStream(file, true)) {
			if (!file.exists()) {
				file.createNewFile();
			}
			byte[] contentInBytes = msg.getBytes();
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

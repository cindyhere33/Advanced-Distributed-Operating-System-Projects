package kootoueg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

	static String filePath = "/home/010/s/sx/sxk159231/CS6378/Project3/KooAndToueg/";

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
	
	public static long getExponentialDistributedValue(int value){
		return (long) Math.floor((-value * Math.log(Math.random())));
	}

}

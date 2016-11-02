import java.text.SimpleDateFormat;
import java.util.Date;

public class MutualExclusion {

	private static boolean lock = false;

	static SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm:ss.SSS");

	public static void enterCS() {
		lock = true;

		Utils.log("Enter\t" + sdf.format(new Date())+"\n");
		double time = System.currentTimeMillis() + Math.floor((-Main.csExecutionTime * Math.log(Math.random())));
		while (System.currentTimeMillis() <= time && lock) {

		}
		if (lock)
			exitCS();
	}

	public static void exitCS() {
		Utils.log("Exit\t" + sdf.format(new Date())+"\n");
		if (lock) {
			Client.sendRelease();
		} else {
			Main.noOfRequestsMade--;
		}
		lock = false;
		try {
			Thread.sleep((long) Math.floor((-Main.requestDelay * Math.log(Math.random()))));
			Client.requestCS();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static boolean isExectingCS() {
		return lock;
	}
}

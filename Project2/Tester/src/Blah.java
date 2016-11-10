import java.util.Date;

public class Blah implements Comparable<Blah> {

	public enum Action{
		ENTER, EXIT
	}
	Date time;
	Action action;
	String id;
	String line;
	
	@Override
	public int compareTo(Blah o) {
		if (this.time.before(o.time))
			return -1;
		if (this.time.after(o.time))
			return 1;
		if(this.action==Action.EXIT) return -1;
		return 1;
	}

	public Blah(Date date, String id, Action action, String line) {
		this.time = date;
		this.action = action;
		this.id = id;
		this.line = line;
	}

}

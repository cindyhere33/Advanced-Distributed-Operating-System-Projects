import java.util.Date;

public class Entry implements Comparable<Entry> {
	Date date;
	Node node;

	public Entry(Date date, Node node) {
		this.date = date;
		this.node = node;
	}

	@Override
	public int compareTo(Entry o) {
		if (this.date.before(o.date)) {
			return -1;
		} else if (this.date.after(o.date)) {
			return 1;
		}
		return 0;
	}

}

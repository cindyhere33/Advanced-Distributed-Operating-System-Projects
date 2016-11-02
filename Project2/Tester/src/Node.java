import java.util.Date;

public class Node {

	String nodeId = "";
	Date enterTime = null;
	Date exitTime = null;
	boolean complete = false;

	public Node(String nodeId, Date enterTime, Date exitTime) {
		this.nodeId = nodeId;
		this.enterTime = enterTime;
		this.exitTime = exitTime;
	}

}

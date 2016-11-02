public class Request implements Comparable<Request>{//Comparator<Request> {

	String nodeId;
	int timestamp;
	
	public Request(String nodeId, int timestamp){
		this.nodeId = nodeId;
		this.timestamp = timestamp;
	}

	public String getRequestingNode() {
		return nodeId;
	}

	public int getTimestamp() {
		return timestamp;
	}

	@Override
	public int compareTo(Request o) {
		if(this.timestamp>o.timestamp) return 1;
		else if(this.timestamp==o.timestamp) {
			if(Integer.parseInt(this.nodeId)>Integer.parseInt(o.nodeId)){
				return 1;
			}
		}
		return -1;
	}
		
}

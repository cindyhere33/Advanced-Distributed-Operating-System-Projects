

import java.io.Serializable;

public class Node implements Serializable {

	private static final long serialVersionUID = 22L;
	String id;
	String hostName;
	String portNo;

	public Node(String id, String hostname, String portNo) {
		this.id = id;
		this.hostName = hostname;
		this.portNo = portNo;
	}

	public String getId() {
		return id;
	}

	public String getHostName() {
		return hostName;
	}

	public Integer getPortNo() {
		return Integer.parseInt(portNo);
	}

}

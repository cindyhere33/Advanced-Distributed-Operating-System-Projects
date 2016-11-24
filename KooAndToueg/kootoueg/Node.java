package kootoueg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Node implements Serializable {

	private static final long serialVersionUID = 22L;
	String id;
	String hostName;
	String portNo;
	List<String> quorumList = new ArrayList<>();

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

	public List<String> getQuorumList() {
		return quorumList;
	}

	public void setQuorumList(List<String> quorumList) {
		this.quorumList = quorumList;
		boolean flag = false;
		for(String id : quorumList){
			if(id.equals(this.id)){
				flag = true; break;
			}
		}
		if(!flag) this.quorumList.add(this.id);
		Collections.sort(this.quorumList, new ComparatorOfNumericString());
	}
	
	class ComparatorOfNumericString implements Comparator<String>{

	    public int compare(String string1, String string2) {
	        return Integer.parseInt(string1)-Integer.parseInt(string2);
	    }
	}

}

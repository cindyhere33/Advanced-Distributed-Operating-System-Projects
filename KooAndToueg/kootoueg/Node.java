package kootoueg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node implements Serializable {

	private static final long serialVersionUID = 22L;
	Integer id;
	String hostName;
	String portNo;
	List<Integer> neighbours = new ArrayList<>();

	public Node(Integer id, String hostname, String portNo) {
		this.id = id;
		this.hostName = hostname;
		this.portNo = portNo;
	}

	public Integer getId() {
		return id;
	}

	public String getHostName() {
		return hostName;
	}

	public Integer getPortNo() {
		return Integer.parseInt(portNo);
	}

	public void setNeighbourList(List<Integer> neighbourList) {
		this.neighbours = neighbourList;
		boolean flag = false;
		for(Integer id : neighbourList){
			if(id.equals(this.id)){
				flag = true; break;
			}
		}
		if(!flag) this.neighbours.add(this.id);
		Collections.sort(this.neighbours);
	}
	
	public List<Integer> getNeighbours(){
		return neighbours;
	}
	
	

}

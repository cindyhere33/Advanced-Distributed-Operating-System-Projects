

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Token implements Serializable {

	private static final long serialVersionUID = 2L;

	// The path to be traversed by the token in reverse order. Ends with the
	// node that generated the token.
	List<Node> path = new ArrayList<>();

	// The node that generated this token
	Node originNode;

	// Sum computed by the token
	Integer sum = 0;

	// Is this token a control message indicating termination?
	Boolean termination = false;

	public Token(Node node, Boolean isTerminator, List<Node> tokenPath, Integer sum) {
		this.originNode = node;
		this.termination = isTerminator;
		this.path = tokenPath;
		this.sum = sum;
	}

	/*
	 * Adds current node's label value to the sum
	 */
	public void addValue(Integer nodeLabel) {
		sum += nodeLabel;
	}

	/*
	 * Removes last node from the path
	 */
	public void stripLastNode() {
		path.remove(path.size() - 1);
	}

	/*
	 * Returns true if the path on this token is completely traversed
	 */
	public boolean isPathComplete() {
		return (path.size() == 1 && Main.myNode.getHostName().equals(path.get(0).getHostName()));
	}

	/*
	 * Returns the path printed on this token
	 */
	public List<Node> getPath() {
		return path;
	}

	public void setPath(List<Node> path) {
		this.path = path;
	}

	public Node getOriginNode() {
		return originNode;
	}

	public void setOriginNode(Node originNode) {
		this.originNode = originNode;
	}

	public Integer getSum() {
		return sum;
	}

	public void setSum(Integer sum) {
		this.sum = sum;
	}

	public Boolean isTerminationMessage() {
		return termination;
	}

	public void setTermination(Boolean termination) {
		this.termination = termination;
	}

}

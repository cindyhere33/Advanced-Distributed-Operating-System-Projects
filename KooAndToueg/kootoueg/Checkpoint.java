package kootoueg;

public class Checkpoint {

	Integer[][] vectors = new Integer[4][Main.myNode.neighbours.size()];
	int sequenceNumber=0;
	boolean isRecovery = false;
	
	public Checkpoint(int sequenceNumber, Integer[][] vectors,  boolean isRecovery){
		for(int i=0;i<Main.myNode.neighbours.size(); i++){
			System.arraycopy(vectors[i], 0, this.vectors[i], 0, Main.myNode.neighbours.size());
		}
		this.sequenceNumber=sequenceNumber;
		this.isRecovery = isRecovery;
	}
	
	public boolean isRecovery(){
		return isRecovery;
	}

	public Integer[][] getVectors() {
		return vectors;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	
}

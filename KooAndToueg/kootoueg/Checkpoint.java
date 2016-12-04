package kootoueg;

public class Checkpoint {

	Integer[][] vectors = new Integer[4][Main.noNodes];
	int sequenceNumber = 0;

	public Checkpoint(int sequenceNumber, Integer[][] vectors) {
		for (int i = 0; i < 4; i++) {
			System.arraycopy(vectors[i], 0, this.vectors[i], 0, Main.noNodes);
		}
		this.sequenceNumber = sequenceNumber;
	}

	public Integer[][] getVectors() {
		return vectors;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

}

package kootoueg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Test {
	public static void main(String args[]) throws Exception {
		String fileName = "C:/Users/Sindhura/Documents/Subjects/Advanced Operating System/Projects/KooAndToueg/out.txt";
		String line = "";
		int[][] VectorClocks = new int[5][5];
		String[] temp = new String[5];
		int checkP = 0;
		int numNodes = 0;

		try {

			FileReader fileReader = new FileReader(fileName);

			BufferedReader bufferedReader = new BufferedReader(fileReader);
			int i = 0;

			while ((line = bufferedReader.readLine()) != null) {
				if (line.trim().isEmpty() || line.equals("") || line.contains("Server started") || line.contains(
						"----") /*
								 * || line.contains("CHECKPOINTING INITIATED")
								 */)
					continue;
				else {
					if (line.contains(" INITIATED")) {
						checkP = Integer.parseInt(line.trim().split(" ")[0]);
						numNodes = 0;
					}
					if (line.contains("Checkpoint number")) {
						i = Integer.parseInt(line.trim().split(" ")[0]);
					} else if (line.contains("VECTOR CLOCK")) {
						if (numNodes > 4)
							numNodes = 0;
						if ((line = bufferedReader.readLine()) != null) {
							temp = line.trim().split("\t");

							for (int j = 0; j < temp.length; j++) {
								VectorClocks[j][i] = Integer.parseInt(temp[j]);
							}

							if (numNodes == 4) {
								int cons = 0;

								outerloop: for (int k = 0; k < VectorClocks.length; k++) {
									for (int l = 0; l < VectorClocks.length; l++) {
										if (VectorClocks[k][l] > VectorClocks[k][k]) {
											System.out.println("Inconsistent Checkpoints!");
											System.out.println("Vector Clock value for:" + k + " process is: "
													+ VectorClocks[k][k]);
											System.out.println("Vector Clock value for:" + l + " process is: "
													+ VectorClocks[k][l]);
											System.out.println("Error in checkpointing: " + checkP);
											break outerloop;
										} else
											System.out.println("Consistent system!");
									}
									System.out.println();
									cons++;
								}
								System.out.println();
								/*
								 * 
								 * //System.out.print(VectorClocks[k][l]+" "); }
								 * //System.out.println(); cons++; }
								 */
								// System.out.println("This is where magic
								// happens");
							}
							numNodes++;
						} else {
							System.out.println("Error, incomplete log file");
							break;
						}
					}

				}
				// System.out.println();

			}

			bufferedReader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// System.out.println("Testing12345");
	}
}

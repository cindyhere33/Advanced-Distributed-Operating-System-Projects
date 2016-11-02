import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Parse {
	public static void main(String args[]) {
		String directory = "C:/Users/Sindhura/Documents/Subjects/Advanced Operating System/Projects/Project2_AOS/out.txt";
		FileReader fileReader;
		String line = "";
		Integer lineNo = 0;
		try {
			fileReader = new FileReader(directory);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			List<Entry> times = new ArrayList<>();
			do {
				line = bufferedReader.readLine();
				lineNo++;
				if (line == null)
					continue;
				if (line.split("\t").length < 1)
					continue;
				String words[] = line.split("\t");
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
				Date date = sdf.parse(words[2]);
				Node node;
				if (line.contains("Enter")) {
					node = new Node(words[0], date, null);
				} else {
					node = new Node(words[0], null, date);
				}
				times.add(new Entry(date, node));
			} while (line != null);
			bufferedReader.close();
			Collections.sort(times);
			List<Node> nodes = new ArrayList<>();
			int i = 0;
			for (Entry entry : times) {
				i++;
				if (entry.node.enterTime != null) {
					nodes.add(entry.node);
				} else if (entry.node.exitTime != null) {
					for (Node n : nodes) {
						if (n.enterTime.before(entry.node.exitTime)) {
							if (!n.nodeId.equals(entry.node.nodeId)) {
								System.out
										.println("Overlap! at line " + i + "; " + entry.node.nodeId + ", " + n.nodeId);
								return;
							}
						}
					}
					for (Node n : nodes) {
						if (n.nodeId.equals(entry.node.nodeId)) {
							nodes.remove(n);
							break;
						}
					}
				}
			}

		} catch (

		FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error: line = " + line);
		}

	}
}

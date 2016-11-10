import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class Parser {
	
	static String directory = "C:/Users/Sindhura/Documents/Subjects/Advanced Operating System/Projects/Project2/Maekawa_with_preemption/out.txt";
	static String configPath = "C:/Users/Sindhura/Documents/Subjects/Advanced Operating System/Projects/Project2/Maekawa_with_preemption/config.txt";
	
	HashMap<String, Date> nodeStatus = new HashMap<>(); 
	
	public static void main(String args[]) throws Exception{
		FileReader fileReader;
		String line = "";
		Integer lineNo = 0;
		try {
			fileReader = new FileReader(directory);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			line = bufferedReader.readLine();
			while(line!=null) {
				lineNo++;
				processLine(line);
				line = bufferedReader.readLine();
			}
			bufferedReader.close();
			fileReader.close();
			Collections.sort(times);
			checkForConflict();
		}catch(Exception e){
			System.out.println("Error");
			e.printStackTrace();
		}
	}
	
/*	static int getNumberOfProcesses(){
		int noOfProcesses=0;
		try {
			FileReader fileReader = new FileReader(configPath);
			BufferedReader configBufferedReader = new BufferedReader(fileReader);
			String line = configBufferedReader.readLine().trim();
			while(line!=null){
				if(line.length()>0){
					String[] parts = line.split(" ");
					noOfProcesses = Integer.parseInt(parts[0]);
					break;
				}
				line = configBufferedReader.readLine().trim();
			}
			configBufferedReader.close();
			fileReader.close();
			Collections.sort(times);
			checkForConflict();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return noOfProcesses;
	}
	*/
	static List<Blah> times = new ArrayList<>();
	
	static void processLine(String line){
		if(line==null || line.trim().length()==0 || line.split("\t").length<1) return;
		String words[] = line.split("\t");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		try {
			Date date = sdf.parse(words[2].trim());
			String id = words[0].trim();
			if(words[1].contains("Enter")) times.add(new Blah(date, id, Blah.Action.ENTER, line));
			else times.add(new Blah(date, id, Blah.Action.EXIT, line));	
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	static void checkForConflict(){
		boolean flag = false, overlap = false;
		for(Blah time: times){
			if(time.action==Blah.Action.ENTER) {
				if(flag) {
					overlap = true;
					System.out.println("Overlap detected :  Node " + time.id + " at time : " + time.time + "\nLine : " + time.line);
					break;
				}
				flag=true;
			}else{
				flag=false;
			}
		}
		if(!overlap) System.out.println("No overlaps");
	}
	
	
}

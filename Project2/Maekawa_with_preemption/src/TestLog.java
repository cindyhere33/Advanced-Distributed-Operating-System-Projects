import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Vibha on 11/1/2016.
 */
public class TestLog {

    public static void main(String [] args){

        String file = args[0];
        //int noNodes = Integer.parseInt(args[1]);
        int numEnter = 0;
        int numExit = 0;
        int flag = 0;
        int lineNo = 0;

        //check for simultaneous CS
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();

            while (line!=null){

                //System.out.println(line);
                lineNo++;
                if(line.contains("Enter")){
                    numEnter++;
                    int diff = numEnter - numExit;
                    if(Math.abs(diff) > 1) {
                        flag = -1;
                        System.out.println("Simultaneous critical section");
                        System.out.println(lineNo);
                        break;
                    }
                }
                else if(line.contains("Exit")){
                    numExit++;
                }

                line = bufferedReader.readLine();
            }

            if(flag == 0)
                System.out.println("No simultaneous execution!");

            bufferedReader.close();
            fileReader.close();

        }catch (IOException ex){
            System.out.println("Error reading the file!");
        }
    }
}

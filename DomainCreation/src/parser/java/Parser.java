package parser.java;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
public class Parser {

    public static void main(String[] args) throws IOException {
        System.out.println("Hello, World " +  " \n");
        
    parserFile();
        
        
        }
    
    public static void parserFile () throws IOException{
    	BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("test.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String line = null;
    	while ((line = reader.readLine()) != null) {
    	    
    		Boolean found;
    		
    		System.out.println(line);
    		found = line.contains("H");
    		System.out.println(found + " \n");
    		if( line.contains("{")) // need to make it { + new line
    			innerBracket(reader);
    		
    	}
    	reader.close();
    	
    }

    

	
	public static void innerBracket (BufferedReader reader) throws IOException
	{
		
		String line = null;
    	while ((line = reader.readLine()) != null) {
    		System.out.println("inbracket");
    		System.out.println(line);
    		if(line.contains("}"))
    			break;
    		String [] tokens = line.split("\\s+");
    	    String action = tokens[0]; // parses the action
    	    String sketch = tokens[1]; // parses the sketch
    	  //  String argument1 = tokens[2]; // parses the first 
    	   
    	    System.out.println("Action is: " + action);
    	    System.out.println("Sketch is: " + sketch);
    	   // System.out.println("Argument is: " + argument1);
    	    executeLine(tokens);
    	}
		
	}
	
	public static void executeLine (String[] tokens) throws IOException
	{
		
	}	}


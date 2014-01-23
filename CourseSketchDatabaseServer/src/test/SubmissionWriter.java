package test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class SubmissionWriter {
	public static void start() throws IOException {
		
		JFileChooser chooser = new JFileChooser();
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	        "Binary files", "dat");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(null);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	       System.out.println("You chose to open this file: " +
	            chooser.getSelectedFile().getName());
	    } else {
	    	return;
	    }
	    File directory = new File(chooser.getSelectedFile().getParent());
	    
	    
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB db = mongoClient.getDB("submissions");
		DBCollection collection = db.getCollection("Experiments");
		DBCursor cursor = collection.find();
		int numbers = cursor.count();
		System.out.println("Number of submissions: " + numbers);
		int i = 0;
		while (cursor.hasNext()) {
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(directory.getAbsolutePath() + "/" + i+".dat"));
			DBObject object = cursor.next();
			Object obj = object.get("UpdateList");
			byte[] bytes = (byte[]) obj;
			stream.write(bytes);
			stream.flush();
			stream.close();
			i++;
		}
	}
	
	public static void main(String args[]) {
		try {
			start();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

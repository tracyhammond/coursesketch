package test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import protobuf.srl.school.Assignment.SrlAssignment;
import protobuf.srl.school.Problem.SrlBankProblem;

import com.mongodb.Document;
import com.mongodb.DB;
import com.mongodb.MongoCollection<Document>;
import com.mongodb.DBCursor;
import com.mongodb.Document;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import coursesketch.database.auth.AuthenticationException;
import database.institution.Institution;

public class SubmissionWriter {
	public static void start() throws IOException, AuthenticationException, DatabaseAccessException {

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
		MongoCollection<Document> collection = db.getCollection("Experiments");
		MongoCursor<Document> cursor = collection.find().skip(2);
		int numbers = cursor.count();
		System.out.println("Number of submissions: " + numbers);

		// User Name DB things
		DB ldb = mongoClient.getDB("login");
		MongoCollection<Document> lcollection = ldb.getCollection("CourseSketchUsers");
		final String mastId = "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332";
		DB idb = mongoClient.getDB("Institution");
		int i = 0;
		while (cursor.hasNext()) {
			i++;
			Document object = cursor.next();
			BufferedOutputStream stream;

			Object uid = object.get("UserId");
			Document query = new Document("ServerId",uid);
			MongoCursor<Document> lcursor = lcollection.find(query);

			String userName = "null";
			if (lcursor.hasNext()) {
				Document result = lcursor.next();
				userName = result.get("UserName").toString();
			}


			String pid = object.get("CourseProblemId").toString();
			SrlProblem currentProblem;
			try {currentProblem =  Institution.mongoGetCourseProblem(toStringArray(pid), mastId).get(0);}
			catch (Exception e) {
				System.out.println(i + " " + e.getMessage());
				continue;
			}
			SrlAssignment currentAssignment = Institution.mongoGetAssignment(toStringArray(currentProblem.getAssignmentId()), mastId).get(0);


			stream = new BufferedOutputStream(new FileOutputStream(directory.getAbsolutePath() + "/" +  i + "_" + userName + "_" +
			currentAssignment.getName() + "_" + currentProblem.getName() + ".dat"));
//			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(directory.getAbsolutePath() + "/"
//			+ object.get("UserName").toString() + "_Assignemnt" + object.get("AssignmentId").toString() + "_Problem" + object.get("CourseProblemId").toString() +".dat"));
			Object obj = object.get("UpdateList");
			byte[] bytes = (byte[]) obj;
			stream.write(bytes);
			stream.flush();
			stream.close();

		}
	}


	private static ArrayList<String> toStringArray(String str) {
		ArrayList<String> strs = new ArrayList<String>();
		strs.add(str);
		return strs;
	}


	public static void main(String args[]) throws AuthenticationException, DatabaseAccessException {
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

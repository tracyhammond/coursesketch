package test;

import static database.StringConstants.*;

import java.net.UnknownHostException;
import java.util.ArrayList;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.submission.Submission.SrlExperiment;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;

public class RemoveDuplicates {
	public static void main(String args[]) throws UnknownHostException, AuthenticationException, DatabaseAccessException {
		System.out.println("Starting program");
		String mastId = "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332";
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB sub = mongoClient.getDB("submissions");
		DBCollection exp = sub.getCollection("Experiments");
		ArrayList<String> couresId = new ArrayList<String>();
		couresId.add("52d55a580364615fe8a4496c");
		ArrayList<SrlCourse> courses = Institution.mongoGetCourses(couresId, mastId);
		for (int k = 0; k < courses.size(); k++) {
			System.out.println(courses.get(k).getAssignmentListList());
			ArrayList<SrlAssignment> assignments = Institution.mongoGetAssignment(courses.get(k).getAssignmentListList(), mastId);
			System.out.println("number of assignments found: " + assignments.size());
			for (int q = 0; q < assignments.size(); q++) {
				ArrayList<SrlProblem> problems =  Institution.mongoGetCourseProblem(assignments.get(q).getProblemListList(), mastId);
				for (int r = 0; r < problems.size(); r++) {
					BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, problems.get(k).getId());
					removeDusplicates(exp.find(findQuery));
				}
			}
		}
	}
	
	public static void removeDusplicates(DBCursor dbCursor) {
		ArrayList<SrlExperiment> extraExperiments = new ArrayList<SrlExperiment>();
		while (dbCursor.hasNext()) {
			// we look at every single one and try and remove all extraExperiments
		}
	}
}

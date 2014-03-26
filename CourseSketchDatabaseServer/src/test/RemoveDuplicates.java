package test;

import static util.StringConstants.COURSE_PROBLEM_ID;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;

import protobuf.srl.commands.Commands.SrlUpdate;
import protobuf.srl.commands.Commands.SrlUpdateList;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.submission.Submission.SrlChecksum;
import protobuf.srl.submission.Submission.SrlExperiment;
import protobuf.srl.submission.Submission.SrlSubmission;
import util.Checksum;

import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.Institution;

public class RemoveDuplicates {
	static DBCollection trash;
	static DBCollection experiments;
	public static void main(String args[]) throws UnknownHostException, AuthenticationException, DatabaseAccessException, InterruptedException {		
		System.out.println("Starting program");
		String mastId = "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332";
		MongoClient mongoClient = new MongoClient("goldberglinux.tamu.edu");
		DB sub = mongoClient.getDB("submissions");
		DBCollection exp = sub.getCollection("Experiments");
		experiments = exp;
		trash = sub.getCollection("Trash");
		ArrayList<String> couresId = new ArrayList<String>();
		couresId.add("52d55a580364615fe8a4496c");
		ArrayList<SrlCourse> courses = Institution.mongoGetCourses(couresId, mastId);
		
		for (int k = 0; k < courses.size(); k++) {
			String courseId = courses.get(k).getId();
			System.out.println(courses.get(k).getAssignmentListList());
			ArrayList<SrlAssignment> assignments = Institution.mongoGetAssignment(courses.get(k).getAssignmentListList(), mastId);
			System.out.println("number of assignments found: " + assignments.size());
			
			for (int q = 0; q < assignments.size(); q++) { // 3rd and 4th are fine (which are 0 and 1)
				String assignmentId = assignments.get(q).getId();
				System.out.println("\n\nLooking at assignment " + assignments.get(q).getName() + " " + assignmentId);
				ArrayList<SrlProblem> problems =  Institution.mongoGetCourseProblem(assignments.get(q).getProblemListList(), mastId);
				System.out.println("number of problems found: " + problems.size());

				for (int r = 0; r < problems.size(); r++) {
					System.out.println("\n\nLooking at problem " +  problems.get(r).getName() + " " + problems.get(r).getId());
					BasicDBObject findQuery = new BasicDBObject(COURSE_PROBLEM_ID, problems.get(r).getId());
					removeDusplicates(exp.find(findQuery), problems.get(r).getId(), assignmentId, courseId);
				}
			}
		}
	}

	public static void removeDusplicates(DBCursor dbCursor, String problemId, String assignmentId, String courseId) throws InterruptedException, DatabaseAccessException {
		Map<String, SrlExperiment> reducerMap = new HashMap<String, SrlExperiment>();
		Map<String, SrlExperiment> duplicateMap = new HashMap<String, SrlExperiment>();
		System.out.println("Number of submissions found: " + dbCursor.count());
		int count = 0;
		while (dbCursor.hasNext()) {
			DBObject obj = dbCursor.next();
			//UserId
			//time
			Object result = obj.get(util.StringConstants.USER_ID);
			if (result != null && !result.equals("")) {
				String userId = (String) result;
				SrlExperiment.Builder nextExperiment = SrlExperiment.newBuilder();
				nextExperiment.setAssignmentId(assignmentId);
				nextExperiment.setCourseId(courseId);
				nextExperiment.setProblemId(problemId);
				nextExperiment.setUserId(userId);

				SrlSubmission.Builder build = SrlSubmission.newBuilder();
				build.setSubmissionTime((Long) obj.get("time"));
				build.setId(obj.get(util.StringConstants.SELF_ID).toString());
				byte[] byteArray = (byte[])obj.get("UpdateList");
				build.setUpdateList(ByteString.copyFrom(byteArray));

				System.out.println("\nLooking at submisssion " + count + " out of " + dbCursor.count() + " with Id: " + build.getId());
				nextExperiment.setSubmission(build);
				System.out.println("UserId : " + userId);

				// we look at every single one and try and remove all extraExperiments
				List<SrlUpdate> updates;
				try {
					SrlUpdateList list = SrlUpdateList.parseFrom(byteArray);
					updates = list.getListList();
					if (updates.size() == 0) {
						removeSketch(build.getId(), nextExperiment.build());
						System.out.println("This sketch has no data!");
						count -= 1;
						continue;
					}
				} catch(Exception e) {
					e.printStackTrace();
					removeSketch(build.getId(), nextExperiment.build());
					count -= 1;
					continue;
				}

				boolean deleted = false;
				SrlChecksum checkSum = Checksum.computeChecksum(updates);
				String checkKey = userId + ":" + checkSum.getFirstBits() + "-" + checkSum.getSecondBits();
				System.out.println(checkKey);
				if (duplicateMap.containsKey(checkKey)) {
					deleted = true;
					System.out.println("reject: deleting");
					removeSketch(build.getId(), nextExperiment.build());
					count -= 1;
					//extraExperiments.add(nextExperiment.build());// created item
				} else {
					System.out.println("add as checksum");
					duplicateMap.put(checkKey, nextExperiment.build());
					// add created item
				}

				if (! deleted) {
					if (reducerMap.containsKey(userId)) {
						SrlExperiment ment = reducerMap.get(userId);
						long time = ment.getSubmission().getSubmissionTime();
						long nextTime = build.getSubmissionTime();
						System.out.println("Old experiment - current: " + time +" " + nextTime);
						if (nextTime > time) {
							System.out.println("replace");
							removeSketch(build.getId(), ment, true); // removes the one that was in the map
							reducerMap.put(userId, nextExperiment.build());
							count--;
							// we neeed to replace
						} else {
							System.out.println("reject: adding to trash");
							removeSketch(build.getId(), nextExperiment.build(), true); // removes the one we were looking at
							count--;
						}
					} else {
						reducerMap.put(userId, nextExperiment.build());
						// add created item
					}
				}

				Thread.sleep(100);
			}
			count++;
		}

		// now we go through our map and relink
		for (String userId: reducerMap.keySet()) {
			SrlExperiment ment = reducerMap.get(userId);
			System.out.println("Fixing experiment: " + problemId + " " + userId + " " + ment.getSubmission().getId());
			Institution.mongoInsertSubmission(problemId, userId, ment.getSubmission().getId(), true);
		}
	}

	private static void removeSketch(String id, SrlExperiment build, boolean putInTrash) {
		if (putInTrash) {
			DBObject obj = new BasicDBObject(util.StringConstants.SELF_ID, new ObjectId(id)).append("result", build.toByteArray());
			trash.insert(obj);
		}
		//DBObject removeObj = new BasicDBObject(database.StringConstants.SELF_ID, new ObjectId(id));
		//experiments.remove(removeObj);
	}

	private static void removeSketch(String id, SrlExperiment exp) {
		removeSketch(id, exp, false);
	}
}

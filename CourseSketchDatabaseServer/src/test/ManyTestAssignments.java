package test;

import java.util.Date;

import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlAssignment.LatePolicy;
import protobuf.srl.school.School.SrlPermission;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.institution.Institution;

public class ManyTestAssignments {
	public static void testAssignments(String courseId) {
		String[] name = new String[]{"Assignment1", "Assignment2", "Assignment3", "Assignmen4", "Assignment5"};
		String[] descsription = new String[]{"This is first assignment is to get you used to the system and find out how it works",
				
				"This is the second assignment",
				
				"This is the last assignment",
				
				"Wait i added this assignment",
						
				"blah blah blah"};
		for(int k = 0; k < 1; k ++) {
			SrlAssignment.Builder testBuilder = SrlAssignment.newBuilder();
			testBuilder.setName(name[k]);
			testBuilder.setCourseId(courseId);
			testBuilder.setDescription(descsription[k]);
			testBuilder.setGradeWeight("50%");
			testBuilder.setLatePolicy(LatePolicy.POLICY1);
			testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
			testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((new Date(1414126800000L).getTime())));
			SrlPermission.Builder permissions = SrlPermission.newBuilder();
			permissions.addAdminPermission("larry");
	
			permissions.addModeratorPermission("raniero");
			permissions.addModeratorPermission("manoj");
	
			permissions.addUserPermission("vijay");
			permissions.addUserPermission("matt");
	
			testBuilder.setAccessPermission(permissions.build());
			System.out.println(testBuilder.toString());
	
			// testing inserting course
				System.out.println("INSERTING ASSIGNMENT");
				String assignmentId = null;
				try {
					assignmentId = Institution.mongoInsertAssignment("david", testBuilder.buildPartial());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DatabaseAccessException e) {
					e.printStackTrace();
				}
				System.out.println("INSERTING ASSIGNMENT SUCCESSFULT");
				System.out.println(courseId);
				ManyTestProblems.testProblems(courseId, assignmentId);
		}
	}
}

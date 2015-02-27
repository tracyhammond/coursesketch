package local.data;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.auth.AuthenticationException;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlPermission;

import java.util.Date;

public class LocalAddAssignments {
	public static void testAssignments(String courseId, String mastId) {
		String[] name = new String[]{"Assignment 1"};
		String[] description = new String[]{"This is the first assignment over lewis dot diagrams"};
		for (int k = 0; k < 1; k ++) {
			SrlAssignment.Builder testBuilder = SrlAssignment.newBuilder();
			testBuilder.setName(name[k]);
			testBuilder.setCourseId(courseId);
			testBuilder.setDescription(description[k]);
			testBuilder.setGradeWeight("50%");
			//testBuilder.setLatePolicy(LatePolicy.POLICY1);
			testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds(0));
			testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds(315576000000000L));
			Date d = new Date();
			d.setYear(2014-1900);
			d.setMonth(1);
			d.setDate(21);
			d.setHours(0);
			testBuilder.setDueDate(RequestConverter.getProtoFromMilliseconds(d.getTime()));
			SrlPermission.Builder permissions = SrlPermission.newBuilder();

			testBuilder.setAccessPermission(permissions.build());
			System.out.println(testBuilder.toString());

			// testing inserting course
				System.out.println("INSERTING ASSIGNMENT");
				String assignmentId = null;
				try {
					assignmentId = MongoInstitution.getInstance().insertAssignment(mastId, testBuilder.buildPartial());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DatabaseAccessException e) {
					e.printStackTrace();
				}
				System.out.println("INSERTING ASSIGNMENT SUCCESSFUL"); /*SUCCESSFULT*/
				System.out.println(courseId);
				LocalAddProblems.testProblems(courseId, assignmentId, mastId);
		}
	}

	public static void main(String args[]) {
		new MongoInstitution(false, null); // makes the database point locally
		new UserClient(false, null); // makes the database point locally
		testAssignments(""/*course id */,""/*instructor id*/);
	}
}
// 0b7ac244-b785-6961-9347-7621abeada88-277aa353914b7c5f

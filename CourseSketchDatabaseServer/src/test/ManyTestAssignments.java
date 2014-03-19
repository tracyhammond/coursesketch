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
	public static void testAssignments(String courseId, String mastId) {
		String[] name = new String[]{"Assignment7"};		/*Assignment6*//*"Assignment5"*//*"Assignment4"*//*"Assignment3"*/
		String[] descsription = new String[]{"Due Wednesday 3/19/2014 at midnight. This is the seventh assignment."/*"Due Wednesday 2/26/2014 at midnight. This is the Fifth Assignment"*//*"This is the Fourth Assignment"*//*"This is the Third Assignment"*/};
		for (int k = 0; k < 1; k ++) {
			SrlAssignment.Builder testBuilder = SrlAssignment.newBuilder();
			testBuilder.setName(name[k]);
			testBuilder.setCourseId(courseId);
			testBuilder.setDescription(descsription[k]);
			testBuilder.setGradeWeight("50%");
			//testBuilder.setLatePolicy(LatePolicy.POLICY1);
			testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
			testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((new Date(1395291599000L/*1394085599000L*//*1393480799000L*//*1392875999000L*//*1392703199000L*/).getTime())));
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
					assignmentId = Institution.mongoInsertAssignment(mastId, testBuilder.buildPartial());
				} catch (AuthenticationException e) {
					e.printStackTrace();
				} catch (DatabaseAccessException e) {
					e.printStackTrace();
				}
				System.out.println("INSERTING ASSIGNMENT SUCCESSFUL"); /*SUCCESSFULT*/
				System.out.println(courseId);
				ManyTestProblems.testProblems(courseId, assignmentId, mastId);
		}
	}

	public static void main(String args[]) {
		System.out.println("Running program");
		testAssignments("52d55a580364615fe8a4496c", "0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332");
	}
}
// 0b7ac244-b785-6961-9347-7621abeada88-277aa353914b7c5f
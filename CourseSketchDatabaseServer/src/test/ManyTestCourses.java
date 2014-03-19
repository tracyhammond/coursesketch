package test;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import database.DatabaseAccessException;
import database.institution.Institution;
import database.user.UserClient;

public class ManyTestCourses {
	public static void testCourses() throws DatabaseAccessException {
		String[] name = new String[]{"CourseSketch 101"};
		String[] descsription = new String[]{"Hi Welcome to CourseSketch, you have automatically been enrolled in this tutorial."
				+ " To expand the description of a class click the down arrow."};
		for(int k = 0; k < 1; k ++) {
			SrlCourse.Builder testBuilder = SrlCourse.newBuilder();
			testBuilder.setAccess(SrlCourse.Accessibility.SUPER_PUBLIC);
			testBuilder.setSemester("FALL");
			testBuilder.setName(name[k]);
			testBuilder.setDescription(descsription[k]);
			SrlPermission.Builder permissions = SrlPermission.newBuilder();

			testBuilder.setAccessPermission(permissions.build());
			System.out.println(testBuilder.toString());

			// testing inserting course
				System.out.println("INSERTING COURSE");
				String courseId = Institution.mongoInsertCourse("0aeee914-3411-6e12-8012-50ab6e769496-6eff24dba01bc332", testBuilder.buildPartial());
				System.out.println("INSERTING COURSE SUCCESSFULT");
				System.out.println(courseId);
				ManyTestAssignments.testAssignments(courseId);
		}
	}
	
	public static void main(String[] args) throws DatabaseAccessException {
		new Institution(false); // makes the database point locally
		new UserClient(false); // makes the database point locally
		testCourses();
	}
}

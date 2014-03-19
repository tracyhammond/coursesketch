package test;

import javax.swing.JOptionPane;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import database.DatabaseAccessException;
import database.institution.Institution;
import database.user.UserClient;

public class ManyTestCourses {
	public static void testCourses(String instructionID) throws DatabaseAccessException {
		String[] name = new String[]{"CourseSketch 101"};
		String[] descsription = new String[]{"Hi Welcome to CourseSketch, you have automatically been enrolled in this tutorial."
				+ " To expand the description of a class click the down arrow."};
		for (int k = 0; k < 1; k ++) {
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
				String courseId = Institution.mongoInsertCourse(instructionID, testBuilder.buildPartial());
				System.out.println("INSERTING COURSE SUCCESSFULT");
				System.out.println(courseId);
				ManyTestAssignments.testAssignments(courseId, instructionID);
		}
	}
	
	public static void main(String[] args) throws DatabaseAccessException {
		new Institution(false); // makes the database point locally
		new UserClient(false); // makes the database point locally
		String id = JOptionPane.showInputDialog("Insert your the Id of the person inserting the class");
		//0b7ac244-b785-6961-9347-7621abeada88-277aa353914b7c5f
		testCourses(id);
	}
}

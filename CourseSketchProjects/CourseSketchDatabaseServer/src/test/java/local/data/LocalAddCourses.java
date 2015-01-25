package local.data;

import database.DatabaseAccessException;
import database.RequestConverter;
import database.institution.mongo.MongoInstitution;
import database.user.UserClient;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;

import javax.swing.JOptionPane;

public class LocalAddCourses {
	public static void testCourses(String instructionID) throws DatabaseAccessException {
		String[] name = new String[]{"Chem 107", "Simple Circuits"};
		String[] descsription = new String[]{"Howdy! Welcome to Chem 107 where you learn about luis dot diagrams",
				""};
		for (int k = 0; k < name.length; k ++) {
			SrlCourse.Builder testBuilder = SrlCourse.newBuilder();
			testBuilder.setAccess(SrlCourse.Accessibility.SUPER_PUBLIC);
			testBuilder.setSemester("FALL");
			testBuilder.setName(name[k]);
			testBuilder.setDescription(descsription[k]);
			testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds(0));
			testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds(315576000000000L));
			SrlPermission.Builder permissions = SrlPermission.newBuilder();

			testBuilder.setAccessPermission(permissions.build());
			System.out.println(testBuilder.toString());

			// testing inserting course
			System.out.println("INSERTING COURSE");
			String courseId = MongoInstitution.getInstance().insertCourse(instructionID, testBuilder.buildPartial());
			System.out.println("INSERTING COURSE SUCCESSFUL");
			System.out.println(courseId);
			LocalAddAssignments.testAssignments(courseId, instructionID);
		}
	}

	public static void main(String[] args) throws DatabaseAccessException {
		new MongoInstitution(false, null); // makes the database point locally
		new UserClient(false, null); // makes the database point locally
		String id = JOptionPane.showInputDialog("Insert the Id of the person inserting the class");
		//0b7ac244-b785-6961-9347-7621abeada88-277aa353914b7c5f
		testCourses(id);
	}
}

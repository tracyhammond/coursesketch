package test;

import protobuf.srl.school.School.SrlUser;
import database.DatabaseAccessException;
import database.institution.Institution;
import database.user.UserClient;

public class UserTester {
	public static void testUsers(String courseId) throws DatabaseAccessException {
		String[] userNameList = new String[] {"sister", "brother", "girlfriend", "mom", "dad", "grandma"};
		for(String userName : userNameList) {
			SrlUser.Builder build = SrlUser.newBuilder();
			build.setUsername(userName);
			build.setEmail("email@mail.com");
			UserClient.insertUser(build.build(), userName);
			Institution.putUserInCourse(courseId, userName);
		}
	}
}

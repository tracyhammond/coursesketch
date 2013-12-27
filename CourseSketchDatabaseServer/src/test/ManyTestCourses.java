package test;

import java.util.Date;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import database.RequestConverter;
import database.institution.Institution;

public class ManyTestCourses {
	public static void testCourses() {
		String[] name = new String[]{"Music 214", "Math 312", "Physics 108", "Speling 101", "Computer Class 1024"};
		String[] descsription = new String[]{"This hands-on course will expose students to musical tools developed by the Center for New Music and Audio Technologies (CNMAT). "
				+ "Topics include performative and compositional applications of current research at CNMAT, "
				+ "including sound synthesis and diffusion, high-level control, and network applications.",
				
				"Linear Algebra is at the heart of many diverse current applications of mathematics."
				+ "Notable contemporary examples involve understanding large data setssuch as the idea behind Google searches and the structure of DNA. "
				+ "Our goal is to present both the major ideas and give you technical skills. "
				+ "To be successful in this course, you should be present for all class meetings and plan to take good notes.",
				
				"Welcome to Phys 108! The purpose of this course is to help you explore the natural phenomenaof electricity and magnetism by "
				+ "<br>exposing you to physical phenomena in the laboratory<br>"
				+ "engaging you in a group oriented setting to allow collaboration with peers",
				
				"In this classrom u will learn crucial spelling information dawgs"
				+ "This course is importatn to ur edu",
						
				"496620796f752063616e206465636f64652074686973207468656e20796f752073686f756c642074616b65207468697320636f75727365"};
		for(int k = 0; k < 5; k ++) {
			SrlCourse.Builder testBuilder = SrlCourse.newBuilder();
			testBuilder.setAccess(SrlCourse.Accessibility.PUBLIC);
			testBuilder.setSemester("FALL");
			testBuilder.setName(name[k]);
			testBuilder.setDescription(descsription[k]);
			testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
			testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() + 1000000).getTime())));
			SrlPermission.Builder permissions = SrlPermission.newBuilder();
			permissions.addAdminPermission("larry");
	
			permissions.addModeratorPermission("raniero");
			permissions.addModeratorPermission("manoj");
	
			permissions.addUserPermission("vijay");
			permissions.addUserPermission("matt");
	
			testBuilder.setAccessPermission(permissions.build());
			System.out.println(testBuilder.toString());
	
			// testing inserting course
				System.out.println("INSERTING COURSE");
				String courseId = Institution.mongoInsertCourse("david", testBuilder.buildPartial());
				System.out.println("INSERTING COURSE SUCCESSFULT");
				System.out.println(courseId);
		}
	}
	
	public static void main(String[] args) {
		testCourses();
	}
}

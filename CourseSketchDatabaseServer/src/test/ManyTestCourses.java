package test;

import java.util.Date;

import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlPermission;
import database.RequestConverter;
import database.institution.Institution;

public class ManyTestCourses {
	public static void testCourses() {
		String[] name = new String[]{"CSCE 222 Discrete Structures for Computing"};
		String[] descsription = new String[]{"The course provides the mathematical foundations from discrete mathematics for analyzing computer  algorithms, for both correctness and performance;"
				+ " introduction to models of computation, including finite state machines and Turing machines. "
				+ "At the end of the course, students will understand the basic principles of logic, proofs and sets."
				+ " They will be able to apply results from discrete mathematics to analysis of algorithms."
				+ " They will be able to produce proofs by induction and apply counting techniques."
				+ " They will have a basic understanding of models of computation."};
		for(int k = 0; k < 1; k ++) {
			SrlCourse.Builder testBuilder = SrlCourse.newBuilder();
			testBuilder.setAccess(SrlCourse.Accessibility.PUBLIC);
			testBuilder.setSemester("FALL");
			testBuilder.setName(name[k]);
			testBuilder.setDescription(descsription[k]);
			testBuilder.setAccessDate(RequestConverter.getProtoFromMilliseconds((new Date(System.currentTimeMillis() - 1000000).getTime())));
			Date d = new Date();
			d.setYear(2013);
			d.setMonth(5);
			d.setDate(20);
			testBuilder.setCloseDate(RequestConverter.getProtoFromMilliseconds((d.getTime())));
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
	
	public static void main(String[] args) {
		testCourses();
	}
}

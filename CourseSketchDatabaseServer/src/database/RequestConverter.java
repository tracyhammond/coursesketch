package database;

import java.util.Arrays;

import protobuf.srl.school.School.DateTime;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlCourse;

public class RequestConverter{
	static CourseBuilder convertProtobufToCourseBuilder(SrlCourse protoCourse){
		CourseBuilder courseBuilder = new CourseBuilder();
		courseBuilder.setAccess(protoCourse.getAccess().toString());
		courseBuilder.setAssignmentList((String[]) protoCourse.getAssignmentIdList().toArray());
		courseBuilder.setCloseDate(DateProtobufToString(protoCourse.getCloseDate()));
		courseBuilder.setDescription(protoCourse.getDescription());
		courseBuilder.setImage(protoCourse.getImageUrl());
		courseBuilder.setName(protoCourse.getName());
		courseBuilder.setOpenDate(DateProtobufToString(protoCourse.getAccessDate()));
		courseBuilder.setSemesester(protoCourse.getSemester());
		return courseBuilder;
	}
	
	static SrlCourse convertCourseBuilderToProtobuf(CourseBuilder course){
		SrlCourse.Builder srlCourseBuilder = SrlCourse.newBuilder();
		
		srlCourseBuilder.setAccess(SrlCourse.Accessibility.valueOf(course.access));
		srlCourseBuilder.addAllAssignmentId(Arrays.asList(course.assignmentList));
		srlCourseBuilder.setCloseDate(DateStringToProtobuf(course.closeDate));
		srlCourseBuilder.setDescription(course.description);
		srlCourseBuilder.setImageUrl(course.image);
		srlCourseBuilder.setName(course.name);
		srlCourseBuilder.setAccessDate(DateStringToProtobuf(course.access));
		srlCourseBuilder.setSemester(course.semesester);
		return srlCourseBuilder.build();
	}
	
	static AssignmentBuilder convertProtobufToAssignment(SrlAssignment protoAssignment){
		AssignmentBuilder assignmentBuilder = new AssignmentBuilder();
		assignmentBuilder.setCloseDate(DateProtobufToString(protoAssignment.getCloseDate()));
		assignmentBuilder.setCourseId(protoAssignment.getCourseId());
		assignmentBuilder.setDescription(protoAssignment.getDescription());
		assignmentBuilder.setDueDate(DateProtobufToString(protoAssignment.getDueDate()));
		assignmentBuilder.setGradeWeigh(""+protoAssignment.getGradeWeight());
		assignmentBuilder.setImageUrl(protoAssignment.getImageUrl());
		//assignmentBuilder.setLatePolicy(protoAssignment.getLatePolicy());
		assignmentBuilder.setName(protoAssignment.getName());
		assignmentBuilder.setOpenDate(DateProtobufToString(protoAssignment.getAccessDate()));
		assignmentBuilder.setOther(protoAssignment.getOther());
		//assignmentBuilder.setType(protoAssignment.getType());
		
		return assignmentBuilder;
	}
	
	private static DateTime DateStringToProtobuf(String date){
		DateTime.Builder protobufDateTime = DateTime.newBuilder();
		
		String[] splitDate = date.split(" ");
		
		protobufDateTime.setYear(Integer.parseInt(splitDate[0]));
		protobufDateTime.setDay(Integer.parseInt(splitDate[1]));
		protobufDateTime.setHour(Integer.parseInt(splitDate[2]));
		protobufDateTime.setMinute(Integer.parseInt(splitDate[3]));
		protobufDateTime.setSecond(Integer.parseInt(splitDate[4]));
		protobufDateTime.setMillisecond(Long.parseLong(splitDate[5]));
		
		return protobufDateTime.build();
	}
	
	private static String DateProtobufToString(DateTime protoDate){
		StringBuilder joinedDate = new StringBuilder();
		joinedDate.append(protoDate.getYear());
		joinedDate.append(protoDate.getDay());
		joinedDate.append(protoDate.getHour());
		joinedDate.append(protoDate.getMinute());
		joinedDate.append(protoDate.getSecond());
		joinedDate.append(protoDate.getMillisecond());
		return joinedDate.toString();
	}
}

//convert a string date to a protobuf and vice versa
//Date String Format: "YYYY DD HH MM SS LLLL"
//two parts to a problem: problem bank part, and course problem part. Just convert them
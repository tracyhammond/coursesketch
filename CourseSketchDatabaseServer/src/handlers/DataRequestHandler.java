package handlers;

import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;

import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlSchool;

import com.google.protobuf.InvalidProtocolBufferException;

import database.Institution;
import database.auth.AuthenticationException;

public class DataRequestHandler {
	public static void handleRequest(Request req, WebSocket conn) {
		try {
			System.out.println("Receiving DATA Request...");
			SrlSchool.Builder finalSchool = SrlSchool.newBuilder();
			String userId = req.getSessionId();
			DataRequest request = DataRequest.parseFrom(req.getOtherData());
			if (userId == null) {
				throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
			}
			for(int p=0; p<request.getItemsList().size(); p++){
				ItemRequest itrequest = request.getItemsList().get(p);
				switch(itrequest.getQuery()) {
					case COURSE: ArrayList<SrlCourse> courseLoop = Institution.mongoGetCourses((List)itrequest.getItemIdList(), userId);
								finalSchool.addAllCourses(courseLoop);
								break;
					case ASSIGNMENT: ArrayList<SrlAssignment> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), userId);
								finalSchool.addAllAssignments(assignmentLoop);
								break;
					case COURSE_PROBLEM: ArrayList<SrlProblem> courseProblemLoop = Institution.mongoGetCourseProblem((ArrayList)itrequest.getItemIdList(), userId);
								for(SrlProblem loopCourse: courseProblemLoop){
									finalSchool.addProblems(loopCourse);
								}
								break;
					case BANK_PROBLEM: ArrayList<SrlBankProblem> bankProblemLoop = Institution.mongoGetProblem((ArrayList)itrequest.getItemIdList(), userId);
								for(SrlBankProblem loopCourse: bankProblemLoop){
									finalSchool.addBankProblems(loopCourse);
								}
								break;
					/*case USERGROUP: ArrayList<UserGroupBuilder> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), request.getUserId());
								for(AssignmentBuilder loopCourse: assignmentLoop){
									finalSchool.addAssignments(RequestConverter.convertAssignmentToProtobuf(loopCourse));
								}
								break;
					case CLASS_GRADE: ArrayList<AssignmentBuilder> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), request.getUserId());
								for(AssignmentBuilder loopCourse: assignmentLoop){
									finalSchool.addAssignments(RequestConverter.convertAssignmentToProtobuf(loopCourse));
								}
								break;
					case USER_INFO: ArrayList<AssignmentBuilder> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), request.getUserId());
								for(AssignmentBuilder loopCourse: assignmentLoop){
									finalSchool.addAssignments(RequestConverter.convertAssignmentToProtobuf(loopCourse));
								}
								break;
					case SOLUTION: ArrayList<AssignmentBuilder> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), request.getUserId());
								for(AssignmentBuilder loopCourse: assignmentLoop){
									finalSchool.addAssignments(RequestConverter.convertAssignmentToProtobuf(loopCourse));
								}
								break;
					case EXPERIMENT: ArrayList<AssignmentBuilder> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), request.getUserId());
								for(AssignmentBuilder loopCourse: assignmentLoop){
									finalSchool.addAssignments(RequestConverter.convertAssignmentToProtobuf(loopCourse));
								}
								break;*/
				}
			}
			Request.Builder dataReq = Request.newBuilder();
			dataReq.setRequestType(MessageType.DATA_REQUEST);
			DataResult.Builder dataResult = DataResult.newBuilder();
			ItemResult.Builder result = ItemResult.newBuilder();
			result.setData(finalSchool.build().toByteString());
			result.setQuery(ItemQuery.SCHOOL);
			dataResult.addResults(result.build());
			dataReq.setSessionInfo(req.getSessionInfo());
			dataReq.setOtherData(dataResult.build().toByteString());
			byte[] array = dataReq.build().toByteArray();
			if (array != null) {
				conn.send(array);
			} else {
				System.out.println("BLAH BLAH BALH");
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		// decode request and pull correct information from database (courses, assignments, ...) then repackage everything and send it out
		catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package handlers;

import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import database.auth.AuthenticationException;
import database.institution.Institution;
import database.user.UserClient;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.DataResult;
import protobuf.srl.query.Data.DataSend;
import protobuf.srl.query.Data.ItemQuery;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.query.Data.ItemResult;
import protobuf.srl.query.Data.ItemSend;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlCourse;
import protobuf.srl.school.School.SrlProblem;
import protobuf.srl.school.School.SrlSchool;

public class DataSendHandler {
	public static String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";
	
	public static void handleData(Request req, WebSocket conn) {
		try {
			System.out.println("Receiving DATA SEND Request...");
			
			String userId = req.getServersideId();
			DataSend request = DataSend.parseFrom(req.getOtherData());
			if (userId == null) {
				throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
			}
			ArrayList<ItemResult> results = new ArrayList<ItemResult> ();

			for(int p=0; p<request.getItemsList().size(); p++) {
				ItemSend itrequest = request.getItemsList().get(p);
				switch(itrequest.getQuery()) {
					/*
					case COURSE: ArrayList<SrlCourse> courseLoop = Institution.mongoGetCourses((List)itrequest.getItemIdList(), userId);
								SrlSchool.Builder courseSchool = SrlSchool.newBuilder();
								courseSchool.addAllCourses(courseLoop);
								results.add(buildResult(courseSchool.build().toByteString(),ItemQuery.COURSE));
								break;
					case ASSIGNMENT: ArrayList<SrlAssignment> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), userId);
								SrlSchool.Builder assignmentSchool = SrlSchool.newBuilder();
								assignmentSchool.addAllAssignments(assignmentLoop);
								results.add(buildResult(assignmentSchool.build().toByteString(),ItemQuery.ASSIGNMENT));
								break;
					case COURSE_PROBLEM: ArrayList<SrlProblem> courseProblemLoop = Institution.mongoGetCourseProblem((ArrayList)itrequest.getItemIdList(), userId);
								SrlSchool.Builder problemSchool = SrlSchool.newBuilder();
								problemSchool.addAllProblems(courseProblemLoop);
								results.add(buildResult(problemSchool.build().toByteString(),ItemQuery.COURSE_PROBLEM));
								break;
					case BANK_PROBLEM: ArrayList<SrlBankProblem> bankProblemLoop = Institution.mongoGetProblem((ArrayList)itrequest.getItemIdList(), userId);
								SrlSchool.Builder bankproblemSchool = SrlSchool.newBuilder();
								bankproblemSchool.addAllBankProblems(bankProblemLoop);
								results.add(buildResult(bankproblemSchool.build().toByteString(),ItemQuery.BANK_PROBLEM));
								break;
					*/
					case USER_INFO: {
						if (itrequest.getIsInsert()) {
							UserClient.insertUser(itrequest.getData());
						}
					}
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
			if (results.size() > 0) {
				conn.send(buildRequest(results, SUCCESS_MESSAGE, req).toByteArray());
			}
			return;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			conn.send(buildRequest(null, e.getMessage(), req).toByteArray());
		}
		// decode request and pull correct information from database.institution (courses, assignments, ...) then repackage everything and send it out
		catch (AuthenticationException e) {
			e.printStackTrace();
			conn.send(buildRequest(null, e.getMessage(), req).toByteArray());
		} catch (Exception e) {
			e.printStackTrace();
			conn.send(buildRequest(null, e.getMessage(), req).toByteArray());
		}
	}

	private static ItemResult buildResult(ByteString data, ItemQuery type) {
		ItemResult.Builder result = ItemResult.newBuilder();
		result.setData(data);
		result.setQuery(type);
		return result.build();
	}

	private static Request buildRequest(ArrayList<ItemResult> results, String message, Request req) {
		
		DataResult.Builder dataResult = null;
		if (results!= null && results.size() >0) {
			dataResult = DataResult.newBuilder();
			dataResult.addAllResults(results);
		}

		Request.Builder dataReq = Request.newBuilder();
		dataReq.setRequestType(MessageType.DATA_REQUEST);
		dataReq.setSessionInfo(req.getSessionInfo());
		dataReq.setOtherData(dataResult.build().toByteString());
		dataReq.setResponseText(message);
		if (dataResult!= null) {
			dataReq.setOtherData(dataResult.build().toByteString());
		}
		return dataReq.build();
	}
}

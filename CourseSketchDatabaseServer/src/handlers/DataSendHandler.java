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
import protobuf.srl.school.School.SrlUser;

/**
 * Handles data being added or edited.
 *
 * In most cases insert returns the mongoId and the id that was taken in.
 * This allows the client to replace the old assignment id with the new assignment id.
 * @author gigemjt
 */
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
				ItemSend itemSet = request.getItemsList().get(p);
				switch (itemSet.getQuery()) {
					case COURSE: if (itemSet.getIsInsert()) {
						SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
						String resultId = Institution.mongoInsertCourse(userId, course);
						results.add(buildResult(resultId + " : " + course.getId(), itemSet.getQuery()));
					} else {
						// update here
						// update does not need to return anything
					}
					case ASSIGNMENT: if (itemSet.getIsInsert()) {
						SrlAssignment assignment = SrlAssignment.parseFrom(itemSet.getData());
						String resultId = Institution.mongoInsertAssignment(userId, assignment);
						results.add(buildResult(resultId + " : " + assignment.getId(), itemSet.getQuery()));
					} else {
						// update here
						// update does not need to return anything
					}
					case COURSE_PROBLEM: if (itemSet.getIsInsert()) {
						SrlProblem problem = SrlProblem.parseFrom(itemSet.getData());
						String resultId = Institution.mongoInsertCourseProblem(userId, problem);
						results.add(buildResult(resultId + " : " + problem.getId(), itemSet.getQuery()));
					} else {
						// update here
						// update does not need to return anything
					}
					case BANK_PROBLEM: if (itemSet.getIsInsert()) {
						SrlBankProblem problem = SrlBankProblem.parseFrom(itemSet.getData());
						String resultId = Institution.mongoInsertBankProblem(userId, problem);
						results.add(buildResult(resultId + " : " + problem.getId(), itemSet.getQuery()));
					} else {
						// update here
						// update does not need to return anything
					}
					case USER_INFO: if (itemSet.getIsInsert()) {
						UserClient.insertUser(SrlUser.parseFrom(itemSet.getData()), userId);
					} else {
						// update here
						// update does not need to return anything	
					}
					case REGISTER: {
						SrlCourse course = SrlCourse.parseFrom(itemSet.getData());
						String courseId = course.getId();
						Institution.putUserInCourse(courseId, userId);
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

	private static ItemResult buildResult(String data, ItemQuery type) {
		ItemResult.Builder result = ItemResult.newBuilder();
		result.setReturnText(data);
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

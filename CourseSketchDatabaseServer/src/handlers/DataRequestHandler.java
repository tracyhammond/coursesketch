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

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import database.auth.AuthenticationException;
import database.institution.Institution;

public class DataRequestHandler {
	public static String SUCCESS_MESSAGE = "QUERY WAS SUCCESSFUL!";
	public static String NO_COURSE_MESSAGE = "You do not have any courses associated with this account";
	
	public static void handleRequest(Request req, WebSocket conn) {
		try {
			System.out.println("Receiving DATA Request...");
			
			String userId = req.getServersideId();
			DataRequest request = DataRequest.parseFrom(req.getOtherData());
			if (userId == null) {
				throw new AuthenticationException(AuthenticationException.NO_AUTH_SENT);
			}
			ArrayList<ItemResult> results = new ArrayList<ItemResult> ();

			for(int p=0; p<request.getItemsList().size(); p++) {
				ItemRequest itrequest = request.getItemsList().get(p);
				try {
					System.out.println("looking at query " + itrequest.getQuery().name());
					switch(itrequest.getQuery()) {
						case COURSE: {
							List<SrlCourse> courseLoop = Institution.mongoGetCourses(itrequest.getItemIdList(), userId);
							SrlSchool.Builder courseSchool = SrlSchool.newBuilder();
							courseSchool.addAllCourses(courseLoop);
							results.add(buildResult(courseSchool.build().toByteString(),ItemQuery.COURSE));
							break;
						}
						case ASSIGNMENT: {
							List<SrlAssignment> assignmentLoop = Institution.mongoGetAssignment(itrequest.getItemIdList(), userId);
							SrlSchool.Builder assignmentSchool = SrlSchool.newBuilder();
							assignmentSchool.addAllAssignments(assignmentLoop);
							results.add(buildResult(assignmentSchool.build().toByteString(),ItemQuery.ASSIGNMENT));
							break;
						}
						case COURSE_PROBLEM: {
							List<SrlProblem> courseProblemLoop = Institution.mongoGetCourseProblem(itrequest.getItemIdList(), userId);
							SrlSchool.Builder problemSchool = SrlSchool.newBuilder();
							problemSchool.addAllProblems(courseProblemLoop);
							results.add(buildResult(problemSchool.build().toByteString(),ItemQuery.COURSE_PROBLEM));
							break;
						}
						case BANK_PROBLEM: {
							List<SrlBankProblem> bankProblemLoop = Institution.mongoGetProblem(itrequest.getItemIdList(), userId);
							SrlSchool.Builder bankproblemSchool = SrlSchool.newBuilder();
							bankproblemSchool.addAllBankProblems(bankProblemLoop);
							results.add(buildResult(bankproblemSchool.build().toByteString(),ItemQuery.BANK_PROBLEM));
							break;
						}
						case COURSE_SEARCH: {
							List<SrlCourse> courseLoop = Institution.getAllPublicCourses();
							System.out.println("Searching all public courses: " + courseLoop);
							SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
							courseSearch.addAllCourses(courseLoop);
							results.add(buildResult(courseSearch.build().toByteString(),ItemQuery.COURSE_SEARCH));
							break;
						}
						case SCHOOL: {
							ArrayList<SrlCourse> courseLoop = Institution.getUserCourses(userId);
							SrlSchool.Builder courseSearch = SrlSchool.newBuilder();
							courseSearch.addAllCourses(courseLoop);
							if (courseLoop.size() <= 0) {
								results.add(buildResult(courseSearch.build().toByteString(), NO_COURSE_MESSAGE, ItemQuery.SCHOOL));
							} else {
								results.add(buildResult(courseSearch.build().toByteString(),ItemQuery.SCHOOL));
							}
							break;
						}
						case EXPERIMENT: {
							
						}
						default: {
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
				} catch(AuthenticationException e) {
					if (e.getType() == AuthenticationException.INVALID_DATE) {
						ItemResult.Builder build = ItemResult.newBuilder();
						build.setQuery(itrequest.getQuery());
						results.add(buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
					} else {
						e.printStackTrace();
						throw e;
					}
				} catch(Exception e) {
					e.printStackTrace();
					ItemResult.Builder build = ItemResult.newBuilder();
					build.setQuery(itrequest.getQuery());
					build.setData(itrequest.toByteString());
					results.add(buildResult(build.build().toByteString(), e.getMessage(), ItemQuery.ERROR));
				}
			}
			conn.send(buildRequest(results, SUCCESS_MESSAGE, req).toByteArray());
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

	private static ItemResult buildResult(ByteString data, String text, ItemQuery type) {
		ItemResult.Builder result = ItemResult.newBuilder();
		result.setData(data);
		result.setQuery(type);
		result.setReturnText(text);
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

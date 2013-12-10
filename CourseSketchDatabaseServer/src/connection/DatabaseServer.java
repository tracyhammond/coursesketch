package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import multiConnection.MultiInternalConnectionServer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.google.protobuf.InvalidProtocolBufferException;

import database.Institution;
import database.RequestConverter;
import database.assignment.AssignmentBuilder;
import database.auth.AuthenticationException;
import database.course.CourseBuilder;
import database.problem.CourseProblemBuilder;
import database.problem.ProblemBankBuilder;
import protobuf.srl.query.Data.DataRequest;
import protobuf.srl.query.Data.ItemRequest;
import protobuf.srl.request.Message.Request;
import protobuf.srl.request.Message.Request.MessageType;
import protobuf.srl.school.School.SrlSchool;

/**
 * A simple WebSocketServer implementation.
 *
 * Contains simple proxy information that is sent to other servers.
 */
public class DatabaseServer extends MultiInternalConnectionServer {

	public static final int MAX_CONNECTIONS = 20;
	UpdateHandler updateHandler = new UpdateHandler();
	Database database = new Database();

	static int numberOfConnections = Integer.MIN_VALUE;
	public DatabaseServer( int port ) throws UnknownHostException {
		this( new InetSocketAddress( port ) );
	}

	public DatabaseServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
		System.out.println("Receiving message...");
		Request req = Decoder.parseRequest(buffer);

		if (req == null) {
			System.out.println("protobuf error");
			//this.
			// we need to somehow send an error to the client here
			return;
		}
		if (req.getRequestType() == Request.MessageType.SUBMISSION) {
			updateHandler.addRequest(req);
		}
		if (req.getRequestType() == Request.MessageType.DATA_REQUEST) {
			try {
				System.out.println("Receiving DATA Request...");
				SrlSchool.Builder finalSchool = SrlSchool.newBuilder();
				DataRequest request = DataRequest.parseFrom(req.getOtherData());
				for(int p=0; p<request.getItemsList().size(); p++){
					ItemRequest itrequest = request.getItemsList().get(p);
					switch(itrequest.getQuery()){
						case COURSE: ArrayList<CourseBuilder> courseLoop = Institution.mongoGetCourses((List)itrequest.getItemIdList(), request.getUserId());
									for(CourseBuilder loopCourse: courseLoop){
										finalSchool.addCourses(RequestConverter.convertCourseBuilderToProtobuf(loopCourse));
									}
									break;
						case ASSIGNMENT: ArrayList<AssignmentBuilder> assignmentLoop = Institution.mongoGetAssignment((ArrayList)itrequest.getItemIdList(), request.getUserId());
									for(AssignmentBuilder loopCourse: assignmentLoop){
										finalSchool.addAssignments(RequestConverter.convertAssignmentToProtobuf(loopCourse));
									}
									break;
						case COURSE_PROBLEM: ArrayList<CourseProblemBuilder> courseProblemLoop = Institution.mongoGetCourseProblem((ArrayList)itrequest.getItemIdList(), request.getUserId());
									for(CourseProblemBuilder loopCourse: courseProblemLoop){
										finalSchool.addProblems(RequestConverter.convertProblemToProtobuf(loopCourse));
									}
									break;
						case BANK_PROBLEM: ArrayList<ProblemBankBuilder> bankProblemLoop = Institution.mongoGetProblem((ArrayList)itrequest.getItemIdList(), request.getUserId());
									for(ProblemBankBuilder loopCourse: bankProblemLoop){
										finalSchool.addProblems(RequestConverter.convertProblemBankToProtobuf(loopCourse));
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
				dataReq.setOtherData(finalSchool.build().toByteString());
				dataReq.setSessionInfo(req.getSessionInfo());
				conn.send(dataReq.build().toByteArray());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// decode request and pull correct information from database (courses, assignments, ...) then repackage everything and send it out
 catch (AuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void onFragment( WebSocket conn, Framedata fragment ) {
		//System.out.println( "received fragment: " + fragment );
	}
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println("Database Server: Version 1.0.2.crocodile");
		WebSocketImpl.DEBUG = false;
		int port = 8885; // 843 flash policy port
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		DatabaseServer s = new DatabaseServer( port );
		s.start();
		System.out.println( "Database Server started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			if( in.equals( "exit" ) ) {
				s.stop();
				break;
			} else if( in.equals( "restart" ) ) {
				s.stop();
				s.start();
				break;
			}
		}
	}
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
}

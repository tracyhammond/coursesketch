package database.institution;

import static database.StringConstants.*;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import protobuf.srl.school.School.SrlBankProblem;
import protobuf.srl.school.School.SrlPermission;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;

import database.auth.AuthenticationException;
import database.auth.Authenticator;

public class BankProblemManager 
{
	public static String mongoInsertBankProblem(DB dbs, SrlBankProblem problem) throws AuthenticationException
	{
		DBCollection new_user = dbs.getCollection(PROBLEM_BANK_COLLECTION);
		BasicDBObject query = new BasicDBObject(QUESTION_TEXT, problem.getQuestionText())
										 .append(IMAGE, problem.getImage())
										 .append(SOLUTION_ID, problem.getSolutionId()) 
										 .append(COURSE_TOPIC, problem.getCourseTopic())
										 .append(SUB_TOPIC, problem.getSubTopic())
										 .append(SOURCE, problem.getSource())
										 .append(QUESTION_TYPE, problem.getQuestionType().getNumber())
										 .append(ADMIN, problem.getAccessPermission().getAdminPermissionList())
										 .append(USERS, problem.getAccessPermission().getUserPermissionList())
										 .append(KEYWORDS, problem.getOtherKeywordsList());
										 
		new_user.insert(query);
		DBObject corsor = new_user.findOne(query);
		return corsor.get(SELF_ID).toString();
	}

	public static SrlBankProblem mongoGetBankProblem(DB dbs, String problemBankID,String userId) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankID));
		DBObject corsor = myDbRef.fetch();

		ArrayList adminList = (ArrayList)corsor.get(ADMIN);
		ArrayList usersList = (ArrayList)corsor.get(USERS);
		boolean isAdmin, isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);
		isUsers = Authenticator.checkAuthentication(dbs, userId, usersList);

		if(!isAdmin && !isUsers)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		SrlBankProblem.Builder exactProblem = SrlBankProblem.newBuilder();

		exactProblem.setId(problemBankID);
		exactProblem.setQuestionText((String)corsor.get(QUESTION_TEXT));
		exactProblem.setImage((String)corsor.get(IMAGE));
		if (isAdmin) {
			exactProblem.setSolutionId((String)corsor.get(SOLUTION_ID));
		}
		exactProblem.setCourseTopic((String)corsor.get(COURSE_TOPIC));
		exactProblem.setSubTopic((String)corsor.get(SUB_TOPIC));
		exactProblem.setSource((String)corsor.get(SOURCE));
		exactProblem.setQuestionType(SrlBankProblem.QuestionType.valueOf((Integer)corsor.get(QUESTION_TYPE)));
		exactProblem.addAllOtherKeywords((ArrayList)corsor.get(KEYWORDS));// change arraylist

		SrlPermission.Builder permissions = SrlPermission.newBuilder();
		if (isAdmin) 
		{
			permissions.addAllAdminPermission((ArrayList)corsor.get(ADMIN)); // admin
			permissions.addAllUserPermission((ArrayList)corsor.get(USERS)); // admin
			exactProblem.setAccessPermission(permissions.build());
		}
		return exactProblem.build();

	}

	public static boolean mongoUpdateBankProblem(DB dbs, String problemBankId,String userId, SrlBankProblem problem) throws AuthenticationException
	{
		DBRef myDbRef = new DBRef(dbs, PROBLEM_BANK_COLLECTION, new ObjectId(problemBankId));
		DBObject corsor = myDbRef.fetch();

		ArrayList adminList = (ArrayList<Object>)corsor.get(ADMIN);
		ArrayList<Object> usersList = (ArrayList<Object>)corsor.get(USERS);
		boolean isAdmin,isMod,isUsers;
		isAdmin = Authenticator.checkAuthentication(dbs, userId, adminList);

		if(!isAdmin)
		{
			throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
		}

		BasicDBObject updated = new BasicDBObject();
		if (isAdmin) 
		{
			if (problem.hasQuestionText()) {
				updated.append("$set", new BasicDBObject(QUESTION_TEXT, problem.getQuestionText()));
			}
			if (problem.hasImage()) {
				updated.append("$set", new BasicDBObject(IMAGE, problem.getImage()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (problem.hasSolutionId()) {
				updated.append("$set", new BasicDBObject(SOLUTION_ID, problem.getSolutionId()));
			}
			if (problem.hasCourseTopic()) {
				updated.append("$set", new BasicDBObject(COURSE_TOPIC, problem.getCourseTopic()));
			}
			if (problem.hasSubTopic()) {
				updated.append("$set", new BasicDBObject(SUB_TOPIC, problem.getSubTopic()));
			}
			if (problem.hasSource()) {
				updated.append("$set", new BasicDBObject(SOURCE, problem.getSource()));
			}
			if (problem.hasQuestionType()) {
				updated.append("$set", new BasicDBObject(QUESTION_TYPE, problem.getQuestionType().getNumber()));
			}
			if (problem.getOtherKeywordsCount() > 0) {
				updated.append("$set", new BasicDBObject(KEYWORDS, problem.getOtherKeywordsList()));
			}
		//Optimization: have something to do with pulling values of an array and pushing values to an array
			if (problem.hasAccessPermission()) {
				SrlPermission permissions = problem.getAccessPermission();
				if (isAdmin)
				{
					// ONLY ADMIN CAN CHANGE ADMIN OR MOD
					if (permissions.getAdminPermissionCount() > 0) {
						updated.append("$set", new BasicDBObject(ADMIN, permissions.getAdminPermissionList()));
					}
					if (permissions.getUserPermissionCount() > 0) 
					{
						updated.append("$set", new BasicDBObject(USERS, permissions.getUserPermissionList()));
					}
				}
			}
		}
		return true;
		
	}
}

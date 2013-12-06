package database;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class AssignmentBuilder
{
	String courseId, name, type, other, description, resources, latePolicy, gradeWeigh, openDate, 
	       dueDate, closeDate, imageUrl;
	String[] problemList;
	PermissionBuilder permissions = new PermissionBuilder();
	
	public AssignmentBuilder setCourseId(String x)
	{
		courseId = x;
		return this;
	}
	public AssignmentBuilder setName(String x)
	{
		name = x;
		return this;
	}
	public AssignmentBuilder setType(String x)
	{
		type = x;
		return this;
	}
	public AssignmentBuilder setOther(String x) 
	{
		other = x;
		return this;
	}
	public AssignmentBuilder setDescription(String x)
	{
		description = x;
		return this;
	}
	public AssignmentBuilder setResources(String x)
	{
		resources = x;
		return this;
	}
	public AssignmentBuilder setLatePolicy(String x)
	{
		latePolicy = x;
		return this;
	}
	public AssignmentBuilder setGradeWeigh(String x)
	{
		gradeWeigh = x;
		return this;
	}	
	public AssignmentBuilder setOpenDate(String x) 
	{
		openDate = x;
		return this;
	}
	public AssignmentBuilder setDueDate(String x)
	{
		dueDate = x;
		return this;
	}
	public AssignmentBuilder setCloseDate(String x)
	{
		closeDate = x;
		return this;
	}
	public AssignmentBuilder setImageUrl(String x)
	{
		imageUrl = x;
		return this;
	}
	public AssignmentBuilder setProblemList(String[] x)
	{
		problemList = x;
		return this;
	}
	
	

}
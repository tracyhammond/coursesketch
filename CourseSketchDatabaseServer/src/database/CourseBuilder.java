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

public class CourseBuilder
{
	String description, name, access, semesester, openDate, closeDate, image;
	String[] assignmentList;
	PermissionBuilder permissions = new PermissionBuilder();
	
	public CourseBuilder setDescription(String x)
	{
		description = x;
		return this;
	}
	public CourseBuilder setName(String x)
	{
		name = x;
		return this;
	}
	public CourseBuilder setAccess(String x)
	{
		access = x;
		return this;
	}
	public CourseBuilder setSemesester(String x) 
	{
		semesester = x;
		return this;
	}
	public CourseBuilder setOpenDate(String x)
	{
		openDate = x;
		return this;
	}
	public CourseBuilder setCloseDate(String x)
	{
		closeDate = x;
		return this;
	}
	public CourseBuilder setImage(String x)
	{
		image = x;
		return this;
	}
	public CourseBuilder setAssignmentList(String[] x)
	{
		assignmentList = x;
		return this;
	}	
}
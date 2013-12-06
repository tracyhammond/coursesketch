package mongodb_client;

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

public static class CourseBuilder
{
	String Description, Name, Access, Semesester, OpenDate, CloseDate, Image;
	String[] AssignmentList, Admin, Mod, Users;
	
	public CourseBuilder Description(String x)
	{
		Description = x;
		return this;
	}
	public CourseBuilder Name(String x)
	{
		Name = x;
		return this;
	}
	public CourseBuilder Access(String x)
	{
		Access = x;
		return this;
	}
	public CourseBuilder Semesester(String x) 
	{
		Semesester = x;
		return this;
	}
	public CourseBuilder OpenDate(String x)
	{
		OpenDate = x;
		return this;
	}
	public CourseBuilder CloseDate(String x)
	{
		CloseDate = x;
		return this;
	}
	public CourseBuilder Image(String x)
	{
		Image = x;
		return this;
	}
	public CourseBuilder AssignmentList(String[] x)
	{
		AssignmentList = x;
		return this;
	}	
	public CourseBuilder Admin(String[] x)
	{
		Admin = x;
		return this;
	}	
	public CourseBuilder Mod(String[] x)
	{
		Mod = x;
		return this;
	}	
	public CourseBuilder Users(String[] x)
	{
		Users = x;
		return this;
	}	
}
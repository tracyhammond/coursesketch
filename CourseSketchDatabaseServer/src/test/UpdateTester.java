package test;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.MongoClient;

import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.institution.UpdateManager;

public class UpdateTester {

	public static void main(String[] args) throws UnknownHostException, AuthenticationException, DatabaseAccessException 
	{
		MongoClient mongoClient = new MongoClient("localhost");
		DB db = mongoClient.getDB("login");
		// TODO Auto-generated method stub
		//UpdateManager.mongoInsertUpdate(db, "52fd23b28159fa94ce381552", "1243",16, "assignment");
		//UpdateManager.mongoDeleteUpdate(db, "52fd23b28159fa94ce381552", "1243",121233, "assignment");
		UpdateManager.mongoGetUpdate(db, "52fd23b28159fa94ce381552");
	}

}

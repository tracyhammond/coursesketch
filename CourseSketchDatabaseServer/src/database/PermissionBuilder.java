package database;

import java.util.ArrayList;
import java.util.Date;

import protobuf.srl.school.School.DateTime;

public class PermissionBuilder 
{
	public ArrayList<String> admin;
	public ArrayList<String> mod;
	public ArrayList<String> users;
	
	public PermissionBuilder setAdmin(ArrayList<String> x)
	{
		admin = x;
		return this;
	}
	public PermissionBuilder setMod(ArrayList<String> x)
	{
		mod = x;
		return this;
	}
	public PermissionBuilder setUsers(ArrayList<String> x)
	{
		users = x;
		return this;
	}

	public static boolean isTimeValid(long time, DateTime openDate, DateTime closeDate) {
		return time >= openDate.getMillisecond() && time <= closeDate.getMillisecond();	
	}
}

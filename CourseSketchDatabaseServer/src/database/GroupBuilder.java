package database;

import java.util.ArrayList;

public class GroupBuilder 
{
	public String name;
	public ArrayList<String> userList;
	public ArrayList<String> admin;
	
	public GroupBuilder setName(String x)
	{
		name = x;
		return this;
	}
	public GroupBuilder setuserList(ArrayList<String> x)
	{
		userList = x;
		return this;
	}
	public GroupBuilder setAdmin(ArrayList<String> x)
	{
		admin = x;
		return this;
	}
}

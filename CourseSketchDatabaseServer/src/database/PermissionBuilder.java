package database;

import java.util.ArrayList;

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

}

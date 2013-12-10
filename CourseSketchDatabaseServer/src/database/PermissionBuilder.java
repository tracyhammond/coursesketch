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

	public static boolean isTimeValid(long time, String openDate, String closeDate) {
		long open = Long.parseLong(closeDate.split(" ")[5]);
		long close = Long.parseLong(closeDate.split(" ")[5]);
		return time >= open && time <= close;
	}
}

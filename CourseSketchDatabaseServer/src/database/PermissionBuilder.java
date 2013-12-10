package database;

import java.util.ArrayList;
import java.util.Date;

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
		long open = Long.parseLong(openDate.split(" ")[5]);
		long close = Long.parseLong(closeDate.split(" ")[5]);
		System.out.println("Open: " + new Date(open));
		System.out.println("Time: " + new Date(time));
		System.out.println("Close: " + new Date(close));
		return time >= open && time <= close;	
	}
}

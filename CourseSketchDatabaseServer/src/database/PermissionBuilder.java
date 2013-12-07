package database;

public class PermissionBuilder 
{
	public String[] admin;
	public String[] mod;
	public String[] users;
	
	public PermissionBuilder setAdmin(String[] x)
	{
		admin = x;
		return this;
	}
	public PermissionBuilder setMod(String[] x)
	{
		mod = x;
		return this;
	}
	public PermissionBuilder setUsers(String[] x)
	{
		users = x;
		return this;
	}

}

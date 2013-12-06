package database;

public class PermissionBuilder 
{
	String[] admin, mod, users;
	
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

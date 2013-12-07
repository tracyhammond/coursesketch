package database;

public class GroupBuilder 
{
	public String name;
	public String[] userList;
	public String[] admin;
	
	public GroupBuilder setName(String x)
	{
		name = x;
		return this;
	}
	public GroupBuilder setuserList(String[] x)
	{
		userList = x;
		return this;
	}
	public GroupBuilder setAdmin(String[] x)
	{
		admin = x;
		return this;
	}
}

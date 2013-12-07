package database.course;

import database.PermissionBuilder;

public class CourseBuilder
{
	public String description, name, access, semesester, openDate, closeDate, image;
	public String[] assignmentList;
	public PermissionBuilder permissions = new PermissionBuilder();
	
	public CourseBuilder setDescription(String x)
	{
		description = x;
		return this;
	}
	public CourseBuilder setName(String x)
	{
		name = x;
		return this;
	}
	public CourseBuilder setAccess(String x)
	{
		access = x;
		return this;
	}
	public CourseBuilder setSemesester(String x) 
	{
		semesester = x;
		return this;
	}
	public CourseBuilder setOpenDate(String x)
	{
		openDate = x;
		return this;
	}
	public CourseBuilder setCloseDate(String x)
	{
		closeDate = x;
		return this;
	}
	public CourseBuilder setImage(String x)
	{
		image = x;
		return this;
	}
	public CourseBuilder setAssignmentList(String[] x)
	{
		assignmentList = x;
		return this;
	}	
}
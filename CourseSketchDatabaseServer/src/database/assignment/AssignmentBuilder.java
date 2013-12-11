package database.assignment;

import java.util.ArrayList;

import database.PermissionBuilder;

public class AssignmentBuilder
{
	public String courseId, name, type, other, description, resources, latePolicy, gradeWeight, openDate, 
	       dueDate, closeDate, imageUrl;
	public String id;
	public ArrayList<String> problemList;
	public PermissionBuilder permissions = new PermissionBuilder();
	
	public AssignmentBuilder setCourseId(String x)
	{
		courseId = x;
		return this;
	}
	public AssignmentBuilder setName(String x)
	{
		name = x;
		return this;
	}
	public AssignmentBuilder setType(String x)
	{
		type = x;
		return this;
	}
	public AssignmentBuilder setOther(String x) 
	{
		other = x;
		return this;
	}
	public AssignmentBuilder setDescription(String x)
	{
		description = x;
		return this;
	}
	public AssignmentBuilder setResources(String x)
	{
		resources = x;
		return this;
	}
	public AssignmentBuilder setLatePolicy(String x)
	{
		latePolicy = x;
		return this;
	}
	public AssignmentBuilder setGradeWeigh(String x)
	{
		gradeWeight = x;
		return this;
	}	
	public AssignmentBuilder setOpenDate(String x) 
	{
		openDate = x;
		return this;
	}
	public AssignmentBuilder setDueDate(String x)
	{
		dueDate = x;
		return this;
	}
	public AssignmentBuilder setCloseDate(String x)
	{
		closeDate = x;
		return this;
	}
	public AssignmentBuilder setImageUrl(String x)
	{
		imageUrl = x;
		return this;
	}
	public AssignmentBuilder setProblemList(ArrayList<String> x)
	{
		problemList = x;
		return this;
	}
	public AssignmentBuilder setId(String x) {
		id = x;
		return this;
	}
	
	

}
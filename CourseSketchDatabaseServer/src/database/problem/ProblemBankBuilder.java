package database.problem;

import database.PermissionBuilder;

public class ProblemBankBuilder 
{
	
	
	public String questionText, qestionImageName, questionAnswerId, courseTopic, subTopic, source, questionType, id;
	public String[] access, otherKeywords;
	PermissionBuilder permissions = new PermissionBuilder();
	//admin permission is normal
	//mdo permission is normal
	//user permission is a courseID
	public ProblemBankBuilder setQuestionText(String x)
	{
		questionText = x;
		return this;
	}
	public ProblemBankBuilder setQestionImageName(String x)
	{
		qestionImageName = x;
		return this;
	}
	public ProblemBankBuilder setQuestionAnswerId(String x)
	{
		questionAnswerId = x;
		return this;
	}
	public ProblemBankBuilder setCourseTopic(String x) 
	{
		courseTopic = x;
		return this;
	}
	public ProblemBankBuilder setSubTopic(String x)
	{
		subTopic = x;
		return this;
	}
	public ProblemBankBuilder setSource(String x)
	{
		source = x;
		return this;
	}
	public ProblemBankBuilder setQuestionType(String x)
	{
		questionType = x;
		return this;
	}
	public ProblemBankBuilder setAccess(String[] x)
	{
		access = x;
		return this;
	}	
	public ProblemBankBuilder setOtherKeywords(String[] x) 
	{
		otherKeywords = x;
		return this;
	}

	

}

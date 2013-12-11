package database.problem;

import database.PermissionBuilder;

public class CourseProblemBuilder {
	public String courseId;
	public String assignmentId;
	public String id;
	public String gradeWeight; // either points or percent
	public String problemBankId;
	public ProblemBankBuilder problemResource;
	PermissionBuilder permissions;

	public void setCourseId(String id) {
		courseId = id;
	}

	public void setAssignmentId(String id) {
		assignmentId = id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProblemBankId(String id) {
		problemBankId = id;
	}

	public void setPermissions(PermissionBuilder per) {
		permissions = per;
	}

	public void setGradeWeight(String weight) {
		gradeWeight = weight;
	}
}

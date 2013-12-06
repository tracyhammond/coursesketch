package database;

public class CourseProblemBuilder {
	String courseId, assignmentId, problemId;
	String gradeWeight; // either points or percent
	String problemBankId;
	PermissionBuilder permissions;

	public void setCourseId(String id) {
		courseId = id;
	}

	public void setAssignmentId(String id) {
		assignmentId = id;
	}

	public void setProblemId(String id) {
		problemId = id;
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

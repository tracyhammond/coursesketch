package grade;

public class SubmissionInfo {
	private String submissionId = "";
	private String username = "";
	private String assignment = "";
	private String problem = "";
	private String filename = "";

	public SubmissionInfo() {
	}

	public SubmissionInfo(String[] info, String filename) {
		this.submissionId = info[0];
		this.username = info[1];
		this.assignment = info[2];
		this.problem = info[3];
		this.filename = filename;
	}
	
	public SubmissionInfo(String submissionId, String username, String assignment, String problem, String filename) {
		this.submissionId = submissionId;
		this.username = username;
		this.assignment = assignment;
		this.problem = problem;
		this.filename = filename;
	}

	public String getUsername() {
		return username;
	}

	public String getAssignment() {
		return assignment;
	}

	public String getProblem() {
		return problem;
	}

	public String getFileame() {
		return filename;
	}
}

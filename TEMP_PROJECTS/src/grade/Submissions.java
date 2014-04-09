package grade;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Submissions {
	private List<SubmissionInfo> submissions = new ArrayList<SubmissionInfo>();
	
	public Submissions() {
	}
	
	public void addSubmission(SubmissionInfo sInfo) {
		submissions.add(sInfo);
	}
	
	public Set<String> getUsernames(String assignment, String problem) {
		Set<String> usernames = new TreeSet<String>();
		for (SubmissionInfo submission : submissions) {
			if (!usernames.contains(submission.getUsername()))
				if (assignment.equals("") || assignment.equals(submission.getAssignment()))
					if (problem.equals("") || problem.equals(submission.getProblem()))
							usernames.add(submission.getUsername());
		}
		return usernames;
	}

	public Set<String> getAssignemtns(String username, String problem) {
		Set<String> assignments = new TreeSet<String>();
		for (SubmissionInfo submission : submissions) {
			if (!assignments.contains(submission.getAssignment()))
				if (username.equals("") || username.equals(submission.getUsername()))
					if (problem.equals("") || problem.equals(submission.getProblem()))
						assignments.add(submission.getAssignment());
		}
		return assignments;
	}
	
	public Set<String> getProblems(String username, String assignment) {
		Set<String> problems = new TreeSet<String>();
		for (SubmissionInfo submission : submissions) {
			if (!problems.contains(submission.getProblem()))
				if (assignment.equals("") || assignment.equals(submission.getAssignment()))
					if (username.equals("") || username.equals(submission.getUsername()))
						problems.add(submission.getProblem());
		}
		return problems;
	}
	
	public String getFilename(String username, String assignment, String problem) {
		for (SubmissionInfo submission : submissions) {
			if (username.equals(submission.getUsername()) && assignment.equals(submission.getAssignment()) && problem.equals(submission.getProblem()))
				return submission.getFileame();	
		}
		return "";
	}
	
	public void clear() {
		submissions.clear();
	}
}

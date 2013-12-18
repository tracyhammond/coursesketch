package database;

public class StringConstants {
	// Id
	public static final String SELF_ID = "_id";
	public static final String COURSE_ID = "CourseId";
	public static final String ASSIGNMENT_ID = "AssignmentId";
	public static final String PROBLEM_BANK_ID = "ProblemBankId";
	public static final String COURSE_PROBLEM_ID = "CourseProblemId";
	public static final String SOLUTION_ID = "SolutionId";

	// collections
	public static final String COURSE_COLLECTION = "Courses";
	public static final String ASSIGNMENT_COLLECTION = "Assignments";
	public static final String PROBLEM_BANK_COLLECTION = "Problems";
	public static final String COURSE_PROBLEM_COLLECTION = "ProblemBank";

	// meta fields
	public static final String NAME = "Name";
	public static final String DESCRIPTION = "Description";
	public static final String IMAGE = "Image";

	// date
	public static final String ACCESS_DATE = "AccessDate";
	public static final String DUE_DATE = "DueDate";
	public static final String CLOSE_DATE = "CloseDate";

	// permissions
	public static final String ADMIN = "Admin";
	public static final String MOD = "Mod";
	public static final String USERS = "Users";

	// grades
	public static final String GRADE_WEIGHT = "GradeWeight";
	public static final String GRADE = "Grade";

	// course specific
	public static final String COURSE_SEMESTER = "Semester";
	public static final String COURSE_ACCESS = "Access";
	public static final String ASSIGNMENT_LIST = "AssignmentList";

	// assignment specific
	public static final String ASSIGNMENT_TYPE = "AssignmentType";
	public static final String ASSIGNMENT_OTHER_TYPE = "OtherType";
	public static final String ASSIGNMENT_RESOURCES = "Resources";
	public static final String LATE_POLICY = "LatePolicy";
	public static final String PROBLEM_LIST = "ProblemList";
	
	//solution specific
	public static final String ALLOWED_IN_PROBLEMBANK = "AllowedInProblemBank";
	public static final String IS_PRACTICE_PROBLEM = "PracticeProblem";
	
	//submission specific
	public static final String UPDATELIST = "Solution";
	public static final String SKETCH = "Sketch";

	// problem specific
	public static final String QUESTION_TEXT = "QuestionText";
	public static final String COURSE_TOPIC = "CourseTopic";
	public static final String SUB_TOPIC = "SubTopic";
	public static final String SOURCE = "Source";
	public static final String QUESTION_TYPE = "QuestionType";
	public static final String KEYWORDS = "OtherKeywords";

}

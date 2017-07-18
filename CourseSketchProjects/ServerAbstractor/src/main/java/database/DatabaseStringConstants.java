package database;

/**
 * Contains a list of useful constants used by the database.
 *
 * @author gigemjt
 */
@SuppressWarnings({ "PMD.CommentRequired", "checkstyle:javadocvariable" })
public final class DatabaseStringConstants {
    // Id
    public static final String SELF_ID = "_id";
    public static final String COURSE_ID = "CourseId";
    public static final String ASSIGNMENT_ID = "AssignmentId";
    public static final String PROBLEM_BANK_ID = "ProblemBankId";
    public static final String COURSE_PROBLEM_ID = "CourseProblemId";
    public static final String SOLUTION_ID = "SolutionId";
    public static final String EXPERIMENT_ID = "ExperimentId";
    public static final String USER_ID = "UserId";
    public static final String SCHOOLITEMID = "SchoolItemID";
    public static final String OWNER_ID = "OWNER";
    public static final String ITEM_ID = "ItemId";

    // collections
    public static final String GRADE_COLLECTION = "Grades";
    public static final String GRADING_POLICY_COLLECTION = "GradingPolicies";

    // also contains groups for admins and mods
    public static final String USER_GROUP_COLLECTION = "UserGroups";
    public static final String USER_COLLECTION = "Users";
    public static final String SOLUTION_COLLECTION = "Solutions";
    public static final String EXPERIMENT_COLLECTION = "Experiments";
    public static final String STATE_COLLECTION = "UserStates";
    public static final String UPDATE_COLLECTION = "User_Updates";
    public static final String LOGIN_COLLECTION = "CourseSketchUsers";
    public static final String DATABASE = "institution";
    public static final String LOGIN_DATABASE = "login";
    public static final String AUTH_DATABASE = "auth";
    public static final String IDENTITY_DATABASE = "identity";
    public static final String SUBMISSION_DATABASE = "submissions";
    public static final String RECOGNITION_DATABASE = "Recognition";

    // meta fields
    public static final String NAME = "Name";
    public static final String DESCRIPTION = "Description";
    public static final String IMAGE = "Image";
    public static final String COURSE = "Course";
    public static final String ASSIGNMENT = "Assignment";
    public static final String COURSE_PROBLEM = "CourseProblem";

    // date
    public static final String ACCESS_DATE = "AccessDate";
    public static final String DUE_DATE = "DueDate";
    public static final String CLOSE_DATE = "CloseDate";
    public static final String REVIEW_OPEN_DATE = "ReviewOpenDate"; // The date that the assignment is open for review
    public static final String GRADED_DATE = "GradedDate"; // The date the item was graded.

    // permissions
    public static final String ADMIN = "Admin";
    public static final String MOD = "Mod";
    public static final String USERS = "Users";
    public static final String ADMIN_GROUP_ID = "AdminGroupId";
    public static final String MOD_GROUP_ID = "ModGroupId";
    public static final String USER_GROUP_ID = "UserGroupId";
    public static final int PERMISSION_LEVELS = 3;

    // grades
    public static final String GRADE_WEIGHT = "GradeWeight";
    public static final String GRADE = "Grade";
    public static final String COMMENT = "Comment";
    public static final String CURRENT_GRADE = "CurrentGrade";
    public static final String GRADE_HISTORY = "GradeHistory";
    public static final String GRADE_VALUE = "GradeValue";
    public static final String WHO_CHANGED = "WhoChanged";
    public static final String EXTERNAL_GRADE = "ExternalGrade";
    public static final String SUBMISSION_NUMBER = "SubmissionNumber";

    // grading policy
    public static final String GRADE_CATEGORIES = "GradeCategories";
    public static final String GRADE_CATEGORY_WEIGHT = "CategoryWeight";
    public static final String GRADE_CATEGORY_NAME = "CategoryName";
    public static final String GRADE_POLICY_TYPE = "GradePolicyType";
    public static final String DROPPED_PROBLEMS = "DroppedProblems";
    public static final String DROPPED_ASSIGNMENTS = "DroppedAssignments";
    public static final String DROP_TYPE = "DropType";

    // default categories
    public static final String TEST_CATEGORY = "Test";
    public static final String HOMEWORK_CATEGORY = "Homework";
    public static final String QUIZ_CATEGORY = "Quiz";

    // course specific
    public static final String COURSE_SEMESTER = "Semester";
    public static final String COURSE_ACCESS = "Access";
    public static final String ASSIGNMENT_LIST = "AssignmentList";
    public static final String LECTURE_LIST = "LectureList";

    // assignment specific
    public static final String ASSIGNMENT_TYPE = "AssignmentType";
    public static final String NAVIGATION_TYPE = "NavigationType";
    public static final String ASSIGNMENT_CATEGORY = "AssignmentCategory";
    public static final String ASSIGNMENT_RESOURCES = "Resources";
    public static final String LATE_POLICY = "LatePolicy";
    public static final String LATE_POLICY_FUNCTION_TYPE = "LatePolicyFunctionType";
    public static final String LATE_POLICY_TIME_FRAME_TYPE = "LatePolicyTimeFrameType";
    public static final String LATE_POLICY_SUBTRACTION_TYPE = "LatePolicySubtractionType";
    public static final String LATE_POLICY_RATE = "LatePolicyRate";
    public static final String APPLY_ONLY_TO_LATE_PROBLEMS = "ApplyOnlyToLateProblems";
    public static final String PROBLEM_LIST = "ProblemList";

    // course problem specific
    public static final String PROBLEM_NUMBER = "ProblemNumber";

    // solution specific
    public static final String ALLOWED_IN_PROBLEMBANK = "AllowedInProblemBank";
    public static final String IS_PRACTICE_PROBLEM = "PracticeProblem";

    // submission specific
    public static final String UPDATELIST = "UpdateList";
    public static final String TEXT_ANSWER = "TextAnswer";
    public static final String SKETCH = "Sketch";
    public static final String ANSWER_CHOICE = "AnswerChoice";
    public static final String SUBMISSION_TIME = "time";
    public static final String FIRST_STROKE_TIME = "FirstStrokeTime";
    public static final String FIRST_SUBMISSION_TIME = "FirstSubmissionTime";

    // problem specific
    public static final String QUESTION_TEXT = "QuestionText";
    public static final String COURSE_TOPIC = "CourseTopic";
    public static final String SUB_TOPIC = "SubTopic";
    public static final String SOURCE = "Source";
    public static final String QUESTION_TYPE = "QuestionType";
    public static final String KEYWORDS = "OtherKeywords";
    public static final String SCRIPT = "Script";
    public static final String BASE_SKETCH = "BaseSketch";
    public static final String SPECIAL_QUESTION_DATA = "SpecialQuestionData";

    // state specific
    public static final String STATE_PUBLISHED = "Published"; // Instructor has finished construction of school item
    public static final String STATE_STARTED = "Started"; // Has been made the school item available to be viewed and worked on
    public static final String STATE_COMPLETED = "Completed"; // Has been made the school item available to be viewed and worked on
    public static final String STATE_GRADED = "Graded"; // Has been made the school item available to be viewed and worked on

    // user specific
    public static final String COURSE_LIST = "CourseList";
    public static final String FIRST_NAME = "FirstName";
    public static final String LAST_NAME = "LastName";
    public static final String USER_NAME = "UserName";
    public static final String EMAIL = "Email";
    public static final String SCHOOL_IDENTIFICATION = "UIN";
    public static final String CREDENTIALS = "Credentials";
    public static final String UPDATE = "Update";
    public static final String CLASSIFICATION = "Classification";
    public static final String UPDATEID = "UpdateId";
    public static final String TIME = "Time";

    // login specific
    public static final String PASSWORD = "Password";
    public static final String IS_DEFAULT_INSTRUCTOR = "IsInstructor";
    public static final String INSTRUCTOR_ID = "InstructorId";
    public static final String STUDENT_ID = "StudentId";
    public static final String STUDENT_CLIENT_ID = "StudentClientId";
    public static final String INSTRUCTOR_CLIENT_ID = "InstructorClientId";
    public static final String LAST_LOGIN_TIMES = "LoginSuccesses";
    public static final String LOGIN_AMOUNT_FIELD = "LoginCount";
    public static final String IDENTITY_AUTH = "Identity Auth";

    // user group specific
    public static final String NON_USER_LIST = "NonUserList"; // used for moderators, peer teachers, and admins
    public static final String USER_LIST = "UserList";
    public static final String GROUP_PREFIX = "group";
    public static final int GROUP_PREFIX_LENGTH = GROUP_PREFIX.length();
    public static final String SALT = "Salt";
    public static final String REGISTRATION_KEY = "RegistrationKey";

    // mongo commands
    public static final String SET_COMMAND = "$set";
    public static final String UPSERT_COMMAND = "$setOnInsert";
    public static final String ADD_SET_COMMAND = "$addToSet";
    public static final String EXISTS = "$exists";
    public static final String PUSH_COMMAND = "$push";
    public static final String EACH_COMMAND = "$each";
    public static final String SORT_COMMAND = "$sort";
    public static final String SLICE_COMMAND = "$slice";
    public static final String INCREMENT_COMMAND = "$inc";
    public static final String IN_COMMAND = "$in";
    public static final String SUBFIELD_COMMAND = ".";

    // state and grade specific
    public static final String SCHOOL_ITEM_TYPE = "SchoolItemType";

    // lecture specific
    public static final String SLIDES = "Slides";
    public static final String IS_UNLOCKED = "isUnlocked";
    public static final String ELEMENT_LIST = "Elements";
    public static final String IDS_IN_LECTURE = "idsInLecture";
    public static final String X_POSITION = "X";
    public static final String Y_POSITION = "Y";
    public static final String X_DIMENSION = "XDIM";
    public static final String Y_DIMENSION = "YDIM";
    public static final String SLIDE_BLOB_TYPE = "TYPE";
    public static final String SLIDE_BLOB = "BLOB";
}

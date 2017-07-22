package coursesketch.database.institution.mongo;

import com.coursesketch.test.utilities.AuthenticationHelper;
import com.coursesketch.test.utilities.ProtobufComparisonBuilder;
import com.github.fakemongo.junit.FongoRule;
import com.mongodb.client.MongoDatabase;
import coursesketch.database.util.DatabaseAccessException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.grading.Grading.PolicyCategory;
import protobuf.srl.grading.Grading.DroppedAssignment;
import protobuf.srl.grading.Grading.DroppedProblems;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.DropType;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;
import protobuf.srl.school.School.SrlCourse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static coursesketch.database.util.DatabaseStringConstants.APPLY_ONLY_TO_LATE_PROBLEMS;
import static coursesketch.database.util.DatabaseStringConstants.ASSIGNMENT_ID;
import static coursesketch.database.util.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static coursesketch.database.util.DatabaseStringConstants.DROPPED_ASSIGNMENTS;
import static coursesketch.database.util.DatabaseStringConstants.DROPPED_PROBLEMS;
import static coursesketch.database.util.DatabaseStringConstants.DROP_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_CATEGORIES;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_CATEGORY_NAME;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_CATEGORY_WEIGHT;
import static coursesketch.database.util.DatabaseStringConstants.GRADE_POLICY_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.GRADING_POLICY_COLLECTION;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_FUNCTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_RATE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_SUBTRACTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.LATE_POLICY_TIME_FRAME_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static coursesketch.database.institution.mongo.MongoInstitutionTest.genericDatabaseMock;
import static coursesketch.database.util.MongoUtilities.convertStringToObjectId;

/**
 * Tests for GradingPolicyManager.
 * @see GradingPolicyManager
 *
 * Created by Matt on 4/6/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class GradingPolicyManagerTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Rule
    public FongoRule fongo = new FongoRule();
    @Mock
    AuthenticationChecker authChecker;
    @Mock
    AuthenticationOptionChecker optionChecker;

    public MongoDatabase db;
    private Authenticator authenticator;
    private SrlCourse.Builder courseBuilder = SrlCourse.newBuilder();

    private ProtoGradingPolicy.Builder fakeProtoPolicy = ProtoGradingPolicy.newBuilder();
    private PolicyCategory.Builder fakeProtoCategory1 = PolicyCategory.newBuilder();
    private PolicyCategory.Builder fakeProtoCategory2 = PolicyCategory.newBuilder();
    private DroppedAssignment.Builder fakeProtoDropAsgn1 = DroppedAssignment.newBuilder();
    private DroppedAssignment.Builder fakeProtoDropAsgn2 = DroppedAssignment.newBuilder();
    private DroppedProblems.Builder fakeProtoDropProbs1 = DroppedProblems.newBuilder();
    private DroppedProblems.Builder fakeProtoDropProbs2 = DroppedProblems.newBuilder();
    private DroppedProblems.SingleProblem.Builder fakeProtoDropProblem1 = DroppedProblems.SingleProblem.newBuilder();
    private DroppedProblems.SingleProblem.Builder fakeProtoDropProblem2 = DroppedProblems.SingleProblem.newBuilder();
    private LatePolicy.Builder fakeProtoLate = LatePolicy.newBuilder();

    private Document fakeMongoPolicy = new Document();
    private Document fakeMongoCategory1 = new Document();
    private Document fakeMongoCategory2 = new Document();
    private Document fakeMongoLate = new Document();
    private Map<String, List<Document>> fakeDroppedProblems = new HashMap<>();
    private List<Document> droppedProblemsList = new ArrayList<>();
    private ArrayList<Document> fakeCategories = new ArrayList<>();
    private List<Document> fakeDroppedAssignments = new ArrayList<>();

    private static final String FAKE_COURSE_ID = "courseId";
    private static final String FAKE_USER_ID = "userId";
    private static final String FAKE_ADMIN_ID = "adminId";
    private static final String FAKE_ASGN_ID = "assignmentId";
    private static final String FAKE_PROB_ID = "problemId";
    private static final String FAKE_CATEGORY_NAME = "category";
    private static final double FAKE_CATEGORY_WEIGHT = 25;
    private static final int FAKE_ENUM = 1;
    private static final double FAKE_LATE_RATE = 25;
    private static final boolean FAKE_BOOL = true;

    @Before
    public void before() {
        db = fongo.getDatabase();

        genericDatabaseMock(authChecker, optionChecker);
        authenticator = new Authenticator(authChecker, optionChecker);

        /**
         * Fake proto objects setup.
         */
        fakeProtoLate.setFunctionType(LatePolicy.FunctionType.valueOf(FAKE_ENUM));
        fakeProtoLate.setTimeFrameType(LatePolicy.TimeFrame.valueOf(FAKE_ENUM));
        fakeProtoLate.setRate(FAKE_LATE_RATE);
        fakeProtoLate.setSubtractionType(LatePolicy.SubtractionType.valueOf(FAKE_ENUM));
        fakeProtoLate.setApplyOnlyToLateProblems(FAKE_BOOL);

        fakeProtoCategory1.setName(FAKE_CATEGORY_NAME + "1").setWeight(FAKE_CATEGORY_WEIGHT).setLatePolicy(fakeProtoLate.build());
        fakeProtoCategory2 = fakeProtoCategory1.clone().setName(FAKE_CATEGORY_NAME + "2");

        fakeProtoDropAsgn1.setAssignmentId(FAKE_ASGN_ID + "1").setDropType(DropType.valueOf(FAKE_ENUM));
        fakeProtoDropAsgn2 = fakeProtoDropAsgn1.clone().setAssignmentId(FAKE_ASGN_ID + "2");

        fakeProtoDropProblem1.setProblemId(FAKE_PROB_ID + "1").setDropType(DropType.valueOf(FAKE_ENUM));
        fakeProtoDropProblem2 = fakeProtoDropProblem1.clone().setProblemId(FAKE_PROB_ID + "2");

        fakeProtoDropProbs1.setAssignmentId(FAKE_ASGN_ID + "1").addProblem(fakeProtoDropProblem1.build()).addProblem(fakeProtoDropProblem2.build());
        fakeProtoDropProbs2 = fakeProtoDropProbs1.clone().setAssignmentId(FAKE_ASGN_ID + "2");

        fakeProtoPolicy.setCourseId(FAKE_COURSE_ID);
        fakeProtoPolicy.setPolicyType(ProtoGradingPolicy.PolicyType.valueOf(FAKE_ENUM));
        fakeProtoPolicy.addGradeCategories(fakeProtoCategory1.build()).addGradeCategories(fakeProtoCategory2.build());
        fakeProtoPolicy.addDroppedAssignments(fakeProtoDropAsgn1.build()).addDroppedAssignments(fakeProtoDropAsgn2.build());
        fakeProtoPolicy.addDroppedProblems(fakeProtoDropProbs1.build()).addDroppedProblems(fakeProtoDropProbs2.build());

        courseBuilder.setId(FAKE_COURSE_ID);

        /**
         * Fake mongo Documents setup.
         */
        fakeMongoLate = new Document(LATE_POLICY_FUNCTION_TYPE, FAKE_ENUM)
                .append(LATE_POLICY_TIME_FRAME_TYPE, FAKE_ENUM).append(LATE_POLICY_RATE, FAKE_LATE_RATE)
                .append(LATE_POLICY_SUBTRACTION_TYPE, FAKE_ENUM)
                .append(APPLY_ONLY_TO_LATE_PROBLEMS, FAKE_BOOL);

        fakeMongoCategory1 = new Document(GRADE_CATEGORY_NAME, FAKE_CATEGORY_NAME + "1")
                .append(GRADE_CATEGORY_WEIGHT, FAKE_CATEGORY_WEIGHT)
                .append(LATE_POLICY, fakeMongoLate);

        fakeMongoCategory2 = new Document(fakeMongoCategory1);
        fakeMongoCategory2.put(GRADE_CATEGORY_NAME, FAKE_CATEGORY_NAME + "2");

        fakeCategories.add(fakeMongoCategory1);
        fakeCategories.add(fakeMongoCategory2);

        Document singleProblem1 = new Document(COURSE_PROBLEM_ID, FAKE_PROB_ID + "1")
                .append(DROP_TYPE, FAKE_ENUM);
        Document singleProblem2 = new Document(COURSE_PROBLEM_ID, FAKE_PROB_ID + "2")
                .append(DROP_TYPE, FAKE_ENUM);
        droppedProblemsList.add(singleProblem1);
        droppedProblemsList.add(singleProblem2);

        fakeDroppedProblems.put(FAKE_ASGN_ID + "1", droppedProblemsList);
        fakeDroppedProblems.put(FAKE_ASGN_ID + "2", droppedProblemsList);

        fakeDroppedAssignments.add(new Document(ASSIGNMENT_ID, FAKE_ASGN_ID + "1").append(DROP_TYPE, FAKE_ENUM));
        fakeDroppedAssignments.add(new Document(ASSIGNMENT_ID, FAKE_ASGN_ID + "2").append(DROP_TYPE, FAKE_ENUM));

        fakeMongoPolicy.append(SELF_ID, FAKE_COURSE_ID).append(GRADE_POLICY_TYPE, FAKE_ENUM).append(GRADE_CATEGORIES, fakeCategories)
                .append(DROPPED_PROBLEMS, fakeDroppedProblems).append(DROPPED_ASSIGNMENTS, fakeDroppedAssignments);
    }

    @Test
    public void buildMongoLatePolicyTest() {
        Document testLatePolicy = GradingPolicyManager.buildMongoLatePolicy(fakeProtoLate.build());
        Assert.assertEquals(fakeMongoLate, testLatePolicy);
    }

    @Test
    public void buildProtoLatePolicyTest() {
        LatePolicy testLatePolicy = GradingPolicyManager.buildProtoLatePolicy(fakeMongoLate);
        new ProtobufComparisonBuilder().build().equals(fakeProtoLate.build(), testLatePolicy);
    }

    @Test
    public void buildMongoDroppedProblemObjectTest() {
        List<Document> testDroppedProblems = GradingPolicyManager.buildMongoDroppedProblemObject(fakeProtoDropProbs1.build());
        Assert.assertEquals(fakeDroppedProblems.get(FAKE_ASGN_ID + "1"), testDroppedProblems);
    }

    @Test
    public void buildProtoDroppedProblemsTest() {
        DroppedProblems.Builder testDroppedProblem = GradingPolicyManager.buildProtoDroppedProblems(fakeDroppedProblems.get(FAKE_ASGN_ID + "1"));
        testDroppedProblem.setAssignmentId(FAKE_ASGN_ID + "1");
        new ProtobufComparisonBuilder().setIgnoreListOrder(true).build().equals(fakeProtoDropProbs1.build(), testDroppedProblem.build());
    }

    @Test
    public void buildProtoCategoryTest() {
        PolicyCategory testProtoCategory = GradingPolicyManager.buildProtoCategory(fakeMongoCategory1);
        new ProtobufComparisonBuilder().build().equals(fakeProtoCategory1.build(), testProtoCategory);
    }

    @Test
    public void buildMongoCategoryTest() {
        Document testMongoCategory = GradingPolicyManager.buildMongoCategory(fakeProtoCategory1.build());
        Assert.assertEquals(fakeMongoCategory1, testMongoCategory);
    }

    @Test
    public void firstInsertGradingPolicyTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, FAKE_ADMIN_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradingPolicyManager.upsertGradingPolicy(authenticator, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());
        Document testPolicy = db.getCollection(GRADING_POLICY_COLLECTION).find().first();

        fakeMongoPolicy.put(SELF_ID, new ObjectId(courseId));
        Assert.assertEquals(fakeMongoPolicy, testPolicy);
    }

    @Test
    public void updateGradingPolicyTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, FAKE_ADMIN_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, FAKE_USER_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        GradingPolicyManager.upsertGradingPolicy(authenticator, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());

        ProtoGradingPolicy testPolicyProto = GradingPolicyManager.getGradingPolicy(authenticator, db, courseId, FAKE_USER_ID);
        Document testPolicy = db.getCollection(GRADING_POLICY_COLLECTION).find(convertStringToObjectId(courseId)).first();

        fakeMongoPolicy.put(SELF_ID, new ObjectId(courseId));
        new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false).setIgnoreListOrder(true)
                .build().equals(fakeProtoPolicy.build(), testPolicyProto);
        Assert.assertEquals(fakeMongoPolicy, testPolicy);

        // Update the grade policy

        fakeProtoPolicy.clearDroppedAssignments();
        GradingPolicyManager.upsertGradingPolicy(authenticator, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());
        testPolicy = db.getCollection(GRADING_POLICY_COLLECTION).find(convertStringToObjectId(courseId)).first();

        fakeMongoPolicy.put(DROPPED_ASSIGNMENTS, new ArrayList<>());
        Assert.assertEquals(fakeMongoPolicy, testPolicy);
        Assert.assertEquals(db.getCollection(GRADING_POLICY_COLLECTION).count(), 1); // Only 1 document in the collection

        testPolicyProto = GradingPolicyManager.getGradingPolicy(authenticator, db, courseId, FAKE_USER_ID);

        new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false).setIgnoreListOrder(true)
                .build().equals(fakeProtoPolicy.build(), testPolicyProto);
    }

    @Test
    public void getGradingPolicyTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, FAKE_ADMIN_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, FAKE_USER_ID, null,
                Authentication.AuthResponse.PermissionLevel.STUDENT);

        GradingPolicyManager.upsertGradingPolicy(authenticator, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());

        ProtoGradingPolicy testPolicy = GradingPolicyManager.getGradingPolicy(authenticator, db, courseId, FAKE_USER_ID);

        new ProtobufComparisonBuilder()
                .setFailAtFirstMisMatch(false).setIgnoreListOrder(true)
                .build().equals(fakeProtoPolicy.build(), testPolicy);
    }

    @Test(expected = AuthenticationException.class)
    public void unauthenticatedAddGradingPolicy() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);
        GradingPolicyManager.upsertGradingPolicy(authenticator, db, FAKE_USER_ID, fakeProtoPolicy.build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void policyDoesNotExist() throws Exception {
        GradingPolicyManager.getGradingPolicy(authenticator, db, new ObjectId().toString(), FAKE_ADMIN_ID);
    }

    @Test
    public void userNotInCourseGetPolicy() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);

        AuthenticationHelper.setMockPermissions(authChecker, Util.ItemType.COURSE, courseId, FAKE_ADMIN_ID, null,
                Authentication.AuthResponse.PermissionLevel.TEACHER);

        GradingPolicyManager.upsertGradingPolicy(authenticator, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());

        exception.expect(AuthenticationException.class);

        GradingPolicyManager.getGradingPolicy(authenticator, db, courseId, "notInCourse");
    }
}

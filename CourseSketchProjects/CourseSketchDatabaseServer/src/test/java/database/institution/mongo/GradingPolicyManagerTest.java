package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import org.hsqldb.Database;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.grading.Grading.PolicyCategory;
import protobuf.srl.grading.Grading.DroppedAssignment;
import protobuf.srl.grading.Grading.DroppedProblems;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.DropType;
import protobuf.srl.utils.Util;
import protobuf.srl.school.School.SrlCourse;

import database.DatabaseStringConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.APPLY_ONLY_TO_LATE_PROBLEMS;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.COURSE_PROBLEM_ID;
import static database.DatabaseStringConstants.DROPPED_ASSIGNMENTS;
import static database.DatabaseStringConstants.DROPPED_PROBLEMS;
import static database.DatabaseStringConstants.DROP_TYPE;
import static database.DatabaseStringConstants.GRADE_CATEGORIES;
import static database.DatabaseStringConstants.GRADE_CATEGORY_NAME;
import static database.DatabaseStringConstants.GRADE_CATEGORY_WEIGHT;
import static database.DatabaseStringConstants.GRADE_POLICY_TYPE;
import static database.DatabaseStringConstants.GRADING_POLICY_COLLECTION;
import static database.DatabaseStringConstants.LATE_POLICY;
import static database.DatabaseStringConstants.LATE_POLICY_FUNCTION_TYPE;
import static database.DatabaseStringConstants.LATE_POLICY_RATE;
import static database.DatabaseStringConstants.LATE_POLICY_SUBTRACTION_TYPE;
import static database.DatabaseStringConstants.LATE_POLICY_TIME_FRAME_TYPE;
import static database.DatabaseStringConstants.SELF_ID;

/**
 * Created by Matt on 4/6/2015.
 */
public class GradingPolicyManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;
    public Util.SrlPermission.Builder permissionBuilder = Util.SrlPermission.newBuilder();
    public SrlCourse.Builder courseBuilder = SrlCourse.newBuilder();

    public ProtoGradingPolicy.Builder fakeProtoPolicy = ProtoGradingPolicy.newBuilder();
    public PolicyCategory.Builder fakeProtoCategory1 = PolicyCategory.newBuilder();
    public PolicyCategory.Builder fakeProtoCategory2 = PolicyCategory.newBuilder();
    public DroppedAssignment.Builder fakeProtoDropAsgn1 = DroppedAssignment.newBuilder();
    public DroppedAssignment.Builder fakeProtoDropAsgn2 = DroppedAssignment.newBuilder();
    public DroppedProblems.Builder fakeProtoDropProbs1 = DroppedProblems.newBuilder();
    public DroppedProblems.Builder fakeProtoDropProbs2 = DroppedProblems.newBuilder();
    public DroppedProblems.SingleProblem.Builder fakeProtoDropProblem1 = DroppedProblems.SingleProblem.newBuilder();
    public DroppedProblems.SingleProblem.Builder fakeProtoDropProblem2 = DroppedProblems.SingleProblem.newBuilder();
    public LatePolicy.Builder fakeProtoLate = LatePolicy.newBuilder();

    public BasicDBObject fakeMongoPolicy = new BasicDBObject();
    public BasicDBObject fakeMongoCategory1 = new BasicDBObject();
    public BasicDBObject fakeMongoCategory2 = new BasicDBObject();
    public BasicDBObject fakeMongoLate = new BasicDBObject();
    Map<String, List<BasicDBObject>> fakeDroppedProblems = new HashMap<>();
    List<BasicDBObject> droppedProblemsList = new ArrayList<>();
    ArrayList<BasicDBObject> fakeCategories = new ArrayList<>();
    List<BasicDBObject> fakeDroppedAssignments = new ArrayList<>();

    public static final String FAKE_COURSE_ID = "courseId";
    public static final String FAKE_USER_ID = "userId";
    public static final String FAKE_ADMIN_ID = "adminId";
    public static final String FAKE_ASGN_ID = "assignmentId";
    public static final String FAKE_PROB_ID = "problemId";
    public static final String FAKE_CATEGORY_NAME = "category";
    public static final float FAKE_CATEGORY_WEIGHT = 25;
    public static final int FAKE_ENUM = 1;
    public static final float FAKE_LATE_RATE = 25;
    public static final boolean FAKE_BOOL = true;

    @Before
    public void before() {
        db = fongo.getDB();
        fauth = new Authenticator(new MongoAuthenticator(fongo.getDB()));

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

        permissionBuilder.addAdminPermission(FAKE_ADMIN_ID).addUserPermission(FAKE_USER_ID);
        courseBuilder.setId(FAKE_COURSE_ID).setAccessPermission(permissionBuilder.build());

        /**
         * Fake mongo DBObjects setup.
         */
        fakeMongoLate = new BasicDBObject(LATE_POLICY_FUNCTION_TYPE, FAKE_ENUM)
                .append(LATE_POLICY_TIME_FRAME_TYPE, FAKE_ENUM).append(LATE_POLICY_RATE, FAKE_LATE_RATE)
                .append(LATE_POLICY_SUBTRACTION_TYPE, FAKE_ENUM)
                .append(APPLY_ONLY_TO_LATE_PROBLEMS, FAKE_BOOL);

        fakeMongoCategory1 = new BasicDBObject(GRADE_CATEGORY_NAME, FAKE_CATEGORY_NAME + "1")
                .append(GRADE_CATEGORY_WEIGHT, FAKE_CATEGORY_WEIGHT)
                .append(LATE_POLICY, fakeMongoLate);

        fakeMongoCategory2 = new BasicDBObject(fakeMongoCategory1);
        fakeMongoCategory2.put(GRADE_CATEGORY_NAME, FAKE_CATEGORY_NAME + "2");

        fakeCategories.add(fakeMongoCategory1);
        fakeCategories.add(fakeMongoCategory2);

        BasicDBObject singleProblem1 = new BasicDBObject(COURSE_PROBLEM_ID, FAKE_PROB_ID + "1")
                .append(DROP_TYPE, FAKE_ENUM);
        BasicDBObject singleProblem2 = new BasicDBObject(COURSE_PROBLEM_ID, FAKE_PROB_ID + "2")
                .append(DROP_TYPE, FAKE_ENUM);
        droppedProblemsList.add(singleProblem1);
        droppedProblemsList.add(singleProblem2);

        fakeDroppedProblems.put(FAKE_ASGN_ID + "1", droppedProblemsList);
        fakeDroppedProblems.put(FAKE_ASGN_ID + "2", droppedProblemsList);

        fakeDroppedAssignments.add(new BasicDBObject(ASSIGNMENT_ID, FAKE_ASGN_ID + "1").append(DROP_TYPE, FAKE_ENUM));
        fakeDroppedAssignments.add(new BasicDBObject(ASSIGNMENT_ID, FAKE_ASGN_ID + "2").append(DROP_TYPE, FAKE_ENUM));

        fakeMongoPolicy.append(SELF_ID, FAKE_COURSE_ID).append(GRADE_POLICY_TYPE, FAKE_ENUM).append(GRADE_CATEGORIES, fakeCategories)
                .append(DROPPED_PROBLEMS, fakeDroppedProblems).append(DROPPED_ASSIGNMENTS, fakeDroppedAssignments);
    }

    @Test
    public void buildMongoLatePolicyTest() {
        BasicDBObject testLatePolicy = GradingPolicyManager.buildMongoLatePolicy(fakeProtoLate.build());
        Assert.assertEquals(fakeMongoLate, testLatePolicy);
    }

    @Test
    public void buildProtoLatePolicyTest() {
        LatePolicy testLatePolicy = GradingPolicyManager.buildProtoLatePolicy(fakeMongoLate);
        Assert.assertEquals(fakeProtoLate.build(), testLatePolicy);
    }

    @Test
    public void buildMongoDroppedProblemObjectTest() {
        List<BasicDBObject> testDroppedProblems = GradingPolicyManager.buildMongoDroppedProblemObject(fakeProtoDropProbs1.build());
        Assert.assertEquals(fakeDroppedProblems.get(FAKE_ASGN_ID + "1"), testDroppedProblems);
    }

    @Test
    public void buildProtoCategoryTest() {
        PolicyCategory testProtoCategory = GradingPolicyManager.buildProtoCategory(fakeMongoCategory1);
        Assert.assertEquals(fakeProtoCategory1.build(), testProtoCategory);
    }

    @Test
    public void buildMongoCategoryTest() {
        BasicDBObject testMongoCategory = GradingPolicyManager.buildMongoCategory(fakeProtoCategory1.build());
        Assert.assertEquals(fakeMongoCategory1, testMongoCategory);
    }

    @Test
    public void insertGradingPolicyTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);

        GradingPolicyManager.insertGradingPolicy(fauth, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());
        DBObject testPolicy = db.getCollection(GRADING_POLICY_COLLECTION).findOne(new ObjectId(courseId));

        fakeMongoPolicy.put(SELF_ID, new ObjectId(courseId));
        Assert.assertEquals(fakeMongoPolicy, testPolicy);
    }

    @Test
    public void getGradingPolicyTest() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);
        GradingPolicyManager.insertGradingPolicy(fauth, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());

        ProtoGradingPolicy testPolicy = GradingPolicyManager.getGradingPolicy(fauth, db, courseId, FAKE_USER_ID);
        Assert.assertEquals(fakeProtoPolicy.build(), testPolicy);
    }

    @Test(expected = AuthenticationException.class)
    public void unauthenticatedAddGradingPolicy() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);
        GradingPolicyManager.insertGradingPolicy(fauth, db, FAKE_USER_ID, fakeProtoPolicy.build());
    }

    @Test(expected = DatabaseAccessException.class)
    public void policyDoesNotExist() throws Exception {
        GradingPolicyManager.getGradingPolicy(fauth, db, new ObjectId().toString(), FAKE_ADMIN_ID);
    }

    @Test(expected = AuthenticationException.class)
    public void userNotInCourseGetPolicy() throws Exception {
        String courseId = CourseManager.mongoInsertCourse(db, courseBuilder.build());
        fakeProtoPolicy.setCourseId(courseId);
        GradingPolicyManager.insertGradingPolicy(fauth, db, FAKE_ADMIN_ID, fakeProtoPolicy.build());

        ProtoGradingPolicy testPolicy = GradingPolicyManager.getGradingPolicy(fauth, db, courseId, "notInCourse");
    }
}

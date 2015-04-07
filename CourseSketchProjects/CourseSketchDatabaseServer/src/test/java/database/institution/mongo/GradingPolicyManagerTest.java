package database.institution.mongo;

import com.github.fakemongo.junit.FongoRule;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.auth.Authenticator;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
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

/**
 * Created by Matt on 4/6/2015.
 */
public class GradingPolicyManagerTest {

    @Rule
    public FongoRule fongo = new FongoRule();

    public DB db;
    public Authenticator fauth;

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

    public static final String FAKE_COURSE_ID = "courseId";
    public static final String FAKE_USER_ID = "userId";
    public static final String FAKE_ASGN_ID = "assignmentId";
    public static final String FAKE_PROB_ID = "problemId";
    public static final String FAKE_CATEGORY_NAME = "category";
    public static final float FAKE_CATEGORY_WEIGHT = 25;
    public static final int FAKE_ENUM = 1;
    public static final int FAKE_LATE_RATE = 25;
    public static final boolean FAKE_BOOL = true;

    public void before() {
        db = fongo.getDB();
        fauth = new Authenticator(new MongoAuthenticator(fongo.getDB()));

        fakeProtoLate.setFunctionType(LatePolicy.FunctionType.valueOf(FAKE_ENUM));
        fakeProtoLate.setTimeFrameType(LatePolicy.TimeFrame.valueOf(FAKE_ENUM));
        fakeProtoLate.setRate(FAKE_LATE_RATE);
        fakeProtoLate.setSubtractionType(LatePolicy.SubtractionType.valueOf(FAKE_ENUM));
        fakeProtoLate.setApplyOnlyToLateProblems(FAKE_BOOL);

        fakeProtoCategory1.setName(FAKE_CATEGORY_NAME + "1");
        fakeProtoCategory1.setWeight(FAKE_CATEGORY_WEIGHT);
        fakeProtoCategory1.setLatePolicy(fakeProtoLate.build());
        fakeProtoCategory2 = fakeProtoCategory1.setName(FAKE_CATEGORY_NAME + "2");

        fakeProtoDropAsgn1.setAssignmentId(FAKE_ASGN_ID + "1");
        fakeProtoDropAsgn1.setDropType(DropType.valueOf(FAKE_ENUM));
        fakeProtoDropAsgn2 = fakeProtoDropAsgn1.setAssignmentId(FAKE_ASGN_ID + "2");

        fakeProtoDropProblem1.setProblemId(FAKE_PROB_ID + "1");
        fakeProtoDropProblem1.setDropType(DropType.valueOf(FAKE_ENUM));
        fakeProtoDropProblem2 = fakeProtoDropProblem1.setProblemId(FAKE_PROB_ID + "2");

        fakeProtoDropProbs1.setAssignmentId(FAKE_ASGN_ID + "1");
        fakeProtoDropProbs1.addProblem(fakeProtoDropProblem1.build());
        fakeProtoDropProbs1.addProblem(fakeProtoDropProblem2.build());
        fakeProtoDropProbs2 = fakeProtoDropProbs1.setAssignmentId(FAKE_ASGN_ID + "2");

        fakeProtoPolicy.setCourseId(FAKE_COURSE_ID);
        fakeProtoPolicy.setPolicyType(ProtoGradingPolicy.PolicyType.valueOf(FAKE_ENUM));
        fakeProtoPolicy.addGradeCategories(fakeProtoCategory1.build());
        fakeProtoPolicy.addGradeCategories(fakeProtoCategory2.build());
        fakeProtoPolicy.addDroppedAssignments(fakeProtoDropAsgn1.build());
        fakeProtoPolicy.addDroppedAssignments(fakeProtoDropAsgn2.build());
        fakeProtoPolicy.addDroppedProblems(fakeProtoDropProbs1.build());
        fakeProtoPolicy.addDroppedProblems(fakeProtoDropProbs2.build());

    }

}

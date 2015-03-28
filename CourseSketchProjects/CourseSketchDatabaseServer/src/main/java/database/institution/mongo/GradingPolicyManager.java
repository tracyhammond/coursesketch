package database.institution.mongo;

import com.mongodb.*;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;
import org.bson.types.ObjectId;
import protobuf.srl.grading.Grading;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.grading.Grading.PolicyCategory;
import protobuf.srl.grading.Grading.DroppedAssignment;
import protobuf.srl.grading.Grading.DroppedProblems;
import protobuf.srl.grading.Grading.DropType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.*;

/**
 * Created by matt on 3/21/15.
 */
public final class GradingPolicyManager {

    /**
     * Private constructor.
     */
    private GradingPolicyManager() {
    }

    /**
     * This method will set or insert the gradingPolicy in SQL based on the proto object passed in.
     * As of now, it is up to the implementation to check if gradingPolicies are valid (ex: add to 100%) beforoe calling this method
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the gradingPolicy is being added to.
     * @param userId
     *         The id of the user asking for the state.
     * @param policy
     *         Proto object containing the gradingPolicy to be set or updated.
     * @throws DatabaseAccessException
     *         Thrown if connecting to sql database cause an error.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     */
    public static void setGradingPolicy(final Authenticator authenticator, final DB dbs, final String userId, final ProtoGradingPolicy policy)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection policyCollection = dbs.getCollection(GRADING_POLICY_COLLECTION);
        final AuthType auth = new AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, policy.getCourseId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final ArrayList<BasicDBObject> categories = new ArrayList<>();
        for (int i = 0; i < policy.getGradeCategoriesCount(); i++) {
            final PolicyCategory category = policy.getGradeCategories(i);
            categories.add(buildMongoCategory(category));
        }

        // Goes through all droppedProblems and creates a map with assignmentId key and a BasicDBObject value.
        // The BasicDBObject has keys problemId and dropType.
        final Map<String, List<BasicDBObject>> droppedProblems = new HashMap<>();
        for (int i = 0; i < policy.getDroppedProblemsCount(); i++) {
            final List<DroppedProblems.SingleProblem> singleProblemList = policy.getDroppedProblems(i).getProblemsList();
            final List<BasicDBObject> mongoProblemList = new ArrayList<>();
            for (int j = 0; j < singleProblemList.size(); j++) {
                final BasicDBObject singleProblem = new BasicDBObject(COURSE_PROBLEM_ID, singleProblemList.get(j).getProblemId())
                        .append(DROP_TYPE, singleProblemList.get(j).getDropType().getNumber());
                mongoProblemList.add(singleProblem);
            }
            droppedProblems.put(policy.getDroppedProblems(i).getAssignmentId(), mongoProblemList);
        }

        final BasicDBObject policyObject = new BasicDBObject(SELF_ID, new ObjectId(policy.getCourseId()))
                .append(GRADE_POLICY_TYPE, policy.getPolicyType().getNumber()).append(GRADE_CATEGORIES, categories)
                .append(DROPPED_PROBLEMS, droppedProblems).append(DROPPED_ASSIGNMENTS, policy.getDroppedAssignmentsList());

        policyCollection.insert(policyObject);
    }

    public static ProtoGradingPolicy getGradingPolicy(final Authenticator authenticator, final DB dbs, final String courseId, final String userId)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, GRADING_POLICY_COLLECTION, new ObjectId(courseId));
        final DBObject policyObject = myDbRef.fetch();
        if (policyObject == null) {
            throw new DatabaseAccessException("Grading policy was not found for course with ID " + courseId);
        }

        boolean isAdmin, isMod, isUsers;
        isAdmin = authenticator.checkAuthentication(userId, (List<String>) policyObject.get(ADMIN));
        isMod = authenticator.checkAuthentication(userId, (List<String>) policyObject.get(MOD));
        isUsers = authenticator.checkAuthentication(userId, (List<String>) policyObject.get(USERS));

        if (!isAdmin && !isMod && !isUsers) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final ProtoGradingPolicy.Builder policy = ProtoGradingPolicy.newBuilder();
        policy.setCourseId(courseId);
        List<DBObject> categories = (List<DBObject>) policyObject.get(GRADE_CATEGORIES);
        for (int i = 0; i < categories.size(); i++) {
            policy.addGradeCategories(buildProtoCategory(categories.get(i)));
        }
        policy.setPolicyType(ProtoGradingPolicy.PolicyType.valueOf((int) policyObject.get(GRADE_POLICY_TYPE)));

        // Builds and adds droppedProblems to the protoGradingPolicy
        final Map<String, List<BasicDBObject>> droppedProblems = (Map) policyObject.get(DROPPED_PROBLEMS);
        for (String assignmentId : droppedProblems.keySet()) {
            final DroppedProblems.Builder problemList = DroppedProblems.newBuilder();
            final List<BasicDBObject> singleProblemList = droppedProblems.get(assignmentId);
            for (int i = 0; i < singleProblemList.size(); i++) {
                final DroppedProblems.SingleProblem.Builder singleProblem = DroppedProblems.SingleProblem.newBuilder();
                singleProblem.setProblemId(singleProblemList.get(i).get(COURSE_PROBLEM_ID).toString());
                singleProblem.setDropType(DropType.valueOf((int) singleProblemList.get(i).get(DROP_TYPE)));
                problemList.addProblems(singleProblem);
            }
            problemList.setAssignmentId(assignmentId);
            policy.addDroppedProblems(problemList);
        }

        // Builds and adds droppedAssignments to the protoGradingPolicy
        final List<BasicDBObject> droppedAssignments = (List<BasicDBObject>) policyObject.get(DROPPED_ASSIGNMENTS);
        for (int i = 0; i < droppedAssignments.size(); i++) {
            final DroppedAssignment.Builder assignment = DroppedAssignment.newBuilder();
            assignment.setAssignmentId(droppedAssignments.get(i).get(ASSIGNMENT_ID).toString());
            assignment.setDropType(DropType.valueOf((int) droppedAssignments.get(i).get(DROP_TYPE)));
            policy.addDroppedAssignments(assignment);
        }

        return policy.build();
    }

    /**
     * Builds a BasicDBObject for a grading policy category.
     * <pre><code>
     * gradeCategory: {
     *      name: String,
     *      weight: Number,
     *      latePolicy: {
     *          functionType: ENUM,
     *          timeFrameType: ENUM,
     *          rate : Number,
     *          subtractionType: ENUM,
     *          applyOnlyToLateProblems: boolean
     *      }
     *  }
     * </code></pre>
     * @param category
     *         The category to build the BasicDBObject for.
     * @return The BasicDBObject representing the category.
     */
    private static BasicDBObject buildMongoCategory(final PolicyCategory category) {
        // Building late policy DBObject first.
        final LatePolicy latePolicy = category.getLatePolicy();
        final BasicDBObject late = buildMongoLatePolicy(latePolicy);

        // Building single DBObject to add to list of categories
        final BasicDBObject temp = new BasicDBObject(GRADE_CATEGORY_NAME, category.getName()).append(GRADE_CATEGORY_WEIGHT, category.getWeight())
                .append(LATE_POLICY, late);
        return temp;
    }

    private static BasicDBObject buildMongoLatePolicy(final LatePolicy latePolicy) {
        final BasicDBObject late = new BasicDBObject(LATE_POLICY_FUNCTION_TYPE, latePolicy.getFunctionType().getNumber())
                .append(LATE_POLICY_TIME_FRAME_TYPE, latePolicy.getTimeFrameType().getNumber()).append(LATE_POLICY_RATE, latePolicy.getRate())
                .append(LATE_POLICY_SUBTRACTION_TYPE, latePolicy.getSubtractionType().getNumber())
                .append(APPLY_ONLY_TO_LATE_PROBLEMS, latePolicy.getApplyOnlyToLateProblems());
        return late;
    }

    private static PolicyCategory buildProtoCategory(final DBObject dbCategory) {
        final PolicyCategory.Builder protoCategory = PolicyCategory.newBuilder();
        protoCategory.setName(dbCategory.get(GRADE_CATEGORY_NAME).toString());
        protoCategory.setWeight((float) dbCategory.get(GRADE_CATEGORY_WEIGHT));
        protoCategory.setLatePolicy(buildProtoLatePolicy((DBObject) dbCategory.get(LATE_POLICY)));
        return protoCategory.build();
    }

    private static LatePolicy buildProtoLatePolicy(final DBObject dbPolicy) {
        final LatePolicy.Builder protoPolicy = LatePolicy.newBuilder();
        protoPolicy.setFunctionType(LatePolicy.FunctionType.valueOf((int) dbPolicy.get(LATE_POLICY_FUNCTION_TYPE)));
        protoPolicy.setTimeFrameType(LatePolicy.TimeFrame.valueOf((int) dbPolicy.get(LATE_POLICY_TIME_FRAME_TYPE)));
        protoPolicy.setRate((float) dbPolicy.get(LATE_POLICY_RATE));
        protoPolicy.setSubtractionType(LatePolicy.SubtractionType.valueOf((int) dbPolicy.get(LATE_POLICY_SUBTRACTION_TYPE)));
        protoPolicy.setApplyOnlyToLateProblems((boolean) dbPolicy.get(APPLY_ONLY_TO_LATE_PROBLEMS));
        return protoPolicy.build();
    }
}

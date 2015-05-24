package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;
import org.bson.types.ObjectId;
import protobuf.srl.grading.Grading.DropType;
import protobuf.srl.grading.Grading.DroppedAssignment;
import protobuf.srl.grading.Grading.DroppedProblems;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.PolicyCategory;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;

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
 * Interfaces with mongo database to manage grading policies.
 *
 * In the mongo database, a grading policy has the following structure.
 *
 * GradingPolicy
 * {
 *     _id: courseId,
 *     policyType: ENUM // Percent or point based system
 *     gradeCategories: [
 *     {
 *         name: String,
 *         weight: Number,
 *         latePolicy: {
 *             functionType: ENUM,
 *             timeFrameType: ENUM,
 *             rate: Number,
 *             subtractionType: ENUM,
 *             applyOnlyToLateProblems: boolean
 *         }
 *     }, repeated gradeCategories
 *     ],
 *     droppedProblems: {
 *         assignmentId1: [ { id: problemId1, dropType: type }, ... ],
 *         assignmentId2: [...],
 *         ...
 *     },
 *     droppedAssignments: [
 *         { id: Id, dropType: type },
 *         {...}
 *     ]
 * }
 *
 * Created by matt on 3/21/15.
 */
@SuppressWarnings("PMD.CommentSize")
public final class GradingPolicyManager {

    /**
     * Private constructor.
     */
    private GradingPolicyManager() {
    }

    /**
     * This method will insert the gradingPolicy in Mongo based on the proto object passed in.
     *
     * As of now, it is up to the implementation to check if gradingPolicies are valid (ex: add to 100%) before calling this method.
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
     *         Thrown if connecting to the database causes an error.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     */
    public static void insertGradingPolicy(final Authenticator authenticator, final DB dbs, final String userId, final ProtoGradingPolicy policy)
            throws AuthenticationException, DatabaseAccessException {
        final AuthType auth = new AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, policy.getCourseId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final DBCollection policyCollection = dbs.getCollection(GRADING_POLICY_COLLECTION);

        final ArrayList<BasicDBObject> categories = new ArrayList<>();
        for (int i = 0; i < policy.getGradeCategoriesCount(); i++) {
            final PolicyCategory category = policy.getGradeCategories(i);
            categories.add(buildMongoCategory(category));
        }

        // Goes through all droppedProblems and creates a map with assignmentId key and a BasicDBObject value.
        // The BasicDBObject has keys problemId and dropType.
        final Map<String, List<BasicDBObject>> droppedProblems = new HashMap<>();
        for (int i = 0; i < policy.getDroppedProblemsCount(); i++) {
            droppedProblems.put(policy.getDroppedProblems(i).getAssignmentId(), buildMongoDroppedProblemObject(policy.getDroppedProblems(i)));
        }

        final BasicDBObject policyObject = new BasicDBObject(SELF_ID, new ObjectId(policy.getCourseId()))
                .append(GRADE_POLICY_TYPE, policy.getPolicyType().getNumber()).append(GRADE_CATEGORIES, categories)
                .append(DROPPED_PROBLEMS, droppedProblems);

        if (policy.getDroppedAssignmentsCount() != 0) {
            final List<BasicDBObject> droppedAssignments = new ArrayList<>();
            final List<DroppedAssignment> droppedList = policy.getDroppedAssignmentsList();
            for (int i = 0; i < droppedList.size(); i++) {
                final BasicDBObject assignment = new BasicDBObject(ASSIGNMENT_ID, droppedList.get(i).getAssignmentId())
                        .append(DROP_TYPE, droppedList.get(i).getDropType().getNumber());
                droppedAssignments.add(assignment);
            }
            policyObject.append(DROPPED_ASSIGNMENTS, droppedAssignments);
        }

        policyCollection.insert(policyObject);
    }

    /**
     * Gets the grading policy for a course from the mongoDb.
     *
     * The first object is the query. The second is the projection which tells to return everything except the _id field.
     * <pre><code>
     *     coll.find( { _id: ObjectId(courseId) }, { _id: false } )
     * </code></pre>
     *
     * @param authenticator
     *         The object that is performing authentication.
     * @param dbs
     *         The database that the gradingPolicy is being added to.
     * @param courseId
     *         The gradingPolicy we will get is from this course.
     * @param userId
     *         The id of the user asking for the policy.
     * @return The protoObject representing the gradingPolicy.
     * @throws AuthenticationException
     *         Thrown if the user did not have the authentication to get the course.
     * @throws DatabaseAccessException
     *         Thrown if a grading policy is not found for the course.
     * Package-private
     */
    static ProtoGradingPolicy getGradingPolicy(final Authenticator authenticator, final DB dbs, final String courseId, final String userId)
            throws AuthenticationException, DatabaseAccessException {
        final DBRef myDbRef = new DBRef(dbs, GRADING_POLICY_COLLECTION, new ObjectId(courseId));
        final DBObject policyObject = myDbRef.fetch();
        if (policyObject == null) {
            throw new DatabaseAccessException("Grading policy was not found for course with ID " + courseId);
        }

        final AuthType auth = new AuthType();
        auth.setCheckAccess(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, courseId, userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        final ProtoGradingPolicy.Builder policy = ProtoGradingPolicy.newBuilder();
        policy.setCourseId(courseId);
        final List<DBObject> categories = (List<DBObject>) policyObject.get(GRADE_CATEGORIES);
        for (int i = 0; i < categories.size(); i++) {
            policy.addGradeCategories(buildProtoCategory(categories.get(i)));
        }
        policy.setPolicyType(ProtoGradingPolicy.PolicyType.valueOf((int) policyObject.get(GRADE_POLICY_TYPE)));

        // Builds and adds droppedProblems to the protoGradingPolicy
        final Map<String, List<BasicDBObject>> droppedProblems = (Map<String, List<BasicDBObject>>) policyObject.get(DROPPED_PROBLEMS);
        for (Map.Entry<String, List<BasicDBObject>> droppedProblemEntry : droppedProblems.entrySet()) {
            final DroppedProblems.Builder protoDroppedProblems = buildProtoDroppedProblems(droppedProblemEntry.getValue());
            protoDroppedProblems.setAssignmentId(droppedProblemEntry.getKey());
            policy.addDroppedProblems(protoDroppedProblems);
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
     * Converts a grading policy category from proto to mongo DBObject.
     *
     * The gradeCategory mongo structure is below.
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
     *
     * @param category
     *         The category to build the BasicDBObject for.
     * @return The BasicDBObject representing the category.
     *
     * Package-private
     */
    static BasicDBObject buildMongoCategory(final PolicyCategory category) {
        // Building single DBObject to add to list of categories
        final BasicDBObject mongoCategory = new BasicDBObject(GRADE_CATEGORY_NAME, category.getName());

        if (category.hasWeight()) {
            mongoCategory.append(GRADE_CATEGORY_WEIGHT, category.getWeight());
        }

        // Building late policy DBObject.
        if (category.hasLatePolicy()) {
            final LatePolicy latePolicy = category.getLatePolicy();
            final BasicDBObject late = buildMongoLatePolicy(latePolicy);
            mongoCategory.append(LATE_POLICY, late);
        }

        return mongoCategory;
    }

    /**
     * Converts a grading policy category from mongo DBObject to proto.
     *
     * @param dbCategory
     *         The DBObject representing the grading policy category we want to convert.
     * @return The protoObject for the grading policy category.
     *
     * Package-private
     */
    static PolicyCategory buildProtoCategory(final DBObject dbCategory) {
        final PolicyCategory.Builder protoCategory = PolicyCategory.newBuilder();
        protoCategory.setName(dbCategory.get(GRADE_CATEGORY_NAME).toString());

        if (dbCategory.containsField(GRADE_CATEGORY_WEIGHT)) {
            protoCategory.setWeight((float) dbCategory.get(GRADE_CATEGORY_WEIGHT));
        }

        if (dbCategory.containsField(LATE_POLICY)) {
            protoCategory.setLatePolicy(buildProtoLatePolicy((DBObject) dbCategory.get(LATE_POLICY)));
        }

        return protoCategory.build();
    }

    /**
     * Converts a latePolicy from proto to mongo BasicDBObject.
     *
     * @see #buildMongoCategory
     *
     * @param latePolicy
     *         The proto latePolicy we wish to build the mongo BasicDBObject for.
     * @return The BasicDBObject representing the proto latePolicy we passed in.
     *
     * Package-private
     */
    static BasicDBObject buildMongoLatePolicy(final LatePolicy latePolicy) {
        final BasicDBObject late = new BasicDBObject(LATE_POLICY_FUNCTION_TYPE, latePolicy.getFunctionType().getNumber())
                .append(LATE_POLICY_TIME_FRAME_TYPE, latePolicy.getTimeFrameType().getNumber()).append(LATE_POLICY_RATE, latePolicy.getRate())
                .append(LATE_POLICY_SUBTRACTION_TYPE, latePolicy.getSubtractionType().getNumber())
                .append(APPLY_ONLY_TO_LATE_PROBLEMS, latePolicy.getApplyOnlyToLateProblems());
        return late;
    }

    /**
     * Converts a latePolicy from mongo BasicDBObject to proto.
     *
     * @param dbPolicy
     *         The mongo latePolicy we want to build a protoObject for.
     * @return The protoObject representing the mongo latePolicy we passed in.
     *
     * Package-private
     */
    static LatePolicy buildProtoLatePolicy(final DBObject dbPolicy) {
        final LatePolicy.Builder protoPolicy = LatePolicy.newBuilder();
        protoPolicy.setFunctionType(LatePolicy.FunctionType.valueOf((int) dbPolicy.get(LATE_POLICY_FUNCTION_TYPE)));
        protoPolicy.setTimeFrameType(LatePolicy.TimeFrame.valueOf((int) dbPolicy.get(LATE_POLICY_TIME_FRAME_TYPE)));
        protoPolicy.setRate((float) dbPolicy.get(LATE_POLICY_RATE));
        protoPolicy.setSubtractionType(LatePolicy.SubtractionType.valueOf((int) dbPolicy.get(LATE_POLICY_SUBTRACTION_TYPE)));
        protoPolicy.setApplyOnlyToLateProblems((boolean) dbPolicy.get(APPLY_ONLY_TO_LATE_PROBLEMS));
        return protoPolicy.build();
    }

    /**
     * Creates a mongo dropped problem object.
     *
     * Returns the list of BasicDBObjects that is in the below mongo document structure.
     * <pre><code>
     * droppedProblems: {
     *     assignmentId1: [ { problemId: problemId1, dropType: type }, ... ],
     *     assignmentId2: [...],
     * }
     * </code></pre>
     *
     * The list is the value corresponding to the assignmentId key in the droppedProblems map.
     *
     * @param problems Proto Object representing the dropped problems.
     * @return List to be used as the value in the droppedProblems key/value pair where the keys are assignmentIds.
     *
     * Package-private
     */
    static List<BasicDBObject> buildMongoDroppedProblemObject(final DroppedProblems problems) {
        final List<DroppedProblems.SingleProblem> singleProblemList = problems.getProblemList();
        final List<BasicDBObject> mongoProblemList = new ArrayList<>();
        for (int i = 0; i < singleProblemList.size(); i++) {
            final BasicDBObject singleProblem = new BasicDBObject(COURSE_PROBLEM_ID, singleProblemList.get(i).getProblemId())
                    .append(DROP_TYPE, singleProblemList.get(i).getDropType().getNumber());
            mongoProblemList.add(singleProblem);
        }
        return mongoProblemList;
    }

    /**
     * Builds a list of proto single problems to use in droppedProblems.
     *
     * @param singleProblemList List of the single problems dropped for an assignment.
     * @return List of proto single problems to be dropped.
     * Package-private
     */
    static DroppedProblems.Builder buildProtoDroppedProblems(final List<BasicDBObject> singleProblemList) {
        final DroppedProblems.Builder problemList = DroppedProblems.newBuilder();
        for (int i = 0; i < singleProblemList.size(); i++) {
            final DroppedProblems.SingleProblem.Builder singleProblem = DroppedProblems.SingleProblem.newBuilder();
            singleProblem.setProblemId(singleProblemList.get(i).get(COURSE_PROBLEM_ID).toString());
            singleProblem.setDropType(DropType.valueOf((int) singleProblemList.get(i).get(DROP_TYPE)));
            problemList.addProblem(singleProblem);
        }
        return problemList;
    }
}

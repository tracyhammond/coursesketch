package database.institution.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import coursesketch.database.auth.AuthenticationException;
import coursesketch.database.auth.AuthenticationResponder;
import coursesketch.database.auth.Authenticator;
import database.DatabaseAccessException;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.grading.Grading.DropType;
import protobuf.srl.grading.Grading.DroppedProblems;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.PolicyCategory;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.services.authentication.Authentication;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static database.DatabaseStringConstants.APPLY_ONLY_TO_LATE_PROBLEMS;
import static database.DatabaseStringConstants.ASSIGNMENT_ID;
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
import static database.DatabaseStringConstants.SET_COMMAND;
import static database.DatabaseStringConstants.UPSERT_COMMAND;
import static database.utilities.MongoUtilities.convertStringToObjectId;

/**
 * Interfaces with mongo database to manage grading policies.
 *
 * In the mongo database, a grading policy has the following structure.
 * <pre>
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
 * </pre>
 * Created by matt on 3/21/15.
 */
@SuppressWarnings("PMD.CommentSize")
final class GradingPolicyManager {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GradingPolicyManager.class);

    /**
     * Private constructor.
     */
    private GradingPolicyManager() {
    }

    /**
     * This method will upsert the gradingPolicy in Mongo based on the proto object passed in.
     *
     * As of now, it is up to the implementation to check if gradingPolicies are valid (ex: add to 100%) before calling this method.
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database that the gradingPolicy is being added to.
     * @param userId The id of the user asking for the state.
     * @param policy Proto object containing the gradingPolicy to be set or updated.
     * @throws DatabaseAccessException Thrown if connecting to the database causes an error.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the course.
     */
    static void upsertGradingPolicy(final Authenticator authenticator, final MongoDatabase dbs, final String userId, final ProtoGradingPolicy policy)
            throws AuthenticationException, DatabaseAccessException {
        final Authentication.AuthType.Builder auth = Authentication.AuthType.newBuilder();
        auth.setCheckingAdmin(true);
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, policy.getCourseId(), userId, 0, auth.build());
        if (!responder.hasModeratorPermission()) {
            throw new AuthenticationException("User does not have permission to insert grade policy", AuthenticationException.INVALID_PERMISSION);
        }

        final MongoCollection<Document> policyCollection = dbs.getCollection(GRADING_POLICY_COLLECTION);

        final ArrayList<Document> categories = new ArrayList<>();
        for (int i = 0; i < policy.getGradeCategoriesCount(); i++) {
            final PolicyCategory category = policy.getGradeCategories(i);
            categories.add(buildMongoCategory(category));
        }

        // Goes through all droppedProblems and creates a map with assignmentId key and a Document value.
        // The Document has keys problemId and dropType.
        final Map<String, List<Document>> droppedProblems = new HashMap<>();
        for (Map.Entry<String, DroppedProblems> entry : policy.getDroppedProblemsMap().entrySet()) {
            droppedProblems.put(entry.getKey(), buildMongoDroppedProblemObject(entry.getValue()));
        }

        final Document policyObject = new Document()
                .append(GRADE_POLICY_TYPE, policy.getPolicyType().getNumber()).append(GRADE_CATEGORIES, categories)
                .append(DROPPED_PROBLEMS, droppedProblems);

        if (policy.getDroppedAssignmentsCount() != 0) {
            final List<Document> droppedAssignments = new ArrayList<>();
            final Map<String, DropType> droppedList = policy.getDroppedAssignmentsMap();
            for (Map.Entry<String, DropType> entry : droppedList.entrySet()) {
                final Document assignment = new Document(ASSIGNMENT_ID, entry.getKey())
                        .append(DROP_TYPE, entry.getValue().getNumber());
                droppedAssignments.add(assignment);
            }
            policyObject.append(DROPPED_ASSIGNMENTS, droppedAssignments);
        } else {
            policyObject.append(DROPPED_ASSIGNMENTS, new ArrayList<Document>());
        }

        // Query for an existing policy for the course.
        final Bson filter = Filters.eq(SELF_ID, new ObjectId(policy.getCourseId()));

        final UpdateOptions options = new UpdateOptions().upsert(true);

        LOG.debug("Update query {}", policyObject);
        final Document idDocument = new Document(SELF_ID, new ObjectId(policy.getCourseId()));
        policyCollection.updateOne(filter, new Document(SET_COMMAND, policyObject).append(UPSERT_COMMAND, idDocument), options);
    }

    /**
     * Gets the grading policy for a course from the mongoDb.
     *
     * The first object is the query. The second is the projection which tells to return everything except the _id field.
     * <pre><code>
     *     coll.find( { _id: ObjectId(courseId) }, { _id: false } )
     * </code></pre>
     *
     * @param authenticator The object that is performing authentication.
     * @param dbs The database that the gradingPolicy is being added to.
     * @param courseId The gradingPolicy we will get is from this course.
     * @param userId The id of the user asking for the state.
     * @return The protoObject representing the gradingPolicy.
     * @throws AuthenticationException Thrown if the user did not have the authentication to get the course.
     * @throws DatabaseAccessException Thrown if a grading policy is not found for the course.
     * Package-private
     */
    static ProtoGradingPolicy getGradingPolicy(final Authenticator authenticator, final MongoDatabase dbs, final String courseId, final String userId)
            throws AuthenticationException, DatabaseAccessException {
        final MongoCollection<Document> gradePolicyCollection = dbs.getCollection(GRADING_POLICY_COLLECTION);
        final Document policyObject = gradePolicyCollection.find(convertStringToObjectId(courseId)).first();

        if (policyObject == null) {
            throw new DatabaseAccessException("Grading policy was not found for course with ID " + courseId);
        }

        final Authentication.AuthType.Builder auth = Authentication.AuthType.newBuilder();
        auth.setCheckAccess(true);
        final AuthenticationResponder responder = authenticator
                .checkAuthentication(Util.ItemType.COURSE, courseId, userId, 0, auth.build());
        if (!responder.hasAccess()) {
            throw new AuthenticationException("User does not have permission to insert grade policy", AuthenticationException.INVALID_PERMISSION);
        }

        final ProtoGradingPolicy.Builder policy = ProtoGradingPolicy.newBuilder();
        policy.setCourseId(courseId);
        final List<Document> categories = (List<Document>) policyObject.get(GRADE_CATEGORIES);
        for (Document category : categories) {
            policy.addGradeCategories(buildProtoCategory(category));
        }
        policy.setPolicyType(ProtoGradingPolicy.PolicyType.valueOf((int) policyObject.get(GRADE_POLICY_TYPE)));

        // Builds and adds droppedProblems to the protoGradingPolicy
        final Map<String, List<Document>> droppedProblems = (Map<String, List<Document>>) policyObject.get(DROPPED_PROBLEMS);
        for (Map.Entry<String, List<Document>> droppedProblemEntry : droppedProblems.entrySet()) {
            policy.putDroppedProblems(droppedProblemEntry.getKey(),
                    buildProtoDroppedProblems(droppedProblemEntry.getValue()));
        }

        // Builds and adds droppedAssignments to the protoGradingPolicy
        final List<Document> droppedAssignments = (List<Document>) policyObject.get(DROPPED_ASSIGNMENTS);
        for (Document document : droppedAssignments) {
            policy.putDroppedAssignments(document.get(ASSIGNMENT_ID).toString(),
                    DropType.forNumber((int) document.get(DROP_TYPE)));
        }

        return policy.build();
    }

    /**
     * Converts a grading policy category from proto to mongo Document.
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
     * @param category The category to build the Document for.
     * @return The Document representing the category.
     *
     * Package-private
     */
    static Document buildMongoCategory(final PolicyCategory category) {
        // Building single Document to add to list of categories
        final Document mongoCategory = new Document(GRADE_CATEGORY_NAME, category.getName());

        if (category.getWeight() != 0.0) {
            mongoCategory.append(GRADE_CATEGORY_WEIGHT, category.getWeight());
        }

        // Building late policy Document.
        if (category.hasLatePolicy()) {
            final LatePolicy latePolicy = category.getLatePolicy();
            final Document late = buildMongoLatePolicy(latePolicy);
            mongoCategory.append(LATE_POLICY, late);
        }

        return mongoCategory;
    }

    /**
     * Converts a grading policy category from mongo Document to proto.
     *
     * @param dbCategory The Document representing the grading policy category we want to convert.
     * @return The protoObject for the grading policy category.
     *
     * Package-private
     */
    static PolicyCategory buildProtoCategory(final Document dbCategory) {
        final PolicyCategory.Builder protoCategory = PolicyCategory.newBuilder();
        protoCategory.setName(dbCategory.get(GRADE_CATEGORY_NAME).toString());

        if (dbCategory.containsKey(GRADE_CATEGORY_WEIGHT)) {
            protoCategory.setWeight((double) dbCategory.get(GRADE_CATEGORY_WEIGHT));
        }

        if (dbCategory.containsKey(LATE_POLICY)) {
            protoCategory.setLatePolicy(buildProtoLatePolicy((Document) dbCategory.get(LATE_POLICY)));
        }

        return protoCategory.build();
    }

    /**
     * Converts a latePolicy from proto to mongo Document.
     *
     * @param latePolicy The proto latePolicy we wish to build the mongo Document for.
     * @return The Document representing the proto latePolicy we passed in.
     *
     * Package-private
     * @see #buildMongoCategory
     */
    static Document buildMongoLatePolicy(final LatePolicy latePolicy) {
        return new Document(LATE_POLICY_FUNCTION_TYPE, latePolicy.getFunctionType().getNumber())
                .append(LATE_POLICY_TIME_FRAME_TYPE, latePolicy.getTimeFrameType().getNumber()).append(LATE_POLICY_RATE, latePolicy.getRate())
                .append(LATE_POLICY_SUBTRACTION_TYPE, latePolicy.getSubtractionType().getNumber())
                .append(APPLY_ONLY_TO_LATE_PROBLEMS, latePolicy.getApplyOnlyToLateProblems());
    }

    /**
     * Converts a latePolicy from mongo Document to proto.
     *
     * @param dbPolicy The mongo latePolicy we want to build a protoObject for.
     * @return The protoObject representing the mongo latePolicy we passed in.
     *
     * Package-private
     */
    static LatePolicy buildProtoLatePolicy(final Document dbPolicy) {
        final LatePolicy.Builder protoPolicy = LatePolicy.newBuilder();
        protoPolicy.setFunctionType(LatePolicy.FunctionType.forNumber((int) dbPolicy.get(LATE_POLICY_FUNCTION_TYPE)));
        protoPolicy.setTimeFrameType(LatePolicy.TimeFrame.forNumber((int) dbPolicy.get(LATE_POLICY_TIME_FRAME_TYPE)));
        protoPolicy.setRate((double) dbPolicy.get(LATE_POLICY_RATE));
        protoPolicy.setSubtractionType(LatePolicy.SubtractionType.forNumber((int) dbPolicy.get(LATE_POLICY_SUBTRACTION_TYPE)));
        protoPolicy.setApplyOnlyToLateProblems((boolean) dbPolicy.get(APPLY_ONLY_TO_LATE_PROBLEMS));
        return protoPolicy.build();
    }

    /**
     * Creates a mongo dropped problem object.
     *
     * Returns the list of Documents that is in the below mongo document structure.
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
    static List<Document> buildMongoDroppedProblemObject(final DroppedProblems problems) {
        final List<Document> mongoProblemList = new ArrayList<>();
        for (Map.Entry<String, DropType> problemIdDropTypeEntry : problems.getProblemMap().entrySet()) {
            final Document singleProblem = new Document(COURSE_PROBLEM_ID, problemIdDropTypeEntry.getKey())
                    .append(DROP_TYPE, problemIdDropTypeEntry.getValue().getNumber());
            mongoProblemList.add(singleProblem);
        }
        return mongoProblemList;
    }

    /**
     * Builds a list of proto single problems to use in droppedProblems.
     *
     * @param droppedProblemList List of the single problems dropped for an assignment.
     * @return List of proto single problems to be dropped.
     * Package-private
     */
    static DroppedProblems buildProtoDroppedProblems(final List<Document> droppedProblemList) {
        final DroppedProblems.Builder problemList = DroppedProblems.newBuilder();
        for (Document problemIdDropTypeEntry : droppedProblemList) {
            problemList.putProblem(problemIdDropTypeEntry.get(COURSE_PROBLEM_ID).toString(),
                    DropType.forNumber((int) problemIdDropTypeEntry.get(DROP_TYPE)));
        }
        return problemList.build();
    }
}

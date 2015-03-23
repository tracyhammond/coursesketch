package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import database.DatabaseAccessException;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;
import org.bson.types.ObjectId;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.ProtoGradingPolicy;
import protobuf.srl.grading.Grading.PolicyCategory;

import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.GRADING_POLICY_COLLECTION;
import static database.DatabaseStringConstants.GRADE_POLICY_TYPE;
import static database.DatabaseStringConstants.GRADE_CATEGORY_NAME;
import static database.DatabaseStringConstants.GRADE_CATEGORY_WEIGHT;
import static database.DatabaseStringConstants.LATE_POLICY_FUNCTION_TYPE;
import static database.DatabaseStringConstants.LATE_POLICY_RATE;
import static database.DatabaseStringConstants.LATE_POLICY_SUBTRACTION_TYPE;
import static database.DatabaseStringConstants.LATE_POLICY_TIME_FRAME_TYPE;
import static database.DatabaseStringConstants.APPLY_ONLY_TO_LATE_PROBLEMS;
import static database.DatabaseStringConstants.LATE_POLICY;
import static database.DatabaseStringConstants.GRADE_CATEGORIES;
import static database.DatabaseStringConstants.SELF_ID;
import static database.DatabaseStringConstants.DROPPED_ASSIGNMENTS;
import static database.DatabaseStringConstants.DROPPED_PROBLEMS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            categories.add(buildCategory(category));
        }

        final Map<String, List<String>> droppedProblems = new HashMap<>();
        for (int i = 0; i < policy.getDroppedProblemsCount(); i++) {
            droppedProblems.put(policy.getDroppedProblems(i).getAssignmentId(), policy.getDroppedProblems(i).getDroppedProblemsList());
        }

        final BasicDBObject policyObject = new BasicDBObject(SELF_ID, new ObjectId(policy.getCourseId()))
                .append(GRADE_POLICY_TYPE, policy.getPolicyType().getNumber()).append(GRADE_CATEGORIES, categories)
                .append(DROPPED_PROBLEMS, droppedProblems).append(DROPPED_ASSIGNMENTS, policy.getDroppedAssignmentsList());

        policyCollection.insert(policyObject);
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
    private static BasicDBObject buildCategory(final PolicyCategory category) {
        // Building late policy DBObject first.
        final LatePolicy latePolicy = category.getLatePolicy();
        final BasicDBObject late = new BasicDBObject(LATE_POLICY_FUNCTION_TYPE, latePolicy.getFunctionType().getNumber())
                .append(LATE_POLICY_TIME_FRAME_TYPE, latePolicy.getTimeFrameType().getNumber()).append(LATE_POLICY_RATE, latePolicy.getRate())
                .append(LATE_POLICY_SUBTRACTION_TYPE, latePolicy.getSubtractionType().getNumber())
                .append(APPLY_ONLY_TO_LATE_PROBLEMS, latePolicy.getApplyOnlyToLateProblems());

        // Building single DBObject to add to list of categories
        final BasicDBObject temp = new BasicDBObject(GRADE_CATEGORY_NAME, category.getName()).append(GRADE_CATEGORY_WEIGHT, category.getWeight())
                .append(LATE_POLICY, late);

        return temp;
    }
}

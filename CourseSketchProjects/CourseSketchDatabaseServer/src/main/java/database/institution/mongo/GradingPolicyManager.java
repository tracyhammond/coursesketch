package database.institution.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import database.DatabaseAccessException;
import database.RequestConverter;
import database.UserUpdateHandler;
import database.auth.AuthenticationException;
import database.auth.Authenticator;
import database.auth.Authenticator.AuthType;
import database.auth.MongoAuthenticator;
import org.bson.types.ObjectId;
import protobuf.srl.school.School.SrlAssignment;
import protobuf.srl.utils.Util.SrlPermission;
import protobuf.srl.school.School.State;
import protobuf.srl.grading.Grading.LatePolicy;
import protobuf.srl.grading.Grading.GradingPolicy;

import static database.DatabaseStringConstants.COURSE_COLLECTION;
import static database.DatabaseStringConstants.GRADING_POLICY_COLLECTION;
import static database.DatabaseStringConstants.COURSE_ID;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matt on 3/21/15.
 */
public class GradingPolicyManager {

    /**
     * Private constructor.
     */
    private GradingPolicyManager() {
    }

    public static String setGradingPolicy(final Authenticator authenticator, final DB dbs, final String userId, final GradingPolicy policy)
            throws AuthenticationException, DatabaseAccessException {
        final DBCollection policyCollection = dbs.getCollection(GRADING_POLICY_COLLECTION);
        final AuthType auth = new AuthType();
        auth.setCheckAdminOrMod(true);
        if (!authenticator.isAuthenticated(COURSE_COLLECTION, policy.getCourseId(), userId, 0, auth)) {
            throw new AuthenticationException(AuthenticationException.INVALID_PERMISSION);
        }

        ArrayList<BasicDBObject> categories = new ArrayList<>();
        for (GradingPolicy.PolicyCategory category : policy.getGradeCategoriesList()) {
            // Building late policy DBObject first.
            LatePolicy latePolicy = category.getLatePolicy();
            BasicDBObject late = new BasicDBObject(LATE_POLICY_FUNCTION_TYPE, latePolicy.getFunctionType().getNumber())
                    .append(LATE_POLICY_TIME_FRAME_TYPE, latePolicy.getTimeFrameType().getNumber())
                    .append(LATE_POLICY_RATE, latePolicy.getRate())
                    .append(LATE_POLICY_SUBTRACTION_TYPE, latePolicy.getSubtractionType().getNumber())
                    .append(APPLY_ONLY_TO_LATE_PROBLEMS, latePolicy.getApplyOnlyToLateProblems());

            // Building single DBObject to add to list of categories
            BasicDBObject temp = new BasicDBObject(GRADE_CATEGORY_NAME, category.getName())
                    .append(GRADE_CATEGORY_WEIGHT, category.getWeight())
                    .append(LATE_POLICY, late);

            categories.add(temp);
        }

        final BasicDBObject query = new BasicDBObject(COURSE_ID, policy.getCourseId())
                .append(GRADE_POLICY_TYPE, policy.getPolicyType().getNumber())
                .append(GRADE_CATEGORIES, categories)

    }
}

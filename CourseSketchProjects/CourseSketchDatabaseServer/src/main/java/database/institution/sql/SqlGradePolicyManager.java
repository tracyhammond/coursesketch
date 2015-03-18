package database.institution.sql;

import database.DatabaseAccessException;
import database.DatabaseStringConstants;
import protobuf.srl.school.School.GradingPolicy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Matt on 3/16/2015.
 */
public final class SqlGradePolicyManager {

    /**
     * Private constructor.
     */
    private SqlGradePolicyManager() {
    }

    /**
     * This method will set or insert the gradingPolicy in SQL based on the proto object passed in.
     * As of now, it is up to the implementation to check if gradingPolicies are valid (ex: add to 100%) before calling this method
     *
     * @param conn
     *         the database that contains the state. Must point to proper database
     * @param userId
     *         the id of the user asking for the state
     * @param policy
     *         proto object containing the gradingPolicy to be set or updated
     * @return result of set: "SET", "INSERT", "ERROR"
     * @throws DatabaseAccessException
     *         thrown if connecting to sql database cause an error
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public static String setGradingPolicy(final Connection conn, final String userId, final GradingPolicy policy) throws DatabaseAccessException {
        String result = "";
        final String courseId = policy.getCourseId();
        final int policyType = policy.getPolicyType().getNumber();
        final List<GradingPolicy.PolicyCategory> categories = policy.getGradeCategoriesList();

        // Select all where CourseId = courseId and CategoryName = categoryName
        final String query = "SELECT * FROM GradePolicies WHERE ?=? AND ?=?";
        try (PreparedStatement stmt = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            stmt.setString(1, DatabaseStringConstants.COURSE_ID);
            stmt.setString(2, courseId);
            stmt.setString(3, DatabaseStringConstants.CATEGORY_NAME);
            for (int i = 0; i < categories.size(); i++) {
                final GradingPolicy.PolicyCategory currentCategory = categories.get(i);
                stmt.setString(4, currentCategory.getName());
                try (final ResultSet rst = stmt.executeQuery()) {
                    if (rst.next()) {
                        rst.updateString(DatabaseStringConstants.CATEGORY_NAME, currentCategory.getName());
                        rst.updateFloat(DatabaseStringConstants.CATEGORY_WEIGHT, currentCategory.getWeight());
                        rst.updateInt(DatabaseStringConstants.GRADE_POLICY_TYPE, policyType);
                        rst.updateRow();
                        result = "SET";
                    } else {
                        rst.moveToInsertRow();
                        rst.updateString(DatabaseStringConstants.COURSE_ID, courseId);
                        rst.updateString(DatabaseStringConstants.CATEGORY_NAME, currentCategory.getName());
                        rst.updateFloat(DatabaseStringConstants.CATEGORY_WEIGHT, currentCategory.getWeight());
                        rst.updateInt(DatabaseStringConstants.GRADE_POLICY_TYPE, policyType);
                        rst.insertRow();
                        rst.moveToCurrentRow();
                        result = "INSERT";
                    }
                } catch (SQLException e) {
                    throw new DatabaseAccessException(e, false);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseAccessException(e, false);
        }
        return result;
    }
}

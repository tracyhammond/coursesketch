package coursesketch.database;

import protobuf.srl.grading.Rubric;
import protobuf.srl.utils.Util;

/**
 * An interface for loading and saving rubric data.
 */
public interface RubricDataHandler {
    /**
     * Loads a rubric from the database.
     *
     * @param rubricId The id of where the rubric lives.
     * @return The rubric data
     */
    Rubric.GradingRubric.Builder loadRubric(Util.DomainId rubricId);
}

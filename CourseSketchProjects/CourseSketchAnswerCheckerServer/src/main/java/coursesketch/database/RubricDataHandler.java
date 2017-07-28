package coursesketch.database;

import protobuf.srl.grading.Rubric;
import protobuf.srl.utils.Util;

public interface RubricDataHandler {
    Rubric.GradingRubric.Builder loadRubric(Util.DomainId rubricId);
}

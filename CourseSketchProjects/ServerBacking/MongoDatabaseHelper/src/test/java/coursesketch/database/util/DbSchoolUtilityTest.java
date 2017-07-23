package coursesketch.database.util;

import org.junit.Test;
import protobuf.srl.utils.Util;

import static org.junit.Assert.assertEquals;

public class DbSchoolUtilityTest {
    @Test
    public void CorrectParent() {
        assertEquals( Util.ItemType.BANK_PROBLEM, DbSchoolUtility.getParentItemType(Util.ItemType.BANK_PROBLEM));
        assertEquals( Util.ItemType.ASSIGNMENT, DbSchoolUtility.getParentItemType(Util.ItemType.COURSE_PROBLEM));
        assertEquals( Util.ItemType.COURSE, DbSchoolUtility.getParentItemType(Util.ItemType.ASSIGNMENT));
        assertEquals( Util.ItemType.COURSE, DbSchoolUtility.getParentItemType(Util.ItemType.COURSE));
        assertEquals( null, DbSchoolUtility.getParentItemType(Util.ItemType.SLIDE));
    }
}

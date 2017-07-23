package coursesketch.database.util;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;

import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static org.junit.Assert.assertEquals;

public class MongoUtilitiesTest {

    @Test
    public void testIdConverter() throws DatabaseAccessException {
        ObjectId id = new ObjectId();
        Document document = MongoUtilities.convertStringToObjectId(id.toString());
        assertEquals(document.get(SELF_ID), id);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testIdConverterNullInput() throws DatabaseAccessException {
        MongoUtilities.convertStringToObjectId(null);
    }

    @Test(expected = DatabaseAccessException.class)
    public void testIdConverterInvalidFormat() throws DatabaseAccessException {
        MongoUtilities.convertStringToObjectId("NOT VALID ID");
    }

    @Test(expected = NullPointerException.class)
    public void testInvalidUserGroupGrabbing() throws DatabaseAccessException {
        MongoUtilities.getUserGroup(null);
    }
}

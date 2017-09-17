package coursesketch.database.util;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.Test;
import protobuf.srl.utils.Util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    @Test
    public void testGrabbingNullListDoesNotReturnNull() throws DatabaseAccessException {
        final List<Object> field = MongoUtilities.getNonNullList(new Document(), "Field");
        assertNotNull(field);
    }

    @Test
    public void testGrabbingNullMapDoesNotReturnNull() throws DatabaseAccessException {
        final Map<Object, Object> field = MongoUtilities.getNonNullMap(new Document(), "Field");
        assertNotNull(field);
    }

    @Test
    public void testGrabbingListDoesNotReturnNull() throws DatabaseAccessException {
        List list = Collections.emptyList();
        new Document().append("Field", list);
        final List<Object> field = MongoUtilities.getNonNullList(new Document(), "Field");
        assertEquals(list, field);
    }

    @Test
    public void testGrabbingMapDoesNotReturnNull() throws DatabaseAccessException {
        Map map = Collections.emptyMap();
        new Document().append("Field", map);
        final Map<Object, Object> field = MongoUtilities.getNonNullMap(new Document(), "Field");
        assertEquals(map, field);
    }

    @Test
    public void testCreatingDomainIdWorksCorrectly() throws DatabaseAccessException {
        Util.DomainId id = Util.DomainId.newBuilder().setQuestionType(Util.QuestionType.MULT_CHOICE)
                .setDomainId("IDVALUE").build();
        final Document domainIdFromProto = MongoUtilities.createDomainIdFromProto(id);
        final Util.DomainId domainIdFromDocument = MongoUtilities.createDomainIdFromDocument(domainIdFromProto);
        assertEquals(id, domainIdFromDocument);
    }
}

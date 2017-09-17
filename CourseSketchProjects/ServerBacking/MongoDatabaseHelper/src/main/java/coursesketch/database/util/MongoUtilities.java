package coursesketch.database.util;

import com.google.common.collect.Lists;
import org.bson.Document;
import org.bson.types.ObjectId;
import protobuf.srl.question.QuestionDataOuterClass.QuestionData;
import protobuf.srl.utils.Util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static coursesketch.database.util.DatabaseStringConstants.DOMAIN_ID;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;


/**
 * Contains various Utilities for working with mongo databases.
 * Created by gigemjt on 9/6/15.
 */
public final class MongoUtilities {
    /**
     * A string that is empty.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Empty constructor.
     */
    private MongoUtilities() {
    }

    /**
     * Tries to convert a string into a mongo ObjectId.
     * <p>
     * Throws a {@link DatabaseAccessException} if a valid id can not be created.
     * An id is valid if it is not null and conforms to the ObjectId Format.  It does not mean the id actually exist.
     *
     * @param objectId The string id that we want convert to a mongo ObjectId.
     * @return {@link ObjectId} if it is successfully created.
     * @throws DatabaseAccessException Thrown if a valid id can not be created.
     */
    public static Document convertStringToObjectId(final String objectId) throws DatabaseAccessException {
        if (objectId == null) {
            throw new DatabaseAccessException(new IllegalArgumentException("Object Id was given a null parameter"), false);
        }
        try {
            return new Document(SELF_ID, new ObjectId(objectId.trim()));
        } catch (IllegalArgumentException e) {
            throw new DatabaseAccessException(e, false);
        }
    }

    /**
     * Creates the database document for a {@link Util.DomainId}.
     *
     * @param domainId The domain id to be converted into database data.
     * @return A {@link Document} containing a domainId.
     */
    public static Document createDomainIdFromProto(final Util.DomainId domainId) {
        final Document document = new Document(DOMAIN_ID, domainId.getDomainId());
        if (domainId.hasQuestionType()) {
            document.append(QUESTION_TYPE, domainId.getQuestionType().getNumber());
        }
        return document;
    }

    /**
     * Creates the {@link Util.DomainId} from the document.
     *
     * @param document The domain id to be converted into protobuf data.
     * @return {@link Util.DomainId} containing a domainId
     */
    public static Util.DomainId createDomainIdFromDocument(final Document document) {
        final Util.DomainId.Builder domainId = Util.DomainId.newBuilder()
                .setDomainId(document.get(DOMAIN_ID, EMPTY_STRING));
        if (document.containsKey(QUESTION_TYPE)) {
            domainId.setQuestionType(Util.QuestionType.valueOf(document.getInteger(QUESTION_TYPE)));
        }
        return domainId.build();
    }

    /**
     * Gets the user group from the database.
     *
     * @param databaseResult The result from the database
     * @return The list of users.
     */
    public static List<String> getUserGroup(final Document databaseResult) {
        return getNonNullList(databaseResult, DatabaseStringConstants.USER_LIST);
    }

    /**
     * Gets a map from the database never returns null.
     *
     * @param document The document to get the map from.
     * @param field The field where the map is.
     * @param <K> The key type for the map.
     * @param <V> The value type for the map.
     * @return The map.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getNonNullMap(final Document document, String field) {
        return getNonNullMap(document, field, (Class<Map<K, V>>) (Object) Map.class);
    }

    /**
     * Gets a map from the database never returns null.
     *
     * @param document The document to get the map from.
     * @param field The field where the map is.
     * @param classObject The class representing the type.
     * @param <K> The key type for the map.
     * @param <V> The value type for the map.
     * @return The map.
     */
    private static <K, V> Map<K, V> getNonNullMap(Document document, String field, Class<Map<K, V>> classObject) {
        final Map<K, V> map = document.get(field, classObject);
        if (map == null) {
            return Collections.emptyMap();
        }
        return map;
    }

    /**
     * Gets a list from the database never returns null.
     *
     * @param document The document to get the list from.
     * @param field The field where the list is.
     * @param <T> The type that the list is.
     * @return The list.
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getNonNullList(final Document document, String field) {
        return getNonNullList(document, field, (Class<List<T>>) (Object) List.class);
    }

    /**
     * Gets a list from the database never returns null.
     *
     * @param document The document to get the list from.
     * @param field The field where the list is.
     * @param classObject The class representing the type.
     * @param <T> The type that the list is.
     * @return The list.
     */
    private static <T> List<T> getNonNullList(final Document document, String field, Class<List<T>> classObject) {
        final List<T> list = document.get(field, classObject);
        if (list == null) {
            return Lists.newArrayList();
        }
        return list;
    }

    /**
     * Returns the type of the submission.  Assumes this method is not called with null.
     *
     * @param cursor The object that we are trying to determine the type of.
     * @return The correct submission type given the cursor object.
     */
    public static QuestionData.ElementTypeCase getQuestionType(final Document cursor) {
        final int type = cursor.getInteger(QUESTION_TYPE);
        if (type == -1) {
            return QuestionData.ElementTypeCase.ELEMENTTYPE_NOT_SET;
        }
        return QuestionData.ElementTypeCase.valueOf(type);
    }

    /**
     * Returns the type of the submission.
     *
     * @param type The type of question that is being appended.
     * @param document The object that we are trying to determine the type of.
     * @return The document that was sent in.
     */
    public static Document appendQuestionTypeToDocument(QuestionData.ElementTypeCase type, final Document document) {
        return document.append(QUESTION_TYPE, type.getNumber());
    }
}

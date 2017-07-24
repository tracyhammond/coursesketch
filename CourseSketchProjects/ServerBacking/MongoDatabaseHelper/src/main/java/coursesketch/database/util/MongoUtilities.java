package coursesketch.database.util;

import org.bson.Document;
import org.bson.types.ObjectId;
import protobuf.srl.question.QuestionDataOuterClass.QuestionData;
import protobuf.srl.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.util.DatabaseStringConstants.DOMAIN_ID;
import static coursesketch.database.util.DatabaseStringConstants.QUESTION_TYPE;
import static coursesketch.database.util.DatabaseStringConstants.SELF_ID;


/**
 * Contains various Utilities for working with mongo databases.
 * Created by gigemjt on 9/6/15.
 */
public final class MongoUtilities {

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
     * @return {@link Document} containing a domainId
     */
    public static Document createDomainIdFromProto(final Util.DomainId domainId) {
        final Document document = new Document(DOMAIN_ID, domainId.getDomainId());
        if (domainId.hasQuestionType()) {
            document.append(QUESTION_TYPE, domainId.getQuestionType().getNumber());
        }
        return document;
    }

    /**
     * Creates the {@link Util.DomainId} from the document
     *
     * @param document The domain id to be converted into protobuf data.
     * @return {@link Util.DomainId} containing a domainId
     */
    public static Util.DomainId createDomainIdFromDocument(final Document document) {
        final Util.DomainId.Builder domainId = Util.DomainId.newBuilder()
                .setDomainId(document.getString(DOMAIN_ID));
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
        return databaseResult.get(DatabaseStringConstants.USER_LIST, new ArrayList<String>());
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
     * @param document The object that we are trying to determine the type of.
     * @return The document that was sent in.
     */
    public static Document appendQuestionTypeToDocument(QuestionData.ElementTypeCase type, final Document document) {
        return document.append(QUESTION_TYPE, type.getNumber());
    }
}

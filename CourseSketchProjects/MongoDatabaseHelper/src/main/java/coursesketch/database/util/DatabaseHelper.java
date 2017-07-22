package coursesketch.database.util;

import org.bson.Document;
import protobuf.srl.utils.Util;

public class DatabaseHelper {

    public static final Document createDomainId(Util.DomainId domainId) {
        return new Document();
    }
}

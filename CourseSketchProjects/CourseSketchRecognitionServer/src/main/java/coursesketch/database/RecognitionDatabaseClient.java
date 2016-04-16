package coursesketch.database;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseAccessException;
import protobuf.srl.sketch.Sketch;

import java.util.List;

import static coursesketch.database.RecognitionStringConstants.*;

/**
 * Created by David Windows on 4/13/2016.
 */
public class RecognitionDatabaseClient extends AbstractCourseSketchDatabaseReader implements TemplateDatabaseInterface {
    private DB database;

    public RecognitionDatabaseClient(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    @Override protected void setUpIndexes() {

    }

    @Override protected void onStartDatabase() throws DatabaseAccessException {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDB(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    @Override
    public void addTemplate(Sketch.SrlInterpretation srlInterpretation, Sketch.SrlSketch srlSketch) {

    }

    @Override
    public void addTemplate(Sketch.SrlInterpretation srlInterpretation, Sketch.SrlShape srlShape) {

    }

    @Override
    public void addTemplate(Sketch.SrlInterpretation srlInterpretation, Sketch.SrlStroke srlStroke) {
        final DBObject templateObject;
        final BasicDBObject interpretationQuery = new BasicDBObject(LABEL, srlInterpretation.getLabel())
                .append(CONFIDENCE, srlInterpretation.getConfidence());

        final BasicDBObject strokeQuery = MakeDbStoke(srlStroke);
    }

    @Override
    public List<Sketch.SrlObject> getTemplate(Sketch.SrlInterpretation srlInterpretation) {
        return null;
    }

    private BasicDBObject MakeDbStoke(Sketch.SrlStroke srlStroke) {
        final BasicDBObject strokeQuery =
        return null;
    }
}

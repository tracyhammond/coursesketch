package coursesketch.database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.sketch.Sketch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static coursesketch.database.RecognitionStringConstants.INT_LABEL;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_COLLECTION;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_ID;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_INT;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_TYPE;
import static coursesketch.database.RecognitionStringConstants.SKETCH_DOMAINID;
import static coursesketch.database.RecognitionStringConstants.SKETCH_SKETCH;

/**
 * Created by David Windows on 4/13/2016.
 */
public class RecognitionDatabaseClient extends AbstractCourseSketchDatabaseReader implements TemplateDatabaseInterface {

    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionDatabaseClient.class);

    private DB database;

    final private ShapeConverter shapeConverter = new ShapeConverter();

    public RecognitionDatabaseClient(final ServerInfo serverInfo) {
        super(serverInfo);
    }

    @Override protected void setUpIndexes() {
        database.getCollection(TEMPLATE_COLLECTION).createIndex(new BasicDBObject(TEMPLATE_INT + '.' + INT_LABEL, 1)
                .append("unique", false));
    }

    @Override protected void onStartDatabase() throws DatabaseAccessException {
        final MongoClient mongoClient = new MongoClient(super.getServerInfo().getDatabaseUrl());
        database = mongoClient.getDB(super.getServerInfo().getDatabaseName());
        super.setDatabaseStarted();
    }

    @Override
    public void addTemplate(final Sketch.SrlInterpretation srlInterpretation, final Sketch.SrlSketch srlSketch) {
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();

        final DBObject interpretationDbObject = shapeConverter.makeDbInterpretation(srlInterpretation);

        final String sketchDomainId = srlSketch.getDomainId();

        final List<Object> sketchSketch = new BasicDBList();
        final List<Sketch.SrlObject> sketches = srlSketch.getSketchList();
        for (Sketch.SrlObject sketch : sketches) {
            final DBObject dbSketch = shapeConverter.makeDbObject(sketch);
            sketchSketch.add(dbSketch);
        }

        final BasicDBObject sketchDbObject = new BasicDBObject();
        sketchDbObject.append(SKETCH_DOMAINID, sketchDomainId);
        sketchDbObject.append(SKETCH_SKETCH, sketchSketch);

        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, sketchDbObject);

        templates.insert(templateObject);
    }

    @Override
    public void addTemplate(final Sketch.SrlInterpretation srlInterpretation, final Sketch.SrlShape srlShape) {
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();

        final DBObject interpretationDbObject = shapeConverter.makeDbInterpretation(srlInterpretation);
        final DBObject shapeDbObject = shapeConverter.makeDbShape(srlShape);

        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, shapeDbObject);

        templates.insert(templateObject);
    }

    @Override
    public void addTemplate(final Sketch.SrlInterpretation srlInterpretation, final Sketch.SrlStroke srlStroke) {
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();

        final DBObject interpretationDbObject = shapeConverter.makeDbInterpretation(srlInterpretation);
        final DBObject strokeDbObject = shapeConverter.makeDbStroke(srlStroke);

        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, strokeDbObject);

        templates.insert(templateObject);
    }

    @Override
    public List<Sketch.RecognitionTemplate> getTemplate(final Sketch.SrlInterpretation srlInterpretation) {
        final List<Sketch.RecognitionTemplate> templateList = new ArrayList<Sketch.RecognitionTemplate>();

        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);
        final DBObject interpretationDbObject = shapeConverter.makeDbInterpretation(srlInterpretation);

        final DBCursor templateObjectCursor = templates.find(interpretationDbObject);

        while (templateObjectCursor.hasNext()) {
            final DBObject templateObject = templateObjectCursor.next();

            final String id = (String) templateObject.get(TEMPLATE_ID);
            final Sketch.SrlInterpretation interpretation = shapeConverter.parseInterpretation(
                    (DBObject) templateObject.get(TEMPLATE_INT));
            final Sketch.SrlStroke stroke = shapeConverter.parseStroke(
                    (DBObject) templateObject.get(TEMPLATE_TYPE));

            final Sketch.RecognitionTemplate.Builder recognitionTemplate =
                    Sketch.RecognitionTemplate.newBuilder();
            // TODO: Mack setTemplateType type agnostic
            recognitionTemplate.setTemplateId(id).setInterpretation(interpretation)
                    .setStroke(stroke);
            templateList.add(recognitionTemplate.build());
        }
        return templateList;
    }

    public List<Sketch.RecognitionTemplate> getTemplate() {
        final List<Sketch.RecognitionTemplate> templateList = new ArrayList<Sketch.RecognitionTemplate>();

        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final DBCursor templateObjectCursor = templates.find();
        LOG.debug("NUMBER OF TEMPLATES FOUND {}", templateObjectCursor.count());

        while (templateObjectCursor.hasNext()) {
            final DBObject templateObject = templateObjectCursor.next();

            final String id = templateObject.get(TEMPLATE_ID).toString();
            final Sketch.SrlInterpretation interpretation = shapeConverter.parseInterpretation(
                    (DBObject) templateObject.get(TEMPLATE_INT));
            final Sketch.SrlStroke stroke = shapeConverter.parseStroke(
                    (DBObject) templateObject.get(TEMPLATE_TYPE));

            final Sketch.RecognitionTemplate.Builder recognitionTemplate =
                    Sketch.RecognitionTemplate.newBuilder();
            // TODO: Mack setTemplateType type agnostic
            recognitionTemplate.setTemplateId(id).setInterpretation(interpretation)
                    .setStroke(stroke);
            templateList.add(recognitionTemplate.build());
        }
        return templateList;
    }

    @Override public List<Sketch.SrlInterpretation> getAllInterpretations() {
        return null;
    }

}

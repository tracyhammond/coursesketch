package coursesketch.database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import coursesketch.database.interfaces.AbstractCourseSketchDatabaseReader;
import coursesketch.recognition.framework.TemplateDatabaseInterface;
import coursesketch.server.interfaces.ServerInfo;
import database.DatabaseAccessException;
import protobuf.srl.sketch.Sketch;

import java.util.List;
import java.util.UUID;

import static coursesketch.database.RecognitionStringConstants.INT_CONFIDENCE;
import static coursesketch.database.RecognitionStringConstants.INT_LABEL;
import static coursesketch.database.RecognitionStringConstants.POINT_ID;
import static coursesketch.database.RecognitionStringConstants.POINT_NAME;
import static coursesketch.database.RecognitionStringConstants.POINT_PRESSURE;
import static coursesketch.database.RecognitionStringConstants.POINT_SIZE;
import static coursesketch.database.RecognitionStringConstants.POINT_SPEED;
import static coursesketch.database.RecognitionStringConstants.POINT_TIME;
import static coursesketch.database.RecognitionStringConstants.POINT_X;
import static coursesketch.database.RecognitionStringConstants.POINT_Y;
import static coursesketch.database.RecognitionStringConstants.STROKE_ID;
import static coursesketch.database.RecognitionStringConstants.STROKE_NAME;
import static coursesketch.database.RecognitionStringConstants.STROKE_POINTS;
import static coursesketch.database.RecognitionStringConstants.STROKE_TIME;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_COLLECTION;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_ID;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_INT;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_TYPE;

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
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();

        final BasicDBObject interpretationDbObject = makeSrlInterpretation(srlInterpretation);
        final BasicDBObject strokeDbObject = makeDbStoke(srlStroke);

        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, strokeDbObject);

        templates.insert(templateObject);
    }

    @Override
    public List<Sketch.SrlObject> getTemplate(Sketch.SrlInterpretation srlInterpretation) {
        return null;
    }


    private BasicDBObject makeDbStoke(Sketch.SrlStroke srlStroke) {
        final BasicDBObject strokeDbObject = new BasicDBObject(STROKE_ID, srlStroke.getId())
                .append(STROKE_TIME, srlStroke.getTime());

        if (srlStroke.hasName()) {
            strokeDbObject.append(STROKE_NAME, srlStroke.getName());
        }

        List<Object> pointsDbList = new BasicDBList();
        List<Sketch.SrlPoint> pointsList = srlStroke.getPointsList();
        for (Sketch.SrlPoint point : pointsList) {
            BasicDBObject pointObject = makeSrlPoint(point);
            pointsDbList.add(pointObject);
        }

        strokeDbObject.append(STROKE_POINTS, pointsDbList);
        return strokeDbObject;
    }

    private BasicDBObject makeSrlPoint(Sketch.SrlPoint srlPoint) {
        BasicDBObject pointObject = new BasicDBObject();

        pointObject.append(POINT_ID, srlPoint.getId());
        pointObject.append(POINT_TIME, srlPoint.getTime());
        pointObject.append(POINT_X, srlPoint.getX());
        pointObject.append(POINT_Y, srlPoint.getY());
        if (srlPoint.hasName()) {
            pointObject.append(POINT_NAME, srlPoint.getName());
        }
        if (srlPoint.hasPressure()) {
            pointObject.append(POINT_PRESSURE, srlPoint.getPressure());
        }
        if (srlPoint.hasSize()) {
            pointObject.append(POINT_SIZE, srlPoint.getSize());
        }
        if (srlPoint.hasSpeed()) {
            pointObject.append(POINT_SPEED, srlPoint.getSpeed());
        }

        return pointObject;
    }

    private BasicDBObject makeSrlInterpretation(Sketch.SrlInterpretation srlInterpretation) {
        return new BasicDBObject(INT_LABEL, srlInterpretation.getLabel())
                .append(INT_CONFIDENCE, srlInterpretation.getConfidence());
    }
}

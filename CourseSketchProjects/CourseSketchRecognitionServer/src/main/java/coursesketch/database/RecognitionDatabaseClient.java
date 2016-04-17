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

import static coursesketch.database.RecognitionStringConstants.INT_CONFIDENCE;
import static coursesketch.database.RecognitionStringConstants.INT_LABEL;
import static coursesketch.database.RecognitionStringConstants.OBJECT_TYPE;
import static coursesketch.database.RecognitionStringConstants.POINT_ID;
import static coursesketch.database.RecognitionStringConstants.POINT_NAME;
import static coursesketch.database.RecognitionStringConstants.POINT_PRESSURE;
import static coursesketch.database.RecognitionStringConstants.POINT_SIZE;
import static coursesketch.database.RecognitionStringConstants.POINT_SPEED;
import static coursesketch.database.RecognitionStringConstants.POINT_TIME;
import static coursesketch.database.RecognitionStringConstants.POINT_X;
import static coursesketch.database.RecognitionStringConstants.POINT_Y;
import static coursesketch.database.RecognitionStringConstants.SHAPE_ID;
import static coursesketch.database.RecognitionStringConstants.SHAPE_INTERPS;
import static coursesketch.database.RecognitionStringConstants.SHAPE_ISUSERCREATED;
import static coursesketch.database.RecognitionStringConstants.SHAPE_NAME;
import static coursesketch.database.RecognitionStringConstants.SHAPE_SUBCOMPONENTS;
import static coursesketch.database.RecognitionStringConstants.SHAPE_TIME;
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
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RecognitionDatabaseClient.class);

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
    public void addTemplate(Sketch.SrlInterpretation srlInterpretation, Sketch.SrlSketch srlSketch) {
        /*
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();



        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, shapeDbObject);

        templates.insert(templateObject);
        */
    }

    @Override
    public void addTemplate(Sketch.SrlInterpretation srlInterpretation, Sketch.SrlShape srlShape) {
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();

        final BasicDBObject interpretationDbObject = makeSrlInterpretation(srlInterpretation);
        final BasicDBObject shapeDbObject = makeSrlShape(srlShape);

        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, shapeDbObject);

        templates.insert(templateObject);
    }

    @Override
    public void addTemplate(Sketch.SrlInterpretation srlInterpretation, Sketch.SrlStroke srlStroke) {
        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);

        final BasicDBObject templateObject = new BasicDBObject();

        final BasicDBObject interpretationDbObject = makeSrlInterpretation(srlInterpretation);
        final BasicDBObject strokeDbObject = makeSrlStoke(srlStroke);

        // TODO: Take in a TEMPLATE_ID instead of creating one here
        templateObject.append(TEMPLATE_ID, UUID.randomUUID());
        templateObject.append(TEMPLATE_INT, interpretationDbObject);
        templateObject.append(TEMPLATE_TYPE, strokeDbObject);

        templates.insert(templateObject);
    }

    @Override
    public List<Sketch.RecognitionTemplate> getTemplate(Sketch.SrlInterpretation srlInterpretation) {
        final List<Sketch.RecognitionTemplate> templateList = new ArrayList<Sketch.RecognitionTemplate>();

        final DBCollection templates = database.getCollection(TEMPLATE_COLLECTION);
        final BasicDBObject interpretationDbObject = makeSrlInterpretation(srlInterpretation);

        final DBCursor templateObjectCursor = templates.find(interpretationDbObject);

        while(templateObjectCursor.hasNext()) {
            DBObject templateObject = templateObjectCursor.next();

            String id = (String)templateObject.get(TEMPLATE_ID);
            Sketch.SrlInterpretation interpretation = getInterpretation(
                    (DBObject)templateObject.get(TEMPLATE_INT));
            Sketch.SrlStroke stroke = getStroke(
                    (DBObject)templateObject.get(TEMPLATE_TYPE));

            Sketch.RecognitionTemplate.Builder recognitionTemplate =
                    Sketch.RecognitionTemplate.newBuilder();
            // TODO: Mack setTemplateType type agnostic
            recognitionTemplate.setTemplateId(id).setInterpretation(interpretation)
                    .setStroke(stroke);
            templateList.add(recognitionTemplate.build());
        }
        return templateList;
    }

    //TODO: Change the names of all the methods below from makeSrl{Object} to makeDb{Object}

    private BasicDBObject makeSrlObject(Sketch.SrlObject srlObject) {
        BasicDBObject result = null;
        if (srlObject.getType().equals(Sketch.SrlObject.ObjectType.SHAPE)) {
            try {
                result = makeSrlShape(Sketch.SrlShape.parseFrom(srlObject.getObject()));
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                LOG.error("There was no shape contained in the object.");
            }
        } else if (srlObject.getType().equals(Sketch.SrlObject.ObjectType.STROKE)) {
            try {
                result = makeSrlStoke(Sketch.SrlStroke.parseFrom(srlObject.getObject()));
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                LOG.error("There was no stroke contained in the object.");
            }
        } else if (srlObject.getType().equals(Sketch.SrlObject.ObjectType.POINT)) {
            try {
                result = makeSrlPoint(Sketch.SrlPoint.parseFrom(srlObject.getObject()));
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                LOG.error("There was no point contained in the object.");
            }
        }
        result.append(OBJECT_TYPE, srlObject.getType().name());

        return result;
    }

    private BasicDBObject makeSrlShape(Sketch.SrlShape srlShape) {
        BasicDBObject shapeObject = new BasicDBObject();
        String shapeId = srlShape.getId();
        shapeObject.append(SHAPE_ID, shapeId);
        long shapeTime = srlShape.getTime();
        shapeObject.append(SHAPE_TIME, shapeTime);

        List<Object> interpretationList = new BasicDBList();
        List<Sketch.SrlInterpretation> shapeInterpretations = srlShape.getInterpretationsList();
        for (Sketch.SrlInterpretation shapeInterpretation : shapeInterpretations) {
            DBObject interpretation = makeSrlInterpretation(shapeInterpretation);
            interpretationList.add(interpretation);
        }
        shapeObject.append(SHAPE_INTERPS, interpretationList);

        List<Object> subcomponentList = new BasicDBList();
        List<Sketch.SrlObject> shapeComponents = srlShape.getSubComponentsList();
        for (Sketch.SrlObject shapeCompenent : shapeComponents) {
            DBObject component = makeSrlObject(shapeCompenent);
            subcomponentList.add(component);
        }
        shapeObject.append(SHAPE_SUBCOMPONENTS, subcomponentList);

        String shapeName = null;
        if(srlShape.hasName()) {
            shapeName = srlShape.getName();
            shapeObject.append(SHAPE_NAME, shapeName);
        }

        Boolean isUserCreated;
        if(srlShape.hasIsUserCreated()) {
            isUserCreated = srlShape.getIsUserCreated();
            shapeObject.append(SHAPE_ISUSERCREATED, isUserCreated);
        }
        return shapeObject;
    }

    private BasicDBObject makeSrlStoke(Sketch.SrlStroke srlStroke) {
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

    private Sketch.SrlInterpretation getInterpretation(DBObject interpretationObject) {
        String intLabel = (String)interpretationObject.get(INT_LABEL);
        Double intConfidence = (Double)interpretationObject.get(INT_CONFIDENCE);

        Sketch.SrlInterpretation.Builder srlInterpretation = Sketch.SrlInterpretation.newBuilder();
        srlInterpretation.setLabel(intLabel);
        srlInterpretation.setConfidence(intConfidence);

        return srlInterpretation.build();
    }

    private Sketch.SrlStroke getStroke(DBObject strokeObject) {
        String strokeId = (String)strokeObject.get(STROKE_ID);
        long time = (long)strokeObject.get(STROKE_TIME);
        String name = null;
        if (strokeObject.containsField(STROKE_NAME)) {
            name = (String) strokeObject.get(STROKE_NAME);
        }

        List<DBObject> pointObjects = (List<DBObject>)strokeObject.get(STROKE_POINTS);
        List<Sketch.SrlPoint> points = new ArrayList<Sketch.SrlPoint>();

        for (DBObject pointObject: pointObjects) {
            Sketch.SrlPoint point = getPoint(pointObject);
            points.add(point);
        }

        Sketch.SrlStroke.Builder stroke = Sketch.SrlStroke.newBuilder();
        stroke.setId(strokeId).setTime(time).setName(name).addAllPoints(points);
        return stroke.build();
    }

    private Sketch.SrlPoint getPoint(DBObject pointObject) {
        String pointId = (String)pointObject.get(POINT_ID);
        long pointTime = (long)pointObject.get(POINT_TIME);
        double x = (double)pointObject.get(POINT_X);
        double y = (double)pointObject.get(POINT_Y);
        String name = null;
        Double pressure = null, size = null, speed = null;
        if (pointObject.containsField(POINT_NAME)) {
            name = (String)pointObject.get(POINT_NAME);
        }
        if (pointObject.containsField(POINT_PRESSURE)) {
            pressure = (Double)pointObject.get(POINT_PRESSURE);
        }
        if (pointObject.containsField(POINT_SIZE)) {
            size = (Double)pointObject.get(POINT_SIZE);
        }
        if (pointObject.containsField(POINT_SPEED)) {
            speed = (Double)pointObject.get(POINT_SPEED);
        }

        Sketch.SrlPoint.Builder point = Sketch.SrlPoint.newBuilder();
        point.setId(pointId).setTime(pointTime).setX(x).setY(y).setName(name).setPressure(pressure)
                .setSize(size).setSpeed(speed);

        return point.build();
    }
}

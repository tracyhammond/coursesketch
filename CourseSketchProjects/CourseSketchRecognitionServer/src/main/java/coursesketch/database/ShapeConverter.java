package coursesketch.database;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import coursesketch.recognition.framework.ShapeConverterInterface;
import coursesketch.recognition.framework.exceptions.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import protobuf.srl.sketch.Sketch;

import java.util.ArrayList;
import java.util.List;

import static coursesketch.database.RecognitionStringConstants.INTERPRETATION_CONFIDENCE;
import static coursesketch.database.RecognitionStringConstants.INTERPRETATION_LABEL;
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
import static coursesketch.database.RecognitionStringConstants.SKETCH_SKETCH;
import static coursesketch.database.RecognitionStringConstants.STROKE_ID;
import static coursesketch.database.RecognitionStringConstants.STROKE_NAME;
import static coursesketch.database.RecognitionStringConstants.STROKE_POINTS;
import static coursesketch.database.RecognitionStringConstants.STROKE_TIME;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_ID;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_INTERPRETATION;
import static coursesketch.database.RecognitionStringConstants.TEMPLATE_DATA;

/**
 * Created by David Windows on 4/20/2016.
 */
public final class ShapeConverter implements ShapeConverterInterface<com.mongodb.DBObject> {
    /**
     * Declaration and Definition of Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ShapeConverter.class);

    @Override
    public DBObject makeDbObject(final Sketch.SrlObject srlObject) {
        DBObject result = null;
        if (srlObject.getType().equals(Sketch.ObjectType.SHAPE)) {
            try {
                result = makeDbShape(Sketch.SrlShape.parseFrom(srlObject.getObject()));
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                LOG.error("There was no shape contained in the object.");
            }
        } else if (srlObject.getType().equals(Sketch.ObjectType.STROKE)) {
            try {
                result = makeDbStroke(Sketch.SrlStroke.parseFrom(srlObject.getObject()));
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                LOG.error("There was no stroke contained in the object.");
            }
        } else if (srlObject.getType().equals(Sketch.ObjectType.POINT)) {
            try {
                result = makeDbPoint(Sketch.SrlPoint.parseFrom(srlObject.getObject()));
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                LOG.error("There was no point contained in the object.");
            }
        }
        result.put(OBJECT_TYPE, srlObject.getType().name());

        return result;
    }

    @Override
    public DBObject makeDbShape(final Sketch.SrlShape srlShape) {
        final BasicDBObject shapeObject = new BasicDBObject();
        final String shapeId = srlShape.getId();
        shapeObject.append(SHAPE_ID, shapeId);
        final long shapeTime = srlShape.getTime();
        shapeObject.append(SHAPE_TIME, shapeTime);

        final List<Object> interpretationList = new BasicDBList();
        final List<Sketch.SrlInterpretation> shapeInterpretations = srlShape.getInterpretationsList();
        for (Sketch.SrlInterpretation shapeInterpretation : shapeInterpretations) {
            final DBObject interpretation = makeDbInterpretation(shapeInterpretation);
            interpretationList.add(interpretation);
        }
        shapeObject.append(SHAPE_INTERPS, interpretationList);

        final List<Object> subcomponentList = new BasicDBList();
        final List<Sketch.SrlObject> shapeComponents = srlShape.getSubComponentsList();
        for (Sketch.SrlObject shapeCompenent : shapeComponents) {
            final DBObject component = makeDbObject(shapeCompenent);
            subcomponentList.add(component);
        }
        shapeObject.append(SHAPE_SUBCOMPONENTS, subcomponentList);

        String shapeName = null;
        if (srlShape.hasName()) {
            shapeName = srlShape.getName();
            shapeObject.append(SHAPE_NAME, shapeName);
        }

        final boolean isUserCreated;
        if (srlShape.hasIsUserCreated()) {
            isUserCreated = srlShape.getIsUserCreated();
            shapeObject.append(SHAPE_ISUSERCREATED, isUserCreated);
        }
        return shapeObject;
    }

    @Override
    public DBObject makeDbStroke(final Sketch.SrlStroke srlStroke) {
        final BasicDBObject strokeDBObject = new BasicDBObject(STROKE_ID, srlStroke.getId())
                .append(STROKE_TIME, srlStroke.getTime());

        if (srlStroke.hasName()) {
            strokeDBObject.append(STROKE_NAME, srlStroke.getName());
        }

        final List<Object> pointsDbList = new BasicDBList();
        final List<Sketch.SrlPoint> pointsList = srlStroke.getPointsList();
        for (Sketch.SrlPoint point : pointsList) {
            final DBObject pointObject = makeDbPoint(point);
            pointsDbList.add(pointObject);
        }

        strokeDBObject.append(STROKE_POINTS, pointsDbList);
        return strokeDBObject;
    }

    @Override
    public DBObject makeDbPoint(final Sketch.SrlPoint srlPoint) {
        final BasicDBObject pointObject = new BasicDBObject();

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

    @Override
    public com.mongodb.DBObject makeDbInterpretation(final Sketch.SrlInterpretation srlInterpretation) {
        return new BasicDBObject(INTERPRETATION_LABEL, srlInterpretation.getLabel())
                .append(INTERPRETATION_CONFIDENCE, srlInterpretation.getConfidence());
    }


    /**
     * Parses a template that was found in the database.
     *
     * @param templateObject The db object that is being parsed.
     * @return A Recognition Tempalte that has been parsed.
     */
    public Sketch.RecognitionTemplate parseRecognitionTemplate(final DBObject templateObject) {
        final String id = templateObject.get(TEMPLATE_ID).toString();
        final Sketch.SrlInterpretation interpretation = parseInterpretation(
                (DBObject) templateObject.get(TEMPLATE_INTERPRETATION));

        final Sketch.RecognitionTemplate.Builder recognitionTemplate =
                Sketch.RecognitionTemplate.newBuilder();

        final String objectType = (String) templateObject.get(OBJECT_TYPE);
        final DBObject templateData = (DBObject) templateObject.get(TEMPLATE_DATA);
        if (objectType == null) {
            final Sketch.SrlSketch srlSketch = parseSketch(templateData);
            recognitionTemplate.setSketch(srlSketch);
        } else if (objectType.equals(Sketch.ObjectType.SHAPE.name())) {
            final Sketch.SrlShape srlShape = parseShape(templateData);
            recognitionTemplate.setShape(srlShape);
        } else if (objectType.equals(Sketch.ObjectType.STROKE.name())) {
            final Sketch.SrlStroke stroke = parseStroke(templateData);
            recognitionTemplate.setStroke(stroke);
        } else {
            LOG.error("", new TemplateException("Unknown template type: " + objectType));
        }

        recognitionTemplate.setTemplateId(id).setInterpretation(interpretation);
        return recognitionTemplate.build();
    }

    @Override
    public Sketch.SrlSketch parseSketch(final DBObject sketchObject) {
        final Sketch.SrlSketch.Builder sketch = Sketch.SrlSketch.newBuilder();
        final List<DBObject> sketchData = (List<DBObject>) sketchObject.get(SKETCH_SKETCH);
        for (DBObject dbObject: sketchData) {
            sketch.addSketch(parseObject(dbObject));
        }
        return sketch.build();
    }

    @Override
    public Sketch.SrlInterpretation parseInterpretation(final DBObject interpretationObject) {
        final String intLabel = (String) interpretationObject.get(INTERPRETATION_LABEL);
        final Double intConfidence = (Double) interpretationObject.get(INTERPRETATION_CONFIDENCE);

        final Sketch.SrlInterpretation.Builder srlInterpretation = Sketch.SrlInterpretation.newBuilder();
        srlInterpretation.setLabel(intLabel);
        srlInterpretation.setConfidence(intConfidence);

        return srlInterpretation.build();
    }

    @Override
    public Sketch.SrlObject parseObject(final DBObject someObject) {
        final Sketch.ObjectType objectType = Sketch.ObjectType.valueOf(
                (String) someObject.get(OBJECT_TYPE));

        final Sketch.SrlObject.Builder srlObject = Sketch.SrlObject.newBuilder();

        if (objectType.equals(Sketch.ObjectType.SHAPE)) {
            final Sketch.SrlShape srlShape = parseShape(someObject);
            srlObject.setType(Sketch.ObjectType.SHAPE);
            srlObject.setObject(srlShape.toByteString());
        } else if (objectType.equals(Sketch.ObjectType.STROKE)) {
            final Sketch.SrlStroke srlStroke = parseStroke(someObject);
            srlObject.setType(Sketch.ObjectType.STROKE);
            srlObject.setObject(srlStroke.toByteString());
        } else {
            final Sketch.SrlPoint srlPoint = parsePoint(someObject);
            srlObject.setType(Sketch.ObjectType.POINT);
            srlObject.setObject(srlPoint.toByteString());
        }
        return srlObject.build();
    }

    @Override
    public Sketch.SrlShape parseShape(final DBObject shapeObject) {
        final Sketch.SrlShape.Builder shape = Sketch.SrlShape.newBuilder();
        final String shapeId = (String) shapeObject.get(SHAPE_ID);
        shape.setId(shapeId);

        final long time = (long) shapeObject.get(SHAPE_TIME);
        shape.setTime(time);

        String name = null;
        if (((DBObject) shapeObject).containsField(SHAPE_NAME)) {
            name = (String) shapeObject.get(SHAPE_NAME);
            shape.setName(name);
        }
        Boolean isUserCreated = null;
        if (((DBObject) shapeObject).containsField(SHAPE_ISUSERCREATED)) {
            isUserCreated = (Boolean) shapeObject.get(SHAPE_ISUSERCREATED);
            shape.setIsUserCreated(isUserCreated);
        }

        final List<DBObject> shapeInterpretations = (List<DBObject>) shapeObject.get(SHAPE_INTERPS);
        final List<Sketch.SrlInterpretation> interpretations = new ArrayList<Sketch.SrlInterpretation>();
        for (DBObject shapeInterpretation : shapeInterpretations) {
            final Sketch.SrlInterpretation interpretation = parseInterpretation(shapeInterpretation);
            interpretations.add(interpretation);
        }
        shape.addAllInterpretations(interpretations);

        final List<DBObject> shapeSubComponents = (List<DBObject>) shapeObject.get(SHAPE_SUBCOMPONENTS);
        final List<Sketch.SrlObject> subComponents = new ArrayList<Sketch.SrlObject>();
        for (DBObject subComponent : shapeSubComponents) {
            final Sketch.SrlObject srlObject = parseObject(subComponent);
            subComponents.add(srlObject);
        }
        shape.addAllSubComponents(subComponents);

        return shape.build();
    }

    @Override
    public Sketch.SrlStroke parseStroke(final DBObject strokeObject) {
        final Sketch.SrlStroke.Builder stroke = Sketch.SrlStroke.newBuilder();

        final String strokeId = (String) strokeObject.get(STROKE_ID);
        final long time = (long) strokeObject.get(STROKE_TIME);
        String name = null;
        if (((DBObject) strokeObject).containsField(STROKE_NAME)) {
            name = (String) strokeObject.get(STROKE_NAME);
            stroke.setName(name);
        }

        final List<DBObject> pointObjects = (List<DBObject>) strokeObject.get(STROKE_POINTS);
        final List<Sketch.SrlPoint> points = new ArrayList<Sketch.SrlPoint>();

        for (DBObject pointObject: pointObjects) {
            final Sketch.SrlPoint point = parsePoint(pointObject);
            points.add(point);
        }

        stroke.setId(strokeId).setTime(time).addAllPoints(points);
        return stroke.build();
    }

    @Override
    public Sketch.SrlPoint parsePoint(final DBObject pointObject) {
        final Sketch.SrlPoint.Builder point = Sketch.SrlPoint.newBuilder();

        final String pointId = (String) pointObject.get(POINT_ID);
        final long pointTime = (long) pointObject.get(POINT_TIME);
        final double x = (double) pointObject.get(POINT_X);
        final double y = (double) pointObject.get(POINT_Y);
        String name = null;
        Double pressure = null, size = null, speed = null;
        if (((DBObject) pointObject).containsField(POINT_NAME)) {
            name = (String) pointObject.get(POINT_NAME);
            point.setName(name);
        }
        if (((DBObject) pointObject).containsField(POINT_PRESSURE)) {
            pressure = (Double) pointObject.get(POINT_PRESSURE);
            point.setPressure(pressure);
        }
        if (((DBObject) pointObject).containsField(POINT_SIZE)) {
            size = (Double) pointObject.get(POINT_SIZE);
            point.setSize(size);
        }
        if (((DBObject) pointObject).containsField(POINT_SPEED)) {
            speed = (Double) pointObject.get(POINT_SPEED);
            point.setSpeed(speed);
        }

        point.setId(pointId).setTime(pointTime).setX(x).setY(y);

        return point.build();
    }
}

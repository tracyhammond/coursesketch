package coursesketch.database;

/**
 * Contains a list of useful constants used by the recognition database.
 *
 * Created by Rauank on 4/16/16.
 */

@SuppressWarnings({ "PMD.CommentRequired", "checkstyle:javadocvariable" })
public class RecognitionStringConstants {
    public static final String TEMPLATE_COLLECTION      = "Templates";

    public static final String TEMPLATE_ID               = "TemplateId";
    public static final String TEMPLATE_INTERPRETATION   = "TemplateInterpretation";
    public static final String TEMPLATE_DATA             = "TemplateData";

    public static final String INTERPRETATION_LABEL      = "InterpretationLabel";
    public static final String INTERPRETATION_CONFIDENCE = "InterpretationConfidence";

    public static final String STROKE_ID            = "StrokeId";
    public static final String STROKE_TIME          = "StrokeTime";
    public static final String STROKE_NAME          = "StrokeName";
    public static final String STROKE_POINTS        = "StrokePoints";

    public static final String POINT_ID             = "PointId";
    public static final String POINT_TIME           = "PointTime";
    public static final String POINT_NAME           = "PointName";
    public static final String POINT_X              = "PointX";
    public static final String POINT_Y              = "PointY";
    public static final String POINT_PRESSURE       = "PointPressure";
    public static final String POINT_SIZE           = "PointSize";
    public static final String POINT_SPEED          = "PointSpeed";

    public static final String OBJECT_TYPE          = "ObjectType";

    public static final String SHAPE_ID             = "ShapeId";
    public static final String SHAPE_TIME           = "ShapeTime";
    public static final String SHAPE_NAME           = "ShapeName";
    public static final String SHAPE_ISUSERCREATED  = "ShapeIsUserCreated";
    public static final String SHAPE_INTERPS        = "ShapeInterpretations";
    public static final String SHAPE_SUBCOMPONENTS  = "ShapeSubComponents";

    public static final String SKETCH_DOMAINID      = "SketchDomainId";
    public static final String SKETCH_SKETCH        = "SketchSketch";
}
